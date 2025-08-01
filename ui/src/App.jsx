
import './App.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import About from './pages/About.jsx';
import TheBay from './pages/TheBay/TheBay.jsx';
import LoginForm from './pages/LoginForm.jsx';
import RegisterForm from './pages/RegisterForm.jsx';
import Header from './components/Framing/Header.jsx';
import NavBar from './components/Framing/NavBar.jsx';
function App() {

  return (
    <>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <Router>
        <Header />
        <NavBar />
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginForm />} />
            <Route path="/register" element={<RegisterForm />} />
            <Route path="/TheBay" element={<TheBay />} />
            <Route path="/About" element={<About />} />
          </Routes>
        </Router>
        

    </>
  );
}

export default App;

