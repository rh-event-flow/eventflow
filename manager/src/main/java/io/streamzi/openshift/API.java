package io.streamzi.openshift;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import io.streamzi.openshift.dataflow.model.crds.*;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorFlowReader;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLWriter;
import io.streamzi.openshift.dataflow.model.serialization.SerializedFlow;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hhiden
 */
@ApplicationScoped
@Path("/api")
public class API {
    private static final Logger logger = Logger.getLogger(API.class.getName());

    @EJB(beanInterface = ClientContainer.class)
    private ClientContainer container;

    private final String bootstrapServersDefault = "my-cluster-kafka:9092";
    private final String brokerUrlDefault = "amqp://dispatch.myproject.svc:5672";

    @GET
    @Path("/pods")
    @Produces("application/json")
    public List<String> listPods() {
        List<Pod> pods = container.getOSClient().pods().inNamespace(container.getNamespace()).list().getItems();
        List<String> results = new ArrayList<>();
        for (Pod p : pods) {
            results.add(p.getMetadata().getName());
        }
        return results;
    }

    @GET
    @Path("/dataflows/{name}")
    @Produces("application/json")
    public String getProcessorFlowDeployment(@PathParam("name") String name) {
        ConfigMap map = container.getOSClient().configMaps().withName(name).get();
        if (map != null) {
            return map.getData().get("flow");
        } else {
            return "";
        }
    }

    @GET
    @Path("/dataflows")
    @Produces("application/json")
    public List<String> listFlows() {
        List<String> results = new ArrayList<>();

        // Find all of the config maps with the streamzi/kind flow labels
        ConfigMapList configMapList = container.getOSClient().configMaps().inNamespace(container.getNamespace()).withLabel("streamzi.io/kind", "flow").list();

        for (ConfigMap cm : configMapList.getItems()) {
            results.add(cm.getMetadata().getName());
        }

        return results;
    }

    @GET
    @Path("/processors")
    @Produces("application/json")
    public List<String> listProcessors() {


        final CustomResourceDefinition procCRD = container.getOSClient().customResourceDefinitions().withName("processors.streamzi.io").get();
        if (procCRD == null) {
            logger.severe("Can't find CRD");
            return Collections.emptyList();
        }

        final NonNamespaceOperation<Processor, ProcessorList, DoneableProcessor, Resource<Processor, DoneableProcessor>> processorClient =
                container.getOSClient().customResources(
                        procCRD,
                        Processor.class,
                        ProcessorList.class,
                        DoneableProcessor.class)
                        .inNamespace(container.getOSClient().getNamespace());

        final List<String> results = new ArrayList<>();

        final List<Processor> processors = processorClient.list().getItems();
        for (Processor proc : processors) {
            try {
                final ProcessorNodeTemplate template = new ProcessorNodeTemplate(proc);
                final ProcessorTemplateYAMLWriter writer = new ProcessorTemplateYAMLWriter(template);
                results.add(writer.writeToYAMLString());

            } catch (Exception e) {
                logger.log(Level.WARNING, "Error reading template from config map: " + e.getMessage());
            }
        }

        return results;
    }


    /**
     * Upload a new flow to a ConfigMap
     */
    @POST
    @Path("/flows")
    @Consumes("application/json")
    public void postFlow(String flowJson) {
        logger.info(flowJson);
        try {
            ObjectMapper mapper = new ObjectMapper();

            SerializedFlow serializedFlow = mapper.readValue(flowJson, SerializedFlow.class);
            ProcessorFlow flow = new ProcessorFlow(serializedFlow);

            if(flow != null){
                logger.info("Flow Parsed OK");
            }else{
                logger.warning("Flow not parsed OK");
            }

            Flow customResource = new Flow();

            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(serializedFlow.getName());
            customResource.setMetadata(metadata);
            customResource.setSpec(serializedFlow);

            final CustomResourceDefinition flowCRD = container.getOSClient().customResourceDefinitions().withName("flows.streamzi.io").get();

            container.getOSClient().customResources(flowCRD, Flow.class, FlowList.class, DoneableFlow.class).inNamespace(container.getNamespace()).createOrReplace(customResource);

            logger.info("Flow written OK");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
        }
    }

    @GET
    @Path("/globalproperties")
    @Produces("application/json")
    public String getGlobalProperties() {
        final Properties props = new Properties();

        String bootstrapServers = EnvironmentResolver.get(ProcessorConstants.KAFKA_BOOTSTRAP_SERVERS);
        if (bootstrapServers != null && !bootstrapServers.equals("")) {
            props.put(ProcessorConstants.KAFKA_BOOTSTRAP_SERVERS, bootstrapServers);
        } else {
            props.put(ProcessorConstants.KAFKA_BOOTSTRAP_SERVERS, bootstrapServersDefault);
        }

        String brokerUrl = EnvironmentResolver.get("broker.url");
        if (brokerUrl != null && !brokerUrl.equals("")) {
            props.put("broker.url", brokerUrl);
        } else {
            props.put("broker.url", brokerUrlDefault);
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(props);
        } catch (JsonProcessingException e) {
            logger.severe(e.getMessage());
            return "{}";
        }
    }

    @GET
    @Path("/topics")
    @Produces("application/json")
    public List<String> listTopics() {
        ConfigMapList list = container.getOSClient().configMaps().withLabel("strimzi.io/kind", "topic").list();

        ArrayList<String> results = new ArrayList<>();
        for (ConfigMap cm : list.getItems()) {
            if (cm.getMetadata().getLabels().containsKey("streamzi.io/source")) {
                // This is one of ours - add it if it wasn't autocreated
                String source = cm.getMetadata().getLabels().get("streamzi.io/source");
                if (source == null || source.isEmpty() || !source.equalsIgnoreCase("autocreated")) {
                    results.add(cm.getMetadata().getName());
                }
            } else {
                results.add(cm.getData().get("name"));
            }
        }
        return results;
    }
}
