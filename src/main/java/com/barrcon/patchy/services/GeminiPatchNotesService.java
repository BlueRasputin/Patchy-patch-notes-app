package com.barrcon.patchy.services;


import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.cloud.ai.generativeai.GenerativeModel;
import com.google.cloud.ai.generativeai.GenerateContentResponse;
import com.barrcon.patchy.dto.ProcessedPatchNotesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiPatchNotesService {
    private final GenerativeModel model;

    @Autowired
    public GeminiPatchNotesService(GenerativeModel model) {
        this.model = model;
    }

    public ProcessedPatchNotesDTO processPatchNotes(String content, String version, String title) {
        try {
            String prompt = String.format(
                    "Convert these patch notes into bullet points:\n%s",
                    content
            );


            GenerateContentResponse response = model.generateContent(prompt);
            String text = response.getCandidates().get(0)
                    .getContent().getParts().get(0)
                    .getText();

            List<String> bulletPoints = Arrays.stream(text.split("\n"))
                    .map(line -> line.replaceFirst("^[•\\-*]\\s*", "").trim())
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            return new ProcessedPatchNotesDTO(version, title, bulletPoints);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process patch notes: " + e.getMessage());
        }
    }
}
