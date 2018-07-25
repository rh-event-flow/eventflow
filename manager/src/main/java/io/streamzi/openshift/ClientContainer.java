package io.streamzi.openshift;


import com.openshift.restclient.IClient;
import io.fabric8.openshift.client.OpenShiftClient;

import java.io.File;
import javax.ejb.Local;

/**
 * Methods for accessing the openshift client, looking up storage dirs etc.
 * @author hhiden
 */
@Local
public interface ClientContainer {
    public String getNamespace();
    public IClient getClient();
    public File getStorageDir();
    public File getTemplateDir();
    public File getFlowsDir();
    public OpenShiftClient getOSClient();
}
