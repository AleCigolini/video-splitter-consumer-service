package br.com.video.splitter.application.usecase;

import br.com.video.splitter.domain.VideoInfo;

/**
 * Use case interface para divisão de vídeos.
 */
public interface SplitVideoUseCase {
    /**
     * Divide o vídeo conforme as informações fornecidas.
     *
     * @param videoInfo Informações do vídeo a ser dividido.
     */
    void splitVideo(VideoInfo videoInfo);
}
