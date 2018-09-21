# cloudevent-flow
Simple dataflow for CloudEvents

## Manager/Watcher Installation

### Automated

Use the [OCP Broker](https://github.com/project-streamzi/ocp-broker) to deploy Strimzi and then the CloudEvent flow

### Manual

1. Deploy [Strimzi](http://strimzi.io).

2. Deploy the EnvironmentVariable Operator as this project uses it to inject the configuration from ConfigMaps

3. If you have the [OCP Broker](https://github.com/project-streamzi/ocp-broker) installed the Environment Variable Operator will be available from the Service Catalog.
If not, follow the steps below.

```bash
$ git clone https://github.com/project-streamzi/EnvironmentVariableOperator.git
$ cd EnvironmentVariableOperator
$ oc login -u system:admin
$ oc adm policy add-cluster-role-to-user cluster-admin system:serviceaccount:myproject:default
$ oc login -u developer
$ mvn clean pacakge fabric8:deploy -Popenshift
```

Install the Manager and Watcher components by running `mvn clean package fabric8:deploy` in the `manager` and `watcher` directories.
The Manager contains the UI and API to support it - the API will create a ConfigMap containing the abstract flow in OpenShift. 
The Watcher will be notified of the presence of the flow `ConfigMap` and will deploy the necessary components (DeploymentConfigs and ConfigMaps).

The Manager can be run outside OpenShift using `mvn clean package thorntail:start`.
The Watcher can be run outside OpenShift using `mvn clean package; java -jar target/FlowController.jar`.

4. Register the CustomResourceDefinition for the Stream Processors

`oc create -f manager/src/main/resources/processor-crd.ymk`

## Stream Processors

A number of stream processors are available [here](https://github.com/project-streamzi/event-flow-operation-samples)!
