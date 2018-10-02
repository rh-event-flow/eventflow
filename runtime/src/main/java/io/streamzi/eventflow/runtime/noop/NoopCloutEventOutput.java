package io.streamzi.eventflow.runtime.noop;

import io.streamzi.eventflow.runtime.CloudEventOutput;

public class NoopCloutEventOutput extends CloudEventOutput {

    public NoopCloutEventOutput() {
        super(null, null);

    }
    @Override
    public void stopOutput() {

    }

    @Override
    public void startOutput() {

    }

    @Override
    public void send(Object event) {

    }

    @Override
    public void send(String key, Object event) {

    }
}
