package io.streamzi.eventflow.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamzi.eventflow.model.ProcessorFlow;
import io.streamzi.eventflow.serialization.SerializedFlow;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author hhiden
 */
public class ModelTest {
    private static final Logger logger = Logger.getLogger(ModelTest.class.getName());

    private ObjectMapper MAPPER = new ObjectMapper();

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


    @Test
    public void testLoadingCR() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("flow-cr.json");
        try {

            SerializedFlow sf = MAPPER.readValue(is, SerializedFlow.class);
            assertThat(sf.getNodes().size()).isEqualTo(2);
            assertThat(sf.getLinks().size()).isEqualTo(1);

            ProcessorFlow flow = new ProcessorFlow(sf);
            assertThat(flow.getNodes().size()).isEqualTo(2);

        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
