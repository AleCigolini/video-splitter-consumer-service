package br.com.video.splitter.application.gateway;

import br.com.video.splitter.domain.VideoInfo;

/**
 * Gateway interface for publishing video-related events.
 */
public interface VideoEventGateway {
    /**
     * Publishes an event indicating that a video has been split.
     *
     * @param videoInfo Information about the video that has been split.
     */
    void publishVideoSplitted(VideoInfo videoInfo);
}

