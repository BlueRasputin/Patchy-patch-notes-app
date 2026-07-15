import { useState, useEffect } from "react";
import { AuthUserContext } from "./authContext";

// Keeps the logged-in user in localStorage so auth survives page reloads
export const AuthUserProvider = ({ children }) => {
  const [userState, setUserState] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try {
        setUserState(JSON.parse(storedUser));
      } catch (error) {
        console.error("Failed to parse stored user data:", error);
        localStorage.removeItem("user");
      }
    }
  }, []);

  const login = (userData) => {
    setUserState(userData);
    localStorage.setItem("user", JSON.stringify(userData));
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
