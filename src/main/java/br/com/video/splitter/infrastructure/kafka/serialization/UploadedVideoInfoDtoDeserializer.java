package br.com.video.splitter.infrastructure.kafka.serialization;

import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

/**
 * Deserializer específico para UploadedVideoInfoDto usando Jackson através do suporte do Quarkus.
 * Necessário porque o DTO é recebido diretamente no método @Incoming("video-splitter").
 */
public class UploadedVideoInfoDtoDeserializer extends ObjectMapperDeserializer<UploadedVideoInfoDto> {
    public UploadedVideoInfoDtoDeserializer() {
        super(UploadedVideoInfoDto.class);
    }
}
