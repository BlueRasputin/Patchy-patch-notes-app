//return title
import React from 'react';
import TheBay from '../../pages/TheBay/TheBay';


//return description

const Card = ({ livePatchNote }) => {
  
  return (
    <div className="tech-card">
      <div className="card-header">
        <h3 className="tech-title">{livePatchNote.techName}</h3>
      </div>
      {/* <div className="action-section">
      <button className="remove-button" onClick={handleRemoveFavorite}>
        Remove from yer Bay
      </button>

      </div> */}
      <div className="card-body">
        <p className="patch-description">{livePatchNote.description}</p>
      </div>
      
      
    </div>
  );
}


export default Card;