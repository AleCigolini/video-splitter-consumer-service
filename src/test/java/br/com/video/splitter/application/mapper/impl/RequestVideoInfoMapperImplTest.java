package br.com.video.splitter.application.mapper.impl;

import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import br.com.video.splitter.domain.VideoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RequestVideoInfoMapperImplTest {
    private ModelMapper modelMapper;
    private RequestVideoInfoMapperImpl mapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        mapper = new RequestVideoInfoMapperImpl(modelMapper);
    }

    @Test
    void shouldMapUploadedVideoInfoDtoToVideoInfo() {
        UUID id = UUID.randomUUID();
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto(
            "container",
            "conn",
            "file.mp4"
        );
        dto.setId(id);
        VideoInfo result = mapper.requestDtoToDomain(dto);
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("container", result.getContainerName());
        assertEquals("conn", result.getConnectionString());
        assertEquals("file.mp4", result.getFileName());
    }

    @Test
    void shouldHandleNullFieldsInDto() {
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto(
            null, null, null
        );
        dto.setId(null);
        VideoInfo result = mapper.requestDtoToDomain(dto);
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getContainerName());
        assertNull(result.getConnectionString());
        assertNull(result.getFileName());
    }

    @Test
    void shouldInstantiateWithModelMapper() {
        new RequestVideoInfoMapperImpl(new ModelMapper());
    }
}
