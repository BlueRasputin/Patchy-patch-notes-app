import { NavLink } from 'react-router-dom';
import './NavBar.css';

function NavBar() {
  return (
    <nav className="nav">
      <ul>
        <li><NavLink to="/">Home</NavLink></li>
        <li><NavLink to="/Techs">Techs</NavLink></li>
        <li><NavLink to="/TheBay">The Bay</NavLink></li>
        <li><NavLink to="/PackageInsights">Package Insights</NavLink></li>
        <li><NavLink to="/Compare">Compare</NavLink></li>
        <li><NavLink to="/About">About</NavLink></li>
        <li><NavLink to="/UserProfile">Profile</NavLink></li>
      </ul>
    </nav>
  );
}

export default NavBar;
