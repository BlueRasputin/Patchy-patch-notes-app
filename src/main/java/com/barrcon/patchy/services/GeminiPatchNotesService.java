package com.barrcon.patchy.services;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.barrcon.patchy.dto.ProcessedPatchNotesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiPatchNotesService {

    Dotenv dotenv = Dotenv.configure().directory("src/main/resources").filename("app.env").ignoreIfMissing().load();
    String apiKey = dotenv.get("GOOGLE_API_KEY", System.getenv("GOOGLE_API_KEY"));
    Client client = Client.builder()
            .apiKey(apiKey)
            .build();
    public ProcessedPatchNotesDTO processPatchNotes(String version, String title) {
        try {
            String query = "Search for the latest patch notes for" + title + version + "and return a concise bulleted list. If unavailable, provide a brief list (<100 chars) of the newest version found.";
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-pro", query, null);
            String description = response.text();
            System.out.println("description = " + description);
            return new ProcessedPatchNotesDTO(version, title, description);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process patch notes: " + e.getMessage(), e);
        }
    }
}