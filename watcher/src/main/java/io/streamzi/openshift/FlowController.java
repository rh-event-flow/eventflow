package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorFlowReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlowController {

    Logger logger = Logger.getLogger(FlowController.class.getName());

    private OpenShiftClient osClient;

    public FlowController() {
        osClient = new DefaultOpenShiftClient();
    }


    public void onAdded(ConfigMap configMap) throws Exception {
        apply(configMap);
    }

    public void onModified(ConfigMap configMap) throws Exception {
        apply(configMap);
    }

    public void onDeleted(ConfigMap configMap) throws Exception {
        apply(configMap);
    }

    private void apply(ConfigMap configMap) throws Exception {

        if (configMap.getData().containsKey("flow")) {

            try {
                ProcessorFlowReader reader = new ProcessorFlowReader();
                ProcessorFlow flow = reader.readFromJsonString(configMap.getData().get("flow"));
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

                //todo: work out why the Strimzi topic CMs aren't being deleted - they get recreated even after the containers have gone away

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
                if(flow.getNodes().size() == 0 && flow.getLinks().size() == 0){
                    osClient.configMaps().inNamespace(osClient.getNamespace()).withName(flow.getName() + ".cm").delete();
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
            }
        } else {
            throw new Exception("No flow key in ConfigMap");
        }
    }
}
