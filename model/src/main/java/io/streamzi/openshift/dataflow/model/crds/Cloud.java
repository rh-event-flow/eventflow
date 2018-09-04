package io.streamzi.openshift.dataflow.model.crds;

import io.fabric8.kubernetes.client.CustomResource;

public class Cloud extends CustomResource {

    private CloudSpec spec;

    @Override
    public String toString() {
        return "Processor{" +
                "apiVersion='" + getApiVersion() + '\'' +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                '}';
    }

    public CloudSpec getSpec() {
        return spec;
    }

    public void setSpec(CloudSpec spec) {
        this.spec = spec;
    }
}
