
import './Card.css';

//organizes tech cards with live patch note information

const Card = ({ patchNote }) => {
  return (
    <div className="tech-card">
      <div className="card-header">
        <h3 className="tech-title">{patchNote.techName}</h3>
      </div>
    
      <div className="card-body">
        <p className="patch-description">{patchNote.content}</p>
      </div>
      
      <div className="card-footer">
        <a 
          href={patchNote.sourceUrl} 
          target="_blank" 
          rel="noopener noreferrer"
          className="source-link"
        >
          View Original Docs →
        </a>
      </div>
    </div>
  );
}



export default Card;