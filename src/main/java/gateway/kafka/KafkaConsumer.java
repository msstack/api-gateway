package gateway.kafka;

import gateway.cache.RequestCache;
import gateway.query.handler.QueryOnCompleteListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KafkaConsumer {
    private final static String TOPIC = "my-example-topic";
    private final static String BOOTSTRAP_SERVERS =
            "localhost:9092,localhost:9093,localhost:9094";


    private static KafkaConsumer kafkaConsumer = null;
    private ConcurrentLinkedQueue<String> inStream = new ConcurrentLinkedQueue<>();

    private QueryOnCompleteListener queryOnCompleteListener;
//    private List<CommandOnCmpleteListener> commandOnCmpleteListeners = new ArrayList<>();

    private KafkaConsumer() {
    }

    public static KafkaConsumer getInstance() {
        if (kafkaConsumer == null) {
            kafkaConsumer = new KafkaConsumer();
            try {
                kafkaConsumer.runConsumer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return kafkaConsumer;
    }

    private static Consumer<Long, String> createConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        // Create the consumer using props.
        final Consumer<Long, String> consumer =
                new org.apache.kafka.clients.consumer.KafkaConsumer(props);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(TOPIC));
        return consumer;
    }

    private void runConsumer() throws InterruptedException {
        final Consumer<Long, String> consumer = createConsumer();
        final int giveUp = 100;
        int noRecordsCount = 0;
        while (true) {
            final ConsumerRecords<Long, String> consumerRecords =
                    consumer.poll(1000);
            if (consumerRecords.count() == 0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }
            consumerRecords.forEach(record -> {

                System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                        record.key(), record.value(),
                        record.partition(), record.offset());

                /**
                 * Implemented only for QUERIES
                 */
                Optional<ChannelHandlerContext> context;
                context = RequestCache.getInstance().getQueryRequestor(String.valueOf(record.key()));
                //Record is a RESPONSE for a QUERY
                context.ifPresent(channelHandlerContext ->
                        queryOnCompleteListener.onComplete(channelHandlerContext, record.value()));

            });
            consumer.commitAsync();
        }
        consumer.close();
        System.out.println("DONE");
    }

    public void setQueryOnCompleteListener(QueryOnCompleteListener queryOnCompleteListener) {
        this.queryOnCompleteListener = queryOnCompleteListener;
    }

    public ConcurrentLinkedQueue getInStream() {
        return inStream;
    }

    public KafkaConsumer setInStream(ConcurrentLinkedQueue queue) {
        this.inStream = queue;
        return this;
    }

    public void queue(String item) {
        this.inStream.add(item);
    }

}
