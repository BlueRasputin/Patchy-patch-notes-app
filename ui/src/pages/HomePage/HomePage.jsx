
import { useState, useEffect } from "react";
import { useAuth } from "../../Services/authContext";
import './HomePage.css';
import { toast } from 'react-toastify';
// import LoadingSpinner from "../../components/LoadingSpinner/LoadingSpinner";

function HomePage() {
  const { userState, isAuthenticated } = useAuth(); 
  const [tech, setTech] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTechIds, setSelectedTechIds] = useState(new Set());
  const [error, setError] = useState(null);

useEffect(() => {
  const fetchTech = async () => {
    try { // Fetch the list of technologies from the backend
      const response = await fetch("http://localhost:8080/tech", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      }); //throw error if response is not ok
      if (!response.ok) throw new Error("Argh! Couldn't fetch yer tech!");
      const data = await response.json();
      setTech(data);

      const userResponse = await fetch("http://localhost:8080/api/currentUserId", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });
      if (!userResponse.ok) throw new Error("Please Log in");
      const userId = await userResponse.json();
      

      const favoritesResponse = await fetch(`http://localhost:8080/users/${userId}/favorites`, {
        method: "GET",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
      });
      if (!favoritesResponse.ok) throw new Error("Argh! Couldn't fetch yer favorites!");
      const favoritesData = await favoritesResponse.json();
      setSelectedTechIds(new Set(favoritesData.map((tech) => tech.id)));
    } catch (error) {
      setError(`Ye got to be Logged in to add tech to yer bay! ${error.message}`);
      
    } finally {
      setLoading(false);
    }
  };

  fetchTech();
}, []);

// const filteredTech = tech.filter((item) =>
//   item.name.toLowerCase().includes(searchQuery.toLowerCase())
// );


//  if (loading) {
//     return (
//       <div className="search-results">
//         <LoadingSpinner 
//           message="Loading..."
//           subtitle="Scouring the seas for yer tech updates..."
//         />
//       </div>
//     );
//   }

// return (
//   <div>
//     <h1> Available Technologies</h1>
//     <searchBar searchQuery={searchQuery} setSearchQuery={setSearchQuery} onSearch={setSearchQuery} />

//      {loading && <p>Loading tech list...</p>}
//       {error && <p style={{ color: "red" }}>{error}</p>}
//       <ul>
//         {filteredTech.length > 0 ? (
//           filteredTech.map((item) => (
//             <li key={item.id}>
//               <strong>{item.name}</strong> – {item.description}
//             </li>
//           ))
//         ) : (
//           <p>No tech found for “{searchQuery}”.</p>
//         )}
//       </ul>

//   </div>
// )
// };



//toggle tech selections

// const toggleTech = async (techId) => {
//     if (!isAuthenticated()) {
//       setError("Please log in to modify your favorite technologies!");
//       return;
//     }

//     setSelectedTechIds((prev) => {
//       const newSet = new Set(prev);
//       const isSelected = newSet.has(techId);
//       if (isSelected) {
//         handleRemoveFavorite(techId);
//         newSet.delete(techId);
//       } else {
//         newSet.add(techId);
//       }
//       return newSet;
//     });




const toggleTech = async (techId) => {
    if (!isAuthenticated()) {
      setError("Please log in to modify your favorite technologies!");
      return;
    }

    const isCurrentlySelected = selectedTechIds.has(techId);
    
    if (isCurrentlySelected) {
      // Remove from favorites
      await handleRemoveFavorite(techId);
      setSelectedTechIds((prev) => {
        const newSet = new Set(prev);
        newSet.delete(techId);
        return newSet;
      });
    } else {
      // Add to favorites
      await handleAddFavorite(techId);
      setSelectedTechIds((prev) => {
        const newSet = new Set(prev);
        newSet.add(techId);
        return newSet;
      });
    }
};



//handle removing favorite tech
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
        throw new Error("Argh! Ye need to be logged in to modify yer bay!");
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

      toast.success("Technology removed from yer Bay!");
    } catch (error) {
      
      toast.error(`Error: ${error.message}`);
    }
  };

  const handleAddFavorite = async (techId) => {
    try {
      const userInSession = await fetch(`http://localhost:8080/api/currentUserId`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!userInSession.ok) {
        throw new Error("Argh! Ye need to be logged in to modify yer bay!");
      }

      const userId = await userInSession.json();

      const response = await fetch(`http://localhost:8080/users/${userId}/favorites/${techId}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
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



//Prevent saving if not logged in
  // const handleSaveFavorites = async () => {
  //   if (!isAuthenticated()) {
  //     toast.error("Please log in to save your favorite technologies!");
  //     return;
  //   } // Ensure at least one technology is selected
  //   const selectedTechs = tech.filter((item) => selectedTechIds.has(item.id));
  //   if (selectedTechs.length === 0) {
  //     toast.error("Please select at least one technology ye want to add, matey!");
  //     return;
  //   }

  //   try { // Fetch the current user ID
  //     const userInSession = await fetch(`http://localhost:8080/api/currentUserId`, {
  //         method: "GET",
  //         headers: {
  //           "Content-Type": "application/json",
  //         },
  //         credentials: "include"
  //     });

  //     if (!userInSession.ok) {
  //       throw new Error("Argh! Ye need to be logged in to save yer favorites!");
  //     }
          
  //       const userId  = await userInSession.json();
  //         // convertes techids into an array to send to backend
  //        const techIds = Array.from(selectedTechIds);
      
      
  //     const response = await fetch(`http://localhost:8080/users/${userId}/favorites`, {
  //       method: "POST",
  //       headers: {
  //         "Content-Type": "application/json",
  //       },
  //       body: JSON.stringify({
  //         userId: userState?.id,
  //         techIds: techIds,
  //       }),
  //     });
  //     if (!response.ok) {
  //       throw new Error("Argh! Failed to save yer favorites!");
  //     }

  //     toast.success(
  //       `Saved ${selectedTechs.length} technologies to yer Bay:\n${selectedTechs
  //         .map((t) => t.name)
  //         .join(", ")}`
  //     );
  //   } catch (error) {
      
  //     toast.error(`Error: ${error.message}`);
  //   }
  // };

//Dislay Load when fetching
  // if (loading) {
  //   return (
  //     <div className="homepage">
  //       <div className="loading">
  //         <h2>Loading Technologies...</h2>
  //         <p>Scouring the seven seas of development...</p>
  //       </div>
  //     </div>
  //   );
  // }


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

      
    </div>
  );

}

export default HomePage;