package io.streamzi.openshift.dataflow.crds;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableProcessor extends CustomResourceDoneable<Processor> {

    public DoneableProcessor(Processor resource, Function function) {
        super(resource, function);
    }
}
