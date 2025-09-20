package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.infrastructure.kafka.VideoSplittedProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        VideoInfo videoInfo = mock(VideoInfo.class);
        gateway.publishVideoSplitted(videoInfo);
        verify(producer, times(1)).send(videoInfo);
    }

    @Test
    void shouldInstantiateWithProducer() {
        new VideoEventGatewayImpl(producer);
    }
}

