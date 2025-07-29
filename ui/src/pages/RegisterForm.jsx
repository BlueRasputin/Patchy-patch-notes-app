import React, { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { registerUser } from '../../components/Services/authService';
import { useNavigate } from 'react-router-dom';
import './TheBay.css';


const  export RegisterForm = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const { login } = useAuth();
    const redirect = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        const user = {
            username,
            email,
            password,
            confirmPassword,
        };
        try {
            const response = await registerUser(user);
            if (response.success) {
                login(response.data);
                window.alert('Ahoy! Welcome Aboard!');
                // Redirect to the home page after successful registration
                redirect('/HomePage');
            } else {
                console.error(response.error);
            }
        } catch (error) {
            console.error('Argh! Registration failed:', error);
        }
    };
    return (
        <div className="register-form">
            <h2>Register</h2>
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
                    <label htmlFor="confirmPassword">Confirm Password:</label>
                    <input
                        type="password"
                        id="confirmPassword"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Register</button>
            </form>
        </div>
    );

};