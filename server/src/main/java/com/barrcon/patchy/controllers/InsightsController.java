package com.barrcon.patchy.controllers;

import com.barrcon.patchy.dto.PackageInsightMatchDTO;
import com.barrcon.patchy.dto.PackageInsightsRequestDTO;
import com.barrcon.patchy.services.PackageInsightsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "http://localhost:5173")
public class InsightsController {

    private final PackageInsightsService packageInsightsService;

    public InsightsController(PackageInsightsService packageInsightsService) {
        this.packageInsightsService = packageInsightsService;
    }

    @PostMapping("/package-json")
    public ResponseEntity<List<PackageInsightMatchDTO>> getPackageInsights(
            @RequestBody PackageInsightsRequestDTO request) {
        return ResponseEntity.ok(packageInsightsService.generateMatches(request));
    }
}
