package br.com.video.splitter.infrastructure.kafka.serialization;

import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UploadedVideoInfoDtoDeserializerTest {

    private final UploadedVideoInfoDtoDeserializer deserializer = new UploadedVideoInfoDtoDeserializer();
    private static final String TOPIC = "video.uploaded";

    @Test
    @DisplayName("Deve desserializar JSON válido com todos os campos")
    void shouldDeserializeValidJson() {
        Long id = 1L;
        String json = "{" +
                "\"videoId\":\"" + id + "\"," +
                "\"containerName\":\"container-a\"," +
                "\"connectionString\":\"conn-xyz\"," +
                "\"fileName\":\"video.mp4\"" +
                "}";
        UploadedVideoInfoDto dto = deserializer.deserialize(TOPIC, json.getBytes(StandardCharsets.UTF_8));
        assertNotNull(dto);
        assertEquals(id, dto.getVideoId());
        assertEquals("container-a", dto.getContainerName());
        assertEquals("conn-xyz", dto.getConnectionString());
        assertEquals("video.mp4", dto.getFileName());
    }

    @Test
    @DisplayName("Deve desserializar mesmo sem o campo id_video (id permanece null)")
    void shouldDeserializeWithoutId() {
        String json = "{" +
                "\"containerName\":\"container-b\"," +
                "\"connectionString\":\"conn-123\"," +
                "\"fileName\":\"file2.mp4\"" +
                "}";
        UploadedVideoInfoDto dto = deserializer.deserialize(TOPIC, json.getBytes(StandardCharsets.UTF_8));
        assertNotNull(dto);
        assertNull(dto.getVideoId());
        assertEquals("container-b", dto.getContainerName());
        assertEquals("conn-123", dto.getConnectionString());
        assertEquals("file2.mp4", dto.getFileName());
    }

    @Test
    @DisplayName("Deve lançar exceção para JSON inválido")
    void shouldThrowForInvalidJson() {
        String invalid = "{\"containerName\":\"x"; // JSON truncado
        assertThrows(RuntimeException.class, () -> deserializer.deserialize(TOPIC, invalid.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Deve ignorar campos extras desconhecidos")
    void shouldIgnoreUnknownFields() {
        String json = "{" +
                "\"id_video\":\"123e4567-e89b-12d3-a456-426614174000\"," +
                "\"containerName\":\"container-c\"," +
                "\"connectionString\":\"conn-abc\"," +
                "\"fileName\":\"video3.mp4\"," +
                "\"extra\":\"ignored\"" +
                "}";
        UploadedVideoInfoDto dto = deserializer.deserialize(TOPIC, json.getBytes(StandardCharsets.UTF_8));
        assertNotNull(dto);
        assertEquals("container-c", dto.getContainerName());
        assertEquals("conn-abc", dto.getConnectionString());
        assertEquals("video3.mp4", dto.getFileName());
    }
}

