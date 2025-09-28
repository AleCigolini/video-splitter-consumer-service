package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.messaging.VideoStatusPublisher;
import br.com.video.splitter.application.usecase.PublishVideoStatusUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class PublishVideoStatusUseCaseImpl implements PublishVideoStatusUseCase {
    private final VideoStatusPublisher videoStatusPublisher;

    @Inject
    public PublishVideoStatusUseCaseImpl(VideoStatusPublisher videoStatusPublisher) {
        this.videoStatusPublisher = videoStatusPublisher;
    }

    @Override
    public void publishStatus(UUID userId, Long videoId, String status) {
        try {
            videoStatusPublisher.publishStatus(userId, videoId, status);
        } catch (Exception e) {
            System.err.println("Falha ao publicar status de vídeo: " + e.getMessage());
        }
    }
}
