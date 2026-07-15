import './HeaderFooter.css';
import { Link } from 'react-router-dom';
import { useAuth } from "../../Services/authContext";
import logo from '../../assets/icons/Patchy-logo-2.png';

function Header() {
    const { userState, isAuthenticated } = useAuth();

    return (
        <div className="Header">
            <div className="logo-section">
                <img className="logo" src={logo} alt="Patchy_Logo" />
            </div>
            <div className="title-section">
                <h1 className="site-title">Patchy</h1>
                <p className="site-tagline">Find yer heading in seas of changelogs</p>
            </div>
            <div className="user-nav">
                <ul>
                    {isAuthenticated() ? (
                        <>
                            {userState?.username && (
                                <li className="welcome-message">
                                    Ahoy, {userState.username}!
                                </li>
                            )}
                            <li><Link to="/Logout">Logout</Link></li>
                        </>
                    ) : (
                        <>
                            <li><Link to="/Login">Login</Link></li>
                            <li><Link to="/Register">Register</Link></li>
                        </>
                    )}
                </ul>
            </div>
        </div>
    );
}

export default Header;
