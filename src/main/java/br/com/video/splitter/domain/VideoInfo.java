package br.com.video.splitter.domain;

import java.util.UUID;

public class VideoInfo {
    private final UUID id;
    private final String containerName;
    private final String connectionString;
    private final String fileName;
    private final UUID userId;

    public VideoInfo(UUID id, String containerName, String connectionString, String fileName, UUID userId) {
        this.id = id;
        this.containerName = containerName;
        this.connectionString = connectionString;
        this.fileName = fileName;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
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

