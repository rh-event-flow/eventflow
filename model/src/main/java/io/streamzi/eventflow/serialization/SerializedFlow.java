package io.streamzi.eventflow.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.streamzi.eventflow.model.ProcessorFlow;
import io.streamzi.eventflow.model.ProcessorLink;
import io.streamzi.eventflow.model.ProcessorNode;
import io.streamzi.eventflow.model.ProcessorOutputPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serialized form of flow
 * @author hhiden
 */
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class SerializedFlow implements KubernetesResource {

    @JsonIgnore
    private ProcessorFlow flow;
    
    private String name;

    private List<SerializedNode> nodes = new ArrayList<>();

    private List<SerializedLink> links = new ArrayList<>();

    private Map<String, String> settings = new HashMap<>();

    public SerializedFlow() {
    }

    public SerializedFlow(ProcessorFlow flow) {
        this.flow = flow;
        this.name = flow.getName();
        for(ProcessorNode n : flow.getNodes()){
            nodes.add(new SerializedNode(n));
            
            for(ProcessorOutputPort output : n.getOutputs().values()){
                for(ProcessorLink link : output.getLinks()){
                    links.add(new SerializedLink(link));
                }
            }
        }
        
        for(String key : flow.getSettings().keySet()){
            settings.put(key, flow.getSettings().get(key));
        }
        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public List<SerializedNode> getNodes(){
        return nodes;
    }

    public List<SerializedLink> getLinks() {
        return links;
    }

    public void setLinks(List<SerializedLink> links) {
        this.links = links;
    }

    public void setNodes(List<SerializedNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "SerializedFlow{" +
                "flow=" + flow +
                ", name='" + name + '\'' +
                ", nodes=" + nodes +
                ", links=" + links +
                ", settings=" + settings +
                '}';
    }
}