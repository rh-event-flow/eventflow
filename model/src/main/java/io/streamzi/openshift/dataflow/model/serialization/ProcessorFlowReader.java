package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;

/**
 * Loads a flow from a JSON file
 * @author hhiden
 */
public class ProcessorFlowReader {

    public ProcessorFlow readFromJsonString(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SerializedFlow flow = mapper.readValue(json, SerializedFlow.class);

        return new ProcessorFlow(flow);
    }
}