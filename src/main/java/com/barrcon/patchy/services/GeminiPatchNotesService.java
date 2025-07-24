package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.ProcessedPatchNotesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GeminiPatchNotesService {

    @Autowired
    private WebClient geminiWebClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public ProcessedPatchNotesDTO processPatchNotes(String content, String version, String title) {
        try {
            // For now, return mock data until you implement full Gemini API integration
            List<String> bulletPoints = Arrays.asList(
                    "• " + content.substring(0, Math.min(50, content.length())) + "...",
                    "• Version: " + version,
                    "• Processed with AI"
            );

            return new ProcessedPatchNotesDTO(version, title, bulletPoints);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process patch notes: " + e.getMessage(), e);
        }
    }
}