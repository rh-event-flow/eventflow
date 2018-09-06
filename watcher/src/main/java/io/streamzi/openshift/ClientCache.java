package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.streamzi.openshift.dataflow.crds.Cloud;
import io.streamzi.openshift.dataflow.crds.CloudList;
import io.streamzi.openshift.dataflow.crds.DoneableCloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ClientCache {

    private static Logger logger = Logger.getLogger(ClientCache.class.getName());
    private static final Map<String, OpenShiftClient> apiClients = new HashMap<>();

    static {
        //Get the local OpenShift Client
        OpenShiftClient osClient = new DefaultOpenShiftClient();
        logger.info("Local OpenShift URL: " + osClient.getOpenshiftUrl().toString());
        logger.info("Local OpenShift Namespace: " + osClient.getNamespace());
        apiClients.put("local", osClient);

        //Get any other OpenShift Clients
        final CustomResourceDefinition cloudCRD = osClient.customResourceDefinitions().withName("clouds.streamzi.io").get();
        if (cloudCRD == null) {
            logger.info("Can't find Cloud CRDs - Local OpenShift only");
        } else {
            osClient.customResources(
                    cloudCRD,
                    Cloud.class,
                    CloudList.class,
                    DoneableCloud.class)
                    .inNamespace(osClient.getNamespace())
                    .list()
                    .getItems()
                    .stream()
                    .filter(cloud -> !cloud.getMetadata().getName().equals("local"))
                    .forEach(cloud -> {
                        ConfigBuilder configBuilder = new ConfigBuilder();
                        configBuilder.withMasterUrl("https://" + cloud.getSpec().getHostname() + ":" + cloud.getSpec().getPort());
                        configBuilder.withOauthToken(cloud.getSpec().getToken());

                        OpenShiftClient client = new DefaultOpenShiftClient(configBuilder.build());
                        apiClients.put(cloud.getMetadata().getName(), client);

                        logger.info("Remote OpenShift URL (" + cloud.getMetadata().getName() + ": " + client.getOpenshiftUrl().toString());
                        logger.info("Remote OpenShift Namespace(" + cloud.getMetadata().getName() + ": " + client.getNamespace());
                    });
        }
    }

    public static OpenShiftClient getClient() {
        return apiClients.get("local");
    }

    public static OpenShiftClient getClient(String name) {
        return apiClients.get(name);
    }

    public Set<String> getOSClientNames() {
        return apiClients.keySet();
    }

}
