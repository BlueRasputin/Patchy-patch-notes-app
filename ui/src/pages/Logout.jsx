import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const Logout = () => {
  const redirect = useNavigate();
  useEffect(() => {
    fetch("http://localhost:8080/api/logout", { method: "GET", credentials: "include" })
      .then(() => {
        redirect("/");
        window.alert("Captain has left the helm!"); // redirect to home page
      })
      .catch((error) => {
        console.error("Error during logout", error);
      });
  }, [redirect]);

  return null; 
};

export default Logout;