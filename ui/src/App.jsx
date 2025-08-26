import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage/HomePage.jsx';
import About from './pages/AboutPage/About.jsx';
import TheBay from './pages/TheBay/TheBay.jsx';
import UserProfile from './pages/UserPages/UserProfile.jsx';
import LoginForm from './pages/UserPages/LoginForm.jsx';
import RegisterForm from './pages/UserPages/RegisterForm.jsx';
import Logout from './pages/UserPages/Logout.jsx';
import Header from './components/Framing/Header.jsx';
import NavBar from './components/Framing/NavBar.jsx';
import Footer from './components/Framing/Footer.jsx';
import {AuthUserProvider} from './Services/AuthUserProvider.jsx';



function App() {

  return (
    <>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <AuthUserProvider>
        <Router>
        <Header />
        <NavBar />
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginForm />} />
            <Route path="/register" element={<RegisterForm />} />
            <Route path="/logout" element={<Logout />} />
            <Route path="/TheBay" element={<TheBay />} />
            <Route path="/About" element={<About />} />
            <Route path="/UserProfile" element={<UserProfile />} />
          </Routes>
          <Footer />
        </Router>
        <ToastContainer position="top-left" autoClose={3000} />
      </AuthUserProvider>
      
    </>
  );
}

export default App;

