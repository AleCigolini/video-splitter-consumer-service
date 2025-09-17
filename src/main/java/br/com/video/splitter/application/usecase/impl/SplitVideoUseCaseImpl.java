package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.usecase.SplitVideoUseCase;
import br.com.video.splitter.common.interfaces.VideoStoragePersister;
import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.application.gateway.VideoEventGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class SplitVideoUseCaseImpl implements SplitVideoUseCase {

    private final VideoStoragePersister persister;
    private final VideoEventGateway eventGateway;

    @Inject
    public SplitVideoUseCaseImpl(VideoStoragePersister persister, VideoEventGateway eventGateway) {
        this.persister = persister;
        this.eventGateway = eventGateway;
    }

    @Override
    public void splitVideo(InputStream inputStream, VideoInfo videoInfo) {
        Path tempInput = null;
        Path tempOutputDir = null;
        String segmentTime = System.getenv("SEGMENT_TIME");
        if (segmentTime == null || segmentTime.isBlank()) {
            segmentTime = "30";
        }
        try {
            tempInput = Files.createTempFile("video-input-", ".mp4");
            Files.copy(inputStream, tempInput, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            tempOutputDir = Files.createTempDirectory("video-chunks-");
            String outputPattern = tempOutputDir.resolve("chunk_%03d.mp4").toString();

            List<String> command = List.of(
                    "ffmpeg",
                    "-hide_banner",
                    "-loglevel", "error",
                    "-i", tempInput.toString(),
                    "-map", "0",
                    "-c", "copy",
                    "-f", "segment",
                    "-segment_time", segmentTime,
                    "-reset_timestamps", "1",
                    outputPattern
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (InputStream is = process.getInputStream()) {
                is.transferTo(OutputStream.nullOutputStream());
            }
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("Falha ao executar FFmpeg para segmentação. Código de saída: " + exit);
            }

            List<Path> chunkFiles = new ArrayList<>();
            try (var stream = Files.list(tempOutputDir)) {
                stream.filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::toString))
                        .forEach(chunkFiles::add);
            }

            for (int i = 0; i < chunkFiles.size(); i++) {
                Path chunk = chunkFiles.get(i);
                String chunkFileName = String.format("%03d.mp4", i);
                long length = Files.size(chunk);
                VideoInfo chunkInfo = new VideoInfo(
                        videoInfo.getId(),
                        videoInfo.getContainerName(),
                        videoInfo.getConnectionString(),
                        chunkFileName
                );
                try (InputStream chunkStream = new FileInputStream(chunk.toFile())) {
                    persister.save(chunkInfo, chunkStream, length);
                }
                eventGateway.publishVideoSplitted(chunkInfo);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao dividir e persistir vídeo: " + e.getMessage(), e);
        } finally {
            safeDelete(tempInput);
            safeDeleteDirectory(tempOutputDir);
        }
    }

    private void safeDelete(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private void safeDeleteDirectory(Path dir) {
        if (dir == null) return;
        try {
            if (Files.exists(dir)) {
                try (var walk = Files.walk(dir)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
                }
            }
        } catch (IOException ignored) {
        }
    }
}
