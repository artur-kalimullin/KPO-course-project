import "./styles.css";

export const Footer = () => {
    return (
        <footer className="footer py-4">
            <div className="container">
                <div className="row gy-4">
                    <div className="col-lg-4 text-center text-lg-start">
                        <h5 className="text-warning mb-3">Вуз города Z</h5>
                        <p className="text-light opacity-75 mb-0">
                            Молодое, динамично развивающееся высшее учебное заведение,
                            соединяющее традиции и инновации.
                        </p>
                    </div>
                    <div className="col-lg-4 text-center text-lg-start">
                        <h5 className="text-warning mb-3">Контакты</h5>
                        <ul className="list-unstyled text-light opacity-75 mb-0">
                            <li className="mb-2">
                                <i className="bi bi-geo-alt-fill me-2 text-warning"></i>
                                г. Z, ул. Университетская, 15
                            </li>
                            <li className="mb-2">
                                <i className="bi bi-telephone-fill me-2 text-warning"></i>
                                +7 (842) 123-45-67
                            </li>
                            <li className="mb-2">
                                <i className="bi bi-envelope-fill me-2 text-warning"></i>
                                info@vuz-z.ru
                            </li>
                        </ul>
                    </div>
                    <div className="col-lg-4 text-center text-lg-start">
                        <h5 className="text-warning mb-3">Режим работы</h5>
                        <ul className="list-unstyled text-light opacity-75 mb-0">
                            <li className="mb-2">
                                <i className="bi bi-clock-fill me-2 text-warning"></i>
                                Пн-Пт: 08:00 – 21:00
                            </li>
                            <li className="mb-2">
                                <i className="bi bi-clock-fill me-2 text-warning"></i>
                                Сб: 09:00 – 16:00
                            </li>
                            <li className="mb-2">
                                <i className="bi bi-clock-fill me-2 text-warning"></i>
                                Вс: выходной
                            </li>
                        </ul>
                    </div>
                </div>
                <hr className="mt-4 border-light opacity-25" />
                <div className="text-center text-light opacity-50 small">
                    © 2026 Вуз города Z. Все права защищены.
                </div>
            </div>
        </footer>
    );
};