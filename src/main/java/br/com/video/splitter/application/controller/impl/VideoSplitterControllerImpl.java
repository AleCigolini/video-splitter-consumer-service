package br.com.video.splitter.application.controller.impl;

import br.com.video.splitter.application.controller.VideoSplitterController;
import br.com.video.splitter.application.gateway.impl.VideoGatewayImpl;
import br.com.video.splitter.application.mapper.RequestVideoInfoMapper;
import br.com.video.splitter.application.mapper.impl.RequestVideoInfoMapperImpl;
import br.com.video.splitter.application.usecase.GetVideoUseCase;
import br.com.video.splitter.application.usecase.impl.GetVideoUseCaseImpl;
import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.infrastructure.azure.storage.adapter.AzureBlobVideoStorageFetcher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;

@ApplicationScoped
public class VideoSplitterControllerImpl implements VideoSplitterController {
    private final GetVideoUseCase getVideoUseCase;
    private final RequestVideoInfoMapper requestVideoInfoMapper;

    @Inject
    public VideoSplitterControllerImpl(RequestVideoInfoMapper requestVideoInfoMapper) {
        final VideoGatewayImpl videoGateway = new VideoGatewayImpl(new AzureBlobVideoStorageFetcher());
        getVideoUseCase = new GetVideoUseCaseImpl(videoGateway);
        this.requestVideoInfoMapper = requestVideoInfoMapper;
    }

    @Override
    public void splitVideo(UploadedVideoInfoDto uploadedVideoInfoDto) {
        final InputStream video = getVideoUseCase.getVideo(requestVideoInfoMapper.requestDtoToDomain(uploadedVideoInfoDto));
    }
}
