package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Parses the definition YAML file to create a processor
 * @author hhiden
 */
public class ProcessorTemplateYAMLReader {
    private File yamlFile;

    public ProcessorTemplateYAMLReader(File yamlFile) {
        this.yamlFile = yamlFile;
    }
    
    public ProcessorNodeTemplate readTemplate() throws Exception {
        try(InputStream inStream = new FileInputStream(yamlFile)){
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(inStream, ProcessorNodeTemplate.class);
        }        
    }
    
    public static ProcessorNodeTemplate readTemplateFromString(String yaml) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yaml, ProcessorNodeTemplate.class);
    }
}