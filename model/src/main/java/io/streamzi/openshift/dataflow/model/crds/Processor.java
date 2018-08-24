package io.streamzi.openshift.dataflow.model.crds;

import io.fabric8.kubernetes.client.CustomResource;

public class Processor extends CustomResource {

    private ProcessorSpec spec;

    @Override
    public String toString() {
        return "Processor{" +
                "apiVersion='" + getApiVersion() + '\'' +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                '}';
    }

    public ProcessorSpec getSpec() {
        return spec;
    }

    public void setSpec(ProcessorSpec spec) {
        this.spec = spec;
    }
}
