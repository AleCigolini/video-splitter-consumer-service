package br.com.video.splitter.domain;

import java.util.UUID;

public class VideoInfo {
    private final UUID id;
    private final String containerName;
    private final String connectionString;
    private final String fileName;

    public VideoInfo(UUID id, String containerName, String connectionString, String fileName) {
        this.id = id;
        this.containerName = containerName;
        this.connectionString = connectionString;
        this.fileName = fileName;
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
}
