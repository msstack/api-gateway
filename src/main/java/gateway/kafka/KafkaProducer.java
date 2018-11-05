package gateway.kafka;

import java.util.concurrent.ConcurrentLinkedQueue;

public class KafkaProducer {
    private static KafkaProducer kafkaProducer = null;
    private ConcurrentLinkedQueue<String> inStream = new ConcurrentLinkedQueue<>();

    private KafkaProducer() {
    }

    public static KafkaProducer getInstance() {
        if (kafkaProducer == null) {
            kafkaProducer = new KafkaProducer();
        }
        return kafkaProducer;
    }

    public void queue(String item) {
        this.inStream.add(item);
    }
}
