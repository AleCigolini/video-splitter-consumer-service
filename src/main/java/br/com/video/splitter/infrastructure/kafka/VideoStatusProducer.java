package br.com.video.splitter.infrastructure.kafka;

import java.util.UUID;

/**
 * Interface para o produtor de status de vídeo.
 */
public interface VideoStatusProducer {
    /**
     * Envia o status do vídeo para o Kafka.
     *
     * @param userId  ID do usuário.
     * @param videoId ID do vídeo.
     * @param status  Status do vídeo.
     */
    void publishStatus(UUID userId, Long videoId, String status);
}