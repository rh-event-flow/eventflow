package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.serialization.SerializedFlow;
import java.io.File;
import java.io.FileOutputStream;


/**
 * Stores a flow to a JSON object
 * @author hhiden
 */
public class ProcessorFlowWriter {
    private ProcessorFlow flow;
    

    public ProcessorFlowWriter(ProcessorFlow flow) {
        this.flow = flow;
    }
    
    public String writeToJsonString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializedFlow sf = new SerializedFlow(flow);
        return mapper.writeValueAsString(sf);
    }

    public String writeToIndentedJsonString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT,  true);
        SerializedFlow sf = new SerializedFlow(flow);
        return mapper.writeValueAsString(sf);
    }
    
    public void writeToFile(File outputFile) throws Exception {
        try(FileOutputStream outStream = new FileOutputStream(outputFile)){
            outStream.write(writeToJsonString().getBytes());
        }
    }
}
