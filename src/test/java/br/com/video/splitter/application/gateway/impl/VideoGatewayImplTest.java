package br.com.video.splitter.application.gateway.impl;

import br.com.video.splitter.common.interfaces.VideoStorageFetcher;
import br.com.video.splitter.domain.VideoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoGatewayImplTest {
    private VideoStorageFetcher videoStorageFetcher;
    private VideoGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        videoStorageFetcher = mock(VideoStorageFetcher.class);
        gateway = new VideoGatewayImpl(videoStorageFetcher);
    }

    @Test
    void shouldReturnOptionalInputStreamWhenFetcherReturnsIt() {
        VideoInfo videoInfo = mock(VideoInfo.class);
        InputStream inputStream = mock(InputStream.class);
        Optional<InputStream> expected = Optional.of(inputStream);
        when(videoStorageFetcher.fetch(videoInfo)).thenReturn(expected);
        Optional<InputStream> result = gateway.getVideo(videoInfo);
        assertTrue(result.isPresent());
        assertEquals(inputStream, result.get());
        verify(videoStorageFetcher, times(1)).fetch(videoInfo);
    }

    @Test
    void shouldReturnEmptyOptionalWhenFetcherReturnsEmpty() {
        VideoInfo videoInfo = mock(VideoInfo.class);
        when(videoStorageFetcher.fetch(videoInfo)).thenReturn(Optional.empty());
        Optional<InputStream> result = gateway.getVideo(videoInfo);
        assertFalse(result.isPresent());
        verify(videoStorageFetcher, times(1)).fetch(videoInfo);
    }

    @Test
    void shouldInstantiateWithFetcher() {
        new VideoGatewayImpl(videoStorageFetcher);
    }
}

