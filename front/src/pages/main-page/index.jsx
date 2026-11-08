import "./styles.css";

export const MainPage = () => {
    return (
        <div className="container py-4 py-lg-5">
            <div className="row align-items-center g-4 g-lg-5">
                <div className="col-lg-6 text-center">
                    <div className="university-photo-wrapper">
                        <img
                            src="/images/university.png"
                            alt="Вуз города Z"
                            className="img-fluid rounded-4 shadow-lg university-photo"
                        />
                    </div>
                </div>
                
                <div className="col-lg-6 text-white text-center text-lg-start">
                    <h1 className="display-4 fw-bold mb-3 mb-lg-4">Вуз города Z</h1>
                    <p className="mb-4">
                        <span className="highlight-gold">Вуз города Z</span> — современное
                        высшее учебное заведение, основанное в 2025 году. Мы готовим
                        специалистов в области информационных технологий, экономики,
                        инженерии и гуманитарных наук.
                    </p>
                    <p className="mb-4">
                        Наша миссия — предоставление качественного образования, развитие
                        научного потенциала и формирование профессиональной элиты региона.
                        Вуз оснащён современными лабораториями, библиотекой с электронными
                        ресурсами и комфортными общежитиями.
                    </p>
                    
                    <div className="d-flex gap-3 gap-md-4 mt-4 flex-wrap justify-content-center justify-content-lg-start">
                        <div className="stat-item text-center">
                            <div className="stat-number">5000+</div>
                            <div className="stat-label">Студентов</div>
                        </div>
                        <div className="stat-item text-center">
                            <div className="stat-number">150+</div>
                            <div className="stat-label">Преподавателей</div>
                        </div>
                        <div className="stat-item text-center">
                            <div className="stat-number">20+</div>
                            <div className="stat-label">Направлений</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};