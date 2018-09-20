package io.streamzi.eventflow;

public class FlowUtil {

    public static String sanitiseEnvVar(String source) {
        return source
                .replace("-", "_")
                .replace(".", "_")
                .toUpperCase();
    }

    public static String sanitisePodName(String source) {
        return source
                .replace(" ", "-")
                .replace("/", "-")
                .toLowerCase();
    }


}
