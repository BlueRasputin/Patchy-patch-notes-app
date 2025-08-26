import { createContext, useContext } from "react";

export const AuthUserContext = createContext();

export const useAuth = () => {
  return useContext(AuthUserContext);
};


export default AuthUserContext;