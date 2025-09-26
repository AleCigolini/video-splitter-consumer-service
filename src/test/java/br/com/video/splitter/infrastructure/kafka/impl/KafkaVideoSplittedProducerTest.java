package br.com.video.splitter.infrastructure.kafka.impl;

import br.com.video.splitter.domain.VideoChunkInfo;
import br.com.video.splitter.domain.VideoInfo;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaVideoSplittedProducerTest {
    Emitter<VideoChunkInfo> emitter;
    KafkaVideoSplittedProducer producer;

    @BeforeEach
    void setUp() {
        emitter = mock(Emitter.class);
        producer = new KafkaVideoSplittedProducer();
        producer.emitter = emitter;
    }

    @Test
    void shouldSendVideoChunkInfoObjectToEmitter() {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        VideoInfo base = new VideoInfo(id, "container", "conn-str", "video.mp4");
        VideoChunkInfo chunk = new VideoChunkInfo(base, 2, 5, "video_2.mp4");

        producer.send(chunk);

        ArgumentCaptor<VideoChunkInfo> captor = ArgumentCaptor.forClass(VideoChunkInfo.class);
        verify(emitter).send(captor.capture());
        VideoChunkInfo sent = captor.getValue();
        assertEquals(id, sent.getId());
        assertEquals(2, sent.getChunkId());
        assertEquals(5, sent.getTotalChunks());
        assertEquals("video_2.mp4", sent.getFileName());
        assertEquals("container", sent.getContainerName());
    }
}
