# EventFlow
Simple EventFlow implementation based on CloudEvents, over Kafka and other protocols, running on Openshift!

## Installation

Use the [OCP Broker](https://github.com/rh-event-flow/ocp-broker) to deploy Strimzi and then the EventFlow component.


### Artifacts

The EventFlow component consists of a few different artifacts:

* `common`: Commonly used classes and generic utilities, shared across different modules.
* `model`: An abstract representation of an EventFlow and its associated `Processor`s, their links etc.
* `sdk`: APIs and Annotations for developers to implement `Processor` implementations.
* `runtime`: Simple JavaSE runtime, responsible for instantiating the `Processor` objects and perform the wiring to the underlying transport protocol.
* `operator`: Java implementation of the [Kubernetes Operator Pattern](https://coreos.com/blog/introducing-operators.html), watching `Flow` and `Processor` custom resources
* `ui`: very simple editor for creating an EvenFlow based on available (Kafka) `topics` and `Processor` objects.


## Stream Processors

A number of stream processors are available [here](https://github.com/rh-event-flow/eventflow-processor-samples)!

## Maven Archetype

We have a few [Maven archetypes](https://github.com/rh-event-flow/processor-archetypes) for getting quickly up to speed with our SDK:

* `source`: Implements a sample for a data source, sending data to the EventFlow
* `sink`: Implements a sample for a data sink, receiving data from the EventFlow
* `processor`: Combines a `source` and a `sink`.
