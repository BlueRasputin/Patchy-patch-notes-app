package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.LivePatchNoteDTO;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

    @Service
    public class GeminiLivePatchNotesService {

        private final Client client;


        public GeminiLivePatchNotesService() {
            Dotenv dotenv = Dotenv.configure().directory("src/main/resources").filename("app.env").ignoreIfMissing().load();

            String apiKey = dotenv.get("GOOGLE_API_KEY", System.getenv("GOOGLE_API_KEY"));
            this.client = Client.builder()
                    .apiKey(apiKey)
                    .build();
        }

        public LivePatchNoteDTO fetchLivePatchNotes(String techName) {
            try { //takes in array of user favorites and generates a patch note for each one
                String query = "Search for the latest patch notes for" + techName + "and return a concise bulleted list. If unavailable, provide a brief list of important changes within the newest version found. The description field must be under 255 characters.";
                GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", query, null);
                String description = response.text();
                System.out.println("description = " + description);
                return new LivePatchNoteDTO(techName, description);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch patch notes: " + e.getMessage(), e);
            }
        }
    }