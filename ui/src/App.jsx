
import './App.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage.jsx';
import About from './pages/About.jsx';
import TheBay from './pages/TheBay/TheBay.jsx';
import LoginForm from './pages/LoginForm.jsx';
import RegisterForm from './pages/RegisterForm.jsx';
import Logout from './pages/Logout.jsx';
import Header from './components/Framing/Header.jsx';
import NavBar from './components/Framing/NavBar.jsx';
import Footer from './components/Framing/Footer.jsx';
import {AuthUserProvider} from './components/Services/AuthUserProvider.jsx';



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
          </Routes>
          <Footer />
        </Router>
      </AuthUserProvider>

    </>
  );
}

export default App;

