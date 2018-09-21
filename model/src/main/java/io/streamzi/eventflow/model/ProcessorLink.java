package io.streamzi.eventflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a link between two processor nodes
 * @author hhiden
 */
public class ProcessorLink extends ProcessorObject {

    private ProcessorOutputPort source;
    
    private ProcessorInputPort target;

    public void setSource(ProcessorOutputPort source) {
        this.source = source;
    }

    public ProcessorOutputPort getSource() {
        return source;
    }

    public void setTarget(ProcessorInputPort target) {
        this.target = target;
    }

    @JsonIgnore
    public ProcessorInputPort getTarget() {
        return target;
    }

}