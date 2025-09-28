package br.com.video.splitter.common.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UploadedVideoInfoDto {
    @NotNull
    private Long videoId;
    @NotBlank
    private UUID userId;
    @NotBlank
    @NotNull
    private String containerName;
    @NotBlank
    @NotNull
    private String connectionString;
    @NotBlank
    @NotNull
    private String fileName;

    public UploadedVideoInfoDto() {
    }

    public UploadedVideoInfoDto(String containerName, String connectionString, String fileName, UUID userId) {
        this.containerName = containerName;
        this.connectionString = connectionString;
        this.fileName = fileName;
        this.userId = userId;
    }
}
