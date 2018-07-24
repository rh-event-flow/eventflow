package io.streamzi.openshift.dataflow.container.config;

/**
 * Simple class to look for an environment variable and then System.properties
 * if it doesn't exist
 * @author hhiden
 */
public class EnvironmentResolver {
    public static String get(String key){
        String value = System.getenv(key);
        if(value!=null){ 
            return value;
        } else {
            return System.getProperty(key);
        }
    }
}
