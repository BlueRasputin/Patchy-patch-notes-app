import { useState } from "react";
import { toast } from "react-toastify";
import Card from "../../components/TechCards/Card";
import { clearToolkitProfile, loadToolkitProfile, saveToolkitProfile } from "../../Services/toolkitProfile";
import { Link } from "react-router-dom";
import { apiFetch } from "../../Services/api";
import "./PackageInsights.css";

// Manifests the backend can parse (see ManifestParser)
const MANIFEST_NAMES = ["package.json", "pom.xml", "build.gradle", "build.gradle.kts", "requirements.txt",
  "pyproject.toml", "Pipfile", "go.mod", "Cargo.toml", "Gemfile", "composer.json", "Dockerfile",
  "mix.exs", "gleam.toml", "pubspec.yaml", "build.sbt", "Project.toml", "App.csproj"];

function PackageInsights() {
  const [packageJsonInput, setPackageJsonInput] = useState("");
  const [manifestName, setManifestName] = useState("package.json");
  const [discovering, setDiscovering] = useState([]);
  const [packageInsights, setPackageInsights] = useState([]);
  const [unmatchedPackages, setUnmatchedPackages] = useState([]);
  const [insightsLoading, setInsightsLoading] = useState(false);
  const [insightsError, setInsightsError] = useState("");
  const [savedProfile, setSavedProfile] = useState(loadToolkitProfile());

  const fetchPackageInsights = async (rawInput, fileName) => {
    try {
      setInsightsLoading(true);
      setInsightsError("");

      const response = await apiFetch("/api/insights/project", {
        method: "POST",
        body: JSON.stringify({ files: { [fileName]: rawInput } })
      });

      if (!response.ok) {
        throw new Error("Couldn't read that file. Check it's a complete manifest and try again.");
      }

      const { matches, unmatchedPackages: unmatched, discovering: queued } = await response.json();
      setPackageInsights(matches);
      setUnmatchedPackages(unmatched);
      setDiscovering(queued);
      const profile = saveToolkitProfile(matches);
      setSavedProfile(profile);

      if (matches.length === 0) {
        toast.info(`No tracked techs found in this ${fileName} yet.`);
      } else {
        toast.success(`Generated ${matches.length} personalized patch note matches.`);
      }
    } catch (fetchError) {
      setPackageInsights([]);
      setUnmatchedPackages([]);
      setDiscovering([]);
      setInsightsError(fetchError.message);
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
    const fileName = MANIFEST_NAMES.includes(selectedFile.name) || /\.(cs|fs)proj$/.test(selectedFile.name)
      ? selectedFile.name
      : manifestName;
    setManifestName(fileName);
    setPackageJsonInput(fileText);
    await fetchPackageInsights(fileText, fileName);
  };

  const handlePackageSubmit = async (event) => {
    event.preventDefault();
    await fetchPackageInsights(packageJsonInput, manifestName);
  };

  const handleClearProfile = () => {
    clearToolkitProfile();
    setSavedProfile({ matchedTechNames: [], savedAt: null });
    setPackageInsights([]);
    setUnmatchedPackages([]);
    toast.info("Cleared saved toolkit relevance profile.");
  };

  return (
    <div className="package-insights-page">
      <h1>Project insights</h1>
      <p className="instruction">
        Upload or paste a project manifest to see the latest patch notes for every language,
        framework and library in it. Packages Patchy hasn&apos;t seen yet are looked up and added
        to the catalog. To get this automatically in your editor, <Link to="/About">install Patchy</Link>.
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
        <div className="manifest-row">
          <label>
            Upload a manifest
            <input type="file" onChange={handlePackageFileUpload} />
          </label>
          <label>
            Or paste it as
            <select value={manifestName} onChange={(event) => setManifestName(event.target.value)}>
              {MANIFEST_NAMES.map((name) => <option key={name} value={name}>{name}</option>)}
            </select>
          </label>
        </div>
        <label className="visually-hidden" htmlFor="manifest-content">{manifestName} contents</label>
        <textarea
          id="manifest-content"
          value={packageJsonInput}
          onChange={(event) => setPackageJsonInput(event.target.value)}
          placeholder={`Paste your ${manifestName} here`}
          rows={12}
          spellCheck={false}
        />
        <button type="submit" disabled={insightsLoading || !packageJsonInput.trim()}>
          {insightsLoading ? "Matching…" : "Show patch notes"}
        </button>
      </form>

      {insightsError && <div className="error-banner">{insightsError}</div>}

      <div aria-live="polite">
        {discovering.length > 0 && (
          <p className="unmatched-packages">
            Looking up release notes for {discovering.join(", ")}. Check back in a few minutes.
          </p>
        )}
        {unmatchedPackages.length > discovering.length && (
          <p className="unmatched-packages">
            Not tracked: {unmatchedPackages.filter((name) => !discovering.includes(name)).join(", ")}.
          </p>
        )}
      </div>

      {packageInsights.length > 0 && (
        <div className="package-insight-results">
          {packageInsights.map((match, index) => (
            <div className="package-insight-item" key={`${match.packageName}-${index}`}>
              <p className="package-match-meta">
                {match.packageName}{match.packageVersion ? `@${match.packageVersion}` : ""} ({match.dependencyType})
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
