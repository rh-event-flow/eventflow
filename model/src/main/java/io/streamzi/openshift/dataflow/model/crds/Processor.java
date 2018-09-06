package io.streamzi.openshift.dataflow.model.crds;

import io.fabric8.kubernetes.client.CustomResource;
import io.streamzi.openshift.dataflow.model.serialization.SerializedTemplate;

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
