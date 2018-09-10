/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamzi.openshift.dataflow.tests;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.extensions.DeploymentListBuilder;
import io.streamzi.openshift.dataflow.deployment.FlowDeployment;
import io.streamzi.openshift.dataflow.deployment.FlowDeploymentListBuilder;
import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorInputPort;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import io.streamzi.openshift.dataflow.model.ProcessorOutputPort;
import io.streamzi.openshift.dataflow.serialization.SerializedFlow;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for deployments
 * @author hhiden
 */
public class DeploymentTest {
    private static final Logger logger = Logger.getLogger(ModelTest.class.getName());
    private ObjectMapper MAPPER = new ObjectMapper();
    
    @Test
    public void deploymentTest() throws Exception {
        
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("deployment-cr.json");
        try {

            SerializedFlow sf = MAPPER.readValue(is, SerializedFlow.class);
            ProcessorFlow flow = new ProcessorFlow(sf);
            List<FlowDeployment> deployments = new FlowDeploymentListBuilder(flow)
                    .detectClouds()
                    .build();
            for(FlowDeployment deployment : deployments){
                logger.info("Deoployment: " + deployment.getCloud());
                deployment.print();
                
                
            }
            
            
            System.out.println("Done");            
        } catch (Exception e){
            
        }
        
        

    }
    
}
