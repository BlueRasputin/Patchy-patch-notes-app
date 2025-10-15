package com.barrcon.patchy.services;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryService.class);
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

    public String generateSummary(String content) {
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
            message.put("content", "Summarize these patch notes concisely and organize them in a bulletted list" + content);
            messages.put(message);
            requestBody.put("messages", messages);

            request.setEntity(new StringEntity(requestBody.toString()));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(responseBody);

                return jsonResponse.getJSONArray("content")
                        .getJSONObject(0)
                        .getString("text");
            }

        } catch (Exception e) {
            log.error("Failed to generate summary: {}", e.getMessage(), e);
            throw new RuntimeException("Claude API call failed", e);
        }
    }
}

