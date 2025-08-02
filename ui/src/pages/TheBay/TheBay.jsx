import React, { useEffect, useState } from 'react';
import { fetchBay } from '../../components/Services/bayService';
import Card from '../../components/TechCards/Card';
import './TheBay.css';


const TheBay = () => {
  const [loading, setLoading] = useState(true);
  const [bayFeed, setBayFeed] = useState([]);
  

  //TODO: once you have user authentication, pass the userId to fetchBay
  //TODO: implement error handling and loading state so you can better manage errors.


    const fetchData = async () => {
      try {
        const userInSession = await fetch(`http://localhost:8080/api/currentUserId`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include"
      });

        const userId  = await userInSession.json(); 
        
        if (!userId) {
          throw new Error("No user ID found in session");
        }

        const data = await fetchBay(userId);
        setBayFeed(data);
      } catch (err) {
        console.error("Error loading Bay data:", err);
        setLoading(false);
      } 
    };

  useEffect(() => {
    fetchData().then(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="the-bay">
        <div className="bay-header">
          <h1>The Bay</h1>
          <p className="bay-subtitle">Your personalized tech patch notes</p>
        </div>
        <div className="loading">
          <h2>Loading Your Bay...</h2>
          <p>Scouring the seas for your tech updates...</p>
        </div>
      </div>
    );
  }

  return (

    <div className="the-bay">
      <div className="bay-header">
        <h1>The Bay</h1>
        <p className="bay-subtitle">Your personalized tech patch notes</p>
        <div className="bay-stats">
          <span className="tech-count">{bayFeed.length} technologies tracked</span>
        </div>
      </div>
      
      <div className="card-container">
        {bayFeed.length > 0 ? (
          bayFeed.map((livePatchNote) => (
            <Card 
              key={livePatchNote.id || livePatchNote.techName} 
              livePatchNote={livePatchNote} 
            />
          ))
        ) : (
          <div className="no-data">
            <h3>No technologies in yer bay yet!</h3>
            <p>Visit the home page to add some technologies to track.</p>
          </div>
        )}
      </div>
    </div>
  );

};
export default TheBay;