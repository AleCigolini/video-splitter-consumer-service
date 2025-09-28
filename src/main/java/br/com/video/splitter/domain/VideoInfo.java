package br.com.video.splitter.domain;

import java.util.UUID;

public class VideoInfo {
    private final String containerName;
    private final String connectionString;
    private final String fileName;
    private final Long videoId;
    private final UUID userId;

    public VideoInfo(Long videoId, String containerName, String connectionString, String fileName, UUID userId) {
        this.videoId = videoId;
        this.containerName = containerName;
        this.connectionString = connectionString;
        this.fileName = fileName;
        this.userId = userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getFileName() {
        return fileName;
    }

    public UUID getUserId() {
        return userId;
    }
}
