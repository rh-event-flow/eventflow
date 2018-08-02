package io.streamzi.openshift;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import java.util.logging.Logger;


/**
 * Contains a Openshift client
 *
 * @author hhiden
 */
@Singleton(name = "ClientContainerBean")
public class ClientContainerBean implements ClientContainer {

    private static final Logger logger = Logger.getLogger(ClientContainerBean.class.getName());
    private OpenShiftClient osClient;

    @PostConstruct
    public void init() {

        logger.info("Starting ClientContainer");

        osClient = new DefaultOpenShiftClient();
        logger.info("URL:" + osClient.getOpenshiftUrl().toString());
        logger.info("Namespace: " + osClient.getNamespace());
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
        return osClient;
    }
}
