package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.TechCatalogEntryDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class TechCatalogService {

    private static final String TECH_CATALOG_RESOURCE = "tech-catalog.json";

    private final ObjectMapper objectMapper;

    public TechCatalogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<TechCatalogEntryDTO> loadCatalog() {
        ClassPathResource resource = new ClassPathResource(TECH_CATALOG_RESOURCE);

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load tech catalog from " + TECH_CATALOG_RESOURCE, exception);
        }
    }
}
