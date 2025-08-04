import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from '../components/Services/authContext';


const Logout = () => {
    const redirect = useNavigate();
    const { logout } = useAuth(); 

    useEffect(() => {
        const Logout = async () => {
            try {
                
                await fetch("http://localhost:8080/api/logout", { 
                    method: "GET", 
                    credentials: "include" 
                });
            
                if (logout) {
                    logout();
                }
                
                redirect("/");
                
            } catch (error) {
                console.error("Error during logout", error);
                window.alert("Argh! There was an error logging you out.");
                if (logout) {
                    logout();
                }
                redirect("/");
            }
        };

        Logout();
    }, [redirect, logout]);

    return null; 
};

export default Logout;