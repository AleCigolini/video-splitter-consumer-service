package br.com.video.splitter.infrastructure.kafka.impl;

import br.com.video.splitter.domain.VideoInfo;
import br.com.video.splitter.infrastructure.kafka.VideoSplittedProducer;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaVideoSplittedProducer implements VideoSplittedProducer {

    @Channel("video-splitted")
    Emitter<String> emitter;

    @Override
    public void send(VideoInfo videoInfo) {
        String payload = toJson(videoInfo);
        emitter.send(payload);
    }

    private String toJson(VideoInfo v) {
        StringBuilder sb = new StringBuilder();
        sb.append('{')
          .append("\"id\":\"").append(v.getId()).append('\"')
          .append(',')
          .append("\"containerName\":\"").append(escape(v.getContainerName())).append('\"')
          .append(',')
          .append("\"connectionString\":\"").append(escape(v.getConnectionString())).append('\"')
          .append(',')
          .append("\"fileName\":\"").append(escape(v.getFileName())).append('\"')
          .append('}');
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

