import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import Card from "../../components/TechCards/Card";
import { apiFetch } from "../../Services/api";
import "./Embed.css";

// Chrome-less patch note feed for embedding in other sites via iframe:
//   <iframe src="https://<patchy-host>/embed?techs=React,Node.js"></iframe>
// No techs param = all tracked techs.
function Embed() {
  const [searchParams] = useSearchParams();
  const [patchNotes, setPatchNotes] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const techsParam = searchParams.get("techs");
  const wantedTechs = techsParam
    ? new Set(techsParam.split(",").map((name) => name.trim().toLowerCase()).filter(Boolean))
    : null;

  useEffect(() => {
    const fetchNotes = async () => {
      try {
        const response = await apiFetch("/api/patch-notes");
        if (!response.ok) throw new Error("Couldn't fetch patch notes.");
        setPatchNotes(await response.json());
      } catch (fetchError) {
        setError(fetchError.message);
      } finally {
        setLoading(false);
      }
    };

    fetchNotes();
  }, []);

  const visibleNotes = wantedTechs
    ? patchNotes.filter((note) => wantedTechs.has(note.techName.toLowerCase()))
    : patchNotes;

  if (loading) {
    return <div className="embed-page"><p className="embed-status">Loading patch notes…</p></div>;
  }

  return (
    <div className="embed-page">
      {error && <div className="error-banner">{error}</div>}

      {!error && visibleNotes.length === 0 && (
        <p className="embed-status">No patch notes available for the requested techs.</p>
      )}

      <div className="embed-list">
        {visibleNotes.map((note) => (
          <details className="embed-entry" key={note.id}>
            <summary>
              <span className="embed-entry-name">{note.techName}</span>
              {note.releaseVersion && (
                <span className="embed-entry-version">{note.releaseVersion}</span>
              )}
            </summary>
            <div className="embed-entry-body">
              <Card patchNote={note} />
            </div>
          </details>
        ))}
      </div>

      <p className="embed-footer">
        Patch notes by <a href="/" target="_blank" rel="noopener noreferrer">Patchy</a>
      </p>
    </div>
  );
}

export default Embed;
