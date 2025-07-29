
import './App.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import About from './pages/About.jsx';
import TheBay from './pages/The-Bay/The-Bay.jsx';
import Header from './components/Framing/Header.jsx';
import NavBar from './components/Framing/NavBar.jsx';

function App() {

  return (
    <>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="The-Bay" element={<The-Bay />} />
            <Route path="/About" element={<About />} />
          </Routes>
        

    </>
  );
}

export default App;

