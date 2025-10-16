

import './Card.css';

const Card = ({ patchNote }) => {
  const formatContent = (content) => {
    if (!content) return '';
    
    return content
      .split('\n')
      .map((line, index) => {
        const trimmedLine = line.trim();
        
        // formatting for patchnotes
        if (trimmedLine.startsWith('# ')) {
          return <h1 key={index} className="md-h1">{trimmedLine.replace('# ', '')}</h1>;
        }
        if (trimmedLine.startsWith('## ')) {
          return <h2 key={index} className="md-h2">{trimmedLine.replace('## ', '')}</h2>;
        }
        if (trimmedLine.startsWith('### ')) {
          return <h3 key={index} className="md-h3">{trimmedLine.replace('### ', '')}</h3>;
        }

        
        if (trimmedLine.includes('**')) {
          const parts = trimmedLine.split('**');
          const formatted = parts.map((part, i) => 
            i % 2 === 1 ? <strong key={i}>{part}</strong> : part
          );
          return <p key={index}>{formatted}</p>;
        }
        
        
        if (trimmedLine.startsWith('- ')) {
          return <li key={index}>{trimmedLine.replace('- ', '')}</li>;
        }
        
        
        if (trimmedLine === '') {
          return <br key={index} />;
        }
        
        
        return <p key={index}>{trimmedLine}</p>;
      });
  };

  return (
    <div className="tech-card">
      <div className="card-header">
        <h3 className="tech-title">{patchNote.techName}</h3>
      </div>
    
      <div className="card-body">
        <div className="patch-description">
          {formatContent(patchNote.content)}
        </div>
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
