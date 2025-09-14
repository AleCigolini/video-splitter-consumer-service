package br.com.video.splitter.infrastructure.azure.storage.adapter;

import br.com.video.splitter.common.interfaces.VideoStorageFetcher;
import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.infrastructure.azure.storage.factory.AzureBlobServiceClientFactory;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AzureBlobVideoStorageFetcher implements VideoStorageFetcher {

    @Override
    public Optional<InputStream> fetch(VideoInfo videoInfo) {
        try {
            BlobServiceClient blobServiceClient = AzureBlobServiceClientFactory.getClient(videoInfo.getConnectionString());
            BlobContainerClient containerClient = blobServiceClient
                    .getBlobContainerClient(videoInfo.getContainerName());

            String blobPath = videoInfo.getId() + "/" + videoInfo.getFileName();

            BlobClient blobClient = containerClient.getBlobClient(blobPath);

            if (!blobClient.exists()) {
                return Optional.empty();
            }

            return Optional.of(blobClient.openInputStream());

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
