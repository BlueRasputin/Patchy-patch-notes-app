import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../Services/authContext';
import { apiFetch } from '../../Services/api';
import './AuthPage.css';

// Editors may only receive tokens through their own URI handler
const EDITOR_REDIRECT = /^(vscode|vscode-insiders|vscodium|cursor|windsurf):\/\/barrcon\.patchy\/auth$/;

// Hands a personal API token to an editor plugin or the MCP server
const Connect = () => {
    const { isAuthenticated } = useAuth();
    const [searchParams] = useSearchParams();
    const client = (searchParams.get('client') || 'Editor').slice(0, 60);
    const redirectUri = searchParams.get('redirect_uri');
    const [token, setToken] = useState('');
    const [error, setError] = useState('');

    const connect = async () => {
        setError('');
        const response = await apiFetch('/api/me/tokens', {
            method: 'POST',
            body: JSON.stringify({ name: client }),
        });
        if (!response.ok) {
            setError('Argh! Couldn\'t create a token. Log in again and retry.');
            return;
        }
        const created = await response.json();
        setToken(created.token);
        if (redirectUri && EDITOR_REDIRECT.test(redirectUri)) {
            // state lets the editor reject tokens it didn't ask for
            const state = encodeURIComponent(searchParams.get('state') ?? '');
            window.location.href = `${redirectUri}?token=${encodeURIComponent(created.token)}&state=${state}`;
        }
    };

    if (!isAuthenticated()) {
        return (
            <div className="user-form">
                <h1>Connect {client}</h1>
                <p>Log in to Patchy first, then come back to this page from {client}.</p>
                <Link className="oauth-login" to="/login">Log in</Link>
            </div>
        );
    }

    return (
        <div className="user-form">
            <h1>Connect {client}</h1>
            <p>{client} will be able to read yer Bay and add the techs it finds in yer projects.</p>
            {error && <div className="error-banner">{error}</div>}
            {!token ? (
                <button type="button" onClick={connect}>Connect {client}</button>
            ) : (
                <>
                    <p>
                        {redirectUri && EDITOR_REDIRECT.test(redirectUri)
                            ? `Sent to ${client}. If nothing happened, copy this token into it:`
                            : `Copy this token into ${client}. It won't be shown again:`}
                    </p>
                    <div className="token-box">
                        <input readOnly value={token} onFocus={(e) => e.target.select()} aria-label="API token" />
                        <button type="button" onClick={() => navigator.clipboard.writeText(token)}>Copy</button>
                    </div>
                </>
            )}
        </div>
    );
};

export default Connect;
