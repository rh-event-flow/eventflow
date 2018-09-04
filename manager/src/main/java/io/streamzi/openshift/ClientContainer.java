package io.streamzi.openshift;


import io.fabric8.openshift.client.OpenShiftClient;

import javax.ejb.Local;
import java.util.Set;

/**
 * Methods for accessing the openshift client, looking up storage dirs etc.
 *
 * @author hhiden
 */
@Local
public interface ClientContainer {
    String getNamespace();

    OpenShiftClient getOSClient();

    Set<String> getOSClientNames();

    OpenShiftClient getOsClient(String name);
}
