package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PatchNoteSectionDTO;
import org.springframework.beans.factory.annotation.Value;
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

    // Crawled pages can run 40k+ chars, mostly site chrome; input tokens dominate
    // cost, so cap what we send. ponytail: dumb truncation — smarter noise
    // stripping in the crawler if summaries start missing late-page content.
    private static final int MAX_CONTENT_CHARS = 30_000;

    private static final String SYSTEM_PROMPT = """
            You summarize software release notes for developers. Produce a short \
            markdown summary plus per-category sections covering only the categories \
            actually present. Be concise and factual; skip site navigation noise.""";

    // Structured-output schema: guarantees valid JSON and enforces category names
    private static final JSONObject OUTPUT_SCHEMA = new JSONObject("""
            {
              "type": "object",
              "properties": {
                "summary": {"type": "string", "description": "Short markdown summary of the release"},
                "sections": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "category": {"type": "string", "enum": ["New Features", "Bug Fixes", "Breaking Changes", "Security", "Deprecations", "Performance", "Known Issues", "Documentation"]},
                      "content": {"type": "string", "description": "Concise markdown bullets for this category"}
                    },
                    "required": ["category", "content"],
                    "additionalProperties": false
                  }
                }
              },
              "required": ["summary", "sections"],
              "additionalProperties": false
            }""");

    public SummaryService(@Value("${claude.api.key:}") String claudeApiKey) {
        this.claudeApiKey = claudeApiKey;
    }

    //Claude API call to generate summaries for patch notes
    public SummaryResult generateSummary(String techName, String content) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(CLAUDE_API_URL);

            request.setHeader("x-api-key", claudeApiKey);
            request.setHeader("anthropic-version", "2023-06-01");
            request.setHeader("Content-Type", "application/json");

            String trimmedContent = content.length() > MAX_CONTENT_CHARS
                    ? content.substring(0, MAX_CONTENT_CHARS)
                    : content;

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "claude-sonnet-5");
            requestBody.put("max_tokens", 2048);
            // Summarization doesn't need reasoning; disabling thinking keeps
            // token spend flat (Sonnet 5 runs it by default when omitted)
            requestBody.put("thinking", new JSONObject().put("type", "disabled"));
            requestBody.put("output_config", new JSONObject()
                    .put("effort", "low")
                    .put("format", new JSONObject()
                            .put("type", "json_schema")
                            .put("schema", OUTPUT_SCHEMA)));
            requestBody.put("system", SYSTEM_PROMPT);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "Tech: " + techName + "\n\nPatch notes:\n" + trimmedContent);
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
