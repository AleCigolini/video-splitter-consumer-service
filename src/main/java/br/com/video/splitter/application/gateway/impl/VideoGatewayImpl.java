package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.application.gateway.VideoGateway;
import br.com.video.splitter.common.interfaces.VideoStorageFetcher;
import br.com.video.splitter.domain.VideoInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.util.Optional;

@ApplicationScoped
public class VideoGatewayImpl implements VideoGateway {
    private final VideoStorageFetcher videoStorageFetcher;

    @Inject
    public VideoGatewayImpl(VideoStorageFetcher videoStorageFetcher) {
        this.videoStorageFetcher = videoStorageFetcher;
    }

    @Override
    public Optional<InputStream> getVideo(VideoInfo videoInfo) {
        return videoStorageFetcher.fetch(videoInfo);
    }

}
