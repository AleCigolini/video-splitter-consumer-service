package br.com.video.splitter.common.interfaces;

import br.com.video.splitter.domain.VideoInfo;

import java.io.InputStream;
import java.util.Optional;

/**
 * Interface para buscar vídeos em um armazenamento.
 */
public interface VideoStorageFetcher {
    /**
     * Busca o vídeo com base nas informações fornecidas.
     *
     * @param videoInfo Informações do vídeo a ser buscado.
     * @return Um Optional contendo um InputStream do vídeo, ou vazio se não encontrado.
     */
    Optional<InputStream> fetch(VideoInfo videoInfo);
}
