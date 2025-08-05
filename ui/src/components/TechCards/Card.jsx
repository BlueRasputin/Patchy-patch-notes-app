
import './Card.css';

//organizes tech cards with live patch note information

const Card = ({ livePatchNote }) => {
  
  return (
    <div className="tech-card">
      <div className="card-header">
        <h3 className="tech-title">{livePatchNote.techName}</h3>
      </div>
    
      <div className="card-body">
        <p className="patch-description">{livePatchNote.description}</p>
      </div>
      
      
    </div>
  );
}


export default Card;