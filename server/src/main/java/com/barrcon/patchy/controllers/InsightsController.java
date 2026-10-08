package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.PackageInsightsRequestDTO;
import com.barrcon.patchy.dto.PackageInsightsResponseDTO;
import com.barrcon.patchy.services.PackageInsightsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "http://localhost:5173")
public class InsightsController {

    private static final int MAX_FILES = 25;
    private static final int MAX_FILE_CHARS = 512 * 1024;

    private final PackageInsightsService packageInsightsService;

    public InsightsController(PackageInsightsService packageInsightsService) {
        this.packageInsightsService = packageInsightsService;
    }

    @PostMapping("/package-json")
    public ResponseEntity<PackageInsightsResponseDTO> getPackageInsights(
            @RequestBody PackageInsightsRequestDTO request) {
        return ResponseEntity.ok(packageInsightsService.generateInsights(request));
    }

    public record ProjectRequest(Map<String, String> files) {
    }

    // Editors and the MCP server send raw manifest files; parsing happens here
    @PostMapping("/project")
    public ResponseEntity<PackageInsightsResponseDTO> getProjectInsights(@RequestBody ProjectRequest request) {
        if (request.files() == null || request.files().isEmpty() || request.files().size() > MAX_FILES
                || request.files().values().stream().anyMatch(content -> content == null || content.length() > MAX_FILE_CHARS)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(packageInsightsService.generateProjectInsights(request.files()));
    }
}
