package io.streamzi.openshift.dataflow.annotations;

import io.cloudevents.CloudEvent;

/**
 * This interface defines an object that can receive cloud events
 * @author hhiden
 */
public interface CloudEventProducerTarget {
    public void send(CloudEvent event);
}
