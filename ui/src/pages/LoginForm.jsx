import React, { useState } from 'react';
import { useAuth } from '../components/Services/authContext';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

import './AuthPage.css';

const LoginForm = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useAuth();
    const redirect = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const user = {
            username,
            password
        };

        try {
            const response = await fetch("http://localhost:8080/api/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials:"include",
                body: JSON.stringify(user),

            });
            if (response.ok) {
                const userData = await response.json();
                console.log(userData);
                login(userData);
                toast.success('Ahoy! Captain on Deck!');
                // Redirect user to bay after successful login
                redirect('/TheBay');
            } else {
                console.error(response.error);
            }
        } catch (error) {
            console.error('Argh! Couldn\'t log ye in', error);
        }
    };

     return (
        <div className="user-form">
            <h2>Login</h2>
            {error && (
                <div className="error-banner">
                    {error}
                </div>
            )}
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="username">Username:</label>
                    <input
                        type="text"
                        id="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="password">Password:</label>
                    <input
                        type="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Login</button>
            </form>
        </div>
    );
};

export default LoginForm;