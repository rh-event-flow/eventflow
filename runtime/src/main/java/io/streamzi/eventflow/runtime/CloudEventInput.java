package io.streamzi.eventflow.runtime;

import io.streamzi.eventflow.annotations.CloudEventConsumer;
import io.streamzi.eventflow.annotations.ObjectType;
import io.streamzi.eventflow.model.ProcessorConstants;
import io.streamzi.eventflow.utils.EnvironmentResolver;

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
    protected ObjectType objectType = ObjectType.OBJECT;
    
    public CloudEventInput(Object consumerObject, Method consumerMethod) {
        this.consumerObject = consumerObject;
        this.consumerMethod = consumerMethod;
        
        processorUuid = EnvironmentResolver.get(ProcessorConstants.NODE_UUID);
        
        // What is the name
        CloudEventConsumer consumerAnnotation = consumerMethod.getAnnotation(CloudEventConsumer.class);
        if(consumerAnnotation!=null){
            inputName = consumerAnnotation.name();
            objectType = consumerAnnotation.type();
            logger.info("CloudEventInput configured for input name: " + inputName);
        }
    }
    
    public abstract void startInput();
    public abstract void stopInput();
}