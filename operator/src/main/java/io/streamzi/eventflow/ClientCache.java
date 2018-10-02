package io.streamzi.eventflow;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.streamzi.eventflow.crds.Cloud;
import io.streamzi.eventflow.crds.CloudList;
import io.streamzi.eventflow.crds.DoneableCloud;
import io.strimzi.api.kafka.KafkaAssemblyList;
import io.strimzi.api.kafka.model.DoneableKafka;
import io.strimzi.api.kafka.model.Kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ClientCache {

    private static Logger logger = Logger.getLogger(ClientCache.class.getName());

    private static final Map<String, OpenShiftClient> apiClients = new HashMap<>();

    private static final Map<String, String> bootstrapServerCache = new HashMap<>();


    static {

        //Get the local OpenShift Client
        OpenShiftClient osClient = new DefaultOpenShiftClient();

        logger.info("Local OpenShift URL: " + osClient.getOpenshiftUrl().toString());
        logger.info("Local OpenShift Namespace: " + osClient.getNamespace());
        apiClients.put("local", osClient);

        //Try and find a Strimzi installation in this namespace
        final CustomResourceDefinition kafkaCRD = osClient.customResourceDefinitions().withName("kafkas.kafka.strimzi.io").get();
        if (kafkaCRD == null) {
            logger.info("Can't find Kafka CRD - Not setting BOOTSTRAP_SERVERS");
        } else {
            List<Kafka> kafkas = osClient.customResources(
                    kafkaCRD,
                    Kafka.class,
                    KafkaAssemblyList.class,
                    DoneableKafka.class)
                    .inNamespace(osClient.getNamespace())
                    .list()
                    .getItems();

            if (kafkas.size() > 0) {
                String localBs = kafkas.get(0).getMetadata().getName() + "-kafka-bootstrap." + kafkas.get(0).getMetadata().getNamespace() + ".svc.cluster.local:9092";
                logger.info("Setting BOOTSTRAP_SERVERS for local to " + localBs);
                bootstrapServerCache.put("local", localBs);
            } else {
                logger.info("Can't find Kafka installation - Not setting BOOTSTRAP_SERVERS");

            }
        }

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
                    .forEach(cloud -> {
                        ConfigBuilder configBuilder = new ConfigBuilder();
                        configBuilder.withMasterUrl("https://" + cloud.getSpec().getHostname() + ":" + cloud.getSpec().getPort());
                        configBuilder.withOauthToken(cloud.getSpec().getToken());
                        configBuilder.withNamespace(cloud.getSpec().getNamespace());

                        OpenShiftClient client = new DefaultOpenShiftClient(configBuilder.build());
                        apiClients.put(cloud.getMetadata().getName(), client);

                        bootstrapServerCache.put(cloud.getMetadata().getName(), cloud.getSpec().getBootstrapServers());

                        logger.info("OpenShift URL (" + cloud.getMetadata().getName() + "): " + client.getOpenshiftUrl().toString());
                        logger.info("OpenShift Namespace(" + cloud.getMetadata().getName() + "): " + client.getNamespace());
                    });
        }
    }

    public static OpenShiftClient getClient() {
        return apiClients.get("local");
    }

    public static OpenShiftClient getClient(String name) {
        return apiClients.get(name);
    }

    public static Set<String> getClientNames() {
        return apiClients.keySet();
    }

    public static Map<String, String> getBootstrapServerCache() {
        return bootstrapServerCache;
    }
}
