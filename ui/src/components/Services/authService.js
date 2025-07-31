export const loginUser = async (user) => {
    try {
        const response = await fetch('http://localhost:8080/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(user),
        });

        if (response.ok) {
            const userData = await response.json();
            return { success: true, data: userData };
        } else {
            let errorMessage = 'Login failed';
            if (response.status === 401) {
                errorMessage = 'Invalid username or password';
            }
            return { success: false, error: errorMessage };
        }
    } catch (error) {
        return { success: false, error: error.message };
    }
};

export const registerUser = async (user) => {
    try {
        const response = await fetch('http://localhost:8080/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(user),
        });

        if (response.ok) {
            const userData = await response.json();
            return { success: true, data: userData };
        } else {
            let errorMessage = 'Registration failed';
            if (response.status === 409) {
                errorMessage = 'Username already exists';
            }
            return { success: false, error: errorMessage };
        }
    } catch (error) {
        return { success: false, error: error.message };
    }
};

export default {
    loginUser,
    registerUser,
};
