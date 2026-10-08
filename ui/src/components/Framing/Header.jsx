import './HeaderFooter.css';
import { Link } from 'react-router-dom';
import { useAuth } from "../../Services/authContext";
import logo from '../../assets/icons/Patchy-logo-2.png';

function Header() {
    const { userState, isAuthenticated } = useAuth();

    return (
        <header className="Header">
            <Link to="/" className="brand" aria-label="Patchy home">
                <img className="logo" src={logo} alt="" />
                <span className="title-section">
                    <span className="site-title">Patchy</span>
                    <span className="site-tagline">Find yer heading in seas of changelogs</span>
                </span>
            </Link>
            <nav className="user-nav" aria-label="Account">
                <ul>
                    {isAuthenticated() ? (
                        <>
                            {userState?.username && (
                                <li className="welcome-message">
                                    Ahoy, {userState.username}
                                </li>
                            )}
                            <li><Link to="/Logout">Log out</Link></li>
                        </>
                    ) : (
                        <>
                            <li><Link to="/login">Log in</Link></li>
                            <li><Link to="/register">Create account</Link></li>
                        </>
                    )}
                </ul>
            </nav>
        </header>
    );
}

export default Header;
