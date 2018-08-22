package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Parses the definition YAML file to create a processor
 * @author hhiden
 */
public final class ProcessorTemplateYAMLReader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private ProcessorTemplateYAMLReader() {
    }
    
    public static ProcessorNodeTemplate readTemplate(final File yamlFile) throws Exception {
        try(InputStream inStream = new FileInputStream(yamlFile)){
            return MAPPER.readValue(inStream, ProcessorNodeTemplate.class);
        }        
    }

    public static ProcessorNodeTemplate readTemplate(final String yaml) throws Exception {
        return MAPPER.readValue(yaml, ProcessorNodeTemplate.class);
    }

    public static ProcessorNodeTemplate readTemplate(final InputStream src) throws Exception {
        return MAPPER.readValue(src, ProcessorNodeTemplate.class);
    }
}