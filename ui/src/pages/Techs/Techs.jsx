import { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../Services/authContext";
import Card from "../../components/TechCards/Card";
import LoadingSpinner from "../../components/LoadingIcon/LoadingSpinner";
import { apiFetch } from "../../Services/api";
import "./Techs.css";

function Techs() {
  const { isAuthenticated } = useAuth();
  const [techList, setTechList] = useState([]);
  const [notesByTech, setNotesByTech] = useState({});
  const [favoriteTechIds, setFavoriteTechIds] = useState(new Set());
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const techResponse = await apiFetch("/tech");
        if (!techResponse.ok) throw new Error("Argh! Couldn't fetch the tech catalog!");
        const techData = await techResponse.json();
        techData.sort((a, b) => a.name.localeCompare(b.name));
        setTechList(techData);

        const notesResponse = await apiFetch("/api/patch-notes");
        if (!notesResponse.ok) throw new Error("Argh! Couldn't fetch patch notes!");
        const notes = await notesResponse.json();
        setNotesByTech(Object.fromEntries(notes.map((note) => [note.techName, note])));

        if (isAuthenticated()) {
          const userResponse = await apiFetch("/api/currentUserId");
          if (userResponse.ok) {
            const userId = await userResponse.json();
            const favoritesResponse = await apiFetch(`/users/${userId}/favorites`);
            if (favoritesResponse.ok) {
              const favorites = await favoritesResponse.json();
              setFavoriteTechIds(new Set(favorites.map((favorite) => favorite.id)));
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

  // Add or remove a tech from the user's favorites (shown in The Bay)
  const toggleFavorite = async (techId) => {
    const removing = favoriteTechIds.has(techId);

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

      setFavoriteTechIds((previous) => {
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
      <div className="techs-page">
        <LoadingSpinner
          message="Loading the catalog..."
          subtitle="Charting every tech on the map..."
        />
      </div>
    );
  }

  return (
    <div className="techs-page">
      <h2>Tech Catalog</h2>
      <p className="instruction">
        Every technology Patchy tracks. Expand one to read its latest patch note
        {isAuthenticated()
          ? ", or check it to follow it in The Bay."
          : ". Log in to follow techs in The Bay."}
      </p>

      {error && <div className="error-banner">{error}</div>}

      <div className="tech-catalog">
        {techList.map((tech) => {
          const note = notesByTech[tech.name];
          return (
            <details className="tech-entry" key={tech.id}>
              <summary>
                <span className="tech-entry-name">{tech.name}</span>
                {note?.releaseVersion && (
                  <span className="tech-entry-version">{note.releaseVersion}</span>
                )}
                <span className="tech-entry-spacer" />
                {isAuthenticated() && (
                  <label
                    className="tech-entry-follow"
                    onClick={(event) => event.stopPropagation()}
                  >
                    <input
                      type="checkbox"
                      checked={favoriteTechIds.has(tech.id)}
                      onChange={() => toggleFavorite(tech.id)}
                    />
                    Following
                  </label>
                )}
              </summary>
              <div className="tech-entry-body">
                {note
                  ? <Card patchNote={note} />
                  : <p className="instruction">No patch note collected for {tech.name} yet.</p>}
              </div>
            </details>
          );
        })}
      </div>
    </div>
  );
}

export default Techs;
