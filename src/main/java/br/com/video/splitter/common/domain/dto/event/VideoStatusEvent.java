package br.com.video.splitter.common.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoStatusEvent {
    private UUID userId;
    private Long videoId;
    private String status;
}
