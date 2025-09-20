package br.com.video.splitter.infrastructure.kafka.impl;

import br.com.video.splitter.domain.VideoInfo;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaVideoSplittedProducerTest {
    private Emitter<String> emitter;
    private KafkaVideoSplittedProducer producer;

    @BeforeEach
    void setUp() {
        emitter = mock(Emitter.class);
        producer = new KafkaVideoSplittedProducer();
        // inject mock emitter
        producer.emitter = emitter;
    }

    @Test
    void shouldSendJsonPayloadToEmitter() {
        VideoInfo info = mock(VideoInfo.class);
        when(info.getId()).thenReturn(java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        when(info.getContainerName()).thenReturn("cont");
        when(info.getConnectionString()).thenReturn("conn");
        when(info.getFileName()).thenReturn("file.mp4");
        producer.send(info);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(emitter).send(captor.capture());
        String json = captor.getValue();
        assertTrue(json.contains("\"id\":\"123e4567-e89b-12d3-a456-426614174000\""));
        assertTrue(json.contains("\"containerName\":\"cont\""));
        assertTrue(json.contains("\"connectionString\":\"conn\""));
        assertTrue(json.contains("\"fileName\":\"file.mp4\""));
    }

    @Test
    void shouldEscapeSpecialCharactersInJson() {
        VideoInfo info = mock(VideoInfo.class);
        when(info.getId()).thenReturn(java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        when(info.getContainerName()).thenReturn("cont\"\\");
        when(info.getConnectionString()).thenReturn(null);
        when(info.getFileName()).thenReturn("file\"\\.mp4");
        String json = invokeToJson(producer, info);
        assertTrue(json.contains("cont\\\"\\\\"));
        assertTrue(json.contains("\"connectionString\":\"\""));
        assertTrue(json.contains("file\\\"\\\\.mp4"));
    }

    // Helper to access private toJson
    private String invokeToJson(KafkaVideoSplittedProducer prod, VideoInfo info) {
        try {
            var m = KafkaVideoSplittedProducer.class.getDeclaredMethod("toJson", VideoInfo.class);
            m.setAccessible(true);
            return (String) m.invoke(prod, info);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldEscapeNullString() throws Exception {
        var m = KafkaVideoSplittedProducer.class.getDeclaredMethod("escape", String.class);
        m.setAccessible(true);
        assertEquals("", m.invoke(producer, (String) null));
    }

    @Test
    void shouldEscapeBackslashAndQuote() throws Exception {
        var m = KafkaVideoSplittedProducer.class.getDeclaredMethod("escape", String.class);
        m.setAccessible(true);
        assertEquals("abc\\\\def\\\"ghi", m.invoke(producer, "abc\\def\"ghi"));
    }
}
