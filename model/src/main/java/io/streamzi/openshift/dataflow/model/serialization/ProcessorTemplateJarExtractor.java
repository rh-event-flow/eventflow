package io.streamzi.openshift.dataflow.model.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class finds, extracts and parses the streamzi.yaml file from a packaged jar
 * to build a processor node template that can be used to create dataflows.
 * @author hhiden
 */
public class ProcessorTemplateJarExtractor {
    private String templateYamlFileName = "streamzi.yml";
    
    private JarFile jar;

    public ProcessorTemplateJarExtractor(JarFile jar) {
        this.jar = jar;
    }

    public String getTemplateYamlFileName() {
        return templateYamlFileName;
    }

    public void setTemplateYamlFileName(String templateYamlFileName) {
        this.templateYamlFileName = templateYamlFileName;
    }
    
    public ProcessorNodeTemplate getProcessorNodeTempate() throws Exception {
        JarEntry entry = jar.getJarEntry(templateYamlFileName);
        try(InputStream inStream = jar.getInputStream(entry)){
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(inStream, ProcessorNodeTemplate.class);
        }
    }
}
