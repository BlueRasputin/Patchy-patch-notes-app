import './Header.css';

function Header() {
    return (
    <>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      
        <div className="Header">
            <img className="logo" src="./src/assets/icons/Patchy-logo.png" alt="Patchy Logo" />
            <h1 className="site-title">Patchy</h1>
        </div>
        
    </>
    )
    }

export default Header;