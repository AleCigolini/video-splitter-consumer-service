package br.com.video.splitter.infrastructure.azure.storage.adapter;

import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.infrastructure.azure.storage.factory.AzureBlobServiceClientFactory;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureBlobVideoStorageFetcherTest {
    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;
    private BlobClient blobClient;
    private VideoInfo videoInfo;
    private AzureBlobVideoStorageFetcher fetcher;

    @BeforeEach
    void setUp() {
        blobServiceClient = mock(BlobServiceClient.class);
        containerClient = mock(BlobContainerClient.class);
        blobClient = mock(BlobClient.class);
        videoInfo = mock(VideoInfo.class);
        fetcher = new AzureBlobVideoStorageFetcher();
    }

    @Test
    void shouldReturnInputStreamWhenBlobExists() {
        try (MockedStatic<AzureBlobServiceClientFactory> mockedFactory = mockStatic(AzureBlobServiceClientFactory.class)) {
            when(videoInfo.getConnectionString()).thenReturn("UseDevelopmentStorage=true;");
            when(videoInfo.getContainerName()).thenReturn("container");
            when(videoInfo.getId()).thenReturn(UUID.randomUUID());
            when(videoInfo.getFileName()).thenReturn("file.mp4");
            mockedFactory.when(() -> AzureBlobServiceClientFactory.getClient(anyString())).thenReturn(blobServiceClient);
            when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(containerClient);
            when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
            when(blobClient.exists()).thenReturn(true);
            BlobInputStream is = mock(BlobInputStream.class);
            when(blobClient.openInputStream()).thenReturn(is);

            Optional<? extends java.io.InputStream> result = fetcher.fetch(videoInfo);
            assertTrue(result.isPresent());
            assertEquals(is, result.get());
        }
    }

    @Test
    void shouldReturnEmptyWhenBlobDoesNotExist() {
        try (MockedStatic<AzureBlobServiceClientFactory> mockedFactory = mockStatic(AzureBlobServiceClientFactory.class)) {
            when(videoInfo.getConnectionString()).thenReturn("UseDevelopmentStorage=true;");
            when(videoInfo.getContainerName()).thenReturn("container");
            when(videoInfo.getId()).thenReturn(UUID.randomUUID());
            when(videoInfo.getFileName()).thenReturn("file.mp4");
            mockedFactory.when(() -> AzureBlobServiceClientFactory.getClient(anyString())).thenReturn(blobServiceClient);
            when(blobServiceClient.getBlobContainerClient(anyString())).thenReturn(containerClient);
            when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
            when(blobClient.exists()).thenReturn(false);

            Optional<? extends java.io.InputStream> result = fetcher.fetch(videoInfo);
            assertFalse(result.isPresent());
        }
    }

    @Test
    void shouldReturnEmptyWhenExceptionIsThrown() {
        try (MockedStatic<AzureBlobServiceClientFactory> mockedFactory = mockStatic(AzureBlobServiceClientFactory.class)) {
            when(videoInfo.getConnectionString()).thenReturn("UseDevelopmentStorage=true;");
            mockedFactory.when(() -> AzureBlobServiceClientFactory.getClient(anyString())).thenThrow(new RuntimeException("fail"));
            Optional<? extends java.io.InputStream> result = fetcher.fetch(videoInfo);
            assertFalse(result.isPresent());
        }
    }
}
