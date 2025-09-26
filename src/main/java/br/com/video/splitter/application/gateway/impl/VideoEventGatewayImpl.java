package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.application.gateway.VideoEventGateway;
import br.com.video.splitter.domain.VideoChunkInfo;
import br.com.video.splitter.infrastructure.kafka.VideoSplittedProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class VideoEventGatewayImpl implements VideoEventGateway {

    private final VideoSplittedProducer producer;

    @Inject
    public VideoEventGatewayImpl(VideoSplittedProducer producer) {
        this.producer = producer;
    }

    @Override
    public void publishVideoSplitted(VideoChunkInfo videoChunkInfo) {
        producer.send(videoChunkInfo);
    }
}
