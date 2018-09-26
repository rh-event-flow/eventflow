package io.streamzi.eventflow;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.streamzi.eventflow.crds.Cloud;
import io.streamzi.eventflow.crds.CloudList;
import io.streamzi.eventflow.crds.DoneableCloud;
import io.streamzi.eventflow.crds.DoneableFlow;
import io.streamzi.eventflow.crds.DoneableProcessor;
import io.streamzi.eventflow.crds.Flow;
import io.streamzi.eventflow.crds.FlowList;
import io.streamzi.eventflow.crds.Processor;
import io.streamzi.eventflow.crds.ProcessorList;
import io.streamzi.eventflow.serialization.SerializedFlow;
import io.strimzi.api.kafka.Crds;
import io.strimzi.api.kafka.KafkaTopicList;
import io.strimzi.api.kafka.model.DoneableKafkaTopic;
import io.strimzi.api.kafka.model.KafkaTopic;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author hhiden
 */
@ApplicationScoped
@Path("/api")
public class API {
    private static final Logger logger = Logger.getLogger(API.class.getName());

    private final ObjectMapper MAPPER = new ObjectMapper();

    @EJB(beanInterface = ClientContainer.class)
    private ClientContainer container;

    @GET
    @Path("/pods")
    @Produces("application/json")
    public List<String> listPods() {
        return container.getOSClient().pods().inNamespace(container.getNamespace()).list()
                .getItems()
                .stream()
                .map(pod -> pod.getMetadata().getName())
                .collect(Collectors.toList());
    }

    @GET
    @Path("/dataflows/{name}")
    @Produces("application/json")
    public String getProcessorFlowDeployment(@PathParam("name") String name) throws Exception {

        final CustomResourceDefinition flowCRD = container.getOSClient().customResourceDefinitions().withName("flows.streamzi.io").get();
        if (flowCRD == null) {
            logger.severe("Can't find Flow CRD");
            return "";
        }

        return MAPPER.writeValueAsString(container.getOSClient().customResources(
                flowCRD,
                Flow.class,
                FlowList.class,
                DoneableFlow.class)
                .inNamespace(container.getOSClient().getNamespace())
                .withName(name).get());
    }

    @GET
    @Path("/dataflows")
    @Produces("application/json")
    public List<String> listFlows() {
        final CustomResourceDefinition flowCRD = container.getOSClient().customResourceDefinitions().withName("flows.streamzi.io").get();
        if (flowCRD == null) {
            logger.severe("Can't find Flow CRD");
            return Collections.emptyList();
        }

        return container.getOSClient().customResources(
                flowCRD,
                Flow.class,
                FlowList.class,
                DoneableFlow.class)
                .inNamespace(container.getOSClient().getNamespace()).list().getItems().stream()
                .map(flow -> flow.getMetadata().getName())
                .collect(Collectors.toList());
    }

    @GET
    @Path("/processors")
    @Produces("application/json")
    public String listProcessors() throws Exception {

        final CustomResourceDefinition procCRD = container.getOSClient().customResourceDefinitions().withName("processors.streamzi.io").get();
        if (procCRD == null) {
            logger.severe("Can't find CRD");
            return "[]";
        }

        return MAPPER.writeValueAsString(container.getOSClient().customResources(
                procCRD,
                Processor.class,
                ProcessorList.class,
                DoneableProcessor.class)
                .inNamespace(container.getOSClient().getNamespace())
                .list().getItems());
    }


    /**
     * Upload a new Custom Resource
     */
    @POST
    @Path("/flows")
    @Consumes("application/json")
    public void postFlow(String flowJson) {
        logger.info(flowJson);
        try {

            SerializedFlow serializedFlow = MAPPER.readValue(flowJson, SerializedFlow.class);
            logger.info("Flow Parsed OK");

            serializedFlow.setName(serializedFlow.getName()
                    .toLowerCase()
                    .replace("_", "-")
                    .replace(" ", "-")
            );

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
    @Path("/topics")
    @Produces("application/json")
    public String listTopicNames() throws Exception {

        return MAPPER.writeValueAsString(container.getOSClient().customResources(
                Crds.topic(),
                KafkaTopic.class,
                KafkaTopicList.class,
                DoneableKafkaTopic.class)
                .inNamespace(container.getOSClient().getNamespace()).list().getItems().stream()
                .filter(kafkaTopic -> (kafkaTopic.getMetadata().getLabels().get("streamzi.io/source") == null))
                .map(topic -> {
                    return new TopicDetails(topic.getMetadata().getName(), "local");
                })
                .collect(Collectors.toList()));

    }

    @GET
    @Path("/clouds")
    @Produces("application/json")
    public String getClouds() {

        final CustomResourceDefinition cloudCRD = container.getOSClient().customResourceDefinitions().withName("clouds.streamzi.io").get();
        if (cloudCRD == null) {
            logger.severe("Can't find Cloud CRD");
            return "[]";
        }

        try {
            return MAPPER.writeValueAsString(new ArrayList<>(container.getOSClient().customResources(
                    cloudCRD,
                    Cloud.class,
                    CloudList.class,
                    DoneableCloud.class)
                    .inNamespace(container.getOSClient().getNamespace())
                    .list().getItems())
                    .stream().peek(cloud -> cloud.getSpec().setToken(null))
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing JSON topic data: " + e.getMessage(), e);
            return "";
        }
    }

    @GET
    @Path("/clouds/names")
    @Produces("application/json")
    public String getCloudNames() throws Exception {

        final CustomResourceDefinition cloudCRD = container.getOSClient().customResourceDefinitions().withName("clouds.streamzi.io").get();
        if (cloudCRD == null) {
            logger.severe("Can't find Cloud CRD");
            return "[\"local\"]";
        }

        Set<String> cloudNames = container.getOSClient().customResources(
                cloudCRD,
                Cloud.class,
                CloudList.class,
                DoneableCloud.class)
                .inNamespace(container.getOSClient().getNamespace())
                .list().getItems()
                .stream().map(cloud -> cloud.getMetadata().getName())
                .collect(Collectors.toSet());

        cloudNames.add("local");
        return MAPPER.writeValueAsString(cloudNames);
    }

    @GET
    @Path("/clouds/{name}")
    @Produces("application/json")
    public String getCloud(@PathParam("name") String name) throws Exception {

        final CustomResourceDefinition cloudCRD = container.getOSClient().customResourceDefinitions().withName("clouds.streamzi.io").get();
        if (cloudCRD == null) {
            logger.severe("Can't find Cloud CRD");
            return "";
        }

        Cloud cloud = container.getOSClient().customResources(
                cloudCRD,
                Cloud.class,
                CloudList.class,
                DoneableCloud.class)
                .inNamespace(container.getOSClient().getNamespace())
                .withName(name)
                .get();

        if (cloud != null) {
            cloud.getSpec().setToken(null);
            return MAPPER.writeValueAsString(cloud);
        } else {
            return "{}";
        }
    }
}

class TopicDetails implements Serializable {

    private String name;
    private String cloud;

    public TopicDetails(String name, String cloud) {
        this.name = name;
        this.cloud = cloud;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }
}
