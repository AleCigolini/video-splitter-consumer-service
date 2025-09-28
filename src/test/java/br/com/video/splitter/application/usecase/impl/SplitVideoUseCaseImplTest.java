package br.com.video.splitter.application.usecase.impl;

import br.com.video.splitter.application.gateway.VideoEventGateway;
import br.com.video.splitter.common.interfaces.VideoStoragePersister;
import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.domain.VideoChunkInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
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
        videoInfo = new VideoInfo(1L, "container", "conn", "file.mp4", UUID.randomUUID());
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
    void buildFfmpegCommand_shouldContainAllArguments() {
        Path tempInput = Path.of("/tmp/in.mp4");
        Path tempDir = Path.of("/tmp/out");
        List<String> cmd = useCase.buildFfmpegCommand(tempInput, tempDir, "15");
        assertTrue(cmd.contains("ffmpeg"));
        assertTrue(cmd.contains("-segment_time"));
        assertTrue(cmd.contains("15"));
        assertTrue(cmd.getLast().contains("chunk_%03d.mp4"));
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
        VideoChunkInfo info = useCase.buildChunkInfo(videoInfo, "abc.mp4", 2, 10);
        assertEquals(videoInfo.getVideoId(), info.getVideoId());
        assertEquals("abc.mp4", info.getFileName());
        assertEquals(2, info.getChunkId());
        assertEquals(10, info.getTotalChunks());
    }

    @Test
    void persistSingleChunk_shouldCallPersister() throws Exception {
        Path chunk = Files.createTempFile("chunk-", ".mp4");
        Files.write(chunk, new byte[]{1, 2});
        VideoInfo info = new VideoInfo(1L, "c", "c", "f", UUID.randomUUID());
        useCase.persistSingleChunk(chunk, info);
        verify(persister).save(eq(info), any(), eq(2L));
        Files.deleteIfExists(chunk);
    }

    @Test
    void publishChunkEvent_shouldCallGateway() {
        VideoChunkInfo chunkInfo = new VideoChunkInfo(videoInfo, 0, 2, "000.mp4");
        useCase.publishChunkEvent(chunkInfo);
        verify(eventGateway).publishVideoSplitted(chunkInfo);
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
        Files.createTempFile(dir, "b", ".mp4");
        Files.createTempFile(dir, "a", ".mp4");
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
    @EnabledOnOs(OS.LINUX)
    void createTempInputFrom_onLinux_shouldCreateFileWithOwnerRW() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4};
        Path temp = useCase.createTempInputFrom(new ByteArrayInputStream(data));
        try {
            assertTrue(Files.exists(temp));
            assertArrayEquals(data, Files.readAllBytes(temp));
            try {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(temp);
                assertTrue(perms.contains(PosixFilePermission.OWNER_READ));
                assertTrue(perms.contains(PosixFilePermission.OWNER_WRITE));
            } catch (UnsupportedOperationException ignored) {
            }
        } finally {
            useCase.safeDelete(temp);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createTempOutputDir_onLinux_shouldCreateDirWithOwnerRWX() throws Exception {
        Path dir = useCase.createTempOutputDir();
        try {
            assertTrue(Files.exists(dir));
            assertTrue(Files.isDirectory(dir));
            try {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(dir);
                assertTrue(perms.contains(PosixFilePermission.OWNER_READ));
                assertTrue(perms.contains(PosixFilePermission.OWNER_WRITE));
                assertTrue(perms.contains(PosixFilePermission.OWNER_EXECUTE));
            } catch (UnsupportedOperationException ignored) {
            }
        } finally {
            useCase.safeDeleteDirectory(dir);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createTempInputFrom_onLinux_shouldFallbackWhenAttrBuilderThrows() throws Exception {
        SplitVideoUseCaseImpl spyUseCase = Mockito.spy(new SplitVideoUseCaseImpl(persister, eventGateway));
        doThrow(new UnsupportedOperationException("attr builder UOE")).when(spyUseCase).buildFilePosixAttr();
        byte[] data = {7,7,7};
        Path temp = spyUseCase.createTempInputFrom(new ByteArrayInputStream(data));
        try {
            assertTrue(Files.exists(temp));
            assertArrayEquals(data, Files.readAllBytes(temp));
        } finally {
            spyUseCase.safeDelete(temp);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createTempInputFrom_onLinux_shouldFallbackWhenCreateWithAttrThrows() throws Exception {
        SplitVideoUseCaseImpl spyUseCase = Mockito.spy(new SplitVideoUseCaseImpl(persister, eventGateway));
        doThrow(new UnsupportedOperationException("create with attr UOE")).when(spyUseCase)
                .createTempFileWithAttr(any(), anyString(), anyString(), any());
        byte[] data = {8,8,8};
        Path temp = spyUseCase.createTempInputFrom(new ByteArrayInputStream(data));
        try {
            assertTrue(Files.exists(temp));
            assertArrayEquals(data, Files.readAllBytes(temp));
        } finally {
            spyUseCase.safeDelete(temp);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createTempInputFrom_onLinux_shouldFallbackToFileFlagsWhenPosixSetThrows() throws Exception {
        SplitVideoUseCaseImpl spyUseCase = Mockito.spy(new SplitVideoUseCaseImpl(persister, eventGateway));
        doThrow(new UnsupportedOperationException("attr builder UOE")).when(spyUseCase).buildFilePosixAttr();
        doThrow(new UnsupportedOperationException("apply posix UOE")).when(spyUseCase)
                .applyPosixPermissions(any(Path.class), anyString());
        byte[] data = {9,9,9};
        Path temp = spyUseCase.createTempInputFrom(new ByteArrayInputStream(data));
        try {
            assertTrue(Files.exists(temp));
            assertArrayEquals(data, Files.readAllBytes(temp));
            File f = temp.toFile();
            assertTrue(f.canRead() || f.canWrite());
        } finally {
            spyUseCase.safeDelete(temp);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createTempOutputDir_onLinux_shouldFallbackWhenAttrBuilderThrows() throws Exception {
        SplitVideoUseCaseImpl spyUseCase = Mockito.spy(new SplitVideoUseCaseImpl(persister, eventGateway));
        doThrow(new UnsupportedOperationException("dir attr builder UOE")).when(spyUseCase).buildDirPosixAttr();
        Path dir = spyUseCase.createTempOutputDir();
        try {
            assertTrue(Files.exists(dir));
            assertTrue(Files.isDirectory(dir));
        } finally {
            spyUseCase.safeDeleteDirectory(dir);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createTempOutputDir_onLinux_shouldFallbackWhenCreateWithAttrThrows() throws Exception {
        SplitVideoUseCaseImpl spyUseCase = Mockito.spy(new SplitVideoUseCaseImpl(persister, eventGateway));
        doThrow(new UnsupportedOperationException("create dir with attr UOE")).when(spyUseCase)
                .createTempDirectoryWithAttr(any(), anyString(), any());
        Path dir = spyUseCase.createTempOutputDir();
        try {
            assertTrue(Files.exists(dir));
            assertTrue(Files.isDirectory(dir));
        } finally {
            spyUseCase.safeDeleteDirectory(dir);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void createTempOutputDir_onLinux_shouldFallbackToFileFlagsWhenPosixSetThrows() throws Exception {
        SplitVideoUseCaseImpl spyUseCase = Mockito.spy(new SplitVideoUseCaseImpl(persister, eventGateway));
        doThrow(new UnsupportedOperationException("dir attr builder UOE")).when(spyUseCase).buildDirPosixAttr();
        doThrow(new UnsupportedOperationException("apply dir posix UOE")).when(spyUseCase)
                .applyPosixPermissions(any(Path.class), anyString());
        Path dir = spyUseCase.createTempOutputDir();
        try {
            assertTrue(Files.exists(dir));
            assertTrue(Files.isDirectory(dir));
            File f = dir.toFile();
            assertTrue(f.canRead() || f.canWrite() || f.canExecute());
        } finally {
            spyUseCase.safeDeleteDirectory(dir);
        }
    }
}
