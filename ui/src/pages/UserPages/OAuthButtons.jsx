import { useEffect, useState } from 'react';
import { apiFetch, API_BASE } from '../../Services/api';

const LABELS = { github: 'Continue with GitHub', google: 'Continue with Google' };

// Only shows providers the backend has credentials for
const OAuthButtons = () => {
    const [providers, setProviders] = useState([]);

    useEffect(() => {
        apiFetch('/api/auth/providers')
            .then((response) => (response.ok ? response.json() : []))
            .then(setProviders)
            .catch(() => setProviders([]));
    }, []);

    if (providers.length === 0) {
        return null;
    }

    return (
        <>
            <div className="oauth-divider">or</div>
            {providers.map((provider) => (
                <a key={provider} className="oauth-login" href={`${API_BASE}/oauth2/authorization/${provider}`}>
                    {LABELS[provider] ?? provider}
                </a>
            ))}
        </>
    );
};

export default OAuthButtons;
