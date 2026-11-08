import { useState } from "react";
import { Link, NavLink, useLocation } from "react-router-dom"; // <-- Добавили useLocation
import { useAuth } from "../../shared/auth";
import { LogoutButton } from "../logout-button";
import "./styles.css";

export const Header = () => {
    const { user } = useAuth();
    const [isOpen, setIsOpen] = useState(false);
    const location = useLocation();

    const navLinkClass = ({ isActive }) => `nav-link ${isActive ? "active text-warning" : "text-light"}`;
    const closeMenu = () => setIsOpen(false);

    const isLoginPage = location.pathname === "/login";

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-transparent py-3">
            <div className="container">
                <Link className="navbar-brand d-flex align-items-center" to="/" onClick={closeMenu}>
                    <i className="bi bi-mortarboard-fill me-2 brand-icon"></i>
                    <span className="brand-text">Анализ успеваемости</span>
                </Link>
                
                <button 
                    className={`navbar-toggler ${isOpen ? "" : "collapsed"}`} 
                    type="button" 
                    onClick={() => setIsOpen(!isOpen)}
                    aria-expanded={isOpen}
                >
                    <span className="navbar-toggler-icon"></span>
                </button>

                <div className={`collapse navbar-collapse ${isOpen ? "show" : ""}`} id="mainNav">
                    <ul className="navbar-nav me-auto mb-2 mb-lg-0 ms-lg-4">
                        {user?.role === "ROLE_ADMIN" && (
                            <>
                                <li className="nav-item"><NavLink to="/admin" className={navLinkClass} onClick={closeMenu} end>Дашборд</NavLink></li>
                                <li className="nav-item"><NavLink to="/admin/groups" className={navLinkClass} onClick={closeMenu}>Группы</NavLink></li>
                                <li className="nav-item"><NavLink to="/admin/subjects" className={navLinkClass} onClick={closeMenu}>Предметы</NavLink></li>
                                <li className="nav-item"><NavLink to="/admin/users" className={navLinkClass} onClick={closeMenu}>Пользователи</NavLink></li>
                            </>
                        )}
                        {user?.role === "ROLE_TEACHER" && (
                            <li className="nav-item"><NavLink to="/teacher" className={navLinkClass} onClick={closeMenu}>Панель преподавателя</NavLink></li>
                        )}
                        {user?.role === "ROLE_STUDENT" && (
                            <li className="nav-item"><NavLink to="/student" className={navLinkClass} onClick={closeMenu}>Мои оценки</NavLink></li>
                        )}
                    </ul>

                    <div className="d-flex align-items-center gap-3 mt-3 mt-lg-0">
                        {user ? (
                            <>
                                <span className="text-light opacity-75 d-none d-lg-block">
                                    <i className="bi bi-person-circle me-1"></i>
                                    {user.role === "ROLE_ADMIN" ? "Администратор" : user.role === "ROLE_TEACHER" ? "Преподаватель" : "Студент"}
                                </span>
                                <LogoutButton className="px-3 fw-bold" />
                            </>
                        ) : (
                            !isLoginPage && (
                                <Link to="/login" className="btn btn-outline-light rounded-pill px-4" onClick={closeMenu}>
                                    <i className="bi bi-box-arrow-in-right me-2"></i>Войти
                                </Link>
                            )
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};