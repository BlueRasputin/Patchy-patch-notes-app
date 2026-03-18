import './HeaderFooter.css';
import { Link } from 'react-router-dom';
import { useAuth } from "../../Services/authContext";
import { useEffect } from "react";


function Header() {
  const { userState, isAuthenticated } = useAuth();

  

 useEffect(() => {
        // Log the current auth state for debugging
        console.log("Auth state changed:", {
            isAuthenticated: isAuthenticated(),
            userState: userState
        });
    }, [isAuthenticated, userState]);


    // Method to check if user is logged in
    const checkUserLoggedIn = () => {
        const authStatus = isAuthenticated() && userState !== null;
        return authStatus;
    };

    return (
    <>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      
       <div className="Header">
       <div className="logo-section">
           <img className="logo" src="./src/assets/icons/Patchy-logo-2.png" alt="Patchy_Logo" />
       </div>
                <div className="title-section">
                    <h1 className="site-title">Patchy</h1>
                    <p className="site-tagline">Find yer heading in seas of changelogs</p>
                </div>
                <div className="user-nav">
                    <ul>
                        {checkUserLoggedIn() ? (
                            // Display welcome message and logout link when logged in
                            <>
                            
                                {userState?.username && (
                                    <li className="welcome-message">
                                        Ahoy, {userState.username}!
                                    </li>
                                )}
                                <li><Link to="/Logout">Logout</Link></li>
                            </>
                        ) : (
                            // Show login/register buttons when not logged in
                            <>
                                <li><Link to="/Login">Login</Link></li>
                                <li><Link to="/Register">Register</Link></li>
                            </>
                        )}
                    </ul>
                </div>
            </div>
        </>
    )
    }

export default Header;