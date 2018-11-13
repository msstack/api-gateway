package gateway.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KafkaProducerService {
    private static KafkaProducerService kafkaProducerService = null;
    private final KafkaProducer<String, String> kafkaProducer;

    private KafkaProducerService() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "cloud.grydtech.com:9092");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 5);

        this.kafkaProducer = new KafkaProducer<>(properties);
    }

    public static KafkaProducerService getInstance() {
        if (kafkaProducerService == null) {
            kafkaProducerService = new KafkaProducerService();
        }
        return kafkaProducerService;
    }

    public void publish(String topic, String eventKey, String eventValue) {
//        POST- json has ID field
//        GET - query has :id

//        key = handler's entity's ID Ex:-StudentID
//        topic = entity type Ex:-student
//        value = payload_Msg
        this.kafkaProducer.send(new ProducerRecord<>(topic, eventKey, eventValue));
        this.kafkaProducer.flush();
    }

}
