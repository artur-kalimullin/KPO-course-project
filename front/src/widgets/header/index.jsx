import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../../shared/auth";
import { LogoutButton } from "../logout-button";
import "./styles.css";

export const Header = () => {
    const { user } = useAuth();

    const navLinkClass = ({ isActive }) => `nav-link ${isActive ? "active text-warning" : "text-light"}`;

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-transparent py-3">
            <div className="container">
                <Link className="navbar-brand d-flex align-items-center" to="/">
                    <i className="bi bi-mortarboard-fill me-2 brand-icon"></i>
                    <span className="brand-text">Анализ успеваемости</span>
                </Link>
                
                <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
                    <span className="navbar-toggler-icon"></span>
                </button>

                <div className="collapse navbar-collapse" id="mainNav">
                    <ul className="navbar-nav me-auto mb-2 mb-lg-0 ms-lg-4">
                        {user?.role === "ROLE_ADMIN" && (
                            <>
                                <li className="nav-item"><NavLink to="/admin" className={navLinkClass} end>Дашборд</NavLink></li>
                                <li className="nav-item"><NavLink to="/admin/groups" className={navLinkClass}>Группы</NavLink></li>
                                <li className="nav-item"><NavLink to="/admin/subjects" className={navLinkClass}>Предметы</NavLink></li>
                                <li className="nav-item"><NavLink to="/admin/users" className={navLinkClass}>Пользователи</NavLink></li>
                            </>
                        )}
                        {user?.role === "ROLE_TEACHER" && (
                            <li className="nav-item"><NavLink to="/teacher" className={navLinkClass}>Панель преподавателя</NavLink></li>
                        )}
                        {user?.role === "ROLE_STUDENT" && (
                            <li className="nav-item"><NavLink to="/student" className={navLinkClass}>Мои оценки</NavLink></li>
                        )}
                    </ul>

                    <div className="d-flex align-items-center gap-3">
                        {user ? (
                            <>
                                <span className="text-light opacity-75 d-none d-lg-block">
                                    <i className="bi bi-person-circle me-1"></i>
                                    {user.role === "ROLE_ADMIN" ? "Администратор" : user.role === "ROLE_TEACHER" ? "Преподаватель" : "Студент"}
                                </span>
                                <LogoutButton />
                            </>
                        ) : (
                            <Link to="/login" className="btn btn-outline-light rounded-pill px-4">
                                <i className="bi bi-box-arrow-in-right me-2"></i>Войти
                            </Link>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};