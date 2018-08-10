
package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorLink;
import io.streamzi.openshift.dataflow.model.ProcessorNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class deploys a ProcessorFlow as a series of containers and wires them
 * together with ConfigMaps
 *
 * @author hhiden
 */
public class DeploymentConfigBuilder {
    private static final Logger logger = Logger.getLogger(DeploymentConfigBuilder.class.getName());
    private ProcessorFlow flow;
    private String namespace;
    private final String kafkaClusterName = "my-cluster";
    private final List<ConfigMap> topicMaps = new ArrayList<>();
    private final String registryAddress = "172.30.1.1:5000";


    public DeploymentConfigBuilder(String namespace, ProcessorFlow flow) {
        this.flow = flow;
        this.namespace = namespace;
    }

    public List<ConfigMap> getTopicMaps() {
        return topicMaps;
    }

    public List<DeploymentConfig> buildDeploymentConfigs() {

        final Map<String, DeploymentConfig> deploymentConfigs = new HashMap<>();

        for (ProcessorNode node : flow.getNodes()) {

//            Random r = new Random();
//            int x = r.nextInt(1000);

            final String dcName = flow.getName() + "-" + node.getImageName(); //TODO: Add back in to get unique deployments + "-" + x;
            final Container container = populateNodeDeployments(node);

            final DeploymentConfig dc = new io.fabric8.openshift.api.model.DeploymentConfigBuilder()
                    .withNewMetadata()
                    .withName(dcName)
                    .withNamespace(namespace)
                    .addToLabels("app", flow.getName())
                    .addToLabels("streamzi/type", "processor-flow")
                    .endMetadata()
                    .withNewSpec()
                    .withReplicas(1)
                    .addNewTrigger()
                    .withType("ConfigChange")
                    .endTrigger()
                    .withNewTemplate()
                    .withNewMetadata()
                    .addToLabels("app", flow.getName())
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainerLike(container)
                    .endContainer()
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            deploymentConfigs.put(dcName, dc);

        }

        populateTopicMaps();

        return new ArrayList<>(deploymentConfigs.values());
    }


    private Container populateNodeDeployments(ProcessorNode node) {


        final List<EnvVar> envVars = new ArrayList<>();
        envVars.add(new EnvVar(sanitiseEnvVar(ProcessorConstants.NODE_UUID), node.getUuid(), null));

        // Add environment variables for node settingds
        for (String key : node.getSettings().keySet()) {
            envVars.add(new EnvVar(sanitiseEnvVar(key), node.getSettings().get(key), null));
        }

        // Add environment variables for the flow global settings
        for (String key : node.getParent().getGlobalSettings().keySet()) {
            envVars.add(new EnvVar(sanitiseEnvVar(key), node.getParent().getGlobalSettings().get(key), null));
        }

        for (ProcessorLink link : flow.getLinks()) {

            final String topicName = flow.getName() + "-" + link.getSource().getParent().getUuid() + "-" + link.getSource().getName();

            // Set the topic name in the source node container
            if (node.getUuid().equals(link.getSource().getParent().getUuid())) {
                envVars.add(new EnvVar(sanitiseEnvVar(link.getSource().getName()), topicName, null));
            }

            // Set the topic name in the target node container
            if (node.getUuid().equals(link.getTarget().getParent().getUuid())) {
                envVars.add(new EnvVar(sanitiseEnvVar(link.getTarget().getName()), topicName, null));
            }

        }
        return new ContainerBuilder()
                .withName(node.getParent().getName())
                .withImage(registryAddress + "/" + namespace + "/" + node.getImageName() + ":latest")
                .withEnv(envVars)
                .build();
    }


    private void populateTopicMaps() {
        final ConfigMap cm = new ConfigMap();
        String topicName;

        //todo: deal with unconnected outputs - should still have a topic created
        //todo: deal with different transports
        for (ProcessorLink link : flow.getLinks()) {
            logger.info("Processing link");
            topicName = flow.getName() + "-" + link.getSource().getParent().getUuid() + "-" + link.getSource().getName();

            final Map<String, String> data = new HashMap<>();
            data.put("name", topicName);
            data.put("partitions", "2");
            data.put("replicas", "1");

            final Map<String, String> labels = new HashMap<>();
            labels.put("strimzi.io/cluster", kafkaClusterName);
            labels.put("strimzi.io/kind", "topic");
            labels.put("app", flow.getName());

            final ObjectMeta om = new ObjectMeta();
            om.setName(topicName);
            om.setNamespace(namespace);
            om.setLabels(labels);

            cm.setMetadata(om);
            cm.setData(data);

            topicMaps.add(cm);
        }
    }


    private String sanitiseEnvVar(String source) {
        return source
                .replace("-", "_")
                .replace(".", "_")
                .toUpperCase();
    }
}