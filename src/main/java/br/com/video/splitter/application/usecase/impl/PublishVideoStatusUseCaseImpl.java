package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoStatusGateway;
import br.com.video.splitter.application.usecase.PublishVideoStatusUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class PublishVideoStatusUseCaseImpl implements PublishVideoStatusUseCase {
    private final VideoStatusGateway videoStatusGateway;

    @Inject
    public PublishVideoStatusUseCaseImpl(VideoStatusGateway videoStatusGateway) {
        this.videoStatusGateway = videoStatusGateway;
    }

    @Override
    public void publishStatus(UUID userId, Long videoId, String status) {
        try {
            videoStatusGateway.publishStatus(userId, videoId, status);
        } catch (Exception e) {
            System.err.println("Falha ao publicar status de vídeo: " + e.getMessage());
        }
    }
}
