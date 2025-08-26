
import React, { useState, useEffect} from "react";
import { AuthUserContext } from "./authContext";


export const AuthUserProvider = ({ children }) => {
  const [userState, setUserState] = useState(null);
  // Stores user info in local storage to be accessible across all pages
  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try { //parse stored user data
        setUserState(JSON.parse(storedUser));
      } catch (error) {
        console.error("Failed to parse stored user data:", error);
        localStorage.removeItem("user");
      }
    }
  }, []);

  const login = (userData) => {
    return new Promise((resolve) => {{
      setUserState(userData);
      localStorage.setItem("user", JSON.stringify(userData));
      resolve();
    }});
  };

  const logout = () => {
    localStorage.removeItem("user");
    setUserState(null);
  };

  const isAuthenticated = () => {
    return !!userState;
  };

  return (
    <AuthUserContext.Provider
      value={{ userState, setUserState, login, logout, isAuthenticated}}
    >
      {children}
    </AuthUserContext.Provider>
  );
};

export default AuthUserProvider;