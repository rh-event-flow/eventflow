/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.streamzi.openshift;


import com.openshift.restclient.IClient;
import java.io.File;
import javax.ejb.Local;

/**
 *
 * @author hhiden
 */
@Local
public interface ClientContainer {
    public IClient getClient();
    public File getStorageDir();
    public File getTemplateDir();
    public File getFlowsDir();
}
