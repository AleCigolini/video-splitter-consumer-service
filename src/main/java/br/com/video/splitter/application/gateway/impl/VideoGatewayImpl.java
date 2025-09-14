package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.application.gateway.VideoGateway;
import br.com.video.splitter.common.interfaces.VideoStorageFetcher;
import br.com.video.splitter.domain.VideoInfo;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class VideoGatewayImpl implements VideoGateway {
    private final VideoStorageFetcher videoStorageFetcher;


    @Override
    public Optional<InputStream> getVideo(VideoInfo videoInfo) {
        return videoStorageFetcher.fetch(videoInfo);
    }

}
