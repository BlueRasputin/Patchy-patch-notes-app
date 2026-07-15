import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from '../../Services/authContext';
import { toast } from 'react-toastify';
import { apiFetch } from '../../Services/api';

const Logout = () => {
    const redirect = useNavigate();
    const { logout } = useAuth();

    useEffect(() => {
        const runLogout = async () => {
            try {
                await apiFetch("/api/logout");
            } catch {
                toast.error("Argh! There was an error logging you out.");
            } finally {
                logout();
                redirect("/");
            }
        };

        runLogout();
    }, [redirect, logout]);

    return null;
};

export default Logout;
