import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import './HomePage.css';
import Card from "../../components/TechCards/Card";
import LoadingSpinner from '../../components/LoadingIcon/LoadingSpinner';
import { loadToolkitProfile } from "../../Services/toolkitProfile";
import { apiFetch } from "../../Services/api";

function HomePage() {
  const [patchNotes, setPatchNotes] = useState([]);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [toolkitOnly, setToolkitOnly] = useState(false);
  const [toolkitProfile, setToolkitProfile] = useState(loadToolkitProfile());
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const availableCategories = Array.from(
    new Set(
      patchNotes.flatMap((note) => Array.isArray(note.categories) ? note.categories : [])
    )
  ).sort();

  const categoryFilteredPatchNotes = selectedCategories.length === 0
    ? patchNotes
    : patchNotes.filter((note) =>
        selectedCategories.some((category) => (note.categories || []).includes(category))
      );

  const filteredPatchNotes = toolkitOnly
    ? categoryFilteredPatchNotes.filter((note) =>
        toolkitProfile.matchedTechNames.includes(note.techName)
      )
    : categoryFilteredPatchNotes;

  useEffect(() => {
    const fetchData = async () => {
      try {
        const patchNotesResponse = await apiFetch("/api/patch-notes");
        if (!patchNotesResponse.ok) throw new Error("Argh! Couldn't fetch patch notes!");
        setPatchNotes(await patchNotesResponse.json());
      } catch (fetchError) {
        setError(fetchError.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Re-read the toolkit profile when returning from the Package Insights page
  useEffect(() => {
    const syncToolkitProfile = () => {
      setToolkitProfile(loadToolkitProfile());
    };

    window.addEventListener("focus", syncToolkitProfile);
    return () => window.removeEventListener("focus", syncToolkitProfile);
  }, []);

  const toggleCategoryFilter = (category) => {
    setSelectedCategories((previous) => (
      previous.includes(category)
        ? previous.filter((item) => item !== category)
        : [...previous, category]
    ));
  };

  if (loading) {
    return (
      <div className="homepage">
        <LoadingSpinner
          message="Loading patch notes..."
          subtitle="Scouring the seas for yer tech updates..."
        />
      </div>
    );
  }

  return (
    <div className="homepage">
      <h2>Welcome to Patchy!</h2>
      <p className="instruction">
        The latest patch notes across every tracked technology.{" "}
        Browse the <Link to="/Techs">tech catalog</Link> to follow the tools you use.
      </p>

      {error && (
        <div className="error-banner">
          {error}
        </div>
      )}

      <section className="patch-notes-section">
        <div className="patch-notes-header">
          <h3>Latest Patch Notes</h3>
          <div className="patch-note-controls">
            <label className="toolkit-toggle">
              <input
                type="checkbox"
                checked={toolkitOnly}
                disabled={toolkitProfile.matchedTechNames.length === 0}
                onChange={(event) => setToolkitOnly(event.target.checked)}
              />
              <span>Only show updates relevant to my toolkit</span>
            </label>
            <div className="patch-note-filters">
              {availableCategories.map((category) => (
                <button
                  key={category}
                  type="button"
                  className={selectedCategories.includes(category) ? "filter-chip active" : "filter-chip"}
                  onClick={() => toggleCategoryFilter(category)}
                >
                  {category}
                </button>
              ))}
            </div>
          </div>
        </div>
        {toolkitOnly && toolkitProfile.matchedTechNames.length > 0 && (
          <p className="toolkit-filter-summary">
            Showing matches for: {toolkitProfile.matchedTechNames.join(", ")}
          </p>
        )}
        {!toolkitOnly && toolkitProfile.matchedTechNames.length === 0 && (
          <p className="toolkit-filter-summary">
            Upload a package.json on the Package Insights page to enable toolkit-specific filtering.
          </p>
        )}
        <div className="patch-notes-grid">
          {filteredPatchNotes.map((note) => (
            <Card key={note.id} patchNote={note} activeSectionFilters={selectedCategories} />
          ))}
        </div>
      </section>
    </div>
  );
}

export default HomePage;
