const TOOLKIT_PROFILE_KEY = "patchyToolkitProfile";

export const saveToolkitProfile = (matches) => {
  const normalizedMatches = Array.isArray(matches) ? matches : [];
  const matchedTechNames = Array.from(
    new Set(
      normalizedMatches
        .map((match) => match?.patchNote?.techName)
        .filter(Boolean)
    )
  );

  const profile = {
    matchedTechNames,
    savedAt: new Date().toISOString()
  };

  localStorage.setItem(TOOLKIT_PROFILE_KEY, JSON.stringify(profile));
  return profile;
};

export const loadToolkitProfile = () => {
  const rawValue = localStorage.getItem(TOOLKIT_PROFILE_KEY);
  if (!rawValue) {
    return { matchedTechNames: [], savedAt: null };
  }

  try {
    const parsed = JSON.parse(rawValue);
    return {
      matchedTechNames: Array.isArray(parsed.matchedTechNames) ? parsed.matchedTechNames : [],
      savedAt: parsed.savedAt ?? null
    };
  } catch {
    return { matchedTechNames: [], savedAt: null };
  }
};

export const clearToolkitProfile = () => {
  localStorage.removeItem(TOOLKIT_PROFILE_KEY);
};
