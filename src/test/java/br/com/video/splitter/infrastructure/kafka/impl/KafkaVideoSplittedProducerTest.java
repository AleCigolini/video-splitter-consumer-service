package br.com.video.splitter.infrastructure.kafka.impl;

import br.com.video.splitter.domain.VideoChunkInfo;
import br.com.video.splitter.domain.VideoInfo;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaVideoSplittedProducerTest {
    Emitter<Object> emitter;
    KafkaVideoSplittedProducer producer;

    @BeforeEach
    void setUp() {
        emitter = mock(Emitter.class);
        producer = new KafkaVideoSplittedProducer();
        producer.emitter = emitter;
        producer.splitTopic = "video.split";
    }

    @Test
    void shouldSendVideoChunkInfoObjectToEmitter() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Long videoId = 1L;
        VideoInfo base = new VideoInfo(videoId, "container", "conn-str", "video.mp4", id);
        VideoChunkInfo chunk = new VideoChunkInfo(base, 2, 5, "video_2.mp4");

        producer.send(chunk);

        ArgumentCaptor<Message<?>> captor = ArgumentCaptor.forClass(Message.class);
        verify(emitter).send(captor.capture());
        Message<?> sentMsg = captor.getValue();
        Object payloadObj = sentMsg.getPayload();
        assertTrue(payloadObj instanceof VideoChunkInfo);
        VideoChunkInfo payload = (VideoChunkInfo) payloadObj;
        assertEquals(videoId, payload.getVideoId());
        assertEquals(2, payload.getChunkPosition());
        assertEquals(5, payload.getTotalChunks());
        assertEquals("video_2.mp4", payload.getFileName());
        assertEquals("container", payload.getContainerName());
        assertEquals(id, payload.getUserId());
        OutgoingKafkaRecordMetadata<?> metadata = sentMsg.getMetadata(OutgoingKafkaRecordMetadata.class).orElse(null);
        assertNotNull(metadata);
        assertEquals("video.split", metadata.getTopic());
        assertEquals(videoId.toString(), metadata.getKey());
    }
}
