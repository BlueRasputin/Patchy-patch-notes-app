import { Link } from 'react-router-dom';
import './NavBar.css';
// import { useAuth } from "../components/Services/authContext";
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
        <li>
          <Link to="/UserProfile" className={isMobile ? 'icon-user-profile' : ''}>{isMobile ? '' : 'USER PROFILE'}</Link>
        </li>
      </ul>
      
    </nav>
  );
}

export default NavBar;