package br.com.video.splitter.infrastructure.kafka.impl;

import br.com.video.splitter.domain.VideoChunkInfo;
import br.com.video.splitter.infrastructure.kafka.VideoSplittedProducer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class KafkaVideoSplittedProducer implements VideoSplittedProducer {

    @Channel("video-events")
    Emitter<Object> emitter;

    @ConfigProperty(name = "video.topic.split")
    String splitTopic;

    @Override
    public void send(VideoChunkInfo chunkInfo) {
        OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String>builder()
                .withTopic(splitTopic)
                .withKey(chunkInfo.getVideoId() != null ? chunkInfo.getVideoId().toString() : null)
                .build();
        Message<Object> message = Message.of((Object) chunkInfo).addMetadata(metadata);
        emitter.send(message);
    }
}
