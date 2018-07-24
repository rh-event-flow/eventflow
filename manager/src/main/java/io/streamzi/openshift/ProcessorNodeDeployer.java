
package io.streamzi.openshift;


import com.openshift.restclient.IClient;
import io.streamzi.openshift.dataflow.model.ProcessorNode;

/**
 * Create a single deployment for a processor node
 * @author hhiden
 */
public class ProcessorNodeDeployer {
    private ProcessorNode node;
    private IClient client;

    public ProcessorNodeDeployer(ProcessorNode node, IClient client) {
        this.node = node;
        this.client = client;
    }
    
    
}
