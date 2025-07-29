//return title



//return description

export const Card = ({ livePatchNote }) => {
  return (
    <div className="tech-card">
      <h3>{livePatchNote.techName}</h3>
      <p>{livePatchNote.description}</p>
    </div>
  );
}