package io.streamzi.openshift.dataflow.crds;

import io.fabric8.kubernetes.client.CustomResource;
import io.streamzi.openshift.dataflow.serialization.SerializedCloud;

public class Cloud extends CustomResource {

    private SerializedCloud spec;

    @Override
    public String toString() {
        return "Processor{" +
                "apiVersion='" + getApiVersion() + '\'' +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                '}';
    }

    public SerializedCloud getSpec() {
        return spec;
    }

    public void setSpec(SerializedCloud spec) {
        this.spec = spec;
    }
}
