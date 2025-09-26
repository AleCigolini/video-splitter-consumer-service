package br.com.video.splitter.infrastructure.kafka;

import br.com.video.splitter.domain.VideoChunkInfo; // changed

/**
 * Producer interface for sending video split events to Kafka.
 */
public interface VideoSplittedProducer {
    /**
     * Sends a video split event to Kafka.
     *
     * @param videoChunkInfo Information about the video chunk that has been split.
     */
    void send(VideoChunkInfo videoChunkInfo);
}
