package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.infrastructure.kafka.VideoStatusProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VideoStatusGatewayImplTest {

    private VideoStatusProducer producer;
    private VideoStatusGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        producer = mock(VideoStatusProducer.class);
        gateway = new VideoStatusGatewayImpl(producer);
    }

    @Test
    void shouldDelegatePublishStatusToProducer() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Long videoId = 42L;
        String status = "FALHA";

        gateway.publishStatus(userId, videoId, status);

        verify(producer, times(1)).publishStatus(userId, videoId, status);
    }

    @Test
    void shouldPropagateExceptionsFromProducer() {
        UUID userId = UUID.randomUUID();
        Long videoId = 99L;
        String status = "ERRO";
        doThrow(new RuntimeException("publishing error")).when(producer).publishStatus(userId, videoId, status);

        assertThrows(RuntimeException.class, () -> gateway.publishStatus(userId, videoId, status));
    }
}

