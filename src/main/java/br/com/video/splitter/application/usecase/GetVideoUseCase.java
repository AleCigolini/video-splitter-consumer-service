package br.com.video.splitter.application.usecase;

import br.com.video.splitter.domain.VideoInfo;

import java.io.InputStream;

/**
 * Use case interface para obtenção de vídeos.
 */
public interface GetVideoUseCase {
    /**
     * Obtém o vídeo conforme as informações fornecidas.
     *
     * @param videoInfo Informações do vídeo a ser obtido.
     * @return InputStream do vídeo obtido.
     */
    InputStream getVideo(VideoInfo videoInfo);
}
