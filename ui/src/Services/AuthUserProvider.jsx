import { useState, useEffect } from "react";
import { AuthUserContext } from "./authContext";
import { apiFetch } from "./api";
import { clearLocalBay, loadLocalBay } from "./localBay";

// Techs followed before signing in move into the account
const mergeLocalBay = async () => {
  const techNames = [...loadLocalBay()];
  if (techNames.length === 0) return;
  const response = await apiFetch("/api/me/favorites", { method: "POST", body: JSON.stringify({ techNames }) });
  if (response.ok) clearLocalBay();
};

// Keeps the logged-in user in localStorage so auth survives page reloads
export const AuthUserProvider = ({ children }) => {
  const [userState, setUserState] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try {
        setUserState(JSON.parse(storedUser));
        return;
      } catch (error) {
        console.error("Failed to parse stored user data:", error);
        localStorage.removeItem("user");
      }
    }

    // No stored user — pick up a server session (e.g. after GitHub OAuth redirect)
    const syncFromServer = async () => {
      try {
        const response = await apiFetch("/api/me");
        if (response.ok) {
          const userData = await response.json();
          setUserState(userData);
          localStorage.setItem("user", JSON.stringify(userData));
          mergeLocalBay().catch(() => {});
        }
      } catch {
        // Backend unreachable; stay logged out
      }
    };
    syncFromServer();
  }, []);

  const login = (userData) => {
    setUserState(userData);
    localStorage.setItem("user", JSON.stringify(userData));
    mergeLocalBay().catch(() => {});
  };

  const logout = () => {
    localStorage.removeItem("user");
    setUserState(null);
  };

  const isAuthenticated = () => !!userState;

  return (
    <AuthUserContext.Provider
      value={{ userState, setUserState, login, logout, isAuthenticated }}
    >
      {children}
    </AuthUserContext.Provider>
  );
};

export default AuthUserProvider;
