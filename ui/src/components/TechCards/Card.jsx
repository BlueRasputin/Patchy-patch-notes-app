import './Card.css';
import { extractVisibleContent } from '../../Services/patchNoteSections';

const Card = ({ patchNote, activeSectionFilters = [] }) => {
  const categories = Array.isArray(patchNote.categories) ? patchNote.categories : [];
  const hasSummary = Boolean(patchNote.content);
  const visibleContent = extractVisibleContent(
    patchNote.content,
    activeSectionFilters,
    Array.isArray(patchNote.sections) ? patchNote.sections : []
  );

  // Renders the summary's lightweight markdown (headings, bold, bullets)
  const formatContent = (content) => {
    if (!content) return '';

    return content
      .split('\n')
      .map((line, index) => {
        const trimmedLine = line.trim();

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
        {patchNote.releaseVersion && (
          <span className="version-badge">{patchNote.releaseVersion}</span>
        )}
      </div>

      {categories.length > 0 && (
        <div className="category-badges">
          {categories.map((category) => (
            <span key={category} className="category-badge">{category}</span>
          ))}
        </div>
      )}

      {patchNote.statusLabel && (
        <div className="note-status-banner">{patchNote.statusLabel}</div>
      )}
    
      <div className="card-body">
        <div className="patch-description">
          {!hasSummary
            ? <p className="filtered-empty-state">Patch note not collected yet for this tech.</p>
            : visibleContent.hasMatches
            ? formatContent(visibleContent.content)
            : <p className="filtered-empty-state">No matching sections for the selected filters.</p>}
        </div>
      </div>
      
      <div className="card-footer">
        {patchNote.sourceUrl ? (
          <a 
            href={patchNote.sourceUrl} 
            target="_blank" 
            rel="noopener noreferrer"
            className="source-link"
          >
            View Original Docs →
          </a>
        ) : (
          <span className="source-link source-link-disabled">Source URL not available yet</span>
        )}
      </div>
    </div>
  );
}

export default Card;
