package br.com.video.splitter.core.config.mapper;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;

class ModelMapperConfigTest {
    @Test
    void shouldProduceModelMapperInstance() {
        ModelMapperConfig config = new ModelMapperConfig();
        ModelMapper mapper = config.modelMapper();
        assertNotNull(mapper);
        assertTrue(mapper instanceof ModelMapper);
    }
}

