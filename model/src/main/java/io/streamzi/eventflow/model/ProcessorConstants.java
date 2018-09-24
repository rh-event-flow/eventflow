package io.streamzi.eventflow.model;

/**
 * Various constants
 *
 * @author hhiden
 */
public interface ProcessorConstants {
    enum ProcessorType {
        DEPLOYABLE_IMAGE,
        TOPIC_ENDPOINT
    }

    String KAFKA_BOOTSTRAP_SERVERS = "STREAMZI_KAFKA_BOOTSTRAP_SERVER";
    String NODE_UUID = "STREAMZI_NODE_UUID";
}
