package br.com.video.splitter.presentation.kafka.impl;

import br.com.video.splitter.application.controller.VideoSplitterController;
import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.presentation.kafka.VideoSplitterConsumer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@RequiredArgsConstructor
public class VideoSplitterConsumerImpl implements VideoSplitterConsumer {
    private final VideoSplitterController videoSplitterController;

    @Incoming("video-splitter")
    public void consume(UploadedVideoInfoDto uploadedVideoInfoDto) {
        videoSplitterController.splitVideo(uploadedVideoInfoDto);
    }

}
