import React from 'react';
import './LoadingSpinner.css';

const LoadingSpinner = ({ message = "Loading...", subtitle = "" }) => {
    return (
        <div className="loading-spinner-container">
            <div className="ship-wheel-spinner">
                <img 
                    src="/src/assets/icons/ship-wheel.png" 
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