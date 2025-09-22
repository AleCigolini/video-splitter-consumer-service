package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoEventGateway;
import br.com.video.splitter.application.usecase.SplitVideoUseCase;
import br.com.video.splitter.common.interfaces.VideoStoragePersister;
import br.com.video.splitter.domain.VideoInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
        String segmentTime = resolveSegmentTime();
        try {
            tempInput = createTempInputFrom(inputStream);
            tempOutputDir = createTempOutputDir();
            executeSegmentation(tempInput, tempOutputDir, segmentTime);
            List<Path> chunkFiles = listChunkFiles(tempOutputDir);
            persistAndPublishChunks(chunkFiles, videoInfo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao dividir e persistir vídeo: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processo interrompido durante a divisão do vídeo: " + e.getMessage(), e);
        } finally {
            safeDelete(tempInput);
            safeDeleteDirectory(tempOutputDir);
        }
    }

    void executeSegmentation(Path tempInput, Path tempOutputDir, String segmentTime) throws IOException, InterruptedException {
        List<String> command = buildFfmpegCommand(tempInput, tempOutputDir, segmentTime);
        runFfmpeg(command);
    }

    String resolveSegmentTime() {
        String prop = System.getProperty("SEGMENT_TIME");
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        String propEnv = System.getProperty("SEGMENT_TIME_ENV");
        if (propEnv != null && !propEnv.isBlank()) {
            return propEnv;
        }
        String segmentTime = System.getenv("SEGMENT_TIME");
        return (segmentTime == null || segmentTime.isBlank()) ? "30" : segmentTime;
    }

    Path createTempInputFrom(InputStream inputStream) throws IOException {
        Path tempInput;
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path baseDir = null;
        try {
            baseDir = Path.of(tmpDir);
        } catch (Exception ignored) {
        }
        try {
            FileAttribute<Set<PosixFilePermission>> attr = buildFilePosixAttr();
            tempInput = (baseDir != null)
                    ? createTempFileWithAttr(baseDir, "video-input-", ".mp4", attr)
                    : createTempFileWithAttr(null, "video-input-", ".mp4", attr);

        } catch (UnsupportedOperationException e) {
            tempInput = (baseDir != null)
                    ? Files.createTempFile(baseDir, "video-input-", ".mp4")
                    : Files.createTempFile("video-input-", ".mp4");
            try {
                applyPosixPermissions(tempInput, "rw-------");
            } catch (UnsupportedOperationException ignored) {
                File f = tempInput.toFile();
                f.setReadable(true, true);
                f.setWritable(true, true);
                f.setExecutable(true, true);
            }
        }
        Files.copy(inputStream, tempInput, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return tempInput;
    }

    Path createTempOutputDir() throws IOException {
        Path tempDir;
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path baseDir = null;
        try {
            baseDir = Path.of(tmpDir);
        } catch (Exception ignored) {
        }
        try {
            FileAttribute<Set<PosixFilePermission>> attr = buildDirPosixAttr();
            tempDir = (baseDir != null)
                    ? createTempDirectoryWithAttr(baseDir, "video-chunks-", attr)
                    : createTempDirectoryWithAttr(null, "video-chunks-", attr);
        } catch (UnsupportedOperationException e) {
            tempDir = (baseDir != null)
                    ? Files.createTempDirectory(baseDir, "video-chunks-")
                    : Files.createTempDirectory("video-chunks-");
            try {
                applyPosixPermissions(tempDir, "rwx------");
            } catch (UnsupportedOperationException ignored) {
                File f = tempDir.toFile();
                f.setReadable(true, true);
                f.setWritable(true, true);
                f.setExecutable(true, true);
            }
        }
        return tempDir;
    }

    List<String> buildFfmpegCommand(Path tempInput, Path tempOutputDir, String segmentTime) {
        String outputPattern = determineOutputPattern(tempOutputDir);
        return List.of(
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
    }

    String determineOutputPattern(Path tempOutputDir) {
        return tempOutputDir.resolve("chunk_%03d.mp4").toString();
    }

    void runFfmpeg(List<String> command) throws IOException, InterruptedException {
        Process process = startProcess(command);
        consumeProcessOutput(process);
        waitForSuccess(process, "FFmpeg");
    }

    Process startProcess(List<String> command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        return pb.start();
    }

    void consumeProcessOutput(Process process) throws IOException {
        try (InputStream is = process.getInputStream()) {
            is.transferTo(OutputStream.nullOutputStream());
        }
    }

    void waitForSuccess(Process process, String context) throws InterruptedException {
        int exit = process.waitFor();
        if (exit != 0) {
            throw new RuntimeException("Falha ao executar " + context + " para segmentação. Código de saída: " + exit);
        }
    }

    List<Path> listChunkFiles(Path tempOutputDir) throws IOException {
        List<Path> chunkFiles = new ArrayList<>();
        try (var stream = Files.list(tempOutputDir)) {
            stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(chunkFiles::add);
        }
        return chunkFiles;
    }

    private void persistAndPublishChunks(List<Path> chunkFiles, VideoInfo videoInfo) throws IOException {
        for (int i = 0; i < chunkFiles.size(); i++) {
            Path chunk = chunkFiles.get(i);
            String chunkFileName = formatChunkFileName(i);
            VideoInfo chunkInfo = buildChunkInfo(videoInfo, chunkFileName);
            persistSingleChunk(chunk, chunkInfo);
            publishChunkEvent(chunkInfo);
        }
    }

    String formatChunkFileName(int index) {
        return String.format("%03d.mp4", index);
    }

    VideoInfo buildChunkInfo(VideoInfo original, String chunkFileName) {
        return new VideoInfo(
                original.getId(),
                original.getContainerName(),
                original.getConnectionString(),
                chunkFileName
        );
    }

    void persistSingleChunk(Path chunk, VideoInfo chunkInfo) throws IOException {
        long length = Files.size(chunk);
        try (InputStream chunkStream = new FileInputStream(chunk.toFile())) {
            persister.save(chunkInfo, chunkStream, length);
        }
    }

    void publishChunkEvent(VideoInfo chunkInfo) {
        eventGateway.publishVideoSplitted(chunkInfo);
    }

    void safeDelete(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    void safeDeleteDirectory(Path dir) {
        if (dir == null) return;
        try {
            if (Files.exists(dir)) {
                try (var walk = Files.walk(dir)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
        } catch (IOException ignored) {
        }
    }

    FileAttribute<Set<PosixFilePermission>> buildFilePosixAttr() {
        return PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
    }

    FileAttribute<Set<PosixFilePermission>> buildDirPosixAttr() {
        return PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
    }

    Path createTempFileWithAttr(Path baseDir, String prefix, String suffix, FileAttribute<Set<PosixFilePermission>> attr) throws IOException {
        return baseDir != null ? Files.createTempFile(baseDir, prefix, suffix, attr) : Files.createTempFile(prefix, suffix, attr);
    }

    Path createTempDirectoryWithAttr(Path baseDir, String prefix, FileAttribute<Set<PosixFilePermission>> attr) throws IOException {
        return baseDir != null ? Files.createTempDirectory(baseDir, prefix, attr) : Files.createTempDirectory(prefix, attr);
    }

    void applyPosixPermissions(Path path, String mode) throws IOException {
        var perms = PosixFilePermissions.fromString(mode);
        Files.setPosixFilePermissions(path, perms);
    }
}
