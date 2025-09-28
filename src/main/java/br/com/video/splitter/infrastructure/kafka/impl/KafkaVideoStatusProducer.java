package br.com.video.splitter.infrastructure.kafka.impl;

import br.com.video.splitter.common.domain.dto.event.VideoStatusEvent;
import br.com.video.splitter.infrastructure.kafka.VideoStatusProducer;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.UUID;

@ApplicationScoped
public class KafkaVideoStatusProducer implements VideoStatusProducer {
    @Channel("video-events")
    Emitter<Object> emitter;

    @ConfigProperty(name = "video.topic.status")
    String statusTopic;

    @Override
    public void publishStatus(UUID userId, Long videoId, String status) {
        VideoStatusEvent event = new VideoStatusEvent(userId, videoId, status);
        OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String>builder()
                .withTopic(statusTopic)
                .withKey(videoId != null ? videoId.toString() : null)
                .build();
        Message<Object> message = Message.of((Object) event).addMetadata(metadata);
        emitter.send(message);
    }
}
