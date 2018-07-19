package io.streamzi.openshift.dataflow.container;

import io.streamzi.openshift.dataflow.annotations.CloudEventProducerTarget;
import io.streamzi.openshift.dataflow.container.config.EnvironmentResolver;
import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Variable that gets injected into a class to receive CloudEvents
 * @author hhiden
 */
public abstract class CloudEventOutput implements CloudEventProducerTarget {
    private static final Logger logger = Logger.getLogger(CloudEventInput.class.getName());
    
    protected Object producerObject;
    protected String outputName;
    protected String processorUuid;

    public CloudEventOutput(Object producerObject, String outputName) {
        this.producerObject = producerObject;
        this.outputName = outputName;
        processorUuid = EnvironmentResolver.get(ProcessorConstants.NODE_UUID);
    }
    
    
    public abstract void stopOutput();
    public abstract void startOutput();
}
