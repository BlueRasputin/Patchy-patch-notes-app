import { useState } from 'react';
import { useAuth } from '../../Services/authContext';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { apiFetch } from '../../Services/api';

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

        try {
            const response = await apiFetch("/api/login", {
                method: "POST",
                body: JSON.stringify({ username, password }),
            });
            if (response.ok) {
                const userData = await response.json();
                login(userData);
                toast.success('Ahoy! Captain on Deck!');
                redirect('/TheBay');
            } else {
                setError('Argh! Couldn\'t log ye in: check yer username and password.');
            }
        } catch (error) {
            setError('Argh! Couldn\'t log ye in: ' + error.message);
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