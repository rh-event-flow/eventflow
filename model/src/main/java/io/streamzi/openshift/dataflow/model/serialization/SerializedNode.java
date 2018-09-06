package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import io.streamzi.openshift.dataflow.model.ProcessorInputPort;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import io.streamzi.openshift.dataflow.model.ProcessorOutputPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serialized form of node
 *
 * @author hhiden
 */
public class SerializedNode {
    @JsonIgnore
    private ProcessorNode node;

    private String uuid;
    private String displayName;
    private String templateName;
    private String templateId;
    private String transport;
    private String processorType;
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs = new ArrayList<>();
    private Map<String, Integer> targetClouds = new HashMap<>();
    
    
    private String imageName;
    private Map<String, String> settings = new HashMap<>();

    public SerializedNode() {
    }

    public SerializedNode(ProcessorNode node) {
        this.node = node;
        displayName = node.getDisplayName();
        uuid = node.getUuid();
        templateId = node.getTemplateId();
        templateName = node.getTemplateName();
        transport = node.getTransport();
        processorType = node.getProcessorType().toString();

        this.imageName = node.getImageName();
        for (String key : node.getSettings().keySet()) {
            settings.put(key, node.getSettings().get(key));
        }

        for (ProcessorOutputPort output : node.getOutputs().values()) {
            this.outputs.add(output.getName());
        }

        for (ProcessorInputPort input : node.getInputs().values()) {
            this.inputs.add(input.getName());
        }
        
        for(String id: node.getTargetClouds().keySet()){
            this.targetClouds.put(id, node.getTargetClouds().get(id));
        }
    }

    public ProcessorNode createNode() {
        ProcessorNode node = new ProcessorNode();

        node.setUuid(uuid);
        node.setImageName(imageName);
        node.setSettings(settings);
        node.setTemplateId(templateId);
        node.setTemplateName(templateName);
        node.setTransport(transport);
        node.setProcessorType(ProcessorConstants.ProcessorType.valueOf(processorType));
        node.setDisplayName(displayName);
        for (String input : inputs) {
            node.addInput(new ProcessorInputPort(input));
        }
        for (String output : outputs) {
            node.addOutput(new ProcessorOutputPort(output));
        }
        for (String id : targetClouds.keySet()){
            node.getTargetClouds().put(id, targetClouds.get(id));
        }

        return node;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Integer> getTargetClouds() {
        return targetClouds;
    }

    public void setTargetClouds(Map<String, Integer> targetClouds) {
        this.targetClouds = targetClouds;
    }

    @Override
    public String toString() {
        return "SerializedNode{" +
                "node=" + node +
                ", displayName='" + displayName + '\'' +
                ", uuid='" + uuid + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateId='" + templateId + '\'' +
                ", transport='" + transport + '\'' +
                ", processorType='" + processorType + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", imageName='" + imageName + '\'' +
                ", settings=" + settings +
                '}';
    }
}
