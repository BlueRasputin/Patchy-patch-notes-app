package com.barrcon.patchy.dto;

import com.barrcon.patchy.services.AdvisoryService;

import java.util.List;

// techNames: every tracked tech the project uses (with or without notes yet);
// discovering: unknown packages queued for on-the-fly discovery;
// advisories: known vulnerabilities in the project's dependency versions (OSV.dev)
public record PackageInsightsResponseDTO(
        List<PackageInsightMatchDTO> matches,
        List<String> unmatchedPackages,
        List<String> techNames,
        List<String> discovering,
        List<AdvisoryService.Advisory> advisories) {
}
