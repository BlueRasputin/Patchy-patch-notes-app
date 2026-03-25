package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PatchNoteSectionDTO;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SummaryService {

    private final String claudeApiKey;
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    public SummaryService() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .filename("app.env")
                .ignoreIfMissing()
                .load();
        this.claudeApiKey = dotenv.get("claude.api.key");
    }
    //Claude API call to generate summaries for patch notes
    public SummaryResult generateSummary(String content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(CLAUDE_API_URL);

            request.setHeader("x-api-key", claudeApiKey);
            request.setHeader("anthropic-version", "2023-06-01");
            request.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "claude-sonnet-4-20250514");
            requestBody.put("max_tokens", 1024);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", """
                    Summarize these patch notes and return only valid JSON with this exact shape:
                    {
                      "summary": "short markdown summary",
                      "sections": [
                        { "category": "New Features", "content": "markdown bullets for that section" }
                      ]
                    }

                    Rules:
                    - Use only these category names when relevant: New Features, Bug Fixes, Breaking Changes, Security, Deprecations, Performance, Known Issues, Documentation.
                    - Omit categories that are not present.
                    - Keep section content concise and useful.
                    - Return JSON only, no code fences or extra text.

                    Patch notes:
                    """ + content);
            messages.put(message);
            requestBody.put("messages", messages);

            request.setEntity(new StringEntity(requestBody.toString()));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);
                String responseText = jsonResponse.getJSONArray("content")
                        .getJSONObject(0)
                        .getString("text");

                return parseSummaryResult(responseText);
            }

        } catch (Exception e) {
            throw new RuntimeException("Claude API call failed", e);
        }
    }

    private SummaryResult parseSummaryResult(String responseText) {
        try {
            String normalized = responseText.strip();
            if (normalized.startsWith("```")) {
                int firstBrace = normalized.indexOf('{');
                int lastBrace = normalized.lastIndexOf('}');
                normalized = normalized.substring(firstBrace, lastBrace + 1);
            }

            JSONObject summaryJson = new JSONObject(normalized);
            String summary = summaryJson.optString("summary");
            JSONArray sectionsJson = summaryJson.optJSONArray("sections");
            List<PatchNoteSectionDTO> sections = new ArrayList<>();

            if (sectionsJson != null) {
                for (int index = 0; index < sectionsJson.length(); index++) {
                    JSONObject sectionObject = sectionsJson.getJSONObject(index);
                    String category = sectionObject.optString("category");
                    String sectionContent = sectionObject.optString("content");

                    if (category.isBlank() || sectionContent.isBlank()) {
                        continue;
                    }

                    sections.add(new PatchNoteSectionDTO(category, sectionContent));
                }
            }

            if (summary.isBlank()) {
                summary = sections.stream()
                        .map(section -> "## " + section.getCategory() + "\n" + section.getContent())
                        .reduce((left, right) -> left + "\n\n" + right)
                        .orElse("");
            }

            return new SummaryResult(summary, sections);
        } catch (Exception exception) {
            return new SummaryResult(responseText, List.of());
        }
    }

    public record SummaryResult(String summary, List<PatchNoteSectionDTO> sections) {
    }
}
