package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoGateway;
import br.com.video.splitter.domain.VideoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetVideoUseCaseImplTest {
    private VideoGateway videoGateway;
    private GetVideoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        videoGateway = mock(VideoGateway.class);
        useCase = new GetVideoUseCaseImpl(videoGateway);
    }

    @Test
    void shouldReturnInputStreamWhenVideoExists() {
        VideoInfo videoInfo = mock(VideoInfo.class);
        InputStream inputStream = mock(InputStream.class);
        when(videoGateway.getVideo(videoInfo)).thenReturn(Optional.of(inputStream));
        InputStream result = useCase.getVideo(videoInfo);
        assertNotNull(result);
        assertEquals(inputStream, result);
        verify(videoGateway, times(1)).getVideo(videoInfo);
    }

    @Test
    void shouldThrowExceptionWhenVideoNotFound() {
        VideoInfo videoInfo = mock(VideoInfo.class);
        when(videoGateway.getVideo(videoInfo)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.getVideo(videoInfo));
        assertEquals("Video not found", ex.getMessage());
        verify(videoGateway, times(1)).getVideo(videoInfo);
    }

    @Test
    void shouldInstantiateWithGateway() {
        new GetVideoUseCaseImpl(videoGateway);
    }
}

