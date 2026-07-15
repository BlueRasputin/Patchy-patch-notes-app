const CATEGORY_ALIASES = {
  "New Features": ["new features", "new feature", "what's new", "whats new", "new", "features"],
  "Bug Fixes": ["bug fixes", "bug fix", "fixes", "fixed"],
  "Breaking Changes": ["breaking changes", "breaking change"],
  "Security": ["security", "security fixes"],
  "Deprecations": ["deprecations", "deprecation", "deprecated", "removed"],
  "Performance": ["performance", "improvements", "improvement", "optimization", "optimizations"],
  "Known Issues": ["known issues", "known issue"],
  "Documentation": ["documentation", "docs"]
};

const normalizeHeading = (line) =>
  line
    .replace(/^[-*#\s]+/, "")
    .replace(/[:\-]+$/, "")
    .trim()
    .toLowerCase();

const matchCategory = (line) => {
  const normalizedLine = normalizeHeading(line);

  for (const [category, aliases] of Object.entries(CATEGORY_ALIASES)) {
    if (aliases.includes(normalizedLine)) {
      return category;
    }
  }

  return null;
};

export const extractVisibleContent = (content, selectedCategories, sections = []) => {
  if (!content) {
    return { content: "", hasMatches: false };
  }

  if (!Array.isArray(selectedCategories) || selectedCategories.length === 0) {
    return { content, hasMatches: true };
  }

  if (Array.isArray(sections) && sections.length > 0) {
    const visibleSections = selectedCategories
      .map((category) => sections.find((section) => section.category === category))
      .filter((section) => section && section.content)
      .map((section) => `## ${section.category}\n${section.content}`);

    return {
      content: visibleSections.join("\n\n"),
      hasMatches: visibleSections.length > 0
    };
  }

  const sectionMap = new Map();
  let currentCategory = null;

  content.split("\n").forEach((line) => {
    const matchedCategory = matchCategory(line);

    if (matchedCategory) {
      currentCategory = matchedCategory;
      if (!sectionMap.has(matchedCategory)) {
        sectionMap.set(matchedCategory, []);
      }
      return;
    }

    if (!currentCategory) {
      return;
    }

    if (line.trim()) {
      sectionMap.get(currentCategory).push(line);
    }
  });

  const visibleSections = selectedCategories
    .filter((category) => sectionMap.has(category) && sectionMap.get(category).length > 0)
    .map((category) => `## ${category}\n${sectionMap.get(category).join("\n")}`);

  return {
    content: visibleSections.join("\n\n"),
    hasMatches: visibleSections.length > 0
  };
};
