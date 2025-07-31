import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { loginUser } from '../components/Services/authService';
import { useNavigate } from 'react-router-dom';


const LoginForm = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const { login } = useAuth();
    const redirect = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

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
    }
};

export default LoginForm;