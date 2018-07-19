package io.streamzi.openshift.dataflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contains a connected graph of processor nodes
 * @author hhiden
 */
public class ProcessorFlow implements Serializable {
    private List<ProcessorNode> nodes = new ArrayList<>();
    private String name;
    private Map<String, String> settings = new HashMap<>();
    private Map<String, String> globalSettings = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getGlobalSettings() {
        return globalSettings;
    }

    public void setGlobalSettings(Map<String, String> globalSettings) {
        this.globalSettings = globalSettings;
    }
    
    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
    
    public List<ProcessorNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ProcessorNode> nodes) {
        this.nodes = nodes;
    }
    
    public void addProcessorNode(ProcessorNode node){
        if(node.getUuid()==null || node.getUuid().isEmpty()){
            node.setUuid(UUID.randomUUID().toString());
        }
        node.setParent(this);
        nodes.add(node);
    }
    
    public void linkNodes(String sourceUuid, String sourcePortName, String targetUuid, String targetPortName){
        linkNodes(getNodeByUuid(sourceUuid), sourcePortName, getNodeByUuid(targetUuid), targetPortName);
    }
    
    public void linkNodes(ProcessorNode source, String sourcePortName, ProcessorNode target, String targetPortName){
        ProcessorOutputPort sourcePort = source.getOutput(sourcePortName);
        ProcessorInputPort targetPort = target.getInput(targetPortName);
        if(sourcePort!=null && targetPort!=null){
            ProcessorLink link = new ProcessorLink();
            link.setSource(sourcePort);
            link.setTarget(targetPort);
            sourcePort.addLink(link);
            targetPort.addLink(link);
        }
    }
    
    /** Returns the links so that nodes can be reconnected */
    public List<ProcessorLink> getLinks(){
        List<ProcessorLink> links = new ArrayList<>();
        for(ProcessorNode n : nodes){
            // Add the output links for each node
            for(ProcessorOutputPort output : n.getOutputs().values()){
                for(ProcessorLink link : output.getLinks()){
                    links.add(link);
                }
            }
        }
        return links;
    }
    
    public ProcessorNode getNodeByUuid(String uuid){
        for(ProcessorNode n : nodes){
            if(n.getUuid().equals(uuid)){
                return n;
            }
        }
        return null;
    }
}
