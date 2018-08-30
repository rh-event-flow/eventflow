package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.crds.DoneableFlow;
import io.streamzi.openshift.dataflow.model.crds.Flow;
import io.streamzi.openshift.dataflow.model.crds.FlowList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlowController {

    private static Logger logger = Logger.getLogger(FlowController.class.getName());

    private OpenShiftClient osClient;

    public FlowController() {
        osClient = new DefaultOpenShiftClient();
    }


    public void onAdded(Flow flow) {
        createOrUpdate(flow);
    }

    public void onModified(Flow flow) {
        createOrUpdate(flow);
    }

    public void onDeleted(Flow flow) {
        delete(flow);
    }

    private void createOrUpdate(Flow customResource) {

        try {

            ProcessorFlow flow = new ProcessorFlow(customResource.getSpec());
            logger.info("Flow Parsed OK");

            // Now try and build a deployment
            final DeploymentConfigBuilder deployer = new DeploymentConfigBuilder(osClient.getNamespace(), flow);
            final List<DeploymentConfig> deploymentConfigs = deployer.buildDeploymentConfigs();

            for (DeploymentConfig dc : deploymentConfigs) {

                for (ConfigMap map : deployer.getTopicMaps()) {
                    logger.info("Creating ConfigMap: " + map.getMetadata().getName());
                    logger.info(map.toString());

                    osClient.configMaps().inNamespace(map.getMetadata().getNamespace()).withName(map.getMetadata().getName()).createOrReplace(map);
                }

                //Add in the ConfigMaps for updating the environment variables of the deployments
                for (Container c : dc.getSpec().getTemplate().getSpec().getContainers()) {
                    final List<EnvVar> evs = c.getEnv();
                    if (evs != null && evs.size() > 0) {
                        final String cmName = dc.getMetadata().getName() + "-ev.cm";
                        final String namespace = dc.getMetadata().getNamespace();

                        final Map<String, String> labels = new HashMap<>();
                        labels.put("streamzi.io/kind", "ev");
                        labels.put("streamzi.io/target", dc.getMetadata().getName());
                        labels.put("app", flow.getName());

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

                        //add these CMs to the flow so that we can check if they're still needed
                        deployer.getTopicMaps().add(cm);

                        osClient.configMaps().inNamespace(namespace).withName(cmName).createOrReplace(cm);
                    }

                    logger.info("Creating deployment: " + dc.getMetadata().getName());
                    logger.info(dc.toString());
                    osClient.deploymentConfigs().inNamespace(dc.getMetadata().getNamespace()).createOrReplace(dc);
                }
            }

            //remove DCs that are no longer required.
            List<DeploymentConfig> existingDCs = osClient.deploymentConfigs().inNamespace(osClient.getNamespace()).withLabel("app", flow.getName()).list().getItems();

            for (DeploymentConfig existingDC : existingDCs) {
                //don't delete the flow itself
                boolean found = false;
                for (DeploymentConfig newDC : deploymentConfigs) {
                    if (existingDC.getMetadata().getName().equals(newDC.getMetadata().getName())) {
                        found = true;
                    }
                }

                if (!found) {
                    logger.info("Removing DeploymentConfig: " + osClient.getNamespace() + "/" + existingDC.getMetadata().getName());
                    osClient.deploymentConfigs().inNamespace(osClient.getNamespace()).withName(existingDC.getMetadata().getName()).delete();
                }
            }

            List<ConfigMap> existingCMs = osClient.configMaps().inNamespace(osClient.getNamespace()).withLabel("app", flow.getName()).list().getItems();
            for (ConfigMap existing : existingCMs) {

                if (!(existing.getMetadata().getLabels().containsKey("streamzi.io/kind") && existing.getMetadata().getLabels().get("streamzi.io/kind").equals("flow"))) {
                    boolean found = false;
                    for (ConfigMap newCM : deployer.getTopicMaps()) {
                        if (existing.getMetadata().getName().equals(newCM.getMetadata().getName())) {
                            found = true;
                        }
                    }

                    if (!found) {
                        logger.info("Deleting ConfigMap: " + existing.getMetadata().getName());
                        osClient.configMaps().inNamespace(osClient.getNamespace()).withName(existing.getMetadata().getName()).delete();
                    }
                }
            }

            //remove the flow CM if the flow is empty
            if (flow.getNodes().size() == 0 && flow.getLinks().size() == 0) {
                //remove the flow
                final CustomResourceDefinition flowCRD = osClient.customResourceDefinitions().withName("flows.streamzi.io").get();
                osClient.customResources(flowCRD, Flow.class, FlowList.class, DoneableFlow.class).inNamespace(osClient.getNamespace()).delete(customResource);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
        }
    }

    private void delete(Flow customResource) {

        try {
            ProcessorFlow flow = new ProcessorFlow(customResource.getSpec());
            logger.info("Flow Parsed OK");

            //todo: is this safe and/or the best way to do it. Other apps *could* be deleted.
            //remove DCs that are no longer required.
            List<DeploymentConfig> existingDCs = osClient.deploymentConfigs().inNamespace(osClient.getNamespace()).withLabel("app", flow.getName()).list().getItems();

            for (DeploymentConfig existingDC : existingDCs) {
                osClient.deploymentConfigs().inNamespace(osClient.getNamespace()).withName(existingDC.getMetadata().getName()).delete();
            }

            //todo: work out why the Strimzi topic CMs aren't being deleted - they get recreated even after the containers have gone away
            List<ConfigMap> existingCMs = osClient.configMaps().inNamespace(osClient.getNamespace()).withLabel("app", flow.getName()).list().getItems();
            for (ConfigMap existing : existingCMs) {
                osClient.configMaps().inNamespace(osClient.getNamespace()).withName(existing.getMetadata().getName()).delete();
            }

            //remove the flow
            final CustomResourceDefinition flowCRD = osClient.customResourceDefinitions().withName("flows.streamzi.io").get();
            osClient.customResources(flowCRD, Flow.class, FlowList.class, DoneableFlow.class).inNamespace(osClient.getNamespace()).delete(customResource);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
        }
    }
}
