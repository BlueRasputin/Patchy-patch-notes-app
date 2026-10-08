import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchBay } from '../../Services/bayService';
import Card from '../../components/TechCards/Card';
import './TheBay.css';
import LoadingSpinner from '../../components/LoadingIcon/LoadingSpinner';
import { toast } from 'react-toastify';
import { useAuth } from '../../Services/authContext';

const TheBay = () => {
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(true);
  const [bayFeed, setBayFeed] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const patchNotes = await fetchBay(isAuthenticated());
        
        setBayFeed(patchNotes);
      } catch (err) {
        toast.error(`Problem loading Yer Bay: ${err.message}`);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated]);

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
          <span className="tech-count">{bayFeed.length} {bayFeed.length === 1 ? "technology" : "technologies"} tracked</span>
        </div>
      </div>
      
      <div className="card-container">
        {bayFeed.length > 0 ? (
          bayFeed.map((note) => (
            <details className="bay-entry" key={note.id}>
              <summary>
                <span className="bay-entry-name">{note.techName}</span>
                {note.releaseVersion && (
                  <span className="bay-entry-version">{note.releaseVersion}</span>
                )}
              </summary>
              <div className="bay-entry-body">
                <Card patchNote={note} />
              </div>
            </details>
          ))
        ) : (
          <div className="no-data">
            <h2>Yer Bay is empty</h2>
            <p>
              Follow techs in the <Link to="/Techs">tech catalog</Link>, or{" "}
              <Link to="/About">install Patchy</Link> to fill it from your projects automatically.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default TheBay;
