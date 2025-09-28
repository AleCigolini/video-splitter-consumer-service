package br.com.video.splitter.infrastructure.kafka.impl;

import br.com.video.splitter.domain.VideoChunkInfo;
import br.com.video.splitter.infrastructure.kafka.VideoSplittedProducer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaVideoSplittedProducer implements VideoSplittedProducer {

    @Channel("video-split")
    Emitter<VideoChunkInfo> emitter;

    @Override
    public void send(VideoChunkInfo chunkInfo) {
        emitter.send(chunkInfo);
    }
}
