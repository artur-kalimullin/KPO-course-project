import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Table, Spinner, Alert, Row, Col } from "react-bootstrap";

export const StudentGradesPage = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadData = async () => {
            try {
                const res = await apiFetch("/student/my-grades");
                setData(res);
            } catch (err) {
                setError("Ошибка загрузки данных");
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, []);

    const renderGrade = (value) => {
        if (value === 0) return <span className="fw-bold text-danger">Неявка</span>;
        if (value === 5) return <span className="fw-bold text-success fs-5">{value}</span>;
        if (value === 4) return <span className="fw-bold text-info fs-5">{value}</span>;
        if (value === 3) return <span className="fw-bold text-warning fs-5">{value}</span>;
        if (value === 2) return <span className="fw-bold text-danger fs-5">{value}</span>;
        return <span className="text-muted">-</span>;
    };

    const getAverageColor = (avg) => {
        if (avg >= 4.5) return "text-success"; 
        if (avg >= 3.5) return "text-info";    
        if (avg >= 2.5) return "text-warning"; 
        return "text-danger";                  
    };

    if (loading) return <div className="text-center mt-5"><Spinner animation="border" variant="warning" /></div>;
    if (error) return <Alert variant="danger" className="mt-4">{error}</Alert>;
    if (!data) return null;

    return (
        <div className="container py-4">
            <h2 className="text-white mb-4">Мои оценки</h2>

            <div className="bg-dark bg-opacity-75 text-white p-4 rounded shadow mb-4 border-start border-warning border-4">
                <h4 className="mb-1">{data.studentName}</h4>
                <p className="text-light opacity-75 mb-0">Группа: {data.groupName}</p>
            </div>

            <Row>
                <Col md={8}>
                    <div className="card bg-dark bg-opacity-75 text-white shadow border-0 mb-4">
                        <div className="card-header bg-transparent border-secondary fw-bold">
                            Таблица оценок
                        </div>
                        <Table hover responsive variant="dark" className="mb-0 align-middle">
                            <thead>
                                <tr>
                                    <th>Название предмета</th>
                                    <th className="text-center" style={{ width: "150px" }}>Оценка</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data.grades.map((g, idx) => (
                                    <tr key={idx}>
                                        <td className="fw-bold">{g.subjectName}</td>
                                        <td className="text-center fs-5">{renderGrade(g.value)}</td>
                                    </tr>
                                ))}
                                {data.grades.length === 0 && (
                                    <tr><td colSpan="2" className="text-center py-4">Оценок пока нет</td></tr>
                                )}
                            </tbody>
                        </Table>
                    </div>
                </Col>

                <Col md={4}>
                    <div className="card bg-dark bg-opacity-75 text-white shadow border-0 mb-4 p-3 text-center">
                        <h5 className="text-light opacity-75 mb-2">Средний балл</h5>
                        <h1 className={`display-4 fw-bold mb-0 ${getAverageColor(data.average)}`}>
                            {data.average}
                        </h1>
                    </div>

                    <div className="card bg-dark bg-opacity-75 text-white shadow border-0 p-3">
                        <h5 className="text-danger fw-bold mb-3">
                            <i className="bi bi-exclamation-triangle me-2"></i>
                            Долги
                        </h5>
                        {data.debts.length > 0 ? (
                            <ul className="list-group list-group-flush bg-transparent">
                                {data.debts.map((debt, idx) => (
                                    <li key={idx} className="list-group-item bg-transparent text-white border-secondary px-0">
                                        {debt}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="text-success mb-0">Долгов нет! Отличная работа!</p>
                        )}
                    </div>
                </Col>
            </Row>
        </div>
    );
};