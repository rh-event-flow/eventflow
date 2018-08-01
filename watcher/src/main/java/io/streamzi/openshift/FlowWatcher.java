package io.streamzi.openshift;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;

import java.util.Map;
import java.util.logging.Logger;

public class FlowWatcher implements Watcher<ConfigMap>, Runnable {

    private static final Logger logger = Logger.getLogger(FlowWatcher.class.getName());

    private String cmPredicate;

    private FlowController controller;

    public FlowWatcher(FlowController controller, String cmPredicate) {
        this.cmPredicate = cmPredicate;
        this.controller = controller;
    }

    @Override
    public void eventReceived(Action action, ConfigMap configMap) {
        final ObjectMeta metadata = configMap.getMetadata();
        final Map<String, String> labels = metadata.getLabels();

        if (labelValid(cmPredicate, configMap)) {
            final String name = metadata.getName();

            logger.fine("ConfigMap watch received event " + action + " on map " + name + " with labels" + labels);

            try {
                switch (action) {
                    case ADDED:
                        controller.onAdded(configMap);
                        break;
                    case MODIFIED:
                        controller.onModified(configMap);
                        break;
                    case DELETED:
                        controller.onDeleted(configMap);
                        break;
                    case ERROR:
                        logger.warning("Watch received action=ERROR for ConfigMap " + name);
                }
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
    }

    /**
     * Thread that's running the CM Watcher
     */
    @Override
    public void run() {
        logger.info("Starting FlowWatcher");

        final KubernetesClient client = new DefaultKubernetesClient();

        client.configMaps().inNamespace(client.getNamespace()).watch(this);
    }

    @Override
    public void onClose(KubernetesClientException e) {
        logger.info("Closing Watcher: " + this);
        logger.info(e.getMessage());
    }

    /**
     * Is the label valid according to the predicate that we're 'listening' to?
     *
     * @param predictate label in the form of key=value e.g. streamzi.io/kind=ev
     * @param configMap  ConfigMap to test
     * @return true if the key=value label exists, false if not
     */
    private boolean labelValid(String predictate, ConfigMap configMap) {
        String[] parts = predictate.split("=");
        String labelKey = parts[0];
        String labelValue = parts[1];

        if (configMap.getMetadata().getLabels() != null && configMap.getMetadata().getLabels().containsKey(labelKey)) {
            return configMap.getMetadata().getLabels().get(labelKey).equals(labelValue);
        }
        return false;
    }
}
