package io.streamzi.openshift.dataflow.crds;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableCloud extends CustomResourceDoneable<Cloud> {

    public DoneableCloud(Cloud resource, Function function) {
        super(resource, function);
    }
}
