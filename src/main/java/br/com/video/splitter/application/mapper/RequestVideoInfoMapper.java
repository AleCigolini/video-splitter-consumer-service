package br.com.video.splitter.application.mapper;

import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.domain.VideoInfo;

public interface RequestVideoInfoMapper {
    VideoInfo requestDtoToDomain(UploadedVideoInfoDto requestDto);
}
