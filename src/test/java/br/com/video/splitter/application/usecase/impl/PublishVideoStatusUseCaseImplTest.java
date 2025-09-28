package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoStatusGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class PublishVideoStatusUseCaseImplTest {
    private VideoStatusGateway videoStatusGateway;
    private PublishVideoStatusUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        videoStatusGateway = mock(VideoStatusGateway.class);
        useCase = new PublishVideoStatusUseCaseImpl(videoStatusGateway);
    }

    @Test
    void shouldCallPublisherWithCorrectParams() {
        UUID userId = UUID.randomUUID();
        Long videoId = 123L;
        String status = "FALHA";

        useCase.publishStatus(userId, videoId, status);

        verify(videoStatusGateway, times(1)).publishStatus(userId, videoId, status);
    }

    @Test
    void shouldCatchExceptionFromPublisher() {
        UUID userId = UUID.randomUUID();
        Long videoId = 456L;
        String status = "ERRO";
        doThrow(new RuntimeException("erro de publicação")).when(videoStatusGateway).publishStatus(userId, videoId, status);

        useCase.publishStatus(userId, videoId, status);
    }
}

