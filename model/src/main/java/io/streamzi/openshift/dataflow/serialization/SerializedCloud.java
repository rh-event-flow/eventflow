package io.streamzi.openshift.dataflow.serialization;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class SerializedCloud implements KubernetesResource {

    private String description;

    private String hostname;

    private int port;

    private String namespace;

    private String bootstrapServers;

    //todo: Should this be a secret?
    private String token;

    @Override
    public String toString() {
        return "SerializedCloud{" +
                "description='" + description + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", namespace='" + namespace + '\'' +
                ", bootstrapServers='" + bootstrapServers + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
}
