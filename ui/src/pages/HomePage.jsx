
import { useState, useEffect } from "react";
import { useAuth } from "../components/Services/authContext";
import './HomePage.css';

function HomePage() {
  const { userState, isAuthenticated } = useAuth();
  const [tech, setTech] = useState([]);
  const [selectedTechIds, setSelectedTechIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
        if(isAuthenticated()) {
          const userResponse = await fetch("http://localhost:8080/api/currentUserId", {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
            },
            credentials: "include",
          });
          if (!userResponse.ok){
            throw new Error("AOWDMAOWMDOA");
          }
          const userId = await userResponse.json();
          const favoritesResponse = await fetch(`http://localhost:8080/users/${userId}/favorites`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
        });
        if (!favoritesResponse.ok) {
          throw new Error("Argh! Couldn't fetch yer favorites!");
        }
        const favoritesData = await favoritesResponse.json();
        setSelectedTechIds(new Set(favoritesData.map((tech) => tech.id)));
      }
      } catch (error) {
        setError(`Failed to load tech data: ${error.message}`);
        console.error("Error loading tech data:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchTech();
  }, [isAuthenticated]);

// const toggleTech = (techId) => {
//   setSelectedTechIds((prev) => {
//     const newSet = new Set(prev);
//     newSet.has(techId) ? newSet.delete(techId) : 
//     newSet.add(techId);
//     return newSet;
//   });
// };
const toggleTech = async (techId) => {
    if (!isAuthenticated()) {
      alert("Please log in to modify your favorite technologies!");
      return;
    }

    setSelectedTechIds((prev) => {
      const newSet = new Set(prev);
      const isSelected = newSet.has(techId);
      if (isSelected) {
        handleRemoveFavorite(techId);
        newSet.delete(techId);
      } else {
        newSet.add(techId);
      }
      return newSet;
    });
  };

const handleRemoveFavorite = async (techId) => {
    try {
      const userInSession = await fetch(`http://localhost:8080/api/currentUserId`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!userInSession.ok) {
        throw new Error("Argh! Ye need to be logged in to remove yer favorites!");
      }

      const userId = await userInSession.json();

      const response = await fetch(`http://localhost:8080/users/${userId}/favorites/${techId}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Argh! Failed to remove yer favorite!");
      }

      alert("Technology removed from yer Bay!");
    } catch (error) {
      console.error("Error removing favorite:", error);
      alert(`Error: ${error.message}`);
    }
  };

  const handleSaveFavorites = async () => {
    if (!isAuthenticated()) {
      alert("Please log in to save your favorite technologies!");
      return;
    }
    const selectedTechs = tech.filter((item) => selectedTechIds.has(item.id));
    if (selectedTechs.length === 0) {
      alert("Please select at least one technology ye want to add, matey!");
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

      if (!userInSession.ok) {
        throw new Error("Argh! Ye need to be logged in to save yer favorites!");
      }

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
        throw new Error("Argh! Failed to save yer favorites!");
      }

      alert(
        `Saved ${selectedTechs.length} technologies to yer Bay:\n${selectedTechs
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
      <h2>Welcome to Patchy!</h2>
      <h3>Select Technologies to send 'em to yer Bay!</h3>
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

}

export default HomePage;