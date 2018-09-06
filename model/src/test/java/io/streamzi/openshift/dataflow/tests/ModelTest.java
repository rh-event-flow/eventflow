package io.streamzi.openshift.dataflow.tests;

import io.streamzi.openshift.dataflow.model.ProcessorFlow;
import io.streamzi.openshift.dataflow.model.ProcessorInputPort;
import io.streamzi.openshift.dataflow.model.ProcessorNode;
import io.streamzi.openshift.dataflow.model.ProcessorOutputPort;
import org.junit.*;

import java.util.logging.Logger;

import static org.junit.Assert.fail;

/**
 * @author hhiden
 */
public class ModelTest {
    private static final Logger logger = Logger.getLogger(ModelTest.class.getName());

    public ModelTest() {
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
    public void testModel() {
        try {
            ProcessorFlow flow = new ProcessorFlow();

            ProcessorNode kafkaInput = new ProcessorNode();
            kafkaInput.addOutput(new ProcessorOutputPort("events"));

            ProcessorNode kafkaFilter = new ProcessorNode();
            kafkaFilter.addInput(new ProcessorInputPort("input"));
            kafkaFilter.addOutput(new ProcessorOutputPort("output"));

            ProcessorNode kafkaPublish = new ProcessorNode();
            kafkaPublish.addInput(new ProcessorInputPort("events"));

            flow.setName("FilterFlow");
            flow.addProcessorNode(kafkaInput);
            flow.addProcessorNode(kafkaFilter);
            flow.addProcessorNode(kafkaPublish);

            flow.linkNodes(kafkaInput, "events", kafkaFilter, "input");
            flow.linkNodes(kafkaFilter, "output", kafkaPublish, "events");

//            ProcessorFlowWriter writer = new ProcessorFlowWriter(flow);
//            String json = writer.writeToJsonString();
//            logger.info(json);
//
//            ProcessorFlowReader reader = new ProcessorFlowReader();
//            ProcessorFlow reconstructed = reader.readFromJsonString(json);
//
//            ProcessorFlowWriter rewriter = new ProcessorFlowWriter(reconstructed);
//            String rewrittenJson = rewriter.writeToJsonString();
//            logger.info(rewrittenJson);
//            assertEquals(json, rewrittenJson);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
