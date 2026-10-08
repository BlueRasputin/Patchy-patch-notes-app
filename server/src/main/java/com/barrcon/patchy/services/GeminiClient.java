package com.barrcon.patchy.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

// Gemini API on the free tier. Use a key from a Google AI Studio project with no
// billing account attached: it can't be charged, it just returns 429 when the
// daily quota runs out, and callers retry on the next scheduled run.
@Service
public class GeminiClient {

    public static class QuotaExceededException extends RuntimeException {
        QuotaExceededException(String message) {
            super(message);
        }
    }

    private final String apiKey;
    private final String model;
    private final String fallbackModel;
    private final String baseUrl;
    private final Duration spacing;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private Instant lastCall = Instant.EPOCH;

    public GeminiClient(@Value("${GEMINI_API_KEY:}") String apiKey,
                        @Value("${GEMINI_MODEL:gemini-flash-latest}") String model,
                        @Value("${GEMINI_FALLBACK_MODEL:gemini-flash-lite-latest}") String fallbackModel,
                        @Value("${patchy.gemini.spacing:PT6S}") Duration spacing,
                        @Value("${patchy.gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.fallbackModel = fallbackModel;
        this.spacing = spacing;
        this.baseUrl = baseUrl;
    }

    public boolean enabled() {
        return !apiKey.isBlank();
    }

    // Structured output: the response is guaranteed to match `schema` (OpenAPI subset).
    // Synchronized + spaced so every caller together stays under the free-tier RPM.
    public synchronized JSONObject generateJson(String systemPrompt, String userContent, JSONObject schema) throws Exception {
        Duration wait = Duration.between(Instant.now(), lastCall.plus(spacing));
        if (!wait.isNegative()) {
            Thread.sleep(wait.toMillis());
        }
        lastCall = Instant.now();

        JSONObject body = new JSONObject()
                .put("systemInstruction", new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", systemPrompt))))
                .put("contents", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("parts", new JSONArray().put(new JSONObject().put("text", userContent)))))
                .put("generationConfig", new JSONObject()
                        .put("temperature", 0.1)
                        .put("responseMimeType", "application/json")
                        .put("responseSchema", schema));

        HttpResponse<String> response = call(model, body);
        // Newest Flash models are often overloaded on the free tier (503); Flash-Lite
        // usually has capacity and a higher free daily limit
        if ((response.statusCode() == 503 || response.statusCode() == 500) && !fallbackModel.isBlank()) {
            response = call(fallbackModel, body);
        }
        if (response.statusCode() == 429) {
            throw new QuotaExceededException("Gemini free-tier quota reached");
        }
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Gemini returned " + response.statusCode() + ": "
                    + response.body().substring(0, Math.min(300, response.body().length())));
        }
        return new JSONObject(textOf(new JSONObject(response.body())));
    }

    private HttpResponse<String> call(String modelName, JSONObject body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1beta/models/" + modelName + ":generateContent"))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    static String textOf(JSONObject response) {
        JSONArray parts = response.getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            if (!parts.getJSONObject(i).optBoolean("thought")) {
                text.append(parts.getJSONObject(i).optString("text"));
            }
        }
        return text.toString();
    }
}
