package br.com.video.splitter.infrastructure.messaging;

import br.com.video.splitter.common.domain.dto.event.VideoStatusEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaVideoStatusPublisherTest {

    private Emitter<Object> emitter;
    private KafkaVideoStatusPublisher publisher;

    @BeforeEach
    void setUp() {
        emitter = mock(Emitter.class);
        publisher = new KafkaVideoStatusPublisher();
        publisher.emitter = emitter;
        publisher.statusTopic = "video.status";
    }

    @Test
    void shouldSendVideoStatusEvent() {
        UUID userId = UUID.randomUUID();
        Long videoId = 99L;
        String status = "FALHA";

        publisher.publishStatus(userId, videoId, status);

        ArgumentCaptor<Message<?>> captor = ArgumentCaptor.forClass(Message.class);
        verify(emitter, times(1)).send(captor.capture());
        Message<?> sentMsg = captor.getValue();
        Object payload = sentMsg.getPayload();
        assertTrue(payload instanceof VideoStatusEvent);
        VideoStatusEvent event = (VideoStatusEvent) payload;
        assertEquals(userId, event.getUserId());
        assertEquals(videoId, event.getVideoId());
        assertEquals(status, event.getStatus());
        OutgoingKafkaRecordMetadata<?> metadata = sentMsg.getMetadata(OutgoingKafkaRecordMetadata.class).orElse(null);
        assertNotNull(metadata);
        assertEquals("video.status", metadata.getTopic());
        assertEquals(videoId.toString(), metadata.getKey());
    }

    @Test
    void shouldAllowNullVideoId() {
        UUID userId = UUID.randomUUID();
        String status = "PROCESSING";

        publisher.publishStatus(userId, null, status);

        ArgumentCaptor<Message<?>> captor = ArgumentCaptor.forClass(Message.class);
        verify(emitter).send(captor.capture());
        Message<?> sentMsg = captor.getValue();
        VideoStatusEvent event = (VideoStatusEvent) sentMsg.getPayload();
        assertEquals(userId, event.getUserId());
        assertNull(event.getVideoId());
        assertEquals(status, event.getStatus());
        OutgoingKafkaRecordMetadata<?> metadata = sentMsg.getMetadata(OutgoingKafkaRecordMetadata.class).orElse(null);
        assertNotNull(metadata);
        assertEquals("video.status", metadata.getTopic());
        assertNull(metadata.getKey());
    }
}
