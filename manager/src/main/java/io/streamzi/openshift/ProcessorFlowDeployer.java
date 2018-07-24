
package io.streamzi.openshift;

import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.images.DockerImageURI;
import com.openshift.restclient.model.IConfigMap;
import com.openshift.restclient.model.IContainer;
import com.openshift.restclient.model.IDeploymentConfig;
import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorLink;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * This class deploys a ProcessorFlow as a series of containers and wires them
 * together with ConfigMaps
 * @author hhiden
 */
public class ProcessorFlowDeployer {
    private static final Logger logger = Logger.getLogger(ProcessorFlowDeployer.class.getName());
    private ProcessorFlow flow;
    private String namespace;
    private String kafkaClusterName = "my-cluster";
    private int kafkaPort = 9092;
    private List<IConfigMap> topicMaps = new ArrayList<>();
    private String registryAddress = "172.30.1.1:5000";
    
    private IClient client;
    
    public ProcessorFlowDeployer(IClient client, String namespace, ProcessorFlow flow) {
        this.flow = flow;
        this.client = client;
        this.namespace = namespace;
    }
    
    public IDeploymentConfig buildDeploymentConfig(){
        IDeploymentConfig config = client.getResourceFactory().stub(ResourceKind.DEPLOYMENT_CONFIG, flow.getName(), namespace);
        
        // Basic metadata and settings
        config.addLabel("app", flow.getName());
        config.addLabel("streamzi.type", "processor-flow");
        config.addTemplateLabel("app", flow.getName());        
        config.setReplicas(1);
        HashMap<String, IContainer> containers = populateNodeDeployments(config);
        populateTopicMaps(containers);
        
        return config;
    }
    
    public List<IConfigMap> getTopicMaps(){
        return topicMaps;
    }
    
    private HashMap<String, IContainer> populateNodeDeployments(IDeploymentConfig config){
        IContainer nodeContainer;
        HashMap<String, IContainer> configs = new HashMap<>();
        for(ProcessorNode node : flow.getNodes()){
            nodeContainer = config.addContainer("streamzi-processor-" + node.getUuid());
            nodeContainer.addEnvVar(ProcessorConstants.NODE_UUID, node.getUuid());

            // Add environment variables for node settingds
            for(String key : node.getSettings().keySet()){
                nodeContainer.addEnvVar(key, node.getSettings().get(key));
            }
            
            // Add environment variables for the flow global settings
            for(String key : node.getParent().getGlobalSettings().keySet()){
                nodeContainer.addEnvVar(key, node.getParent().getGlobalSettings().get(key));
            }
            
            nodeContainer.setImage(new DockerImageURI(registryAddress + "/" + namespace + "/" + node.getImageName() + ":latest")); 
            configs.put(node.getUuid(), nodeContainer);
        }
        return configs;
    }
    
    private void populateTopicMaps(HashMap<String, IContainer> containers){
        IConfigMap linkConfig;
        String topicName;
        IContainer sourceContainer;
        IContainer targetContainer;
        
        for(ProcessorLink link : flow.getLinks()){
            logger.info("Processing link");
            topicName = link.getSource().getParent().getUuid() + "-" + link.getSource().getName() 
                    + "." + link.getTarget().getParent().getUuid() + "-" + link.getTarget().getName();

            // Set the topic name in the source node container
            if(containers.containsKey(link.getSource().getParent().getUuid())){
                sourceContainer = containers.get(link.getSource().getParent().getUuid());
                sourceContainer.addEnvVar(link.getSource().getName(), topicName);
            }
            
            // Set the topic name in the target node container
            if(containers.containsKey(link.getTarget().getParent().getUuid())){
                targetContainer = containers.get(link.getTarget().getParent().getUuid());
                targetContainer.addEnvVar(link.getTarget().getName(), topicName);
            }
            
            JSONObject topicJson = new JSONObject();
            topicJson.put("apiVersion", "v1");
            topicJson.put("kind", "ConfigMap");
            JSONObject metadata = new JSONObject();
            
            JSONObject labels = new JSONObject();
            labels.put("strimzi.io/cluster", kafkaClusterName);
            labels.put("strimzi.io/kind", "topic");
            
            metadata.put("labels", labels);
            metadata.put("name", topicName);
            metadata.put("namespace", namespace);
            topicJson.put("metadata", metadata);
            
            JSONObject data = new JSONObject();
            data.put("name", topicName);
            data.put("partitions", "2");
            data.put("replicas", "1");
            topicJson.put("data", data);       
            linkConfig = client.getResourceFactory().create(topicJson.toString());
            topicMaps.add(linkConfig);
        }
    }
}