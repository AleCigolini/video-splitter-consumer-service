package br.com.video.splitter.infrastructure.azure.storage.factory;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe de factory para criar e gerenciar instâncias de BlobServiceClient.
 */
public class AzureBlobServiceClientFactory {
    private static final ConcurrentHashMap<String, BlobServiceClient> CLIENT_CACHE = new ConcurrentHashMap<>();

    /**
     * Obtém uma instância de BlobServiceClient para a connection string fornecida.
     * Se uma instância já existir no cache, ela será reutilizada.
     *
     * @param connectionString A connection string do Azure Blob Storage.
     * @return Uma instância de BlobServiceClient.
     */
    public static BlobServiceClient getClient(String connectionString) {
        return CLIENT_CACHE.computeIfAbsent(
                connectionString,
                connStr -> new BlobServiceClientBuilder().connectionString(connStr).buildClient()
        );
    }
}
