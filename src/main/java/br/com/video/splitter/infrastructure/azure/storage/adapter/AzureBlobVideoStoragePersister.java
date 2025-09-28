package br.com.video.splitter.infrastructure.azure.storage.adapter;

import br.com.video.splitter.common.interfaces.VideoStoragePersister;
import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.infrastructure.azure.storage.factory.AzureBlobServiceClientFactory;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;

@ApplicationScoped
public class AzureBlobVideoStoragePersister implements VideoStoragePersister {
    @Override
    public void save(VideoInfo videoInfo, InputStream data, long length) {
        BlobServiceClient blobServiceClient = AzureBlobServiceClientFactory.getClient(videoInfo.getConnectionString());
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(videoInfo.getContainerName());
        containerClient.createIfNotExists();

        String blobPath = videoInfo.getVideoId() + "/chunks/" + videoInfo.getFileName();

        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        blobClient.upload(data, length, true);
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType("video/mp4"));
    }
}
