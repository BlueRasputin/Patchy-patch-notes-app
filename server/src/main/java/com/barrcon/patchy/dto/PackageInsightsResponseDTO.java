package com.barrcon.patchy.dto;

import java.util.List;

public record PackageInsightsResponseDTO(
        List<PackageInsightMatchDTO> matches,
        List<String> unmatchedPackages) {
}
