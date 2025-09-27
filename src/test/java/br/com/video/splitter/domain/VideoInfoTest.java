package br.com.video.splitter.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VideoInfoTest {
    @Test
    void shouldCreateVideoInfoWithAllFields() {
        UUID id = UUID.randomUUID();
        String container = "container";
        String conn = "conn";
        String file = "file.mp4";
        UUID userId = UUID.randomUUID();
        VideoInfo info = new VideoInfo(id, container, conn, file, userId);
        assertEquals(id, info.getId());
        assertEquals(container, info.getContainerName());
        assertEquals(conn, info.getConnectionString());
        assertEquals(file, info.getFileName());
        assertEquals(userId, info.getUserId());
    }

    @Test
    void shouldAllowNullFields() {
        VideoInfo info = new VideoInfo(null, null, null, null, null);
        assertNull(info.getId());
        assertNull(info.getContainerName());
        assertNull(info.getConnectionString());
        assertNull(info.getFileName());
        assertNull(info.getUserId());
    }
}
