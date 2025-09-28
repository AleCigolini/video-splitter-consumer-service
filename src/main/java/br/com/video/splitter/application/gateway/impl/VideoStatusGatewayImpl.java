package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.application.gateway.VideoStatusGateway;
import br.com.video.splitter.infrastructure.kafka.VideoStatusProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class VideoStatusGatewayImpl implements VideoStatusGateway {
    private final VideoStatusProducer videoStatusProducer;

    @Inject
    public VideoStatusGatewayImpl(VideoStatusProducer videoStatusProducer) {
        this.videoStatusProducer = videoStatusProducer;
    }

    @Override
    public void publishStatus(UUID userId, Long videoId, String status) {
        videoStatusProducer.publishStatus(userId, videoId, status);
    }
}