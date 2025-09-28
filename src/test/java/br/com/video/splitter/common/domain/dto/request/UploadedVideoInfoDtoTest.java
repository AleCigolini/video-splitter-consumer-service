package br.com.video.splitter.common.domain.dto.request;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UploadedVideoInfoDtoTest {
    @Test
    void shouldCreateDtoWithAllFields() {
        Long id = 1L;
        String containerName = "container";
        String connectionString = "conn";
        String fileName = "file.mp4";
        UUID userId = UUID.randomUUID();
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto(containerName, connectionString, fileName, userId);
        dto.setVideoId(id);
        assertEquals(id, dto.getVideoId());
        assertEquals(containerName, dto.getContainerName());
        assertEquals(connectionString, dto.getConnectionString());
        assertEquals(fileName, dto.getFileName());
        assertEquals(userId, dto.getUserId());
    }

    @Test
    void shouldAllowNullId() {
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto("container", "conn", "file.mp4", null);
        dto.setVideoId(null);
        assertNull(dto.getVideoId());
    }

    @Test
    void shouldAllowNullFieldsExceptFinals() {
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto(null, null, null, null);
        dto.setVideoId(null);
        assertNull(dto.getVideoId());
        assertNull(dto.getContainerName());
        assertNull(dto.getConnectionString());
        assertNull(dto.getFileName());
        assertNull(dto.getUserId());
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        UploadedVideoInfoDto dto1 = new UploadedVideoInfoDto("container", "conn", "file.mp4", null);
        UploadedVideoInfoDto dto2 = new UploadedVideoInfoDto("container", "conn", "file.mp4", null);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void shouldTestToString() {
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto("container", "conn", "file.mp4", null);
        assertTrue(dto.toString().contains("container"));
        assertTrue(dto.toString().contains("conn"));
        assertTrue(dto.toString().contains("file.mp4"));
    }
}
