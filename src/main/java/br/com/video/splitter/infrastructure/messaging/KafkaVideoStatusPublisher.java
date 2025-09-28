package br.com.video.splitter.infrastructure.messaging;

import br.com.video.splitter.application.messaging.VideoStatusPublisher;
import br.com.video.splitter.common.domain.dto.event.VideoStatusEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

@ApplicationScoped
public class KafkaVideoStatusPublisher implements VideoStatusPublisher {
    private static final String TOPIC = "video.status";
    private Producer<String, String> producer;

    @Inject
    ObjectMapper objectMapper;

    @PostConstruct
    void init() {
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getProperty("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "all");
        this.producer = new KafkaProducer<>(props);
    }

    @PreDestroy
    void shutdown() {
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    public void publishStatus(UUID userId, Long videoId, String status) {
        VideoStatusEvent event = new VideoStatusEvent(userId, videoId, status);
        String payload = toJson(event);
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, videoId != null ? videoId.toString() : null, payload);
        producer.send(record);
    }

    private String toJson(VideoStatusEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize VideoStatusEvent", e);
        }
    }
}
