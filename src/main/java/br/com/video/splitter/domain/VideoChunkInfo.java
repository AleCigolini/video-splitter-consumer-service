package br.com.video.splitter.domain;

/**
 * Representa metadados de um chunk gerado a partir do vídeo original.
 * Estende VideoInfo para manter compatibilidade com interfaces que esperam VideoInfo,
 * adicionando os campos chunkId e totalChunks.
 */
public class VideoChunkInfo extends VideoInfo {
    private final int chunkPosition;
    private final int totalChunks;

    public VideoChunkInfo(VideoInfo base, int chunkPosition, int totalChunks, String chunkFileName) {
        super(base.getId(), base.getContainerName(), base.getConnectionString(), chunkFileName, base.getUserId());
        this.chunkPosition = chunkPosition;
        this.totalChunks = totalChunks;
    }

    public int getChunkId() {
        return chunkPosition;
    }

    public int getTotalChunks() {
        return totalChunks;
    }
}
