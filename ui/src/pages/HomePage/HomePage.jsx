import { useState, useEffect } from "react";
import { useAuth } from "../../Services/authContext";
import './HomePage.css';
import { toast } from 'react-toastify';
import Card from "../../components/TechCards/Card";
import LoadingSpinner from '../../components/LoadingIcon/LoadingSpinner';
import { loadToolkitProfile } from "../../Services/toolkitProfile";
import { apiFetch } from "../../Services/api";

function HomePage() {
  const { isAuthenticated } = useAuth();
  const [tech, setTech] = useState([]);
  const [patchNotes, setPatchNotes] = useState([]);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [toolkitOnly, setToolkitOnly] = useState(false);
  const [toolkitProfile, setToolkitProfile] = useState(loadToolkitProfile());
  const [selectedTechIds, setSelectedTechIds] = useState(new Set());
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
        const techResponse = await apiFetch("/tech");
        if (!techResponse.ok) throw new Error("Argh! Couldn't fetch yer tech!");
        setTech(await techResponse.json());

        const patchNotesResponse = await apiFetch("/api/patch-notes");
        if (!patchNotesResponse.ok) throw new Error("Argh! Couldn't fetch patch notes!");
        setPatchNotes(await patchNotesResponse.json());

        if (isAuthenticated()) {
          const userResponse = await apiFetch("/api/currentUserId");
          if (userResponse.ok) {
            const userId = await userResponse.json();
            const favoritesResponse = await apiFetch(`/users/${userId}/favorites`);
            if (favoritesResponse.ok) {
              const favoritesData = await favoritesResponse.json();
              setSelectedTechIds(new Set(favoritesData.map((favorite) => favorite.id)));
            }
          }
        }
      } catch (fetchError) {
        setError(fetchError.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated]);

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

  // Add or remove a tech from the user's favorites (shown in The Bay)
  const toggleTech = async (techId) => {
    if (!isAuthenticated()) {
      setError("Please log in to modify your favorite technologies!");
      return;
    }

    const removing = selectedTechIds.has(techId);

    try {
      const userResponse = await apiFetch("/api/currentUserId");
      if (!userResponse.ok) {
        throw new Error("Argh! Ye need to be logged in to modify yer bay!");
      }
      const userId = await userResponse.json();

      const response = await apiFetch(`/users/${userId}/favorites/${techId}`, {
        method: removing ? "DELETE" : "POST",
      });
      if (!response.ok) {
        throw new Error(removing
          ? "Argh! Failed to remove yer favorite!"
          : "Argh! Failed to add yer favorite!");
      }

      setSelectedTechIds((previous) => {
        const next = new Set(previous);
        if (removing) {
          next.delete(techId);
        } else {
          next.add(techId);
        }
        return next;
      });
      toast.success(removing
        ? "Technology removed from yer Bay!"
        : "Technology added to yer Bay!");
    } catch (toggleError) {
      toast.error(`Error: ${toggleError.message}`);
    }
  };

  if (loading) {
    return (
      <div className="homepage">
        <h2>Loading...</h2>
        <p>Fetching technologies and patch notes...</p>
        <LoadingSpinner
          message="Loading Yer Bay..."
          subtitle="Scouring the seas for yer tech updates..."
        />
      </div>
    );
  }

  return (
    <div className="homepage">
      <h2>Welcome to Patchy!</h2>
      <h3>Select Technologies to send 'em to yer Bay!</h3>
      <p className="instruction">
        Choose the technologies you want to follow and get their latest patch notes
        in The Bay
      </p>

      {error && (
        <div className="error-banner">
          {error}
        </div>
      )}

      <ul className="tech-list">
        {tech.map((item) => (
          <li key={item.id}>
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={selectedTechIds.has(item.id)}
                onChange={() => toggleTech(item.id)}
              />
              <span className="checkmark"></span>
              <div className="tech-info">
                <h3 className="tech-name">{item.name}</h3>
                {item.description && (
                  <p className="tech-description">{item.description}</p>
                )}
              </div>
            </label>
          </li>
        ))}
      </ul>

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
            Upload a `package.json` on the Package Insights page to enable toolkit-specific filtering.
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
