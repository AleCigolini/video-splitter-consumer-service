package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoGateway;
import br.com.video.splitter.application.usecase.GetVideoUseCase;
import br.com.video.splitter.domain.VideoInfo;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class GetVideoUseCaseImpl implements GetVideoUseCase {
    private final VideoGateway videoGateway;

    @Override
    public InputStream getVideo(VideoInfo videoInfo) {
        Optional<InputStream> videoStream = videoGateway.getVideo(videoInfo);
        return videoStream.orElseThrow(() -> new RuntimeException("Video not found"));
    }
}
