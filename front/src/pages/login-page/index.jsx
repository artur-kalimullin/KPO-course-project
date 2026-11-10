import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../shared/auth";

export const LoginPage = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setIsLoading(true);

        try {
            const role = await login(username, password);
            
            if (role === "ROLE_ADMIN") {
                navigate("/admin");
            } else if (role === "ROLE_TEACHER") {
                navigate("/teacher");
            } else if (role === "ROLE_STUDENT") {
                navigate("/student");
            } else {
                navigate("/");
            }
        } catch (err) {
            setError("Неверный логин или пароль");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="container py-5 d-flex justify-content-center align-items-center" style={{ minHeight: "70vh" }}>
            <div className="card bg-dark bg-opacity-75 text-white shadow-lg p-4" style={{ width: "100%", maxWidth: "400px", borderRadius: "1rem" }}>
                <h2 className="text-center mb-4">
                    <i className="bi bi-shield-lock me-2 text-warning"></i>
                    Вход в систему
                </h2>
                
                {error && <div className="alert alert-danger py-2">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="form-label text-light">Логин</label>
                        <input
                            type="text"
                            className="form-control"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            autoFocus
                        />
                    </div>
                    <div className="mb-4">
                        <label className="form-label text-light">Пароль</label>
                        <input
                            type="password"
                            className="form-control"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="btn btn-warning w-100 fw-bold" disabled={isLoading}>
                        {isLoading ? "Загрузка..." : "Войти"}
                    </button>
                </form>
                <div className="text-center mt-3">
                    <button className="btn btn-link text-light text-decoration-none" onClick={() => navigate("/")}>
                        Вернуться на главную
                    </button>
                </div>
            </div>
        </div>
    );
};