import React, { useEffect, useState } from 'react';
import { fetchBay } from '../../components/Services/bayService';
import Card from '../../components/TechCards/Card';
import './TheBay.css';


const TheBay = () => {
  const [loading, setLoading] = useState(true);
  const [bayFeed, setBayFeed] = useState([]);


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
          throw new Error("Argh! Ye got to be logged in to view yer Bay!");
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
          <p className="bay-subtitle">Your personalized list of patch notes</p>
        </div>
        <div className="loading">
          <h2>Loading Yer Bay...</h2>
          <p>Scouring the seas for yer tech updates...</p>
        </div>
      </div>
    );
  }

  

  

  return (

    <div className="the-bay">
      <div className="bay-header">
        <h1>The Bay</h1>
        <p className="bay-subtitle">Yer personalized fleet of patch notes</p>
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
            <h3>Ye aint got nothin in yer bay yet!</h3>
            <p>Visit the home page to add some technologies to follow.</p>
          </div>
        )}
      </div>
    </div>
  );

};
export default TheBay;