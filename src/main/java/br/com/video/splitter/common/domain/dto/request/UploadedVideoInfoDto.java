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
    @JsonProperty("bucket")
    @NotNull
    private BucketDto bucketDto;
}
