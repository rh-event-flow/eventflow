package io.streamzi.openshift.operations;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import io.streamzi.openshift.dataflow.annotations.CloudEventComponent;
import io.streamzi.openshift.dataflow.annotations.CloudEventComponentTimer;
import io.streamzi.openshift.dataflow.annotations.CloudEventProducer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import io.streamzi.openshift.dataflow.annotations.CloudEventProducerTarget;

/**
 * Provides a random feed of data
 * @author hhiden
 */
@CloudEventComponent
public class RandomDataSource {
    
    @CloudEventProducer(name = "OUTPUT_DATA")
    private CloudEventProducerTarget output;
       
    @CloudEventComponentTimer(interval = 1000)
    public void send(){
        final String eventId = UUID.randomUUID().toString();
        final URI src = URI.create("/trigger");
        final String eventType = "My.Cloud.Event.Type";

        final Map<String, String> contents = new HashMap<>();
        contents.put("value", Double.toString(Math.random()));

        final CloudEvent<Map<String, String>> simpleKeyValueEvent = new CloudEventBuilder()
                .eventType(eventType)
                .eventID(eventId)
                .source(src)
                .data(contents)
                .build();
        System.out.println("x");
        output.send(simpleKeyValueEvent);
    }
}