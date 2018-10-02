package io.streamzi.eventflow.runtime;

import io.streamzi.eventflow.annotations.CloudEventComponentTimer;
import io.streamzi.eventflow.annotations.CloudEventConsumer;
import io.streamzi.eventflow.annotations.CloudEventProducer;
import io.streamzi.eventflow.model.ProcessorConstants;
import io.streamzi.eventflow.runtime.kafka.KafkaCloudEventInputImpl;
import io.streamzi.eventflow.runtime.kafka.KafkaCloudEventOutputImpl;
import io.streamzi.eventflow.runtime.noop.NoopCloutEventOutput;
import io.streamzi.eventflow.utils.EnvironmentResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds a single processor component, checks for annotations etc
 *
 * @author hhiden
 */
public class ProcessorContainer {
    private static final Logger logger = Logger.getLogger(ProcessorContainer.class.getName());

    private List<CloudEventOutput> outputs = new ArrayList<>();
    private List<CloudEventInput> inputs = new ArrayList<>();
    private List<CloudEventTimer> timers = new ArrayList<>();


    private Class processorClass;
    private Object processorObject;

    public ProcessorContainer(Class processorClass) {
        this.processorClass = processorClass;
        setupProcessor();

    }

    /**
     * Find all of the annotations
     */
    private void setupProcessor() {
        try {
            processorObject = processorClass.newInstance();
            logger.info("Created processor: " + processorObject.getClass().getName());

            // Output fields
            Field[] fields = processorClass.getDeclaredFields();
            CloudEventProducer producerAnnotation;
            for (Field f : fields) {
                if (f.getAnnotation(CloudEventProducer.class) != null) {
                    producerAnnotation = f.getAnnotation(CloudEventProducer.class);

                    logger.info("Found producer field: " + f.getName());

                    // Create an output implementation and set it
                    f.setAccessible(true);

                    final String outputName = producerAnnotation.name();
                    CloudEventOutput cloudEventOutput = null;
                    if (EnvironmentResolver.exists(outputName + "_BOOTSTRAP_SERVERS")) {
                        // There is a bootstrap server environment variable
                        logger.info("Bootstrap server Env exists for input: " + outputName);

                        cloudEventOutput = new KafkaCloudEventOutputImpl(processorObject, producerAnnotation.name());
                    } else {
                        // Use the default / old version
                        logger.warning("Using NoopOutput impl., since there is no Bootstrap server Env exists for input: " + outputName);
                        cloudEventOutput = new NoopCloutEventOutput();
                    }

                    f.set(processorObject, cloudEventOutput);
                    outputs.add(cloudEventOutput);
                }
            }

            // Any methods that need to connect to an input stream or are timed
            Method[] methods = processorClass.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getAnnotation(CloudEventConsumer.class) != null) {
                    logger.info("Found consumer method: " + m.getName());

                    // Create a consumer implementation and set it up
                    KafkaCloudEventInputImpl input = new KafkaCloudEventInputImpl(processorObject, m);
                    inputs.add(input);
                }

                if (m.getAnnotation(CloudEventComponentTimer.class) != null) {
                    logger.info("Found timer method: " + m.getName());
                    CloudEventTimer timer = new CloudEventTimer(processorObject, m);
                    timers.add(timer);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error setting up processor: " + e.getMessage(), e);
        }

    }

    public void startProcessor() {
        for (CloudEventOutput output : outputs) {
            output.startOutput();
        }

        for (CloudEventInput input : inputs) {
            input.startInput();
        }

        for (CloudEventTimer timer : timers) {
            timer.startTimer();
        }
    }

    public void stopProcessor() {

    }
}