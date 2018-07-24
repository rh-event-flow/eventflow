package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.serialization.SerializedFlow;
import io.streamzi.openshift.dataflow.model.serialization.SerializedNode;

/**
 * Loads a flow from a JSON file
 * @author hhiden
 */
public class ProcessorFlowReader {
    public ProcessorFlow readFromJsonString(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SerializedFlow flow = mapper.readValue(json, SerializedFlow.class);
        ProcessorFlow result = new ProcessorFlow();
        result.setName(flow.getName());
        // Add the nodes
        for(SerializedNode node : flow.getNodes()){
            result.addProcessorNode(node.createNode());
        }
        
        // Connect them together
        for(SerializedLink link : flow.getLinks()){
            result.linkNodes(link.getSourceUuid(), link.getSourcePortName(), link.getTargetUuid(), link.getTargetPortName());
        }
        
        for(String key : flow.getSettings().keySet()){
            result.getSettings().put(key, flow.getSettings().get(key));
        }
        
        for(String key : flow.getGlobalSettings().keySet()){
            result.getGlobalSettings().put(key, flow.getGlobalSettings().get(key));
        }
        return result;    
    }
}