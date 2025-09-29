package br.com.video.splitter.application.controller.impl;

import br.com.video.splitter.application.controller.VideoSplitterController;
import br.com.video.splitter.application.mapper.RequestVideoInfoMapper;
import br.com.video.splitter.application.usecase.GetVideoUseCase;
import br.com.video.splitter.application.usecase.PublishVideoStatusUseCase;
import br.com.video.splitter.application.usecase.SplitVideoUseCase;
import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.domain.VideoInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;

@ApplicationScoped
public class VideoSplitterControllerImpl implements VideoSplitterController {
    private static final Logger LOG = Logger.getLogger(VideoSplitterControllerImpl.class);
    private final GetVideoUseCase getVideoUseCase;
    private final RequestVideoInfoMapper requestVideoInfoMapper;
    private final SplitVideoUseCase splitVideoUseCase;
    private final PublishVideoStatusUseCase publishVideoStatusUseCase;

    @Inject
    public VideoSplitterControllerImpl(RequestVideoInfoMapper requestVideoInfoMapper,
                                       GetVideoUseCase getVideoUseCase,
                                       SplitVideoUseCase splitVideoUseCase,
                                       PublishVideoStatusUseCase publishVideoStatusUseCase) {
        this.getVideoUseCase = getVideoUseCase;
        this.splitVideoUseCase = splitVideoUseCase;
        this.requestVideoInfoMapper = requestVideoInfoMapper;
        this.publishVideoStatusUseCase = publishVideoStatusUseCase;
    }

    @Override
    public void splitVideo(UploadedVideoInfoDto uploadedVideoInfoDto) {
        VideoInfo videoInfo = requestVideoInfoMapper.requestDtoToDomain(uploadedVideoInfoDto);
        try (InputStream video = getVideoUseCase.getVideo(videoInfo)) {
            splitVideoUseCase.splitVideo(video, videoInfo);
        } catch (Exception e) {
            LOG.errorf(e, "Erro ao processar splitVideo para userId=%s, videoId=%s", uploadedVideoInfoDto.getUserId(), uploadedVideoInfoDto.getVideoId());
            publishVideoStatusUseCase.publishStatus(uploadedVideoInfoDto.getUserId(), uploadedVideoInfoDto.getVideoId(), "ERROR");
        }
    }
}
