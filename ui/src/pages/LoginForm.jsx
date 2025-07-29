

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
                // Redirect to the home page after successful login
                redirect('/TheBay');
            } else {
                console.error(response.error);
            }
        } catch (error) {
            console.error('Argh! Login failed:', error);
        }
    }
};
