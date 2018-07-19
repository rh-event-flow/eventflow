package io.streamzi.openshift.dataflow.container;

import io.streamzi.openshift.dataflow.annotations.CloudEventConsumer;
import io.streamzi.openshift.dataflow.container.config.EnvironmentResolver;
import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Defines a class that can supply cloud events to a consumer method
 * @author hhiden
 */
public abstract class CloudEventInput {
    private static final Logger logger = Logger.getLogger(CloudEventInput.class.getName());
    
    protected Object consumerObject;
    protected Method consumerMethod;
    protected String inputName;
    protected String processorUuid;
    
    public CloudEventInput(Object consumerObject, Method consumerMethod) {
        this.consumerObject = consumerObject;
        this.consumerMethod = consumerMethod;
        
        processorUuid = EnvironmentResolver.get(ProcessorConstants.NODE_UUID);
        
        // What is the name
        CloudEventConsumer consumerAnnotation = consumerMethod.getAnnotation(CloudEventConsumer.class);
        if(consumerAnnotation!=null){
            inputName = consumerAnnotation.name();
            logger.info("CloudEventInput configured for input name: " + inputName);
        }
    }
    
    public abstract void startInput();
    public abstract void stopInput();
}