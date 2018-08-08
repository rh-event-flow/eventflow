package io.streamzi.openshift.dataflow.container.config;

import java.util.Properties;

public class JMSResolver {

    private static final String NAMING_FACTORY_KEY = "java.naming.factory.initial";
    private static final String CONNECTION_FACTORY_KEY = "connectionfactory.factory";
    private static final String QUEUE_KEY = "queue.queue";

    public static Properties getJMSProperties(){
        Properties props = new Properties();
        props.put(NAMING_FACTORY_KEY, EnvironmentResolver.get(NAMING_FACTORY_KEY));
        props.put(CONNECTION_FACTORY_KEY, EnvironmentResolver.get(CONNECTION_FACTORY_KEY));
        props.put(QUEUE_KEY, EnvironmentResolver.get(QUEUE_KEY));

        props.list(System.out);

        return props;

    }
}
