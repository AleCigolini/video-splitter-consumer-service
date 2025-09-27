package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.domain.VideoChunkInfo;
import br.com.video.splitter.infrastructure.kafka.VideoSplittedProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class VideoEventGatewayImplTest {

    private VideoSplittedProducer producer;
    private VideoEventGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        producer = mock(VideoSplittedProducer.class);
        gateway = new VideoEventGatewayImpl(producer);
    }

    @Test
    void shouldCallProducerSendWhenPublishVideoSplitted() {
        VideoInfo base = new VideoInfo(UUID.randomUUID(), "container", "conn", "original.mp4", UUID.randomUUID());
        VideoChunkInfo chunk = new VideoChunkInfo(base, 0, 5, "000.mp4");
        gateway.publishVideoSplitted(chunk);
        verify(producer, times(1)).send(chunk);
    }

    @Test
    void shouldInstantiateWithProducer() {
        new VideoEventGatewayImpl(producer);
    }
}
