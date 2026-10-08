import { NavLink } from 'react-router-dom';
import { useAuth } from '../../Services/authContext';
import './NavBar.css';

function NavBar() {
  const { isAuthenticated } = useAuth();

  return (
    <nav className="nav" aria-label="Main">
      <ul>
        <li><NavLink to="/">Home</NavLink></li>
        <li><NavLink to="/Techs">Techs</NavLink></li>
        <li><NavLink to="/TheBay">The Bay</NavLink></li>
        <li><NavLink to="/PackageInsights">Project Insights</NavLink></li>
        <li><NavLink to="/Compare">Compare</NavLink></li>
        <li><NavLink to="/About">Get Patchy</NavLink></li>
        {isAuthenticated() && <li><NavLink to="/UserProfile">Profile</NavLink></li>}
      </ul>
    </nav>
  );
}

export default NavBar;
