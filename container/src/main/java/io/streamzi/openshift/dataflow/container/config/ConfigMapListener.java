package io.streamzi.openshift.dataflow.container.config;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import java.util.logging.Logger;

/**
 * Simple wrapper around the K8S client to listen for config map changes and store all of the
 * labels locally
 * @author hhiden
 */
public class ConfigMapListener {
    private static final Logger logger = Logger.getLogger(ConfigMapListener.class.getName());
    private String flowUuid;
    private String processorUuid;
    
    IClient client;

    public ConfigMapListener(String namespace, String flowUuid, String processorUuid) {
        logger.info("Starting ClientContainer");
        String host = System.getenv("KUBERNETES_SERVICE_HOST");
        Integer port = Integer.parseInt(System.getenv("KUBERNETES_SERVICE_PORT_HTTPS"));
        logger.info("Connecting: " + host + ":" + port);
        client = new ClientBuilder("https://" + host + ":" + port)
                .withUserName("system")
                .withPassword("admin")
                .build();

    }
    
    private void findConfigMap(){
        
    }
}
