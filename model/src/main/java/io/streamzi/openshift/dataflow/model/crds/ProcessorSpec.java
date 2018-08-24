package io.streamzi.openshift.dataflow.model.crds;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.List;
import java.util.Map;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class ProcessorSpec implements KubernetesResource {

    private String displayName;

    private String description;

    private String imageName;

    private List<String> inputs;

    private List<String> outputs;

    private Map<String, String> settings;

    @Override
    public String toString() {
        return "ProcessorSpec{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", imageName='" + imageName + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", settings=" + settings +
                '}';
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}
