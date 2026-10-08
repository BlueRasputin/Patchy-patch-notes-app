import { Link } from 'react-router-dom';
import './HeaderFooter.css'

function Footer() {
    return (
        <footer className="Footer">
            <p>Release notes are shown as each project published them. Summaries in your editor come from your own assistant (Copilot, Claude, Codex or Gemini).</p>
            <p><Link to="/About">Install Patchy in your editor</Link> · No account needed</p>
        </footer>
    );
}

export default Footer;
