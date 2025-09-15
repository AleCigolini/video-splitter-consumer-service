package br.com.video.splitter.application.usecase;

import br.com.video.splitter.domain.VideoInfo;

import java.io.InputStream;

/**
 * Use case interface para divisão de vídeos.
 */
public interface SplitVideoUseCase {
    /**
     * Divide o vídeo conforme as informações fornecidas.
     *
     * @param inputStream Stream de entrada do vídeo a ser dividido.
     * @param videoInfo Informações do vídeo a ser dividido.
     */
    void splitVideo(InputStream inputStream, VideoInfo videoInfo);
}
