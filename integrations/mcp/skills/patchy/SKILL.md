---
name: patchy
description: Check Patchy release notes before adding, upgrading, or debugging a dependency. Use when the user upgrades packages, bumps versions, adds a library, or a build/test breaks after a dependency update.
---

Patchy's MCP tools know the latest release notes for this project's dependencies.

- Before upgrading or adding a dependency, call `breaking_changes` and mention any breaking or security changes that affect the plan.
- When a build or test fails after an update, call `breaking_changes` (or `patch_notes` for the specific tech) before guessing at a fix.
- When the user asks "what changed" in their stack, call `project_updates`.
- Cite the tech name, version, and release-notes link from the tool output.
