package br.com.video.splitter.application.gateway;

import br.com.video.splitter.domain.VideoInfo;

import java.io.InputStream;
import java.util.Optional;

/**
 * Interface para o gateway de vídeo.
 */
public interface VideoGateway {

    /**
     * Obtém o vídeo com base nas informações fornecidas.
     *
     * @param videoInfo Informações do vídeo a ser obtido.
     * @return Um InputStream do vídeo.
     */
    Optional<InputStream> getVideo(VideoInfo videoInfo);

}
