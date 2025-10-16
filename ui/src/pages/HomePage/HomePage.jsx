
import { useState, useEffect } from "react";
import { useAuth } from "../../Services/authContext";
import './HomePage.css';
import { toast } from 'react-toastify';
import Card from "../../components/TechCards/Card"; 

function HomePage() {
  const { isAuthenticated } = useAuth(); 
  const [tech, setTech] = useState([]);
  const [patchNotes, setPatchNotes] = useState([]);
  const [selectedTechIds, setSelectedTechIds] = useState(new Set());
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch technologies
        const techResponse = await fetch("http://localhost:8080/tech", {
          headers: { "Content-Type": "application/json" },
          credentials: "include",
        });
        if (!techResponse.ok) throw new Error("Argh! Couldn't fetch yer tech!");
        const techData = await techResponse.json();
        setTech(techData);

        // Fetch patch notes
        const patchNotesResponse = await fetch("http://localhost:8080/api/patch-notes", {
          headers: { "Content-Type": "application/json" },
          credentials: "include",
        });
        if (!patchNotesResponse.ok) throw new Error("Argh! Couldn't fetch patch notes!");

        const patchNotesData = await patchNotesResponse.json();
        setPatchNotes(patchNotesData);
        console.log(patchNotesData);

        // Fetch user favorites if authenticated
        if (isAuthenticated()) {
          const userResponse = await fetch("http://localhost:8080/api/currentUserId", {
            headers: { "Content-Type": "application/json" },
            credentials: "include",
          });
          
          if (userResponse.ok) {
            const userId = await userResponse.json();
            
            const favoritesResponse = await fetch(`http://localhost:8080/users/${userId}/favorites`, {
              headers: { "Content-Type": "application/json" },
              credentials: "include",
            });
            
            if (favoritesResponse.ok) {
              const favoritesData = await favoritesResponse.json();
              setSelectedTechIds(new Set(favoritesData.map((tech) => tech.id)));
            }
          }
        }
      } catch (error) {
        setError(`Ye got to be Logged in to add tech to yer bay! ${error.message}`);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated]);





  const toggleTech = async (techId) => {
    if (!isAuthenticated()) {
      setError("Please log in to modify your favorite technologies!");
      return;
    }

    const isCurrentlySelected = selectedTechIds.has(techId);
    
    if (isCurrentlySelected) {
      await handleRemoveFavorite(techId);
      setSelectedTechIds((prev) => {
        const newSet = new Set(prev);
        newSet.delete(techId);
        return newSet;
      });
    } else {
      await handleAddFavorite(techId);
      setSelectedTechIds((prev) => {
        const newSet = new Set(prev);
        newSet.add(techId);
        return newSet;
      });
    }
  };






  const handleRemoveFavorite = async (techId) => {
    try {
      const userInSession = await fetch(`http://localhost:8080/api/currentUserId`, {
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!userInSession.ok) {
        throw new Error("Argh! Ye need to be logged in to modify yer bay!");
      }

      const userId = await userInSession.json();

      const response = await fetch(`http://localhost:8080/users/${userId}/favorites/${techId}`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Argh! Failed to remove yer favorite!");
      }

      toast.success("Technology removed from yer Bay!");
    } catch (error) {
      toast.error(`Error: ${error.message}`);
    }
  };





  const handleAddFavorite = async (techId) => {
    try {
      const userInSession = await fetch(`http://localhost:8080/api/currentUserId`, {
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!userInSession.ok) {
        throw new Error("Argh! Ye need to be logged in to modify yer bay!");
      }

      const userId = await userInSession.json();

      const response = await fetch(`http://localhost:8080/users/${userId}/favorites/${techId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Argh! Failed to add yer favorite!");
      }

      toast.success("Technology added to yer Bay!");
    } catch (error) {
      toast.error(`Error: ${error.message}`);
    }
  };






  if (loading) {
    return (
      <div className="homepage">
        <h2>Loading...</h2>
        <p>Fetching technologies and patch notes...</p>
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
        <h3>Latest Patch Notes</h3>
        <div className="patch-notes-grid">
          {patchNotes.map((note) => (
            <Card key={note.id} patchNote={note} />
          ))}
        </div>
      </section>
    </div>
  );
}

export default HomePage;
