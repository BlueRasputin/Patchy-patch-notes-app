import { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../Services/authContext";
import Card from "../../components/TechCards/Card";
import LoadingSpinner from "../../components/LoadingIcon/LoadingSpinner";
import { apiFetch } from "../../Services/api";
import { loadLocalBay, saveLocalBay } from "../../Services/localBay";
import "./Techs.css";

// Older releases for one tech, fetched the first time the user opens it
function ReleaseHistory({ techId }) {
  const [history, setHistory] = useState(null);
  const [failed, setFailed] = useState(false);

  const loadHistory = async () => {
    if (history !== null) return;
    try {
      const response = await apiFetch(`/api/patch-notes/history?techId=${techId}`);
      if (!response.ok) throw new Error();
      setHistory(await response.json());
    } catch {
      setFailed(true);
    }
  };

  const olderNotes = (history ?? []).slice(1);

  return (
    <details className="release-history" onToggle={(event) => event.target.open && loadHistory()}>
      <summary>Release history</summary>
      {failed && <p className="instruction">Couldn't load release history.</p>}
      {history !== null && olderNotes.length === 0 && !failed && (
        <p className="instruction">No earlier releases recorded yet.</p>
      )}
      {olderNotes.map((note) => (
        <details className="release-history-entry" key={note.id}>
          <summary>
            {note.releaseVersion || "unversioned"}
            <span className="release-history-date">
              {note.createdAt ? new Date(note.createdAt).toLocaleDateString() : ""}
            </span>
          </summary>
          <Card patchNote={note} />
        </details>
      ))}
    </details>
  );
}

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
          const favoritesResponse = await apiFetch("/api/me/favorites");
          if (favoritesResponse.ok) {
            const favorites = await favoritesResponse.json();
            setFavoriteTechIds(new Set(favorites.map((favorite) => favorite.id)));
          }
        } else {
          const localBay = loadLocalBay();
          setFavoriteTechIds(new Set(techData.filter((tech) => localBay.has(tech.name)).map((tech) => tech.id)));
        }
      } catch (fetchError) {
        setError(fetchError.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated]);

  // Add or remove a tech from the user's favorites (shown in The Bay).
  // Without an account the Bay lives in this browser.
  const toggleFavorite = async (tech) => {
    const techId = tech.id;
    const removing = favoriteTechIds.has(techId);

    try {
      if (!isAuthenticated()) {
        const localBay = loadLocalBay();
        if (removing) {
          localBay.delete(tech.name);
        } else {
          localBay.add(tech.name);
        }
        saveLocalBay(localBay);
      } else {
          const response = await apiFetch(`/api/me/favorites/${techId}`, {
          method: removing ? "DELETE" : "POST",
        });
        if (!response.ok) {
          throw new Error(removing
            ? "Argh! Failed to remove yer favorite!"
            : "Argh! Failed to add yer favorite!");
        }
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
      <h1>Tech catalog</h1>
      <p className="instruction">
        Every technology Patchy tracks. Expand one to read its latest patch note,
        or check it to follow it in The Bay
        {isAuthenticated() ? "." : " (saved in this browser; log in to sync it everywhere)."}
      </p>

      {error && <div className="error-banner">{error}</div>}

      <div className="tech-catalog">
        {techList.map((tech) => {
          const note = notesByTech[tech.name];
          return (
            // The follow checkbox sits beside <summary>, not inside it:
            // interactive controls inside a summary confuse screen readers
            <div className="tech-entry" key={tech.id}>
              <details>
                <summary>
                  <span className="tech-entry-name">{tech.name}</span>
                  {note?.releaseVersion && (
                    <span className="tech-entry-version">{note.releaseVersion}</span>
                  )}
                </summary>
                <div className="tech-entry-body">
                  {note
                    ? (
                      <>
                        <Card patchNote={note} />
                        <ReleaseHistory techId={tech.id} />
                      </>
                    )
                    : <p className="instruction">No patch note collected for {tech.name} yet.</p>}
                </div>
              </details>
              <label className="tech-entry-follow">
                <input
                  type="checkbox"
                  checked={favoriteTechIds.has(tech.id)}
                  onChange={() => toggleFavorite(tech)}
                  aria-label={`Follow ${tech.name}`}
                />
                Following
              </label>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Techs;
