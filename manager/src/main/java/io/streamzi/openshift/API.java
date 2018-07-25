package io.streamzi.openshift;


import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IContainer;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
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
        List<IResource> pods = container.getClient().list(ResourceKind.POD, container.getNamespace());
        List<String> results = new ArrayList<>();
        for (IResource r : pods) {
            results.add(r.getName());
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
            c1.setImage(new DockerImageURI("172.30.1.1:5000/hardcoded-test/oc-stream-container:latest"));
           
            IContainer c2 = config.addContainer("streamzi-processor-" + UUID.randomUUID().toString());
            c2.addEnvVar("processor-uuid", UUID.randomUUID().toString());
            c2.setImage(new DockerImageURI("172.30.1.1:5000/hardcoded-test/oc-stream-container:latest"));
            
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

        File[] templates = container.getTemplateDir().listFiles();
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
            ProcessorFlowDeployer deployer = new ProcessorFlowDeployer(container.getClient(), container.getNamespace(), flow);
            final List<IDeploymentConfig> deploymentConfigs = deployer.buildDeploymentConfigs();

            for (IDeploymentConfig dc : deploymentConfigs) {

                for (ConfigMap map : deployer.getTopicMaps()) {
                    logger.info("Creating ConfigMap: " + map.getMetadata().getName());
                    logger.info(map.toString());

                    container.getOSClient().configMaps().inNamespace(map.getMetadata().getNamespace()).withName(map.getMetadata().getName()).createOrReplace(map);

                }

                for (IContainer c : dc.getContainers()) {
                    final Map<String, String> evs = c.getEnvVars();
                    if (evs != null && evs.size() > 0) {
                        final String containerName = c.getName();
                        final String cmName = containerName + "-ev.cm";
                        final String namespace = dc.getNamespace().getName();


                        final Map<String, String> labels = new HashMap<>();
                        labels.put("streamzi.io/kind", "ev");
                        labels.put("streamzi.io/target", c.getName());

                        final ObjectMeta om = new ObjectMeta();
                        om.setName(cmName);
                        om.setNamespace(namespace);
                        om.setLabels(labels);

                        final ConfigMap cm = new ConfigMap();
                        cm.setMetadata(om);
                        cm.setData(evs);

                        container.getOSClient().configMaps().inNamespace(namespace).withName(cmName).createOrReplace(cm);
                    }
                }

                //todo: remove the Environment Variables which are embedded in the DC. This cannot be done via the IDeploymentConfig interface. Relies on switch to fabric8 API..
                logger.info("Creating deployment: " + dc.getName());
                logger.info(dc.toJson());
                container.getClient().create(dc);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
        }
    }
}
