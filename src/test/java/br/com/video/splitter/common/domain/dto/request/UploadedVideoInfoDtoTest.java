package br.com.video.splitter.common.domain.dto.request;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UploadedVideoInfoDtoTest {
    @Test
    void shouldCreateDtoWithAllFields() {
        UUID id = UUID.randomUUID();
        String containerName = "container";
        String connectionString = "conn";
        String fileName = "file.mp4";
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto(containerName, connectionString, fileName);
        dto.setId(id);
        assertEquals(id, dto.getId());
        assertEquals(containerName, dto.getContainerName());
        assertEquals(connectionString, dto.getConnectionString());
        assertEquals(fileName, dto.getFileName());
    }

    @Test
    void shouldAllowNullId() {
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto("container", "conn", "file.mp4");
        dto.setId(null);
        assertNull(dto.getId());
    }

    @Test
    void shouldAllowNullFieldsExceptFinals() {
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto(null, null, null);
        dto.setId(null);
        assertNull(dto.getId());
        assertNull(dto.getContainerName());
        assertNull(dto.getConnectionString());
        assertNull(dto.getFileName());
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        UploadedVideoInfoDto dto1 = new UploadedVideoInfoDto("container", "conn", "file.mp4");
        UploadedVideoInfoDto dto2 = new UploadedVideoInfoDto("container", "conn", "file.mp4");
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void shouldTestToString() {
        UploadedVideoInfoDto dto = new UploadedVideoInfoDto("container", "conn", "file.mp4");
        assertTrue(dto.toString().contains("container"));
        assertTrue(dto.toString().contains("conn"));
        assertTrue(dto.toString().contains("file.mp4"));
    }
}

