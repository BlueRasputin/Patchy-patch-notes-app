import React, { useState, useEffect } from 'react';
import { useAuth } from '../components/Services/authContext';
import { useNavigate } from 'react-router-dom';
// import './UserProfile.css';
import { toast } from 'react-toastify';

const UserProfile = () => {
    const { userState, isAuthenticated, login } = useAuth();
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
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
            setEmail(userState.email || '');
        }
    }, [isAuthenticated, userState, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        // Validate passwords if user is trying to change password
        if (newPassword && newPassword !== confirmPassword) {
            setError('New passwords do not match!');
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
                throw new Error("Argh! Ye need to be logged in!");
            }

            const userId = await userResponse.json();

            // Prepare update data
            const updateData = {
                username: username,
                email: email,
                favoriteTechs: userState?.favoriteTechs || []
            };

            // Only include password if user wants to change it
            if (newPassword) {
                updateData.password = newPassword;
            } else {
                // Keep existing password (you might need to handle this differently based on your backend)
                updateData.password = currentPassword;
            }

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
            
            // Clear password fields
            setCurrentPassword('');
            setNewPassword('');
            setConfirmPassword('');

        } catch (error) {
            console.error("Error updating profile:", error);
            setError(`Error: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    if (!isAuthenticated()) {
        return null; // Will redirect in useEffect
    }

    return (
        <div className="user-profile">
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

                    <div className="form-group">
                        <label htmlFor="email">Email:</label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            placeholder="Enter yer email"
                        />
                    </div>

                    <div className="password-section">
                        <h3>Change Password (Optional)</h3>
                        <p className="password-note">Leave blank to keep current password</p>
                        
                        <div className="form-group">
                            <label htmlFor="currentPassword">Current Password:</label>
                            <input
                                type="password"
                                id="currentPassword"
                                value={currentPassword}
                                onChange={(e) => setCurrentPassword(e.target.value)}
                                placeholder="Enter current password"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="newPassword">New Password:</label>
                            <input
                                type="password"
                                id="newPassword"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                placeholder="Enter new password"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmPassword">Confirm New Password:</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                placeholder="Confirm new password"
                            />
                        </div>
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