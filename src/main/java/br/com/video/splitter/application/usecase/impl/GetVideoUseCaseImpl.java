package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoGateway;
import br.com.video.splitter.application.usecase.GetVideoUseCase;
import br.com.video.splitter.domain.VideoInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.util.Optional;

@ApplicationScoped
public class GetVideoUseCaseImpl implements GetVideoUseCase {
    private final VideoGateway videoGateway;

    @Inject
    public GetVideoUseCaseImpl(VideoGateway videoGateway) {
        this.videoGateway = videoGateway;
    }

    @Override
    public InputStream getVideo(VideoInfo videoInfo) {
        Optional<InputStream> videoStream = videoGateway.getVideo(videoInfo);
        return videoStream.orElseThrow(() -> new RuntimeException("Video not found"));
    }
}
