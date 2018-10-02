package io.streamzi.eventflow.runtime.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.streamzi.eventflow.runtime.CloudEventOutput;
import io.streamzi.eventflow.utils.EnvironmentResolver;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cloud event output that sends data to Kafka
 *
 * @author hhiden
 */
public class KafkaCloudEventOutputImpl extends CloudEventOutput {
    private ObjectMapper mapper;
    private static final Logger logger = Logger.getLogger(KafkaCloudEventOutputImpl.class.getName());
    private String bootstrapServers;
    private volatile boolean connected = false;
    private String topicName;
    private Producer<String, String> producer = null;

    public KafkaCloudEventOutputImpl(Object producerObject, String outputName) {
        super(producerObject, outputName);

        bootstrapServers = EnvironmentResolver.get(outputName + "_BOOTSTRAP_SERVERS");
        topicName = EnvironmentResolver.get(outputName);  // Passed in from deployer

        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void send(Object event) {
        send(null, event);
    }

    @Override
    public void send(String key, Object event) {
        if (connected) {
            try {
                String json = mapper.writeValueAsString(event);
                final ProducerRecord<String, String> record = new ProducerRecord(topicName, key, json);
                producer.send(record);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error sending event to Kafka: " + e.getMessage(), e);
            }
        } else {
            logger.log(Level.WARNING, "Producer not connected");
        }
    }

    @Override
    public void startOutput() {
        if (producer == null) {
            logger.info("Trying to connect to Kafka");
            try {
                producer = createProducer();
                connected = true;
                logger.info("Connected to Kafka");
            } catch (Exception e) {
                logger.warning("Cannot connect to Kafka: " + e.getMessage());
            }
        }
    }

    @Override
    public void stopOutput() {
        connected = false;
        if (producer != null) {
            producer.close();
        }
    }

    private Producer<String, String> createProducer() throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, processorUuid);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}