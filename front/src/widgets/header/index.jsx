import { Link } from "react-router-dom";
import "./styles.css";

export const Header = () => {
    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-transparent py-3">
            <div className="container">
                <Link className="navbar-brand d-flex align-items-center" to="/">
                    <i className="bi bi-mortarboard-fill me-2 brand-icon"></i>
                    <span className="brand-text">Анализ успеваемости в вузе</span>
                </Link>
                <div className="ms-auto">
                    <Link to="/login" className="btn btn-outline-light rounded-pill px-4">
                        <i className="bi bi-box-arrow-in-right me-2"></i>Войти в систему
                    </Link>
                </div>
            </div>
        </nav>
    );
};