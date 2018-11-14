package gateway.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import gateway.cache.RequestCache;
import gateway.query.handler.QueryOnCompleteListener;
import gateway.util.JsonSerializer;
import io.netty.channel.ChannelHandlerContext;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class KafkaConsumerService {

    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerService.class.toGenericString());
    private final KafkaConsumer<String, String> kafkaConsumer;
    private QueryOnCompleteListener queryOnCompleteListener;

    public KafkaConsumerService() {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        String brokers = "139.59.77.98:9092";
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);

        String groupId = "my-group";
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, applicationConfiguration.getServer().getId());
        this.kafkaConsumer = new KafkaConsumer<>(properties);
    }

    public void start() {
        String topic = "response";
        kafkaConsumer.subscribe(Collections.singletonList(topic));

        while (true) {
            final ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(100);

            consumerRecords.forEach(record -> {
                System.out.printf("Consumer Record:(%s, %s, %d, %d)\n", record.key(), record.value(), record.partition(),
                        record.offset());

                Optional<ChannelHandlerContext> context;
                String[] consumerResponse = record.value().split("::");
                JsonNode response = JsonSerializer.toJsonObject(consumerResponse[1]);
                context = RequestCache.getInstance().getQueryRequestor(response.get("flowId").asText());
                //Record is a RESPONSE for a QUERY
                context.ifPresent(channelHandlerContext ->
                        queryOnCompleteListener.onComplete(channelHandlerContext, consumerResponse[2]));

            });

            kafkaConsumer.commitAsync();
        }
//        kafkaConsumer.close();
    }

    public void setQueryOnCompleteListener(QueryOnCompleteListener queryOnCompleteListener) {
        this.queryOnCompleteListener = queryOnCompleteListener;
    }

}
