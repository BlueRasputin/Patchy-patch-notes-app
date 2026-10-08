import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css'
import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Outlet, useLocation } from 'react-router-dom';
import HomePage from './pages/HomePage/HomePage.jsx';
import Techs from './pages/Techs/Techs.jsx';
import About from './pages/AboutPage/About.jsx';
import TheBay from './pages/TheBay/TheBay.jsx';
import UserProfile from './pages/UserPages/UserProfile.jsx';
import LoginForm from './pages/UserPages/LoginForm.jsx';
import RegisterForm from './pages/UserPages/RegisterForm.jsx';
import Logout from './pages/UserPages/Logout.jsx';
import Connect from './pages/UserPages/Connect.jsx';
import PackageInsights from './pages/PackageInsights/PackageInsights.jsx';
import ComparePatchNotes from './pages/ComparePatchNotes/ComparePatchNotes.jsx';
import Embed from './pages/Embed/Embed.jsx';
import Header from './components/Framing/Header.jsx';
import NavBar from './components/Framing/NavBar.jsx';
import Footer from './components/Framing/Footer.jsx';

const PAGE_TITLES = {
  '/': 'Latest patch notes',
  '/techs': 'Tech catalog',
  '/thebay': 'The Bay',
  '/packageinsights': 'Project insights',
  '/compare': 'Compare',
  '/about': 'About',
  '/userprofile': 'Profile',
  '/login': 'Log in',
  '/register': 'Create account',
  '/connect': 'Connect an editor',
};

// Header/nav/footer shell for every page except the iframe-able /embed
function Layout() {
  const { pathname } = useLocation();

  // Screen readers announce the new title on client-side navigation
  useEffect(() => {
    const title = PAGE_TITLES[pathname.toLowerCase()];
    document.title = title ? `${title} · Patchy` : 'Patchy';
  }, [pathname]);

  return (
    <>
      <a className="skip-link" href="#main">Skip to content</a>
      <Header />
      <NavBar />
      <main id="main" className="site-main" tabIndex={-1}>
        <Outlet />
      </main>
      <Footer />
    </>
  );
}

function App() {
  return (
    <>
      <Router>
        <Routes>
          <Route path="/embed" element={<Embed />} />
          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/Techs" element={<Techs />} />
            <Route path="/login" element={<LoginForm />} />
            <Route path="/register" element={<RegisterForm />} />
            <Route path="/logout" element={<Logout />} />
            <Route path="/connect" element={<Connect />} />
            <Route path="/TheBay" element={<TheBay />} />
            <Route path="/PackageInsights" element={<PackageInsights />} />
            <Route path="/Compare" element={<ComparePatchNotes />} />
            <Route path="/About" element={<About />} />
            <Route path="/UserProfile" element={<UserProfile />} />
          </Route>
        </Routes>
      </Router>
      <ToastContainer position="top-left" autoClose={3000} />
    </>
  );
}

export default App;
