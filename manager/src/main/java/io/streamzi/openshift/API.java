package io.streamzi.openshift;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorFlowReader;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorFlowWriter;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLReader;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLWriter;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import java.io.File;
import java.util.ArrayList;
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

    @GET
    @Path("/pods")
    @Produces("application/json")
    public List<String> listPods() {
        List<Pod> pods = container.getOSClient().pods().inNamespace(container.getNamespace()).list().getItems();
//        container.getClient().list(ResourceKind.POD, container.getNamespace());
        List<String> results = new ArrayList<>();
        for (Pod p : pods) {
            results.add(p.getMetadata().getName());
        }
        return results;
    }

    /*
    @GET
    @Path("/deployments/{namespace}/create/{name}")
    @Produces("application/json")
    public String create(@PathParam("namespace")String namespace, @PathParam("name")String name){
        // Find the template
        IResource template = container.getClient().get(ResourceKind.IMAGE_STREAM, name, namespace);
        if(template!=null){
            IDeploymentConfig config = container.getClient().getResourceFactory().stub(ResourceKind.DEPLOYMENT_CONFIG, name, namespace);
            
            config.setReplicas(1);
            config.addLabel("app", name);
                config.addLabel("streamzi.flow.uuid", UUID.randomUUID().toString());
            config.addLabel("streamzi.deployment.uuid", UUID.randomUUID().toString());
            config.addLabel("streamzi.type", "processor-flow");
            config.addTemplateLabel("app", name);
            
            IContainer c1 = config.addContainer("streamzi-processor-" + UUID.randomUUID().toString());
            c1.addEnvVar("processor-uuid", UUID.randomUUID().toString());
            c1.setImage(new DockerImageURI("172.30.1.1:5000/myproject/oc-stream-container:latest"));
           
            IContainer c2 = config.addContainer("streamzi-processor-" + UUID.randomUUID().toString());
            c2.addEnvVar("processor-uuid", UUID.randomUUID().toString());
            c2.setImage(new DockerImageURI("172.30.1.1:5000/myproject/oc-stream-container:latest"));
            
            config = container.getClient().create(config);
            return config.toJson();
            
        } else {
            return "NOTHING";
        }
        
    }
    */


    @GET
    @Path("/dataflows/{uuid}")
    @Produces("application/json")
    public String getProcessorFlowDeployment(String uuid) {
        return "";
    }

    @GET
    @Path("/processors")
    @Produces("application/json")
    public List<String> listProcessors() {
        List<String> results = new ArrayList<>();

        //todo: look at applying labels to imagestreams and getting the necessary data from there.
        //todo: could apply special labels to the deployment configs to hold the graph structure.

//        List<ImageStream> images = container.getOSClient().imageStreams().inAnyNamespace().withLabel("streamzi.io/kind", "processor").list().getItems();
//        for (ImageStream image : images) {
//            Map<String, String> labels = image.getMetadata().getLabels();
//            final ProcessorNodeTemplate template = new ProcessorNodeTemplate();
//            template.setId(labels.get("streamzi.io/processor/id"));
//            template.setDescription(labels.get("streamzi.io/processor/description"));
//            template.setName(labels.get("streamzi.io/processor/label"));
//            template.setImageName(labels.get("streamzi.io/processor/imagename"));
//
//            String[] inputsLabel = labels.get("inputs").split(",");
//            List<String> inputs = new ArrayList<>(Arrays.asList(inputsLabel));
//
//            String[] outputsLabel = labels.get("outputs").split(",");
//            List<String> outputs = new ArrayList<>(Arrays.asList(outputsLabel));
//
//            template.setInputs(inputs);
//            template.setOutputs(outputs);
//        }

        File[] templates = container.getTemplateDir().listFiles();
        if (templates != null) {
            for (File f : templates) {
                try {
                    final ProcessorTemplateYAMLReader reader = new ProcessorTemplateYAMLReader(f);
                    final ProcessorNodeTemplate template = reader.readTemplate();
                    final ProcessorTemplateYAMLWriter writer = new ProcessorTemplateYAMLWriter(template);
                    results.add(writer.writeToYAMLString());
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error loading template: " + e.getMessage());
                }
            }
        }
        return results;
    }

    /**
     * Upload a definition for a processor node
     */
    @POST
    @Path("/processors")
    @Consumes("application/json")
    public void postYaml(String yamlData) {
        logger.info(yamlData);
        try {
            ProcessorNodeTemplate template = ProcessorTemplateYAMLReader.readTemplateFromString(yamlData);
            logger.info("Valid template for image: " + template.getImageName());

            // Save this in the storage folder
            ProcessorTemplateYAMLWriter writer = new ProcessorTemplateYAMLWriter(template);
            writer.writeToFile(container.getTemplateDir());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing YAML data: " + e.getMessage(), e);
        }

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
            ProcessorFlowReader reader = new ProcessorFlowReader();
            ProcessorFlow flow = reader.readFromJsonString(flowJson);
            logger.info("Flow Parsed OK");

            // Write this to a ConfigMap
            ProcessorFlowWriter writer = new ProcessorFlowWriter(flow);

            ConfigMap cm = new ConfigMapBuilder()
                    .withNewMetadata()
                    .withName(flow.getName() + ".cm")
                    .withNamespace(container.getNamespace())
                    .addToLabels("streamzi.io/kind", "flow")
                    .addToLabels("app", flow.getName())
                    .endMetadata()
                    .addToData("flow", writer.writeToIndentedJsonString())
                    .build();

            container.getOSClient().configMaps().createOrReplace(cm);

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

        String bootstrapServers = EnvironmentResolver.get("bootstrap.servers");
        if (bootstrapServers != null && !bootstrapServers.equals("")) {
            props.put("bootstrap_servers", bootstrapServers);
        } else {
            props.put("bootstrap_servers", bootstrapServersDefault);
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(props);
        } catch (JsonProcessingException e) {
            logger.severe(e.getMessage());
            return "{}";
        }
    }
}
