import React, { useState, useEffect } from 'react';
import { useAuth } from '../components/Services/authContext';
import { useNavigate } from 'react-router-dom';
import './AuthPage.css';
import { toast } from 'react-toastify';

const UserProfile = () => {
    const { userState, isAuthenticated, login } = useAuth();
    const [username, setUsername] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        if (!isAuthenticated()) {
            toast.error("Ye need to be logged in to view yer profile!");

            navigate('/Login');

            return;
        }

        // Pre-fill form with current user data
        if (userState) {
            setUsername(userState.username || '');
        }
    }, [isAuthenticated, userState, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        if (!username.trim()) {
            setError('Username cannot be empty!');
            setLoading(false);
            return;
        }

        try {
            // Get current user ID
            const userResponse = await fetch(`http://localhost:8080/api/currentUserId`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include",
            });

            if (!userResponse.ok) {
                throw new Error("Argh! Ye need to be logged in to change yer username!");
            }

            const userId = await userResponse.json();

            // Prepare update data
            const updateData = {
                username: username
            };


            // Update user
            const response = await fetch(`http://localhost:8080/users/${userId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: JSON.stringify(updateData),
            });

            if (!response.ok) {
                throw new Error("Argh! Failed to update yer profile!");
            }

            const updatedUser = await response.json();
            
            // Update the auth context with new user data
            login(updatedUser);
            
            setSuccess("Yer profile has been updated successfully, matey!");
            toast.success("Username updated successfully!");

        } catch (error) {
            console.error("Error updating profile:", error);
            setError(`Error: ${error.message}`);
            toast.error(`Error: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    if (!isAuthenticated()) {
        return null;
    }

    return (
        <div className="user-form">
            <div className="profile-container">
                <h2>Yer Profile</h2>
                <p className="profile-subtitle">Update yer account details, matey!</p>

                {error && (
                    <div className="error-banner">
                        {error}
                    </div>
                )}

                {success && (
                    <div className="success-banner">
                        {success}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="profile-form">
                    <div className="form-group">
                        <label htmlFor="username">Username:</label>
                        <input
                            type="text"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            placeholder="Enter yer new username"
                        />
                    </div>

                    <div className="form-actions">
                        <button 
                            type="submit" 
                            disabled={loading}
                            className="update-button"
                        >
                            {loading ? 'Updating...' : 'Update Profile'}
                        </button>
                        
                        <button 
                            type="button" 
                            onClick={() => navigate('/')}
                            className="cancel-button"
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default UserProfile;