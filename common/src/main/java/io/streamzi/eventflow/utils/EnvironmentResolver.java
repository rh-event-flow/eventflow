package io.streamzi.eventflow.utils;

/**
 * Simple class to look for an environment variable and then System.properties
 * if it doesn't exist
 */
public final class EnvironmentResolver {

    private EnvironmentResolver() {
        // no-op
    }

    public static String get(final String key) {
        String resolved = resolve(key);
        if (resolved != null) {
            return resolved;
        }

        resolved = resolve(key.toUpperCase());
        if (resolved != null) {
            return resolved;
        }

        resolved = resolve(key.replace(".", "_"));
        if (resolved != null) {
            return resolved;
        }

        resolved = resolve(key.replace(".", "_").replace("-", "_"));
        if (resolved != null) {
            return resolved;
        }

        resolved = resolve(key
                .replace(".", "_")
                .replace("-", "_")
                .toUpperCase());

        return resolved;
    }

    public static boolean exists(final String key) {
        String resolved = resolve(key);
        if (resolved != null) {
            return true;
        }

        resolved = resolve(key.toUpperCase());
        if (resolved != null) {
            return true;
        }

        resolved = resolve(key.replace(".", "_"));
        if (resolved != null) {
            return true;
        }

        resolved = resolve(key.replace(".", "_").replace("-", "_"));
        if (resolved != null) {
            return true;
        }

        resolved = resolve(key
                .replace(".", "_")
                .replace("-", "_")
                .toUpperCase());

        if (resolved != null) {
            return true;
        } else {
            return false;
        }
    }

    private static String resolve(final String key) {

        final String value = System.getenv(key);

        if (value != null) {
            return value;
        } else {
            return System.getProperty(key);
        }

    }
}
