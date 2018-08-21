/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.streamzi.openshift.dataflow.tests;

import io.streamzi.openshift.dataflow.model.ProcessorConstants;
import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorFlowWriter;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class builds a functioning flow using a random data source and consumer
 * @author hhiden
 */
public class FunctioningFlowTest {
    
    public FunctioningFlowTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testWorkingDataflow() throws Exception {
        ProcessorFlow flow = new ProcessorFlow();
        flow.setName("random-data-feed");
        flow.getGlobalSettings().put(ProcessorConstants.KAFKA_BOOTSTRAP_SERVERS, "my-cluster-kafka:9092");

        ProcessorNodeTemplate sourceTemplate = ProcessorTemplateYAMLReader.readTemplate(Thread.currentThread().getContextClassLoader().getResourceAsStream("random-data.yaml"));
        ProcessorNodeTemplate outputTemplate = ProcessorTemplateYAMLReader.readTemplate(Thread.currentThread().getContextClassLoader().getResourceAsStream("logger.yaml"));

        ProcessorNode source = sourceTemplate.createProcessorNode();
        ProcessorNode logger = outputTemplate.createProcessorNode();

        flow.addProcessorNode(source);
        flow.addProcessorNode(logger);

        flow.linkNodes(source, "output-data", logger, "input-data");

        ProcessorFlowWriter writer = new ProcessorFlowWriter(flow);
        Assert.assertNotNull(writer.writeToJsonString());
    }
}
