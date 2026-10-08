import { useState, useEffect } from 'react';
import { useAuth } from '../../Services/authContext';
import { useNavigate } from 'react-router-dom';
import './AuthPage.css';
import { toast } from 'react-toastify';
import { apiFetch } from '../../Services/api';

const UserProfile = () => {
    const { userState, isAuthenticated, login } = useAuth();
    const [username, setUsername] = useState('');
    const [tokens, setTokens] = useState([]);
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

        if (userState) {
            setUsername(userState.username || '');
        }
    }, [isAuthenticated, userState, navigate]);

    useEffect(() => {
        apiFetch("/api/me/tokens")
            .then((response) => (response.ok ? response.json() : []))
            .then(setTokens)
            .catch(() => setTokens([]));
    }, []);

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
            const response = await apiFetch("/api/me", {
                method: "PUT",
                body: JSON.stringify({ username }),
            });

            if (response.status === 401) {
                throw new Error("Argh! Ye need to be logged in to change yer profile!");
            }
            if (!response.ok) {
                throw new Error(await response.text() || "Argh! Failed to update yer profile!");
            }

            const updatedUser = await response.json();
            login(updatedUser);

            setSuccess("Yer profile has been updated successfully, matey!");
            toast.success("Profile updated successfully!");

        } catch (error) {
            setError(`Error: ${error.message}`);
            toast.error(`Error: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    const revokeToken = async (id) => {
        const response = await apiFetch(`/api/me/tokens/${id}`, { method: "DELETE" });
        if (response.ok) {
            setTokens((previous) => previous.filter((token) => token.id !== id));
            toast.success("Editor disconnected.");
        } else {
            toast.error("Couldn't disconnect that editor.");
        }
    };

    if (!isAuthenticated()) {
        return null;
    }

    return (
        <div className="user-form">
            <div className="profile-container">
                <h1>Yer profile</h1>
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

                <h3 className="token-heading">Connected editors</h3>
                {tokens.length === 0 ? (
                    <p>No editors connected. Sign in from the Patchy VS Code or IntelliJ plugin.</p>
                ) : (
                    <ul className="token-list">
                        {tokens.map((token) => (
                            <li key={token.id}>
                                <span>
                                    {token.name}
                                    <small>
                                        {token.lastUsedAt
                                            ? ` · last used ${new Date(token.lastUsedAt).toLocaleDateString()}`
                                            : " · never used"}
                                    </small>
                                </span>
                                <button type="button" onClick={() => revokeToken(token.id)}>Disconnect</button>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
};

export default UserProfile;
