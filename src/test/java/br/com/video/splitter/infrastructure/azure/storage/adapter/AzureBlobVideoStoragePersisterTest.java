package br.com.video.splitter.infrastructure.azure.storage.adapter;

import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.infrastructure.azure.storage.factory.AzureBlobServiceClientFactory;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureBlobVideoStoragePersisterTest {
    @Test
    void shouldSaveVideoToAzureBlob() {
        VideoInfo videoInfo = mock(VideoInfo.class);
        InputStream data = mock(InputStream.class);
        long length = 123L;
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);

        when(videoInfo.getConnectionString()).thenReturn("UseDevelopmentStorage=true;");
        when(videoInfo.getContainerName()).thenReturn("container");
        Long id = 1L;
        when(videoInfo.getVideoId()).thenReturn(id);
        when(videoInfo.getFileName()).thenReturn("file.mp4");
        when(blobServiceClient.getBlobContainerClient("container")).thenReturn(containerClient);
        when(containerClient.getBlobClient(id + "/chunks/file.mp4")).thenReturn(blobClient);

        try (MockedStatic<AzureBlobServiceClientFactory> mockedFactory = mockStatic(AzureBlobServiceClientFactory.class)) {
            mockedFactory.when(() -> AzureBlobServiceClientFactory.getClient("UseDevelopmentStorage=true;")).thenReturn(blobServiceClient);
            AzureBlobVideoStoragePersister persister = new AzureBlobVideoStoragePersister();
            persister.save(videoInfo, data, length);
            verify(containerClient).createIfNotExists();
            verify(blobClient).upload(data, length, true);
            verify(blobClient).setHttpHeaders(any(BlobHttpHeaders.class));
        }
    }
}

