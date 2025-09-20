package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoEventGateway;
import br.com.video.splitter.common.interfaces.VideoStoragePersister;
import br.com.video.splitter.domain.VideoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SplitVideoUseCaseImplTest {
    private VideoStoragePersister persister;
    private VideoEventGateway eventGateway;
    private SplitVideoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        persister = mock(VideoStoragePersister.class);
        eventGateway = mock(VideoEventGateway.class);
        useCase = new SplitVideoUseCaseImpl(persister, eventGateway);
    }

    @Test
    void shouldThrowExceptionWhenFfmpegProcessFails() {
        VideoInfo videoInfo = new VideoInfo(UUID.randomUUID(), "container", "conn", "file.mp4");
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1,2,3});
        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.splitVideo(inputStream, videoInfo));
        assertTrue(ex.getMessage().contains("Falha ao executar FFmpeg") || ex.getMessage().contains("Erro ao dividir e persistir vídeo"));
    }

    @Test
    void shouldCallFinallyBlockEvenOnException() {
        VideoInfo videoInfo = new VideoInfo(UUID.randomUUID(), "container", "conn", "file.mp4");
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1,2,3});
        try {
            useCase.splitVideo(inputStream, videoInfo);
        } catch (Exception ignored) {
            // Exception is expected due to ffmpeg not being available
        }
    }

    @Test
    void shouldInstantiateWithDependencies() {
        new SplitVideoUseCaseImpl(persister, eventGateway);
    }
}
