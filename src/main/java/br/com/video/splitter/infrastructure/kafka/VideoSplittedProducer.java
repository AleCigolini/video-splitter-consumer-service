package br.com.video.splitter.infrastructure.kafka;

import br.com.video.splitter.domain.VideoInfo;

/**
 * Producer interface for sending video split events to Kafka.
 */
public interface VideoSplittedProducer {
    /**
     * Sends a video split event to Kafka.
     *
     * @param videoInfo Information about the video that has been split.
     */
    void send(VideoInfo videoInfo);
}

