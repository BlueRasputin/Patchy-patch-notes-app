
import { useState, useEffect } from "react";
import { useAuth } from "../components/Services/authContext";


function HomePage() {
  const { userState, isAuthenticated } = useAuth();
  const [tech, setTech] = useState([]);
  const [selectedTechIds, setSelectedTechIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const dummyTechData = [
    { id: 1, name: "React", description: "A JavaScript library for building UIs" },
    { id: 2, name: "Node.js", description: "A JavaScript runtime for server-side apps" },
    { id: 3, name: "TypeScript", description: "A typed superset of JavaScript" },
  ];

  useEffect(() => {
    const fetchTech = async () => {
      try {
        const response = await fetch("http://localhost:8080/tech"
        , {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          
        }
        );
        if (!response.ok) {
          throw new Error("Argh! Couldn't fetch yer tech!");
        }
        const data = await response.json();
        setTech(data);
      } catch (error) {
        setError(`Failed to load tech data: ${error.message}`);
        console.error("Error loading tech data:", error);
        setTech(dummyTechData);
      } finally {
        setLoading(false);
      }
    };
    fetchTech();
  }, []);

const toggleTech = (techId) => {
  setSelectedTechIds((prev) => {
    const newSet = new Set(prev);
    newSet.has(techId) ? newSet.delete(techId) : newSet.add(techId);
    return newSet;
  });
};

  const handleSaveFavorites = async () => {
    if (!isAuthenticated()) {
      alert("Please log in to save your favorite technologies!");
      return;
    }
    const selectedTechs = tech.filter((item) => selectedTechIds.has(item.id));
    if (selectedTechs.length === 0) {
      alert("Please select at least one technology!");
      return;
    }

    try {
      const userInSession = await fetch(`http://localhost:8080/api/currentUserId`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include"
      });

        const userId  = await userInSession.json();
        
         const techIds = Array.from(selectedTechIds);
      
      
      const response = await fetch(`http://localhost:8080/users/${userId}/favorites`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId: userState?.id,
          techIds: techIds,
        }),
      });
      if (!response.ok) {
        throw new Error("Failed to save favorites to the Bay!");
      }

      alert(
        `Saved ${selectedTechs.length} technologies to your Bay:\n${selectedTechs
          .map((t) => t.name)
          .join(", ")}`
      );
    } catch (error) {
      console.error("Error saving favorites:", error);
      alert(`Error: ${error.message}`);
    }
  };



  if (loading) {
    return (
      <div className="homepage">
        <div className="loading">
          <h2>Loading Technologies...</h2>
          <p>Scouring the seven seas of development...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="homepage">
      <h1>Patchy</h1>
      <h2>Select Technologies to add to your Bay</h2>
      <p className="instruction">
        Choose the technologies you want to follow and get their latest patch notes
        in The Bay
      </p>

      {error && (
        <div className="error-banner">
          {error} (Using demo data)
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

      <div className="action-section">
        <button className="save-button" onClick={handleSaveFavorites}>
          Save to yer Bay! ({selectedTechIds.size} selected)
        </button>
      </div>
    </div>
  );

  //TODO: set up api call to send data 
}

export default HomePage;