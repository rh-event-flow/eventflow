/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.streamzi.cloudeventflow.log.data;

import io.cloudevents.CloudEvent;
import io.streamzi.openshift.dataflow.annotations.CloudEventComponent;
import io.streamzi.openshift.dataflow.annotations.CloudEventConsumer;

/**
 * @author hhiden
 */
@CloudEventComponent
public class DataLogger {

    @CloudEventConsumer(name = "INPUT_DATA")
    public void onCloudEvent(CloudEvent evt) {
        System.out.println(evt.getData().toString());
    }

}
