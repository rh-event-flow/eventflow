
package io.streamzi.openshift;

import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.images.DockerImageURI;
import com.openshift.restclient.model.IContainer;
import com.openshift.restclient.model.IDeploymentConfig;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
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
public class ProcessorFlowDeployer {
    private static final Logger logger = Logger.getLogger(ProcessorFlowDeployer.class.getName());
    private ProcessorFlow flow;
    private String namespace;
    private String kafkaClusterName = "my-cluster";
    private int kafkaPort = 9092;
    private List<ConfigMap> topicMaps = new ArrayList<>();
    private String registryAddress = "172.30.1.1:5000";

    private IClient client;

    public ProcessorFlowDeployer(IClient client, String namespace, ProcessorFlow flow) {
        this.flow = flow;
        this.client = client;
        this.namespace = namespace;
    }

    public List<IDeploymentConfig> buildDeploymentConfigs() {

        List<IDeploymentConfig> deploymentConfigs = new ArrayList<>();
        HashMap<String, IContainer> containers = new HashMap<>();

        for (ProcessorNode node : flow.getNodes()) {

            IDeploymentConfig config = client.getResourceFactory().stub(ResourceKind.DEPLOYMENT_CONFIG, "streamzi-processor-" + node.getUuid(), namespace);

            // Basic metadata and settings
            config.addLabel("app", node.getUuid());
            config.addLabel("streamzi.type", "processor-flow");
            config.addTemplateLabel("app", flow.getName());
            config.setReplicas(1);
            IContainer container = populateNodeDeployments(node, config);
            containers.put(container.getName(), container);

            deploymentConfigs.add(config);
        }

        populateTopicMaps(containers);

        return deploymentConfigs;
    }

    public List<ConfigMap> getTopicMaps() {
        return topicMaps;
    }

    private IContainer populateNodeDeployments(ProcessorNode node, IDeploymentConfig config) {
        IContainer nodeContainer;

        nodeContainer = config.addContainer("streamzi-processor-" + node.getUuid());
        nodeContainer.addEnvVar(ProcessorConstants.NODE_UUID, node.getUuid());

        // Add environment variables for node settingds
        for (String key : node.getSettings().keySet()) {
            nodeContainer.addEnvVar(key, node.getSettings().get(key));
        }

        // Add environment variables for the flow global settings
        for (String key : node.getParent().getGlobalSettings().keySet()) {
            nodeContainer.addEnvVar(key, node.getParent().getGlobalSettings().get(key));
        }

        for (ProcessorLink link : flow.getLinks()) {
            logger.info("Processing link");
            String topicName = link.getSource().getParent().getUuid() + "-" + link.getSource().getName()
                    + "." + link.getTarget().getParent().getUuid() + "-" + link.getTarget().getName();

            // Set the topic name in the source node container
            if (node.getUuid().equals(link.getSource().getParent().getUuid())) {
                nodeContainer.addEnvVar(link.getSource().getName(), topicName);
            }

            // Set the topic name in the target node container
            if (node.getUuid().equals(link.getTarget().getParent().getUuid())) {
                nodeContainer.addEnvVar(link.getTarget().getName(), topicName);
            }

        }
        nodeContainer.setImage(new DockerImageURI(registryAddress + "/" + namespace + "/" + node.getImageName() + ":latest"));

        return nodeContainer;
    }

    private void populateTopicMaps(HashMap<String, IContainer> containers) {
        final ConfigMap cm = new ConfigMap();
        String topicName;
        IContainer sourceContainer;
        IContainer targetContainer;

        for (ProcessorLink link : flow.getLinks()) {
            logger.info("Processing link");
            topicName = link.getSource().getParent().getUuid() + "-" + link.getSource().getName()
                    + "." + link.getTarget().getParent().getUuid() + "-" + link.getTarget().getName();

            // Set the topic name in the source node container
            if (containers.containsKey(link.getSource().getParent().getUuid())) {
                sourceContainer = containers.get(link.getSource().getParent().getUuid());
                sourceContainer.addEnvVar(link.getSource().getName(), topicName);
            }

            // Set the topic name in the target node container
            if (containers.containsKey(link.getTarget().getParent().getUuid())) {
                targetContainer = containers.get(link.getTarget().getParent().getUuid());
                targetContainer.addEnvVar(link.getTarget().getName(), topicName);
            }

            final Map<String, String> data = new HashMap<>();
            data.put("name", topicName);
            data.put("partitions", "2");
            data.put("replicas", "1");

            final Map<String, String> labels = new HashMap<>();
            labels.put("strimzi.io/cluster", kafkaClusterName);
            labels.put("strimzi.io/kind", "topic");

            final ObjectMeta om = new ObjectMeta();
            om.setName(topicName);
            om.setNamespace(namespace);
            om.setLabels(labels);

            cm.setMetadata(om);
            cm.setData(data);

            topicMaps.add(cm);
        }
    }
}