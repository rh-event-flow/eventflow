package io.streamzi.openshift.dataflow.container.config;

/**
 * Simple class to look for an environment variable and then System.properties
 * if it doesn't exist
 *
 * @author hhiden
 */
public class EnvironmentResolver {

    public static String get(String key) {
        String resolved = resolve(key);
        if (resolved != null) {
            return resolved;
        }

        resolved = resolve(key.toUpperCase());
        if (resolved != null) {
            return resolved;
        }

        resolved = resolve(key.replace(".", "_"));
        if(resolved != null){
            return resolved;
        }

        resolved = resolve(key.replace(".", "_").replace("-", "_"));
        if(resolved != null){
            return resolved;
        }

        resolved = resolve(key
                .replace(".", "_")
                .replace("-", "_")
                .toUpperCase());

        return resolved;
    }

    private static String resolve(String key) {

        String value = System.getenv(key);

        if (value != null) {
            return value;
        } else {
            return System.getProperty(key);
        }

    }
    
    public static boolean exists(String key){
        String resolved = resolve(key);
        if (resolved != null) {
            return true;
        }

        resolved = resolve(key.toUpperCase());
        if (resolved != null) {
            return true;
        }

        resolved = resolve(key.replace(".", "_"));
        if(resolved != null){
            return true;
        }

        resolved = resolve(key.replace(".", "_").replace("-", "_"));
        if(resolved != null){
            return true;
        }

        resolved = resolve(key
                .replace(".", "_")
                .replace("-", "_")
                .toUpperCase());

        if(resolved!=null){
            return true;
        } else {
            return false;
        }
        
    }
}
