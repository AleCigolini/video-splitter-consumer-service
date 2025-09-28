package br.com.video.splitter.application.usecase;

import java.util.UUID;

public interface PublishVideoStatusUseCase {
    void publishStatus(UUID userId, Long videoId, String status);
}
