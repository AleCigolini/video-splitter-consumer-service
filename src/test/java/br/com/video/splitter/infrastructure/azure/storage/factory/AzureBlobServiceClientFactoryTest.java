package br.com.video.splitter.infrastructure.azure.storage.factory;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureBlobServiceClientFactoryTest {
    @AfterEach
    void clearCache() {
        var cacheField = AzureBlobServiceClientFactory.class.getDeclaredFields()[0];
        cacheField.setAccessible(true);
        try {
            ((ConcurrentHashMap<?, ?>) cacheField.get(null)).clear();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnSameClientForSameConnectionString() {
        try (MockedConstruction<BlobServiceClientBuilder> mockedBuilder = mockConstruction(BlobServiceClientBuilder.class, (builder, context) -> {
            BlobServiceClient client = mock(BlobServiceClient.class);
            when(builder.connectionString(anyString())).thenReturn(builder);
            when(builder.buildClient()).thenReturn(client);
        })) {
            String connStr = "test-conn";
            BlobServiceClient client1 = AzureBlobServiceClientFactory.getClient(connStr);
            BlobServiceClient client2 = AzureBlobServiceClientFactory.getClient(connStr);
            assertSame(client1, client2);
        }
    }

    @Test
    void shouldReturnDifferentClientsForDifferentConnectionStrings() {
        try (MockedConstruction<BlobServiceClientBuilder> mockedBuilder = mockConstruction(BlobServiceClientBuilder.class, (builder, context) -> {
            BlobServiceClient client = mock(BlobServiceClient.class);
            when(builder.connectionString(anyString())).thenReturn(builder);
            when(builder.buildClient()).thenReturn(client);
        })) {
            String connStr1 = "test-conn-1";
            String connStr2 = "test-conn-2";
            BlobServiceClient client1 = AzureBlobServiceClientFactory.getClient(connStr1);
            BlobServiceClient client2 = AzureBlobServiceClientFactory.getClient(connStr2);
            assertNotSame(client1, client2);
        }
    }
}

