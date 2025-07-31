import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { loginUser } from '../components/Services/authService';
import { useNavigate } from 'react-router-dom';


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
            password,
        };

        try {
            const response = await loginUser(user);
            if (response.success) {
                login(response.data);
                window.alert('Ahoy! Welcome Back!');
                // Redirect to user's bay after successful login
                redirect('/TheBay');
            } else {
                console.error(response.error);
            }
        } catch (error) {
            console.error('Argh! Login failed:', error);
        }
    };

     return (
        <div className="login-form">
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