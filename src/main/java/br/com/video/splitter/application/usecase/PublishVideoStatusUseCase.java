package br.com.video.splitter.application.usecase;

import java.util.UUID;

/**
 * Interface use case para publicar o status do vídeo.
 */
public interface PublishVideoStatusUseCase {
    /**
     * Publica o status do vídeo.
     *
     * @param userId  ID do usuário.
     * @param videoId ID do vídeo.
     * @param status  Status do vídeo.
     */
    void publishStatus(UUID userId, Long videoId, String status);
}
