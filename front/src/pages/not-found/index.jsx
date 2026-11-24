import { useNavigate } from "react-router-dom";
import "./styles.css";

export const NotFoundPage = () => {
    const navigate = useNavigate();

    return (
        <div className="container py-4 py-lg-5">
            <div className="card not-found-card bg-dark bg-opacity-75 text-white border-0 shadow-lg rounded-4 mx-auto text-center p-4 p-md-5">
                <div className="not-found-code mb-2">404</div>
                <h1 className="h3 fw-bold mb-3">Страница не найдена</h1>
                <p className="text-light opacity-75 mb-4">
                    Возможно, страница была удалена, адрес изменился
                    или у вас нет прав на её просмотр.
                </p>
                <button
                    type="button"
                    className="btn btn-warning rounded-pill px-4 py-2 fw-bold"
                    onClick={() => navigate("/")}
                >
                    <i className="bi bi-house-door-fill me-2"></i>
                    Вернуться на главную
                </button>
            </div>
        </div>
    );
};
