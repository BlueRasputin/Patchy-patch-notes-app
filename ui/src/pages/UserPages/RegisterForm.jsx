import { useState } from 'react';
import { useAuth } from '../../Services/authContext';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { apiFetch } from '../../Services/api';

import './AuthPage.css';


const RegisterForm = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [verifyPassword, setVerifyPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useAuth();
    const redirect = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (password !== verifyPassword) {
            setError('Argh! Yer passwords don\'t match!');
            return;
        }

        const user = {
            email,
            password,
            username,
            verifyPassword
        };
        
        try {
            const registerResponse = await apiFetch("/api/register", {
                method: "POST",
                body: JSON.stringify(user),
            });
            if (!registerResponse.ok) {
                throw new Error("Argh! Couldn't register ye!")
            }

            // Log the user in right after successful registration
            const loginResponse = await apiFetch("/api/login", {
                method: "POST",
                body: JSON.stringify(user)
            });
            if (loginResponse.ok) {
                const userData = await loginResponse.json();
                login(userData);
                toast.success('Ahoy! Welcome Aboard!');
                redirect('/');
            } else {
                setError('Registered, but automatic login failed. Please log in.');
            }
        } catch (error) {
            setError('Argh! Registration failed: ' + error.message);
        }
    };
    return (
        <div className="user-form">
            <h2>Register</h2>
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
                    <label htmlFor="email">Email:</label>
                    <input
                        type="email"
                        id="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
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
                <div>
                    <label htmlFor="verifyPassword">Verify Password:</label>
                    <input
                        type="password"
                        id="verifyPassword"
                        value={verifyPassword}
                        onChange={(e) => setVerifyPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Register</button>
            </form>
        </div>
    );

};

export default RegisterForm;