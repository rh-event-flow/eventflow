/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.streamzi.openshift;

import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.images.DockerImageURI;
import com.openshift.restclient.model.IContainer;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IResource;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLReader;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author hhiden
 */
@Stateless(name = "ProcessorFlowManagerBean", mappedName = "ProcessorFlowManagerBean")
public class ProcessorFlowManagerBean implements ProcessorFlowManager {

    @EJB(beanInterface = ClientContainer.class)
    private ClientContainer container;

    @Override
    public String echo(String message) {
        return message;
    }

    @Override
    public List<String> listProcessors() {
        List<String> results = new ArrayList<>();
        File dir = container.getTemplateDir();
        File[] yamls = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });

        for (File f : yamls) {
            results.add(f.getName());
        }
        return results;
    }

    @Override
    public String deployProcessor(String filename) throws Exception {
        File yamlFile = new File(container.getTemplateDir(), filename);
        if (yamlFile.exists()) {
            ProcessorTemplateYAMLReader reader = new ProcessorTemplateYAMLReader(yamlFile);
            ProcessorNodeTemplate nodeTemplate = reader.readTemplate();

            String imageName = nodeTemplate.getImageName();
            IResource template = container.getClient().get(ResourceKind.IMAGE_STREAM, imageName, "hardcoded-test");
            if (template != null) {
                IDeploymentConfig config = container.getClient().getResourceFactory().stub(ResourceKind.DEPLOYMENT_CONFIG, filename, "hardcoded-test");

                config.setReplicas(1);
                config.addLabel("app", filename);
                config.addLabel("streamzi.flow.uuid", UUID.randomUUID().toString());
                config.addLabel("streamzi.deployment.uuid", UUID.randomUUID().toString());
                config.addLabel("streamzi.type", "processor-flow");
                config.addTemplateLabel("app", filename);

                IContainer c1 = config.addContainer("streamzi-processor-" + UUID.randomUUID().toString());
                c1.addEnvVar("processor-uuid", UUID.randomUUID().toString());
                c1.setImage(new DockerImageURI("172.30.1.1:5000/hardcoded-test/" + imageName + ":latest"));


                config = container.getClient().create(config);
                return config.toJson();
                
            } else {
                return "";
            }
        } else {
            return "";
        }

    }
}
