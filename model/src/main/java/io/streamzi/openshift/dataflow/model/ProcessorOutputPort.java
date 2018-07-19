package io.streamzi.openshift.dataflow.model;

/**
 * An output
 * @author hhiden
 */
public class ProcessorOutputPort extends ProcessorPort {

    public ProcessorOutputPort(String name) {
        super(name);
    }

    public ProcessorOutputPort() {
        super();
    }

    @Override
    public void addLink(ProcessorLink link) {
        links.add(link);
    }
    
}