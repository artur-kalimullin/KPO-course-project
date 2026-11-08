import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import { MainPage } from "../pages/main-page";
import { Header } from "../widgets/header";
import { Footer } from "../widgets/footer";

const LoginStub = () => (
    <div className="container py-5 text-center">
        <div className="card p-5 shadow-lg mx-auto bg-dark bg-opacity-75 text-white" style={{ maxWidth: 500 }}>
            <h2 className="mb-3">
                <i className="bi bi-box-arrow-in-right me-2 text-warning"></i>
                Вход в систему
            </h2>
            <p className="text-light opacity-75 mb-4">
                Страница входа появится на Этапе реализации 1.
            </p>
            <Link to="/" className="btn btn-outline-light rounded-pill">
                <i className="bi bi-arrow-left me-2"></i>На главную
            </Link>
        </div>
    </div>
);

export const App = () => (
    <BrowserRouter>
        <div className="d-flex flex-column min-vh-100">
            <Header />
            <main className="flex-grow-1 d-flex align-items-center">
                <Routes>
                    <Route path="/" element={<MainPage />} />
                    <Route path="/login" element={<LoginStub />} />
                </Routes>
            </main>
            <Footer />
        </div>
    </BrowserRouter>
);