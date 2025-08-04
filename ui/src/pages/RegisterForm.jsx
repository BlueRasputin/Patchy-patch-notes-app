import React, { useState } from 'react';
import { useAuth } from '../components/Services/authContext';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

// import './RegisterForm.css';


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
            setError('Passwords do not match!');
            return;
        }

        const user = {
            email,
            password,
            username,
            verifyPassword
        };
        try {
            const response = await fetch("http://localhost:8080/api/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(user),
            });
            
            console.log(response.data);
            const userData = await response.json();
            if (response.ok) {
                login(userData);
                toast.success('Ahoy! Welcome Aboard!');
                // Redirect to the home page after successful registration
                redirect('/');
            } else {
                setError(response.error || 'Registration failed');
                console.error(response.error);
            }
        } catch (error) {
            setError('Registration failed: ' + error.message);
            console.error('Argh! Registration failed:', error);
        }
    };
    return (
        <div className="register-form">
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