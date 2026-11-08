import { Link } from "react-router-dom";

export const AdminDashboard = () => {
    const cards = [
        {
            title: "Управление группами",
            icon: "bi-people-fill",
            path: "/admin/groups",
            desc: "Создание групп, назначение кураторов и привязка студентов.",
        },
        {
            title: "Управление предметами",
            icon: "bi-book-fill",
            path: "/admin/subjects",
            desc: "Добавление и редактирование учебных дисциплин.",
        },
        {
            title: "Управление пользователями",
            icon: "bi-person-fill-gear",
            path: "/admin/users",
            desc: "Добавление преподавателей и студентов, смена паролей.",
        },
    ];

    return (
        <div className="container py-4">
            <h1 className="text-white mb-4 text-center">Добро пожаловать, Администратор!</h1>
            <p className="text-light text-center mb-5 opacity-75">Выберите раздел для управления системой</p>
            
            <div className="row g-4 justify-content-center">
                {cards.map((card, index) => (
                    <div className="col-md-6 col-lg-4" key={index}>
                        <div className="card h-100 bg-dark bg-opacity-75 text-white shadow border-0 border-top border-warning border-4">
                            <div className="card-body text-center d-flex flex-column align-items-center p-4">
                                <div className="bg-warning rounded-circle d-flex align-items-center justify-content-center mb-3" style={{ width: "70px", height: "70px" }}>
                                    <i className={`bi ${card.icon} fs-2 text-dark`}></i>
                                </div>
                                <h5 className="card-title fw-bold">{card.title}</h5>
                                <p className="card-text text-light opacity-75 small mb-4">{card.desc}</p>
                                <Link to={card.path} className="btn btn-outline-warning mt-auto w-100 fw-bold">
                                    Перейти
                                </Link>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};