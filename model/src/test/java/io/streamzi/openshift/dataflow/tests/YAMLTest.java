/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.streamzi.openshift.dataflow.tests;

import io.streamzi.openshift.dataflow.model.ProcessorInputPort;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import io.streamzi.openshift.dataflow.model.ProcessorNodeTemplate;
import io.streamzi.openshift.dataflow.model.ProcessorOutputPort;
import io.streamzi.openshift.dataflow.model.serialization.ProcessorTemplateYAMLWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hhiden
 */
public class YAMLTest {

    public YAMLTest() {
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
    public void testYAML() throws Exception {
        ProcessorNodeTemplate kafkaTemplate = new ProcessorNodeTemplate();
        kafkaTemplate.addInput("input-data");
        kafkaTemplate.addOutput("output-data");

        ProcessorTemplateYAMLWriter writer = new ProcessorTemplateYAMLWriter(kafkaTemplate);
        System.out.println(writer.writeToYAMLString());
    }

}
