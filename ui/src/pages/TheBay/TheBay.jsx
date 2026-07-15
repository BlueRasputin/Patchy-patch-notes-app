import { useEffect, useState } from 'react';
import { fetchBay } from '../../Services/bayService';
import Card from '../../components/TechCards/Card';
import './TheBay.css';
import LoadingSpinner from '../../components/LoadingIcon/LoadingSpinner';
import { toast } from 'react-toastify';
import { apiFetch } from '../../Services/api';

const TheBay = () => {
  const [loading, setLoading] = useState(true);
  const [bayFeed, setBayFeed] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const userResponse = await apiFetch("/api/currentUserId");

        if (!userResponse.ok) {
          throw new Error("Argh! Ye got to be logged in to view yer Bay!");
        }

        const userId = await userResponse.json();
        const patchNotes = await fetchBay(userId);
        
        setBayFeed(patchNotes);
      } catch (err) {
        toast.error(`Problem loading Yer Bay: ${err.message}`);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="the-bay">
        <div className="bay-header">
          <h1>The Bay</h1>
          <p className="bay-subtitle">Your personalized list of patch notes</p>
        </div>
        <LoadingSpinner
          message="Loading Yer Bay..."
          subtitle="Scouring the seas for yer tech updates..."
        />
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
          bayFeed.map((note) => (
            <Card 
              key={note.id} 
              patchNote={note} 
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
