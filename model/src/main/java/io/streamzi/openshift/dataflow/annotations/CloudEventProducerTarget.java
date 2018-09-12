package io.streamzi.openshift.dataflow.annotations;


import io.streamzi.cloudevents.CloudEvent;

/**
 * This interface defines an object that can receive cloud events
 * @author hhiden
 */
public interface CloudEventProducerTarget {

    void send(CloudEvent event);

    void send(String key, CloudEvent event);

}
