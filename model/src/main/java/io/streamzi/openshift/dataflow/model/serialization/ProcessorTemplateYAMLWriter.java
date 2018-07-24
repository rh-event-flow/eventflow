package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Logger;

/**
 * Writes a serialized processor node to YAML
 * @author hhiden
 */
public class ProcessorTemplateYAMLWriter {
    private static final Logger logger = Logger.getLogger(ProcessorTemplateYAMLWriter.class.getName());
    
    private ProcessorNodeTemplate template;

    public ProcessorTemplateYAMLWriter() {
    }

    public ProcessorTemplateYAMLWriter(ProcessorNodeTemplate template) {
        this.template = template;
    }

    public String writeToYAMLString() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.writeValueAsString(template);
    }
    
    public void writeToFile(File parentDirectory) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try(FileOutputStream outStream = new FileOutputStream(new File(parentDirectory, template.getId() + ".yml"))){
            String yml = mapper.writeValueAsString(template);
            logger.info(yml);
            outStream.write(yml.getBytes());
        }
        logger.info("Template written to: " + new File(parentDirectory, template.getId() + ".yml"));
    }
}
