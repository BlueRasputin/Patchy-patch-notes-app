import { useEffect, useState } from "react";
import Card from "../../components/TechCards/Card";
import "./ComparePatchNotes.css";

function ComparePatchNotes() {
  const [techList, setTechList] = useState([]);
  const [selectedTechIds, setSelectedTechIds] = useState([]);
  const [results, setResults] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchTechList = async () => {
      try {
        const response = await fetch("http://localhost:8080/tech", {
          headers: { "Content-Type": "application/json" },
          credentials: "include"
        });

        if (!response.ok) {
          throw new Error("Failed to load technologies.");
        }

        const data = await response.json();
        setTechList(data);
      } catch (fetchError) {
        setError(fetchError.message);
      }
    };

    fetchTechList();
  }, []);

  const toggleSelection = (techId) => {
    setSelectedTechIds((prev) => {
      if (prev.includes(techId)) {
        return prev.filter((id) => id !== techId);
      }
      return [...prev, techId];
    });
  };

  const runCompare = async () => {
    if (selectedTechIds.length < 2) {
      setError("Select at least 2 technologies to compare.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      const query = selectedTechIds.map((id) => `techIds=${id}`).join("&");
      const response = await fetch(`http://localhost:8080/api/patch-notes/compare?${query}`, {
        headers: { "Content-Type": "application/json" },
        credentials: "include"
      });

      if (!response.ok) {
        throw new Error("Failed to compare patch notes.");
      }

      const data = await response.json();
      setResults(data);
    } catch (compareError) {
      setResults([]);
      setError(compareError.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="compare-page">
      <h2>Compare Patch Notes</h2>
      <p className="instruction">Select at least two tools to compare their latest summaries.</p>

      {error && <div className="error-banner">{error}</div>}

      <div className="compare-controls">
        <div className="compare-tech-list">
          {techList.map((tech) => (
            <label key={tech.id} className="compare-tech-item">
              <input
                type="checkbox"
                checked={selectedTechIds.includes(tech.id)}
                onChange={() => toggleSelection(tech.id)}
              />
              {tech.name}
            </label>
          ))}
        </div>
        <button type="button" onClick={runCompare} disabled={loading}>
          {loading ? "Comparing..." : "Compare Selected"}
        </button>
      </div>

      {results.length > 0 && (
        <div className="compare-results-grid">
          {results.map((note) => (
            <Card key={note.id} patchNote={note} />
          ))}
        </div>
      )}
    </div>
  );
}

export default ComparePatchNotes;
