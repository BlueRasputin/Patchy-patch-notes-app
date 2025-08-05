import './HeaderFooter.css';
import { Link } from 'react-router-dom';
import { useAuth } from "../Services/authContext";
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
        console.log("checkUserLoggedIn result:", authStatus);
        return authStatus;
    };

    return (
    <>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      
       <div className="Header">
                <img className="logo" src="./src/assets/icons/Patchy-logo.png" alt="Patchy_Logo" />
                
                <ul className="user-nav">
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
        </>
    )
    }

export default Header;