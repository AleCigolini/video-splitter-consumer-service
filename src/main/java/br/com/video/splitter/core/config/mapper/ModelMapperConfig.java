package br.com.video.splitter.core.config.mapper;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.modelmapper.ModelMapper;

@Singleton
public class ModelMapperConfig {

    @Produces
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
