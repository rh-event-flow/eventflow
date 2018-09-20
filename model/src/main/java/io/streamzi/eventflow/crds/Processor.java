package io.streamzi.eventflow.crds;

import io.fabric8.kubernetes.client.CustomResource;
import io.streamzi.eventflow.serialization.SerializedTemplate;

public class Processor extends CustomResource {

    private SerializedTemplate spec;

    @Override
    public String toString() {
        return "Processor{" +
                "apiVersion='" + getApiVersion() + '\'' +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                '}';
    }

    public SerializedTemplate getSpec() {
        return spec;
    }

    public void setSpec(SerializedTemplate spec) {
        this.spec = spec;
    }
}
