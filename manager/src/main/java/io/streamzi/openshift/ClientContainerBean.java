package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.streamzi.openshift.dataflow.crds.Cloud;
import io.streamzi.openshift.dataflow.crds.CloudList;
import io.streamzi.openshift.dataflow.crds.DoneableCloud;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Contains a Openshift client
 *
 * @author hhiden
 */
@Singleton(name = "ClientContainerBean")
public class ClientContainerBean implements ClientContainer {

    private static final Logger logger = Logger.getLogger(ClientContainerBean.class.getName());
    private final Map<String, OpenShiftClient> apiClients = new HashMap<>();

    @PostConstruct
    public void init() {

        logger.info("Starting ClientContainer");

        //Get the local OpenShift Client
        OpenShiftClient osClient = new DefaultOpenShiftClient();
        logger.info("Local OpenShift URL: " + osClient.getOpenshiftUrl().toString());
        logger.info("Local OpenShift Namespace: " + osClient.getNamespace());
        apiClients.put("default", osClient);

        //Get any other OpenShift Clients
        final CustomResourceDefinition cloudCRD = osClient.customResourceDefinitions().withName("clouds.streamzi.io").get();
        if (cloudCRD == null) {
            logger.info("Can't find Cloud CRDs - Local OpenShift only");
            return;
        }

        osClient.customResources(
                cloudCRD,
                Cloud.class,
                CloudList.class,
                DoneableCloud.class)
                .inNamespace(osClient.getNamespace())
                .list()
                .getItems()
                .forEach(cloud -> {
                    ConfigBuilder configBuilder = new ConfigBuilder();
                    configBuilder.withMasterUrl("https://" + cloud.getSpec().getHostname() + ":" + cloud.getSpec().getPort());
                    configBuilder.withOauthToken(cloud.getSpec().getToken());
                    configBuilder.withNamespace(cloud.getSpec().getNamespace());

                    OpenShiftClient client = new DefaultOpenShiftClient(configBuilder.build());
                    apiClients.put(cloud.getMetadata().getName(), client);

                    logger.info("OpenShift URL (" + cloud.getMetadata().getName() + "): " + client.getOpenshiftUrl().toString());
                    logger.info("OpenShift Namespace(" + cloud.getMetadata().getName() + "): " + client.getNamespace());
                });
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Stopping ClientContainer");
    }

    @Override
    public String getNamespace() {
        return getOSClient().getNamespace();
    }

    @Override
    public OpenShiftClient getOSClient() {
        return apiClients.get("default");
    }

    @Override
    public Set<String> getOSClientNames() {
        return apiClients.keySet();
    }

    @Override
    public OpenShiftClient getOSClient(String name) {
        return apiClients.get(name);
    }
}
