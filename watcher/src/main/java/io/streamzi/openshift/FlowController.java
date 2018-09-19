package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.streamzi.openshift.dataflow.crds.DoneableFlow;
import io.streamzi.openshift.dataflow.crds.Flow;
import io.streamzi.openshift.dataflow.crds.FlowList;
import io.streamzi.openshift.dataflow.deployment.TargetState;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.strimzi.api.kafka.Crds;
import io.strimzi.api.kafka.KafkaTopicList;
import io.strimzi.api.kafka.model.DoneableKafkaTopic;
import io.strimzi.api.kafka.model.KafkaTopic;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

            flow.getCloudNames().forEach(cloudName -> {
                OpenShiftClient client = ClientCache.getClient(cloudName);
                if (client != null) {

                    TargetState target = new TargetState(cloudName, flow, ClientCache.getBootstrapServerCache());
                    target.build();

                    //create / update Strimzi Topic configmaps
                    target.getTopicCrds()
                            .forEach(cr -> client.customResources(Crds.topic(), KafkaTopic.class, KafkaTopicList.class, DoneableKafkaTopic.class)
                                    .inNamespace(client.getNamespace())
                                    .withName(cr.getMetadata().getName())
                                    .createOrReplace((KafkaTopic) cr));

                    //create / update deployments
                    target.getDeploymentConfigs().forEach(dc -> client.deploymentConfigs()
                            .inNamespace(client.getNamespace())
                            .withName(dc.getMetadata().getName())
                            .createOrReplace(dc));

                    //create / update environment variable configmaps
                    target.getEvConfigMaps().forEach(cm -> client.configMaps()
                            .inNamespace(client.getNamespace())
                            .withName(cm.getMetadata().getName())
                            .createOrReplace(cm));

                } else {
                    logger.info("Ignoring Cloud: " + cloudName);
                }

            });

            // Because the flow may have removed all items in a cloud we need to go through all the clouds that we know about.
            ClientCache.getClientNames()
                    .forEach(cloudName -> {
                        OpenShiftClient client = ClientCache.getClient(cloudName);

                        TargetState target = new TargetState(cloudName, flow, ClientCache.getBootstrapServerCache());
                        target.build();

                        client.deploymentConfigs().inNamespace(client.getNamespace()).withLabel("app", flow.getName())
                                .list()
                                .getItems()
                                .stream()
                                .filter(existingDC -> !target.getDeploymentConfigNames().contains(existingDC.getMetadata().getName()))
                                .forEach(existingDC -> client.deploymentConfigs().inNamespace(client.getNamespace()).withName(existingDC.getMetadata().getName()).delete());


                        //remove the CMs that are no longer required.
                        client.configMaps().inNamespace(client.getNamespace()).withLabel("app", flow.getName())
                                .list()
                                .getItems()
                                .stream()
                                .filter(existing -> !(existing.getMetadata().getLabels().containsKey("streamzi.io/kind") && existing.getMetadata().getLabels().get("streamzi.io/kind").equals("flow")))
                                .filter(existing -> !target.getConfigMapNames().contains(existing.getMetadata().getName()))
                                .forEach(deleted -> client.configMaps().inNamespace(client.getNamespace()).withName(deleted.getMetadata().getName()).delete());

                        //todo: remove Strimzi CMs

                        //remove the flow CM if the flow is empty
                        if (flow.getNodes().isEmpty() && flow.getLinks().isEmpty()) {
                            if (customResource.getMetadata().getNamespace().equals(client.getNamespace())) {//remove the flow
                                final CustomResourceDefinition flowCRD = client.customResourceDefinitions().withName("flows.streamzi.io").get();
                                client.customResources(flowCRD, Flow.class, FlowList.class, DoneableFlow.class).inNamespace(client.getNamespace()).delete(customResource);
                            }
                        }

                    });

            System.out.println("Done");

        } catch (Exception e) {
            e.printStackTrace();
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
