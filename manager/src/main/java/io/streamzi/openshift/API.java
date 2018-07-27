package io.streamzi.openshift;


import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Upload a new flow
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

            // Write this to the deployments folder
            ProcessorFlowWriter writer = new ProcessorFlowWriter(flow);
            File flowFile = new File(container.getFlowsDir(), flow.getName() + ".json");
            writer.writeToFile(flowFile);
            logger.info("Flow written OK");

            // Now try and build a deployment
            final ProcessorFlowDeployer deployer = new ProcessorFlowDeployer(container.getNamespace(), flow);
            final List<DeploymentConfig> deploymentConfigs = deployer.buildDeploymentConfigs();

            for (DeploymentConfig dc : deploymentConfigs) {

                for (ConfigMap map : deployer.getTopicMaps()) {
                    logger.info("Creating ConfigMap: " + map.getMetadata().getName());
                    logger.info(map.toString());

                    container.getOSClient().configMaps().inNamespace(map.getMetadata().getNamespace()).withName(map.getMetadata().getName()).createOrReplace(map);
                }

                for (Container c : dc.getSpec().getTemplate().getSpec().getContainers()) {
                    final List<EnvVar> evs = c.getEnv();
                    if (evs != null && evs.size() > 0) {
                        final String cmName = dc.getMetadata().getName() + "-ev.cm";
                        final String namespace = dc.getMetadata().getNamespace();

                        final Map<String, String> labels = new HashMap<>();
                        labels.put("streamzi.io/kind", "ev");
                        labels.put("streamzi.io/target", dc.getMetadata().getName());

                        final ObjectMeta om = new ObjectMeta();
                        om.setName(cmName);
                        om.setNamespace(namespace);
                        om.setLabels(labels);

                        final ConfigMap cm = new ConfigMap();
                        cm.setMetadata(om);
                        Map<String, String> cmData = new HashMap<>();
                        for (EnvVar ev : evs) {
                            cmData.put(ev.getName(), ev.getValue());
                        }
                        cm.setData(cmData);

                        container.getOSClient().configMaps().inNamespace(namespace).withName(cmName).createOrReplace(cm);
                    }

                    logger.info("Creating deployment: " + dc.getMetadata().getName());
                    logger.info(dc.toString()); //.toJson());
                    container.getOSClient().deploymentConfigs().inNamespace(dc.getMetadata().getNamespace()).createOrReplace(dc);
                }
            }

            //remove DCs that are no longer required.
            List<DeploymentConfig> existingDCs = container.getOSClient().deploymentConfigs().inNamespace(container.getNamespace()).withLabel("app", flow.getName()).list().getItems();
            for(DeploymentConfig existingDC : existingDCs){

                boolean found = false;
                for(DeploymentConfig  newDC : deploymentConfigs){
                    if(existingDC.getMetadata().getName().equals(newDC.getMetadata().getName())){
                        found = true;
                    }
                }

                if(!found){
                    logger.info("Removing DeploymentConfig: " + container.getNamespace() + "/" + existingDC.getMetadata().getName());
                    container.getOSClient().deploymentConfigs().inNamespace(container.getNamespace()).withName(existingDC.getMetadata().getName()).delete();
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
        }
    }
}
