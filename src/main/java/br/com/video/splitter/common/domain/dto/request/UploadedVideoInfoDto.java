package br.com.video.splitter.common.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UploadedVideoInfoDto {
    @NotBlank
    @NotNull
    @JsonProperty("id_video")
    private UUID id;
    @NotBlank
    @NotNull
    private final String containerName;
    @NotBlank
    @NotNull
    private final String connectionString;
    @NotBlank
    @NotNull
    private final String fileName;
}
