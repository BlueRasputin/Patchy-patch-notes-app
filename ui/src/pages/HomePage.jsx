import React, { useState, useEffect } from "react";
import { useAuth } from '../Services/useAuth';

// !!! Dummy data for tech - ONLY FOR TESTING PURPOSES !!!
const dummyTechData = [
  { id: 1, name: "React", description: "A JavaScript library for building user interfaces" },
  { id: 2, name: "Spring Boot", description: "Java framework for building applications" },
  { id: 3, name: "Node.js", description: "JavaScript runtime for server-side development" },
  { id: 4, name: "TypeScript", description: "Typed superset of JavaScript" },
  { id: 5, name: "Vue.js", description: "Progressive JavaScript framework" },
  { id: 6, name: "Python", description: "High-level programming language" },
  { id: 7, name: "Docker", description: "Container platform" },
  { id: 8, name: "Kubernetes", description: "Container orchestration platform" }
];

function HomePage() {
  const { user, userId } = useAuth();
  const [tech, setTech] = useState([]);
  const [selectedTechIds, setSelectedTechIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fetch tech data from tech table
  useEffect(() => {
    const fetchTech = async () => {
      try {
        //This is dummy data, but the logic is structured for the real API call
        // await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API delay
        // setTech(dummyTechData);

        //TODO: Uncomment when the API is running as expected
        const response = await fetch('http://localhost:8080/tech');
        if (!response.ok) {
          throw new Error("Argh! Couldn't fetch tech data");
        }
        const data = await response.json();
        setTech(data);

      } catch (error) { //error handling if fetch fails
        setError(`Failed to load tech data: ${error.message}`);
        console.error('Error loading tech data:', error);
        // Set dummy data in case of error
        setTech(dummyTechData);
      } finally {
        setLoading(false);
      }
    };
    fetchTech();
  }, []);

    // TODO: Uncomment when the API is working
    // verify backend calls work as expected

    // fetch('http://localhost:8080/tech')
    // .then(response => response.json())
    // .then(data => setTech(data))
    // .catch(err =>
    //   console.error("Argh! Couldn't load tech", err));

//set up checkbox funtionality
//set up list of techs
// set up toggle functionality for techs

// toggle buttons to add tech to user's favorite techs
  const toggleTech = (techId) => {
    setSelectedTechIds(prev => {
      const newSet = new Set(prev);
      //checks to see if tech is already in the bay
      if (newSet.has(techId)) {
        // deletes tech from the bay if it exists
        newSet.delete(techId);
      } else {
        //adds tech to bay
        newSet.add(techId);
      }
      return newSet;
    });
  };
  // Function to handle saving selected techs to user's Bay
  // This function will be called when the user clicks the "Save to yer Bay!" button

  const handleSaveFavorites = async () => {
    const selectedTechs = tech.filter(item => selectedTechIds.has(item.id));
    console.log('Selected Techs:', selectedTechs);
    //sends selected techs to the backend
    //TODO: set up backend endpoint to save selected techs

  if (selectedTechs.length > 0) {
      alert(`Selected ${selectedTechs.length} technologies:\n${selectedTechs.map(t => t.name).join(', ')}`);
    } else {
      alert('Please select at least one technology!');
    }
  };
// loading state message
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

//TODO: Hookup save button

  return (
    <div className="homepage">
      <h1>Patchy</h1>
      <h2>Select Technologies to add to your Bay</h2>
      <p className="instruction">
          Choose the technologies you want to follow and get their latest patch notes in The Bay
        </p>

        {error && (
          <div className="error-banner">
            {error} (Using demo data)
          </div>
        )}

//checkbox list of techs

      <ul className="tech-list">
        {tech.map(item => (
          <li key={item.id}>
          <label className="checkbox-label">
          <input type = "checkbox"
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

  //Function to save selected techs to user's Bay

      <div className="action-section">
        <button className="save-button" onClick={handleSaveFavorites}>
          Save to yer Bay! ({selectedTechIds.size} selected)
          </button>
        </div>
      </div>
  );
  //TODO: add function to save selected techs to user's Bay
}

export default HomePage;