package br.com.video.splitter.application.gateway;

import java.util.UUID;

/**
 * Interface para publicar o status do vídeo.
 */
public interface VideoStatusGateway {
    /**
     * Publica o status do vídeo.
     *
     * @param userId  ID do usuário.
     * @param videoId ID do vídeo.
     * @param status  Status do vídeo (e.g., "processando", "concluído", "falha").
     */
    void publishStatus(UUID userId, Long videoId, String status);
}

