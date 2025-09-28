package br.com.video.splitter.application.controller.impl;

import br.com.video.splitter.application.mapper.RequestVideoInfoMapper;
import br.com.video.splitter.application.usecase.GetVideoUseCase;
import br.com.video.splitter.application.usecase.PublishVideoStatusUseCase;
import br.com.video.splitter.application.usecase.SplitVideoUseCase;
import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.domain.VideoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class VideoSplitterControllerImplTest {
    private GetVideoUseCase getVideoUseCase;
    private RequestVideoInfoMapper requestVideoInfoMapper;
    private SplitVideoUseCase splitVideoUseCase;
    private PublishVideoStatusUseCase publishVideoStatusUseCase;
    private VideoSplitterControllerImpl controller;

    @BeforeEach
    void setUp() {
        getVideoUseCase = mock(GetVideoUseCase.class);
        requestVideoInfoMapper = mock(RequestVideoInfoMapper.class);
        splitVideoUseCase = mock(SplitVideoUseCase.class);
        publishVideoStatusUseCase = mock(PublishVideoStatusUseCase.class);
        controller = new VideoSplitterControllerImpl(requestVideoInfoMapper, getVideoUseCase, splitVideoUseCase, publishVideoStatusUseCase);
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
    void shouldPublishErrorStatusWhenGetVideoFails() throws Exception {
        UUID userId = UUID.randomUUID();
        Long videoId = 123L;
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto();
        dto.setUserId(userId);
        dto.setVideoId(videoId);

        VideoInfo videoInfo = mock(VideoInfo.class);
        when(requestVideoInfoMapper.requestDtoToDomain(dto)).thenReturn(videoInfo);
        when(getVideoUseCase.getVideo(videoInfo)).thenThrow(new RuntimeException("fail"));

        controller.splitVideo(dto);

        verify(splitVideoUseCase, never()).splitVideo(any(), any());
        verify(publishVideoStatusUseCase, times(1)).publishStatus(userId, videoId, "ERROR");
    }

    @Test
    void shouldPublishErrorStatusWhenSplitVideoFails() throws Exception {
        UUID userId = UUID.randomUUID();
        Long videoId = 456L;
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto();
        dto.setUserId(userId);
        dto.setVideoId(videoId);

        VideoInfo videoInfo = mock(VideoInfo.class);
        InputStream inputStream = spy(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        when(requestVideoInfoMapper.requestDtoToDomain(dto)).thenReturn(videoInfo);
        when(getVideoUseCase.getVideo(videoInfo)).thenReturn(inputStream);
        doThrow(new RuntimeException("split fail")).when(splitVideoUseCase).splitVideo(inputStream, videoInfo);

        controller.splitVideo(dto);

        verify(inputStream).close();
        verify(publishVideoStatusUseCase, times(1)).publishStatus(userId, videoId, "ERROR");
    }
}
