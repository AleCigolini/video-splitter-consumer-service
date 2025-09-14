package br.com.video.splitter.application.controller;

import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;

/**
 * Interface for video splitter controller.
 */
public interface VideoSplitterController {
    /**
     * Divide um vídeo em partes menores.
     *
     * @param uploadedVideoInfoDto Informações sobre o vídeo que será dividido.
     */
    void splitVideo(UploadedVideoInfoDto uploadedVideoInfoDto);
}
