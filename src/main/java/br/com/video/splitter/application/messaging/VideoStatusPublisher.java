package br.com.video.splitter.application.messaging;

import java.util.UUID;

/**
 * Interface para publicar o status do vídeo.
 */
public interface VideoStatusPublisher {
    /**
     * Publica o status do vídeo.
     *
     * @param userId  ID do usuário.
     * @param videoId ID do vídeo.
     * @param status  Status do vídeo (e.g., "processando", "concluído", "falha").
     */
    void publishStatus(UUID userId, Long videoId, String status);
}

