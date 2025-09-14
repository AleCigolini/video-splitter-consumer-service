package br.com.video.splitter.presentation.kafka;

import br.com.video.splitter.common.domain.dto.request.UploadedVideoInfoDto;

/**
 * Interface para o consumidor do tópico de vídeos enviados.
 * <p>
 * Este serviço recebe informações sobre vídeos que foram enviados (upload concluído)
 * e executa a lógica de quebra/divisão do vídeo em partes menores para processamento posterior.
 * </p>
 *
 * <p>
 * Implementações desta interface devem consumir mensagens contendo os dados do vídeo enviado,
 * representados por {@link UploadedVideoInfoDto}.
 * </p>
 */
public interface VideoSplitterConsumer {
    /**
     * Consome uma mensagem referente a um vídeo enviado e executa a quebra do vídeo.
     *
     * @param uploadedVideoInfoDto informações do vídeo enviado para processamento
     */
    void consume(UploadedVideoInfoDto uploadedVideoInfoDto);
}
