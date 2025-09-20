package br.com.video.splitter.application.controller.impl;

import br.com.video.splitter.application.mapper.RequestVideoInfoMapper;
import br.com.video.splitter.application.usecase.GetVideoUseCase;
import br.com.video.splitter.application.usecase.SplitVideoUseCase;
import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.domain.VideoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class VideoSplitterControllerImplTest {
    private GetVideoUseCase getVideoUseCase;
    private RequestVideoInfoMapper requestVideoInfoMapper;
    private SplitVideoUseCase splitVideoUseCase;
    private VideoSplitterControllerImpl controller;

    @BeforeEach
    void setUp() {
        getVideoUseCase = mock(GetVideoUseCase.class);
        requestVideoInfoMapper = mock(RequestVideoInfoMapper.class);
        splitVideoUseCase = mock(SplitVideoUseCase.class);
        controller = new VideoSplitterControllerImpl(requestVideoInfoMapper, getVideoUseCase, splitVideoUseCase);
    }

    @Test
    void shouldCallAllDependenciesInOrder() throws Exception {
        UploadedVideoInfoDto dto = mock(UploadedVideoInfoDto.class);
        VideoInfo videoInfo = mock(VideoInfo.class);
        InputStream inputStream = spy(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        when(requestVideoInfoMapper.requestDtoToDomain(dto)).thenReturn(videoInfo);
        when(getVideoUseCase.getVideo(videoInfo)).thenReturn(inputStream);

        controller.splitVideo(dto);

        InOrder inOrder = inOrder(requestVideoInfoMapper, getVideoUseCase, splitVideoUseCase);
        inOrder.verify(requestVideoInfoMapper).requestDtoToDomain(dto);
        inOrder.verify(getVideoUseCase).getVideo(videoInfo);
        inOrder.verify(splitVideoUseCase).splitVideo(inputStream, videoInfo);
        verify(inputStream).close();
    }

    @Test
    void shouldThrowRuntimeExceptionWhenGetVideoFails() throws Exception {
        UploadedVideoInfoDto dto = mock(UploadedVideoInfoDto.class);
        VideoInfo videoInfo = mock(VideoInfo.class);
        when(requestVideoInfoMapper.requestDtoToDomain(dto)).thenReturn(videoInfo);
        when(getVideoUseCase.getVideo(videoInfo)).thenThrow(new RuntimeException("fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.splitVideo(dto));
        assertTrue(ex.getMessage().contains("Falha ao obter ou dividir o vídeo"));
        assertTrue(ex.getCause() instanceof RuntimeException);
        verify(splitVideoUseCase, never()).splitVideo(any(), any());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSplitVideoFails() throws Exception {
        UploadedVideoInfoDto dto = mock(UploadedVideoInfoDto.class);
        VideoInfo videoInfo = mock(VideoInfo.class);
        InputStream inputStream = spy(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        when(requestVideoInfoMapper.requestDtoToDomain(dto)).thenReturn(videoInfo);
        when(getVideoUseCase.getVideo(videoInfo)).thenReturn(inputStream);
        doThrow(new RuntimeException("split fail")).when(splitVideoUseCase).splitVideo(inputStream, videoInfo);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.splitVideo(dto));
        assertTrue(ex.getMessage().contains("Falha ao obter ou dividir o vídeo"));
        assertTrue(ex.getCause().getMessage().contains("split fail"));
        verify(inputStream).close();
    }
}
