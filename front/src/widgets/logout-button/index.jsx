import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../shared/auth";

export const LogoutButton = ({ variant = "btn-outline-danger", className = "" }) => {
    const { logout } = useAuth();
    const navigate = useNavigate();
    const [showModal, setShowModal] = useState(false);

    const handleLogout = async () => {
        setShowModal(false);
        await logout();
        navigate("/");
    };

    return (
        <>
            <button className={`btn btn-sm ${variant} ${className}`} onClick={() => setShowModal(true)}>
                <i className="bi bi-box-arrow-right me-1"></i> Выйти
            </button>

            {showModal && (
                <div className="modal fade show d-block" tabIndex="-1" style={{ backgroundColor: "rgba(0,0,0,0.6)", zIndex: 1050 }}>
                    <div className="modal-dialog modal-dialog-centered">
                        <div className="modal-content bg-dark text-white border-secondary shadow-lg">
                            <div className="modal-header border-secondary">
                                <h5 className="modal-title">Подтверждение выхода</h5>
                                <button type="button" className="btn-close btn-close-white" onClick={() => setShowModal(false)}></button>
                            </div>
                            <div className="modal-body fs-5">
                                Вы действительно хотите выйти из системы?
                            </div>
                            <div className="modal-footer border-secondary">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                                    Отмена
                                </button>
                                <button type="button" className="btn btn-danger" onClick={handleLogout}>
                                    Выйти
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};