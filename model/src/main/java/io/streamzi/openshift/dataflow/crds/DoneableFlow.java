package io.streamzi.openshift.dataflow.crds;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableFlow extends CustomResourceDoneable<Flow> {

    public DoneableFlow(Flow resource, Function function) {
        super(resource, function);
    }
}
