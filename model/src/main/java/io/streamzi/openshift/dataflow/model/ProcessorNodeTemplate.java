package io.streamzi.openshift.dataflow.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple template for a node that doesn't hold any links, status etc
 * @author hhiden
 */
public class ProcessorNodeTemplate {
    private String id = "processor";
    private String name = "Unnamed Processor";
    private String description = "A processor node";
    private List<String> inputs = new ArrayList<>();
    private List<String> outputs = new ArrayList<>();
    private String mainClassName = "io.streamzi.openshift.container.ProcessorRunner";
    private String imageName = "oc-stream-container";
    private Map<String, String> settings = new HashMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
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


    public void addInput(String name) {
        this.inputs.add(name);
    }

    public void addOutput(String name){
        this.outputs.add(name);
    }
    
    public ProcessorNode createProcessorNode(){
        ProcessorNode node = new ProcessorNode();
        
        node.setImageName(imageName);
        node.setSettings(settings);
        if(inputs!=null){
            for(String input : inputs){
                node.addInput(new ProcessorInputPort(input));
            }
        }
        
        if(outputs!=null){
            for(String output : outputs){
                node.addOutput(new ProcessorOutputPort(output));
            }
        }
        return node;
    }
}
