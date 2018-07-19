package io.streamzi.openshift;



import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IConfigMap;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IResource;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorFlowReader;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorFlowWriter;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLReader;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLWriter;
import io.streamzi.openshift.dataflow.model.serialization.SerializedFlow;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author hhiden
 */
@ApplicationScoped
@Path("/api")
public class API {
    private static final Logger logger = Logger.getLogger(API.class.getName());
    
    @EJB(beanInterface = ClientContainer.class)
    private ClientContainer container;

    @GET
    @Path("/pods")
    @Produces("application/json")
    public List<String> listPods() {
        List<IResource> pods = container.getClient().list(ResourceKind.POD, "hardcoded-test");
        List<String> results = new ArrayList<>();
        for(IResource r : pods){
            results.add(r.getName());
        }
        return results;
    }

    @GET
    @Path("/deployments/{namespace}/create/{name}")
    @Produces("application/json")
    public String create(@PathParam("namespace")String namespace, @PathParam("name")String name){
        /*
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
            c1.setImage(new DockerImageURI("172.30.1.1:5000/hardcoded-test/oc-stream-container:latest"));
           
            IContainer c2 = config.addContainer("streamzi-processor-" + UUID.randomUUID().toString());
            c2.addEnvVar("processor-uuid", UUID.randomUUID().toString());
            c2.setImage(new DockerImageURI("172.30.1.1:5000/hardcoded-test/oc-stream-container:latest"));
            
            config = container.getClient().create(config);
            return config.toJson();
            
        } else {
            return "NOTHING";
        }
        */
        return "";
        
    }
    
    @POST
    @Path("/dataflows")
    @Produces("application/json")
    public String deployProcessorFlow(SerializedFlow flow){
        // Create a deployment config
        logger.info("Deploying flow");
        return "";
    }
    
    @POST
    @Path("/processors")
    public String createProcessorDeployment(String processorId) throws Exception {
        File yamlFile = new File(container.getTemplateDir(), processorId + ".yml");
        if(yamlFile.exists()){
            ProcessorTemplateYAMLReader reader = new ProcessorTemplateYAMLReader(yamlFile);
            ProcessorNodeTemplate template = reader.readTemplate();
            
            // Create a new deployment
            
        }
        return "";
    }
    
    @POST
    @Path("/dataflows/{uuid}")
    @Produces("application/json")
    public String updateProcessorFlow(@PathParam("uuid")String deploymentUuid, SerializedFlow flow){
        return "";
    }
    
    @GET
    @Path("/dataflows/{uuid}")
    @Produces("application/json")
    public String getProcessorFlowDeployment(String uuid){
        return "";
    }
    
    @POST
    @Path("/processors")
    @Consumes("application/json")
    public void postYaml(String yamlData){
        logger.info(yamlData);
        try {
            ProcessorNodeTemplate template = ProcessorTemplateYAMLReader.readTemplateFromString(yamlData);
            logger.info("Valid template for image: " + template.getImageName());
            
            // Save this in the storage folder
            ProcessorTemplateYAMLWriter writer = new ProcessorTemplateYAMLWriter(template);
            writer.writeToFile(container.getTemplateDir());
            
        } catch (Exception e){
            logger.log(Level.SEVERE, "Error parsing YAML data: " + e.getMessage(), e);
        }
        
    }
    
    @POST
    @Path("/flows")
    @Consumes("application/json")
    public void postFlow(String flowJson){
        logger.info(flowJson);
        try {
            ProcessorFlowReader reader = new ProcessorFlowReader();
            ProcessorFlow flow = reader.readFromJsonString(flowJson);
            logger.info("Flow Parsed OK");
                    
            // Write this to the deployments folder
            ProcessorFlowWriter writer = new ProcessorFlowWriter(flow);
            File flowFile = new File(container.getFlowsDir(), flow.getName() + ".json");
            writer.writeToFile(flowFile);
            logger.info("Flow written OK");
            
            // Now try and build a deployment
            ProcessorFlowDeployer deployer = new ProcessorFlowDeployer(container.getClient(), "hardcoded-test", flow);
            IDeploymentConfig dc = deployer.buildDeploymentConfig();
            
            IResource existing;
            for(IConfigMap map : deployer.getTopicMaps()){
                logger.info("Creating ConfigMap: " + map.getName());
                logger.info(map.toJson());
                try {
                    existing = container.getClient().get(ResourceKind.CONFIG_MAP, map.getName(), "hardcoded-test");
                } catch (Exception e){
                    logger.info("ConfigMap not found");
                    existing = null;
                }
                if(existing==null){
                    container.getClient().create(map);
                }
            }
            
            logger.info("Creating deployment: " + dc.getName());
            logger.info(dc.toJson());
            container.getClient().create(dc);
            
        } catch (Exception e){
            logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
        }
    }
}
