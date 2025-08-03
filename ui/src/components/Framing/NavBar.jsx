import { Link } from 'react-router-dom';
import './NavBar.css';
import { useEffect, useState } from "react";

// NavBar component for navigation links
function NavBar() {
  // State to determine if the device is mobile and switch to icons
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  //changes navbar labels for icons for smaller devices
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  //TODO: useEffect to display isLoggedIn/isLoggedOut
  
// function 







  return (
    <nav className="nav">
      <ul>
        <li>
          <Link to="/" className={isMobile ? 'icon-home' : ''}>{isMobile ? '' : 'HOME'}</Link>
        </li>
        <li>
          <Link to="/TheBay" className={isMobile ? 'icon-the-bay' : ''}>{isMobile ? '' : 'THE BAY'}</Link>
        </li>
        <li>
          <Link to="/About" className={isMobile ? 'icon-about' : ''}>{isMobile ? '' : 'ABOUT'}</Link>
        </li>
      </ul>
      <ul className="login/reg block">
        <li><Link to="/Login">Login</Link></li>
        <li><Link to="/Register">Register</Link></li>
      </ul>
    </nav>
  );
}

export default NavBar;