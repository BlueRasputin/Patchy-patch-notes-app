import './LoadingSpinner.css';
import shipWheel from '../../assets/icons/ship-wheel.png';

const LoadingSpinner = ({ message = "Loading...", subtitle = "" }) => {
    return (
        <div className="loading-spinner-container">
            <div className="ship-wheel-spinner">
                <img
                    src={shipWheel}
                    alt="Loading..."
                    className="spinning-wheel"
                />
            </div>
            <h2 className="loading-message">{message}</h2>
            {subtitle && <p className="loading-subtitle">{subtitle}</p>}
        </div>
    );
};

export default LoadingSpinner;
