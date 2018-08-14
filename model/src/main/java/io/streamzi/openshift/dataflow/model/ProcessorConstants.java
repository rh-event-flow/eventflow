package io.streamzi.openshift.dataflow.model;

/**
 * Various constants
 * @author hhiden
 */
public interface ProcessorConstants {
    public enum ProcessorType {
        DEPLOYABLE_IMAGE,
        TOPIC_ENDPOINT
    }
    
    public static final String KAFKA_BOOTSTRAP_SERVERS = "STREAMZI_KAFKA_BOOTSTRAP_SERVER";
    public static final String NODE_UUID = "STREAMZI_NODE_UUID";
}
