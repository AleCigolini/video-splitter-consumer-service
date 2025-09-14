package br.com.video.splitter.common.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BucketDto {
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
