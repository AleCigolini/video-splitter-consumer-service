package br.com.video.splitter.application.messaging;

import java.util.UUID;

public interface VideoStatusPublisher {
    void publishStatus(UUID userId, Long videoId, String status);
}

