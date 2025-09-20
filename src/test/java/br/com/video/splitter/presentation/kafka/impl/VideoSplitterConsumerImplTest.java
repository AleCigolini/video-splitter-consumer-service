package br.com.video.splitter.presentation.kafka.impl;

import br.com.video.splitter.application.controller.VideoSplitterController;
import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class VideoSplitterConsumerImplTest {
    private VideoSplitterController controller;
    private VideoSplitterConsumerImpl consumer;

    @BeforeEach
    void setUp() {
        controller = mock(VideoSplitterController.class);
        consumer = new VideoSplitterConsumerImpl(controller);
    }

    @Test
    void shouldCallSplitVideoOnController() {
        UploadedVideoInfoDto dto = mock(UploadedVideoInfoDto.class);
        consumer.consume(dto);
        verify(controller, times(1)).splitVideo(dto);
    }

    @Test
    void shouldInstantiateWithController() {
        new VideoSplitterConsumerImpl(controller);
    }
}

