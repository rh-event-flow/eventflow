package io.streamzi.openshift;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import java.io.File;
import java.util.logging.Logger;


/**
 * Contains a Openshift client
 *
 * @author hhiden
 */
@Singleton(name = "ClientContainerBean")
public class ClientContainerBean implements ClientContainer {

    private static final Logger logger = Logger.getLogger(ClientContainerBean.class.getName());
    private File storageDir = new File("/storage");
    private OpenShiftClient osClient;

    @PostConstruct
    public void init() {
        String host = System.getenv("KUBERNETES_SERVICE_HOST");
        int port = Integer.parseInt(System.getenv("KUBERNETES_SERVICE_PORT_HTTPS"));
        String masterUrl = "https://" + host + ":" + port;

        logger.info("Starting ClientContainer");

        osClient = new DefaultOpenShiftClient();
        logger.info("URL:" + osClient.getOpenshiftUrl().toString());
        logger.info("Namespace: " + osClient.getNamespace());

        // Storage folders
        File templateDir = new File(storageDir, "templates");
        if (!templateDir.exists()) {
            templateDir.mkdirs();
            logger.info("Created template dir");
        }

        File flowsDir = new File(storageDir, "flows");
        if (!flowsDir.exists()) {
            flowsDir.mkdirs();
            logger.info("Created flows dir");
        }
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Stopping ClientContainer");
    }

    @Override
    public File getStorageDir() {
        return storageDir;
    }

    @Override
    public File getFlowsDir() {
        return new File(storageDir, "flows");
    }

    @Override
    public File getTemplateDir() {
        return new File(storageDir, "templates");
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
