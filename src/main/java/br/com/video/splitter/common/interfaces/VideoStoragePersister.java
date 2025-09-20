package br.com.video.splitter.common.interfaces;

import br.com.video.splitter.domain.VideoInfo;

import java.io.InputStream;

/**
 * Interface para persistir (salvar) vídeos/fragmentos em um armazenamento.
 */
public interface VideoStoragePersister {
    /**
     * Salva um conteúdo binário (ex.: fragmento de vídeo) no storage.
     *
     * @param videoInfo Informações do storage alvo (container, connectionString, id, etc.).
     * @param data     Stream do conteúdo a ser salvo.
     * @param length   Tamanho do conteúdo em bytes.
     */
    void save(VideoInfo videoInfo, InputStream data, long length);
}

