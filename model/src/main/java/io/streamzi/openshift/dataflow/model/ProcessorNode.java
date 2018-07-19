package io.streamzi.openshift.dataflow.model;

import io.streamzi.openshift.dataflow.model.serialization.SerializedNode;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a node that processes CloudEvents. It contains
 * details of input and output connections and the code to instantiate
 * when being deployed
 * @author hhiden
 */
public class ProcessorNode extends ProcessorObject {
    /** Input Ports */
    private Map<String, ProcessorInputPort> inputs = new HashMap<>();
    
    /** Output Ports */
    private Map<String, ProcessorOutputPort> outputs = new HashMap<>();
    
    /** Image name */
    private String imageName = "oc-stream-container";
    
    /** Unique ID of the node */
    private String uuid;

    /** Runtime settings */
    private Map<String, String> settings = new HashMap<>();
    
    /** Parent flow */
    private ProcessorFlow parent;
    
    public ProcessorNode() {
    }

    public ProcessorFlow getParent() {
        return parent;
    }

    public void setParent(ProcessorFlow parent) {
        this.parent = parent;
    }
       
    public Map<String, ProcessorInputPort> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, ProcessorInputPort> inputs) {
        this.inputs = inputs;
    }

    public Map<String, ProcessorOutputPort> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, ProcessorOutputPort> outputs) {
        this.outputs = outputs;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    public void addInput(ProcessorInputPort input){
        input.setParent(this);
        inputs.put(input.getName(), input);
    }
    
    public void addOutput(ProcessorOutputPort output){
        output.setParent(this);
        outputs.put(output.getName(), output);
    }
    
    public ProcessorOutputPort getOutput(String name){
        return outputs.get(name);
    }
    
    public ProcessorInputPort getInput(String name){
        return inputs.get(name);
    } 

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}