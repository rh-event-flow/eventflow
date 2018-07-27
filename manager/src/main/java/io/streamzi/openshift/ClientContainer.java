package io.streamzi.openshift;


import io.fabric8.openshift.client.OpenShiftClient;

import javax.ejb.Local;
import java.io.File;

/**
 * Methods for accessing the openshift client, looking up storage dirs etc.
 *
 * @author hhiden
 */
@Local
public interface ClientContainer {
    public String getNamespace();

    public File getStorageDir();

    public File getTemplateDir();

    public File getFlowsDir();

    public OpenShiftClient getOSClient();
}
