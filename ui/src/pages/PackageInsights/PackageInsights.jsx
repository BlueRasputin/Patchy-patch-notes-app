import { useState } from "react";
import { toast } from "react-toastify";
import Card from "../../components/TechCards/Card";
import { clearToolkitProfile, loadToolkitProfile, saveToolkitProfile } from "../../Services/toolkitProfile";
import "./PackageInsights.css";

function PackageInsights() {
  const [packageJsonInput, setPackageJsonInput] = useState("");
  const [packageInsights, setPackageInsights] = useState([]);
  const [insightsLoading, setInsightsLoading] = useState(false);
  const [insightsError, setInsightsError] = useState("");
  const [savedProfile, setSavedProfile] = useState(loadToolkitProfile());

  const parsePackageJson = (rawInput) => {
    const parsed = JSON.parse(rawInput);
    return {
      dependencies: parsed.dependencies ?? {},
      devDependencies: parsed.devDependencies ?? {},
      peerDependencies: parsed.peerDependencies ?? {}
    };
  };

  const fetchPackageInsights = async (rawInput) => {
    try {
      setInsightsLoading(true);
      setInsightsError("");

      const requestBody = parsePackageJson(rawInput);
      const response = await fetch("http://localhost:8080/api/insights/package-json", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(requestBody)
      });

      if (!response.ok) {
        throw new Error("Failed to generate personalized patch notes.");
      }

      const insightMatches = await response.json();
      setPackageInsights(insightMatches);
      const profile = saveToolkitProfile(insightMatches);
      setSavedProfile(profile);

      if (insightMatches.length === 0) {
        toast.info("No tracked tech matches were found in this package.json yet.");
      } else {
        toast.success(`Generated ${insightMatches.length} personalized patch note matches.`);
      }
    } catch (fetchError) {
      setPackageInsights([]);
      if (fetchError instanceof SyntaxError) {
        setInsightsError("Invalid JSON format. Check your package.json and try again.");
      } else {
        setInsightsError(fetchError.message);
      }
    } finally {
      setInsightsLoading(false);
    }
  };

  const handlePackageFileUpload = async (event) => {
    const selectedFile = event.target.files?.[0];
    if (!selectedFile) {
      return;
    }

    const fileText = await selectedFile.text();
    setPackageJsonInput(fileText);
    await fetchPackageInsights(fileText);
  };

  const handlePackageSubmit = async (event) => {
    event.preventDefault();
    await fetchPackageInsights(packageJsonInput);
  };

  const handleClearProfile = () => {
    clearToolkitProfile();
    setSavedProfile({ matchedTechNames: [], savedAt: null });
    setPackageInsights([]);
    toast.info("Cleared saved toolkit relevance profile.");
  };

  return (
    <div className="package-insights-page">
      <h2>Package Insights</h2>
      <p className="instruction">
        Upload your package.json or paste it below to get summaries matched to your toolkit.
      </p>

      {savedProfile.matchedTechNames.length > 0 && (
        <div className="saved-profile-banner">
          <p>
            Saved toolkit profile: {savedProfile.matchedTechNames.join(", ")}
          </p>
          <button type="button" onClick={handleClearProfile}>
            Clear Saved Profile
          </button>
        </div>
      )}

      <form className="package-insights-form" onSubmit={handlePackageSubmit}>
        <input
          type="file"
          accept="application/json,.json"
          onChange={handlePackageFileUpload}
        />
        <textarea
          value={packageJsonInput}
          onChange={(event) => setPackageJsonInput(event.target.value)}
          placeholder="Paste package.json content here..."
          rows={12}
        />
        <button type="submit" disabled={insightsLoading || !packageJsonInput.trim()}>
          {insightsLoading ? "Generating..." : "Generate Personalized Summaries"}
        </button>
      </form>

      {insightsError && <div className="error-banner">{insightsError}</div>}

      {packageInsights.length > 0 && (
        <div className="package-insight-results">
          {packageInsights.map((match, index) => (
            <div className="package-insight-item" key={`${match.packageName}-${index}`}>
              <p className="package-match-meta">
                {match.packageName}@{match.packageVersion} ({match.dependencyType})
              </p>
              <Card patchNote={match.patchNote} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default PackageInsights;
