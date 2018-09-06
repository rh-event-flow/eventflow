package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.crds.DoneableFlow;
import io.streamzi.openshift.dataflow.crds.Flow;
import io.streamzi.openshift.dataflow.crds.FlowList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FlowController {

    private static Logger logger = Logger.getLogger(FlowController.class.getName());

    public FlowController() {

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
            final DeploymentConfigBuilder deployer = new DeploymentConfigBuilder(ClientCache.getClient().getNamespace(), flow);
            final List<DeploymentConfig> deploymentConfigs = deployer.buildDeploymentConfigs();

            for (DeploymentConfig dc : deploymentConfigs) {

                for (ConfigMap map : deployer.getTopicMaps()) {
                    logger.info("Creating ConfigMap: " + map.getMetadata().getName());
                    logger.info(map.toString());

                    ClientCache.getClient().configMaps().inNamespace(map.getMetadata().getNamespace()).withName(map.getMetadata().getName()).createOrReplace(map);
                }

                dc.getSpec().getTemplate().getSpec().getContainers()
                        .stream()
                        .filter(container -> container.getEnv() != null && container.getEnv().size() > 0)
                        .forEach(container -> {
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
                            for (EnvVar ev : container.getEnv()) {
                                cmData.put(ev.getName(), ev.getValue());
                            }
                            cm.setData(cmData);

                            //add these CMs to the flow so that we can check if they're still needed
                            deployer.getTopicMaps().add(cm);

                            ClientCache.getClient().configMaps().inNamespace(namespace).withName(cmName).createOrReplace(cm);

                            logger.info("Creating deployment: " + dc.getMetadata().getName());
                            logger.info(dc.toString());
                            ClientCache.getClient().deploymentConfigs().inNamespace(dc.getMetadata().getNamespace()).createOrReplace(dc);
                        });
            }

            //remove DCs that are no longer required.
            Set<String> newDeploymentConfigNames = deploymentConfigs.stream()
                    .map(dc -> dc.getMetadata().getName())
                    .collect(Collectors.toSet());

            ClientCache.getClient().deploymentConfigs().inNamespace(ClientCache.getClient().getNamespace()).withLabel("app", flow.getName())
                    .list()
                    .getItems()
                    .stream()
                    .filter(existingDC -> !newDeploymentConfigNames.contains(existingDC.getMetadata().getName()))
                    .forEach(existingDC -> ClientCache.getClient().deploymentConfigs().inNamespace(ClientCache.getClient().getNamespace()).withName(existingDC.getMetadata().getName()).delete());


            //remove the CMs that are no longer required.
            ClientCache.getClient().configMaps().inNamespace(ClientCache.getClient().getNamespace()).withLabel("app", flow.getName())
                    .list()
                    .getItems()
                    .stream()
                    .filter(existing -> !(existing.getMetadata().getLabels().containsKey("streamzi.io/kind") && existing.getMetadata().getLabels().get("streamzi.io/kind").equals("flow")))
                    .filter(existing -> !deployer.getTopicMapNames().contains(existing.getMetadata().getName()))
                    .forEach(deleted -> ClientCache.getClient().configMaps().inNamespace(ClientCache.getClient().getNamespace()).withName(deleted.getMetadata().getName()).delete());

            //remove the flow CM if the flow is empty
            if (flow.getNodes().isEmpty() && flow.getLinks().isEmpty()) {
                //remove the flow
                final CustomResourceDefinition flowCRD = ClientCache.getClient().customResourceDefinitions().withName("flows.streamzi.io").get();
                ClientCache.getClient().customResources(flowCRD, Flow.class, FlowList.class, DoneableFlow.class).inNamespace(ClientCache.getClient().getNamespace()).delete(customResource);
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
            List<DeploymentConfig> existingDCs = ClientCache.getClient().deploymentConfigs().inNamespace(ClientCache.getClient().getNamespace()).withLabel("app", flow.getName()).list().getItems();

            existingDCs.forEach(existingDC -> ClientCache.getClient().deploymentConfigs().inNamespace(ClientCache.getClient().getNamespace()).withName(existingDC.getMetadata().getName()).delete());

            //todo: work out why the Strimzi topic CMs aren't being deleted - they get recreated even after the containers have gone away
            List<ConfigMap> existingCMs = ClientCache.getClient().configMaps().inNamespace(ClientCache.getClient().getNamespace()).withLabel("app", flow.getName()).list().getItems();
            existingCMs.forEach(existing -> ClientCache.getClient().configMaps().inNamespace(ClientCache.getClient().getNamespace()).withName(existing.getMetadata().getName()).delete());

            //remove the flow
            final CustomResourceDefinition flowCRD = ClientCache.getClient().customResourceDefinitions().withName("flows.streamzi.io").get();
            ClientCache.getClient().customResources(flowCRD, Flow.class, FlowList.class, DoneableFlow.class).inNamespace(ClientCache.getClient().getNamespace()).delete(customResource);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON flow data: " + e.getMessage(), e);
        }
    }
}
