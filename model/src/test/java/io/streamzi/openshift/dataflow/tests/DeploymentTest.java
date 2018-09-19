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
import io.streamzi.openshift.dataflow.deployment.TargetState;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.serialization.SerializedFlow;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for deployments
 *
 * @author hhiden
 */
public class DeploymentTest {
    private static final Logger logger = Logger.getLogger(ModelTest.class.getName());
    private ObjectMapper MAPPER = new ObjectMapper();


    @Test
    public void testConversionToDC() throws Exception {

        Map<String, String> bootstrapServerCache = new HashMap<>();
        bootstrapServerCache.put("local", "my-cluster-kafka-bootstrap:9092");
        bootstrapServerCache.put("azure", "my-cluster-kafka-bootstrap:9092");

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("deployment-cr.json");

        SerializedFlow sf = MAPPER.readValue(is, SerializedFlow.class);
        ProcessorFlow flow = new ProcessorFlow(sf);

        TargetState localTarget = new TargetState("local", flow, bootstrapServerCache);
        localTarget.build();

        assertThat(localTarget.getDeploymentConfigs().size()).isEqualTo(3);

        TargetState azureTarget = new TargetState("azure", flow, bootstrapServerCache);
        azureTarget.build();

        assertThat(azureTarget.getDeploymentConfigs().size()).isEqualTo(1);

        System.out.println("Done");

    }

    @Test
    public void testStickyCloud() throws Exception {

        Map<String, String> bootstrapServerCache = new HashMap<>();
        bootstrapServerCache.put("local", "my-cluster-kafka-bootstrap:9092");
        bootstrapServerCache.put("azure", "my-cluster-kafka-bootstrap:9092");

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("sticky-cr.json");

        SerializedFlow sf = MAPPER.readValue(is, SerializedFlow.class);
        ProcessorFlow flow = new ProcessorFlow(sf);

        TargetState localTarget = new TargetState("local", flow, bootstrapServerCache);
        localTarget.build();

        assertThat(localTarget.getDeploymentConfigs().size()).isEqualTo(2);
        assertThat(localTarget.getTopicCrds().size()).isEqualTo(1);

        TargetState azureTarget = new TargetState("azure", flow, bootstrapServerCache);
        azureTarget.build();

        assertThat(azureTarget.getTopicCrds().size()).isEqualTo(0);

        System.out.println("Done");

    }
}
