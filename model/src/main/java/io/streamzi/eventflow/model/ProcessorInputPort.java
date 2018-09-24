package io.streamzi.eventflow.model;

/**
 * An input port
 *
 * @author hhiden
 */
public class ProcessorInputPort extends ProcessorPort {

    public ProcessorInputPort(String name) {
        super(name);
    }

    public ProcessorInputPort() {
        super();
    }

    @Override
    public void addLink(ProcessorLink link) {
        if (links.isEmpty()) {
            links.add(link);
        }
    }

}