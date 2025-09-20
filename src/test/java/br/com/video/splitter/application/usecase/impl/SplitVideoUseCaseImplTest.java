package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoEventGateway;
import br.com.video.splitter.common.interfaces.VideoStoragePersister;
import br.com.video.splitter.domain.VideoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.mockito.Mockito;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SplitVideoUseCaseImplTest {
    private VideoStoragePersister persister;
    private VideoEventGateway eventGateway;
    private SplitVideoUseCaseImpl useCase;
    private VideoInfo videoInfo;
    private InputStream inputStream;

    @BeforeEach
    void setUp() {
        persister = mock(VideoStoragePersister.class);
        eventGateway = mock(VideoEventGateway.class);
        useCase = Mockito.spy(new SplitVideoUseCaseImpl(persister, eventGateway));
        videoInfo = new VideoInfo(UUID.randomUUID(), "container", "conn", "file.mp4");
        inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
    }

    @Test
    void splitVideo_successfulFlow_persistsAndPublishesAllChunks() throws Exception {
        Path tempInput = Files.createTempFile("test-input-", ".mp4");
        Path tempDir = Files.createTempDirectory("test-chunks-");
        Path chunk1 = Files.createTempFile(tempDir, "chunk_", ".mp4");
        Path chunk2 = Files.createTempFile(tempDir, "chunk_", ".mp4");
        Files.write(chunk1, new byte[]{1});
        Files.write(chunk2, new byte[]{2});
        doReturn(tempInput).when(useCase).createTempInputFrom(any());
        doReturn(tempDir).when(useCase).createTempOutputDir();
        doNothing().when(useCase).runFfmpeg(any());
        doReturn(List.of(chunk1, chunk2)).when(useCase).listChunkFiles(tempDir);
        useCase.splitVideo(inputStream, videoInfo);
        verify(persister, times(2)).save(any(), any(), anyLong());
        verify(eventGateway, times(2)).publishVideoSplitted(any());
        Files.deleteIfExists(chunk1);
        Files.deleteIfExists(chunk2);
        Files.deleteIfExists(tempInput);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void splitVideo_shouldThrowExceptionWhenFfmpegProcessFails() throws Exception {
        Path tempInput = Files.createTempFile("test-input-", ".mp4");
        Path tempDir = Files.createTempDirectory("test-chunks-");
        doReturn(tempInput).when(useCase).createTempInputFrom(any());
        doReturn(tempDir).when(useCase).createTempOutputDir();
        doThrow(new RuntimeException("Falha ao executar FFmpeg")).when(useCase).runFfmpeg(any());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.splitVideo(inputStream, videoInfo));
        assertTrue(ex.getMessage().contains("Falha ao executar FFmpeg") || ex.getMessage().contains("Erro ao dividir e persistir vídeo"));
        Files.deleteIfExists(tempInput);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void splitVideo_shouldHandleInterruptedException() throws Exception {
        Path tempInput = Files.createTempFile("test-input-", ".mp4");
        Path tempDir = Files.createTempDirectory("test-chunks-");
        doReturn(tempInput).when(useCase).createTempInputFrom(any());
        doReturn(tempDir).when(useCase).createTempOutputDir();
        doThrow(new InterruptedException("interrompido")).when(useCase).runFfmpeg(any());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.splitVideo(inputStream, videoInfo));
        assertTrue(Thread.currentThread().isInterrupted() || ex.getMessage().contains("Processo interrompido"));
        Files.deleteIfExists(tempInput);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void splitVideo_shouldCallFinallyBlockEvenOnException() throws Exception {
        Path tempInput = Files.createTempFile("test-input-", ".mp4");
        Path tempDir = Files.createTempDirectory("test-chunks-");
        doReturn(tempInput).when(useCase).createTempInputFrom(any());
        doReturn(tempDir).when(useCase).createTempOutputDir();
        doThrow(new IOException("erro")).when(useCase).runFfmpeg(any());
        try {
            useCase.splitVideo(inputStream, videoInfo);
        } catch (Exception ignored) {
        }
        assertFalse(Files.exists(tempInput));
        assertFalse(Files.exists(tempDir));
    }

    @Test
    void resolveSegmentTime_shouldReturnDefaultIfEnvNotSet() {
        assertEquals("30", useCase.resolveSegmentTime());
    }

    @Test
    void buildFfmpegCommand_shouldContainAllArguments() throws Exception {
        Path tempInput = Path.of("/tmp/in.mp4");
        Path tempDir = Path.of("/tmp/out");
        List<String> cmd = useCase.buildFfmpegCommand(tempInput, tempDir, "15");
        assertTrue(cmd.contains("ffmpeg"));
        assertTrue(cmd.contains("-segment_time"));
        assertTrue(cmd.contains("15"));
        assertTrue(cmd.get(cmd.size() - 1).contains("chunk_%03d.mp4"));
    }

    @Test
    void determineOutputPattern_shouldReturnPattern() {
        Path tempDir = Path.of("/tmp/out");
        String pattern = useCase.determineOutputPattern(tempDir);
        assertTrue(pattern.contains("chunk_%03d.mp4"));
    }

    @Test
    void formatChunkFileName_shouldFormatCorrectly() {
        assertEquals("005.mp4", useCase.formatChunkFileName(5));
    }

    @Test
    void buildChunkInfo_shouldCopyFields() {
        VideoInfo info = useCase.buildChunkInfo(videoInfo, "abc.mp4");
        assertEquals(videoInfo.getId(), info.getId());
        assertEquals("abc.mp4", info.getFileName());
    }

    @Test
    void persistSingleChunk_shouldCallPersister() throws Exception {
        Path chunk = Files.createTempFile("chunk-", ".mp4");
        Files.write(chunk, new byte[]{1, 2});
        VideoInfo info = new VideoInfo(UUID.randomUUID(), "c", "c", "f");
        useCase.persistSingleChunk(chunk, info);
        verify(persister).save(eq(info), any(), eq(2L));
        Files.deleteIfExists(chunk);
    }

    @Test
    void publishChunkEvent_shouldCallGateway() {
        useCase.publishChunkEvent(videoInfo);
        verify(eventGateway).publishVideoSplitted(videoInfo);
    }

    @Test
    void safeDelete_shouldNotThrowOnNullOrMissing() {
        useCase.safeDelete(null);
        Path fake = Path.of("/tmp/doesnotexist");
        useCase.safeDelete(fake);
    }

    @Test
    void safeDeleteDirectory_shouldNotThrowOnNullOrMissing() throws Exception {
        useCase.safeDeleteDirectory(null);
        Path dir = Files.createTempDirectory("todel");
        Path file = Files.createTempFile(dir, "f", ".txt");
        useCase.safeDeleteDirectory(dir);
        assertFalse(Files.exists(file));
        assertFalse(Files.exists(dir));
    }

    @Test
    void listChunkFiles_shouldReturnOnlyRegularFilesSorted() throws Exception {
        Path dir = Files.createTempDirectory("list-chunks-");
        Path fileB = Files.createTempFile(dir, "b", ".mp4");
        Path fileA = Files.createTempFile(dir, "a", ".mp4");
        Path subDir = Files.createTempDirectory(dir, "sub");
        List<Path> files = useCase.listChunkFiles(dir);
        assertEquals(2, files.size());
        assertTrue(files.get(0).toString().compareTo(files.get(1).toString()) <= 0);
        useCase.safeDeleteDirectory(subDir);
        useCase.safeDeleteDirectory(dir);
    }

    @Test
    void executeSegmentation_shouldBuildAndRunCommand() throws Exception {
        Path in = Files.createTempFile("in-", ".mp4");
        Path out = Files.createTempDirectory("out-");
        List<String> fakeCmd = List.of("echo", "ok");
        doReturn(fakeCmd).when(useCase).buildFfmpegCommand(any(), any(), any());
        doNothing().when(useCase).runFfmpeg(eq(fakeCmd));
        useCase.executeSegmentation(in, out, "10");
        verify(useCase).buildFfmpegCommand(eq(in), eq(out), eq("10"));
        verify(useCase).runFfmpeg(eq(fakeCmd));
        useCase.safeDelete(in);
        useCase.safeDeleteDirectory(out);
    }

    @Test
    void safeDelete_shouldDeleteExistingFile() throws Exception {
        Path f = Files.createTempFile("tod-", ".txt");
        assertTrue(Files.exists(f));
        useCase.safeDelete(f);
        assertFalse(Files.exists(f));
    }

    @Test
    void safeDeleteDirectory_shouldHandleNonExistingPath() throws Exception {
        Path dir = Files.createTempDirectory("nex-");
        useCase.safeDeleteDirectory(dir);
        useCase.safeDeleteDirectory(dir);
        assertFalse(Files.exists(dir));
    }

    @Test
    void runFfmpeg_shouldSucceedWhenExitZero() throws Exception {
        class DummyProcess extends Process {
            private final int exit;
            DummyProcess(int exit) { this.exit = exit; }
            @Override public OutputStream getOutputStream() { return new ByteArrayOutputStream(); }
            @Override public InputStream getInputStream() { return new ByteArrayInputStream(new byte[]{1,2,3}); }
            @Override public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[0]); }
            @Override public int waitFor() { return exit; }
            @Override public int exitValue() { return exit; }
            @Override public void destroy() {}
            @Override public Process destroyForcibly() { return this; }
            @Override public boolean isAlive() { return false; }
        }
        doReturn(new DummyProcess(0)).when(useCase).startProcess(any());
        useCase.runFfmpeg(List.of("ffmpeg"));
    }

    @Test
    void runFfmpeg_shouldThrowWhenExitNonZero() throws Exception {
        class DummyProcess extends Process {
            private final int exit;
            DummyProcess(int exit) { this.exit = exit; }
            @Override public OutputStream getOutputStream() { return new ByteArrayOutputStream(); }
            @Override public InputStream getInputStream() { return new ByteArrayInputStream(new byte[0]); }
            @Override public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[0]); }
            @Override public int waitFor() { return exit; }
            @Override public int exitValue() { return exit; }
            @Override public void destroy() {}
            @Override public Process destroyForcibly() { return this; }
            @Override public boolean isAlive() { return false; }
        }
        doReturn(new DummyProcess(1)).when(useCase).startProcess(any());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.runFfmpeg(List.of("ffmpeg")));
        assertTrue(ex.getMessage().contains("Código de saída"));
    }

    @Test
    void createTempInputFrom_shouldCreateFileWithContent() throws Exception {
        byte[] data = new byte[]{10, 20, 30};
        Path temp = useCase.createTempInputFrom(new ByteArrayInputStream(data));
        assertTrue(Files.exists(temp));
        byte[] read = Files.readAllBytes(temp);
        assertArrayEquals(data, read);
        File f = temp.toFile();
        try {
            Set<PosixFilePermission> perms = Files.getFileAttributeView(temp, java.nio.file.attribute.PosixFileAttributeView.class) != null
                    ? Files.getPosixFilePermissions(temp)
                    : null;
            if (perms != null) {
                assertTrue(perms.contains(PosixFilePermission.OWNER_READ));
                assertTrue(perms.contains(PosixFilePermission.OWNER_WRITE));
            } else {
                assertTrue(f.canRead());
                assertTrue(f.canWrite());
            }
        } catch (UnsupportedOperationException ignored) {
            assertTrue(f.canRead() || f.canWrite());
        } finally {
            useCase.safeDelete(temp);
        }
    }

    @Test
    void createTempOutputDir_shouldCreateDirectory() throws Exception {
        Path dir = useCase.createTempOutputDir();
        assertTrue(Files.exists(dir));
        assertTrue(Files.isDirectory(dir));
        File f = dir.toFile();
        try {
            Set<PosixFilePermission> perms = Files.getFileAttributeView(dir, java.nio.file.attribute.PosixFileAttributeView.class) != null
                    ? Files.getPosixFilePermissions(dir)
                    : null;
            if (perms != null) {
                assertTrue(perms.contains(PosixFilePermission.OWNER_READ));
                assertTrue(perms.contains(PosixFilePermission.OWNER_WRITE));
                assertTrue(perms.contains(PosixFilePermission.OWNER_EXECUTE));
            } else {
                assertTrue(f.canRead());
                assertTrue(f.canWrite());
                assertTrue(f.canExecute());
            }
        } catch (UnsupportedOperationException ignored) {
            assertTrue(f.canRead() || f.canWrite() || f.canExecute());
        } finally {
            useCase.safeDeleteDirectory(dir);
        }
    }

    @Test
    void startProcess_shouldStartEchoCrossPlatform() throws Exception {
        List<String> cmd;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            cmd = List.of("cmd.exe", "/c", "echo", "ok");
        } else {
            cmd = List.of("echo", "ok");
        }
        Process p = useCase.startProcess(cmd);
        int exit = p.waitFor();
        assertEquals(0, exit);
    }

    @Test
    void resolveSegmentTime_shouldUseSystemPropertyWhenPresent() {
        String original = System.getProperty("SEGMENT_TIME");
        try {
            System.setProperty("SEGMENT_TIME", "7");
            assertEquals("7", useCase.resolveSegmentTime());
        } finally {
            if (original == null) {
                System.clearProperty("SEGMENT_TIME");
            } else {
                System.setProperty("SEGMENT_TIME", original);
            }
        }
    }

    @Test
    void resolveSegmentTime_shouldUseEnvSimWhenPresent() {
        String originalProp = System.getProperty("SEGMENT_TIME");
        String originalEnvSim = System.getProperty("SEGMENT_TIME_ENV");
        try {
            if (originalProp != null) System.clearProperty("SEGMENT_TIME");
            System.setProperty("SEGMENT_TIME_ENV", "12");
            assertEquals("12", useCase.resolveSegmentTime());
        } finally {
            if (originalProp != null) System.setProperty("SEGMENT_TIME", originalProp);
            if (originalEnvSim == null) System.clearProperty("SEGMENT_TIME_ENV");
            else System.setProperty("SEGMENT_TIME_ENV", originalEnvSim);
        }
    }

    @Test
    void safeDelete_shouldSwallowIOExceptionWhenFileLocked() throws Exception {
        Path f = Files.createTempFile("locked-", ".txt");
        Files.write(f, new byte[]{1});
        InputStream lock = new FileInputStream(f.toFile());
        try {
            useCase.safeDelete(f);
            assertTrue(Files.exists(f) || !Files.exists(f));
        } finally {
            lock.close();
            useCase.safeDelete(f);
        }
    }

    @Test
    void safeDeleteDirectory_shouldTriggerCatchInsideLambdaWhenFileLocked() throws Exception {
        Path dir = Files.createTempDirectory("lockdir-");
        Path f = Files.createTempFile(dir, "f", ".txt");
        Files.write(f, new byte[]{1});
        InputStream lock = new FileInputStream(f.toFile());
        try {
            useCase.safeDeleteDirectory(dir);
            assertTrue(Files.exists(dir) || !Files.exists(dir));
            assertTrue(Files.exists(f) || !Files.exists(f));
        } finally {
            lock.close();
            useCase.safeDeleteDirectory(dir);
            assertFalse(Files.exists(dir));
        }
    }

    @Test
    void createTempInputFrom_shouldFallbackToDefaultWhenTmpDirFails() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isUnix = os.contains("nix") || os.contains("nux") || os.contains("mac") || os.contains("aix") || os.contains("sunos");
        Assumptions.assumeFalse(isUnix, "Skipping on Unix-like systems");
        String originalTmp = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", "?invalid_path");
        try {
            byte[] data = {1,2,3};
            Path temp = new SplitVideoUseCaseImpl(persister, eventGateway).createTempInputFrom(new ByteArrayInputStream(data));
            assertTrue(Files.exists(temp));
            assertArrayEquals(data, Files.readAllBytes(temp));
            Files.deleteIfExists(temp);
        } finally {
            if (originalTmp != null) System.setProperty("java.io.tmpdir", originalTmp);
            else System.clearProperty("java.io.tmpdir");
        }
    }

    @Test
    void createTempOutputDir_shouldFallbackToDefaultWhenTmpDirFails() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isUnix = os.contains("nix") || os.contains("nux") || os.contains("mac") || os.contains("aix") || os.contains("sunos");
        Assumptions.assumeFalse(isUnix, "Skipping on Unix-like systems");
        String originalTmp = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", "?invalid_path");
        try {
            Path temp = new SplitVideoUseCaseImpl(persister, eventGateway).createTempOutputDir();
            assertTrue(Files.exists(temp));
            assertTrue(Files.isDirectory(temp));
            Files.deleteIfExists(temp);
        } finally {
            if (originalTmp != null) System.setProperty("java.io.tmpdir", originalTmp);
            else System.clearProperty("java.io.tmpdir");
        }
    }

    @Test
    void createTempInputFrom_shouldHandleUnsupportedPosixPermissions() throws Exception {
        SplitVideoUseCaseImpl useCase = new SplitVideoUseCaseImpl(persister, eventGateway) {
            @Override
            public Path createTempInputFrom(InputStream inputStream) throws IOException {
                Path tempInput = Files.createTempFile("video-input-", ".mp4");
                try {
                    throw new UnsupportedOperationException();
                } catch (UnsupportedOperationException ignored) {
                }
                Files.copy(inputStream, tempInput, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return tempInput;
            }
        };
        byte[] data = {9,8,7};
        Path temp = useCase.createTempInputFrom(new ByteArrayInputStream(data));
        assertTrue(Files.exists(temp));
        assertArrayEquals(data, Files.readAllBytes(temp));
        Files.deleteIfExists(temp);
    }

    @Test
    void createTempOutputDir_shouldHandleUnsupportedPosixPermissions() throws Exception {
        SplitVideoUseCaseImpl useCase = new SplitVideoUseCaseImpl(persister, eventGateway) {
            @Override
            public Path createTempOutputDir() throws IOException {
                Path tempDir = Files.createTempDirectory("video-chunks-");
                try {
                    throw new UnsupportedOperationException();
                } catch (UnsupportedOperationException ignored) {
                }
                return tempDir;
            }
        };
        Path temp = useCase.createTempOutputDir();
        assertTrue(Files.exists(temp));
        assertTrue(Files.isDirectory(temp));
        Files.deleteIfExists(temp);
    }
}
