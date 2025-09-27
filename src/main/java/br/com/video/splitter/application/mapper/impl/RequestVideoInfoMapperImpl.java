package br.com.video.splitter.application.mapper.impl;

import br.com.video.splitter.application.mapper.RequestVideoInfoMapper;
import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.domain.VideoInfo;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

@Singleton
public class RequestVideoInfoMapperImpl implements RequestVideoInfoMapper {
    private final ModelMapper modelMapper;

    @Inject
    public RequestVideoInfoMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

        Converter<UploadedVideoInfoDto, VideoInfo> converter = new Converter<>() {
            @Override
            public VideoInfo convert(MappingContext<UploadedVideoInfoDto, VideoInfo> context) {
                UploadedVideoInfoDto src = context.getSource();
                return new VideoInfo(
                        src.getId(),
                        src.getContainerName(),
                        src.getConnectionString(),
                        src.getFileName(),
                        src.getUserId()
                );
            }
        };

        this.modelMapper.addConverter(converter, UploadedVideoInfoDto.class, VideoInfo.class);
    }

    @Override
    public VideoInfo requestDtoToDomain(UploadedVideoInfoDto requestDto) {
        return modelMapper.map(requestDto, VideoInfo.class);
    }
}
