import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Spinner, Alert, Row, Col, Card, Table } from "react-bootstrap";

export const StudentRatingPage = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadData = async () => {
            try {
                const res = await apiFetch("/student/rating");
                setData(res);
            } catch (err) {
                setError("Ошибка загрузки рейтинга");
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, []);

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
            <h2 className="text-white mb-4">Рейтинг в группе</h2>

            <div className="bg-dark bg-opacity-75 text-white p-4 rounded shadow mb-4 border-start border-warning border-4">
                <h4 className="mb-0">Группа: {data.groupName}</h4>
            </div>

            <Row className="mb-4">
                <Col md={8}>
                    <Card className="bg-dark bg-opacity-75 text-white shadow border-0 h-100">
                        <Card.Header className="bg-transparent border-secondary">
                            <h5 className="mb-0 text-warning fw-bold">
                                <i className="bi bi-trophy-fill me-2"></i>Топ-3 студента
                            </h5>
                        </Card.Header>
                        <Card.Body className="p-0">
                            <Table hover responsive variant="dark" className="mb-0 align-middle">
                                <thead>
                                    <tr>
                                        <th className="text-center" style={{ width: "100px" }}>Место</th>
                                        <th className="text-center">Средний балл</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {data.top3.map((student, idx) => (
                                        <tr key={idx}>
                                            <td className="text-center fs-4 fw-bold">
                                                {idx === 0 && <span className="text-warning">1</span>}
                                                {idx === 1 && <span className="text-light opacity-75">2</span>}
                                                {idx === 2 && <span className="text-danger">3</span>}
                                            </td>
                                            <td className={`text-center fs-5 fw-bold ${getAverageColor(student.average)}`}>
                                                {student.average}
                                            </td>
                                        </tr>
                                    ))}
                                    {data.top3.length === 0 && (
                                        <tr><td colSpan="2" className="text-center py-4">Нет данных</td></tr>
                                    )}
                                </tbody>
                            </Table>
                        </Card.Body>
                    </Card>
                </Col>

                <Col md={4}>
                    <Card className="bg-dark bg-opacity-75 text-white shadow border-0 h-100 text-center d-flex justify-content-center">
                        <Card.Body className="d-flex flex-column justify-content-center align-items-center">
                            <h5 className="text-light opacity-75 mb-3">Ваше место в группе</h5>
                            <div className="d-flex align-items-baseline justify-content-center text-warning mb-3">
                                <span className="display-1 fw-bold">{data.myPlace}</span>
                                <span className="fs-3 ms-2 opacity-75">из {data.totalStudents}</span>
                            </div>
                            <p className="fs-5 mb-0">
                                Средний балл: <strong className={getAverageColor(data.myAverage)}>{data.myAverage}</strong>
                            </p>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Row>
                <Col md={6} className="mb-3 mb-md-0">
                    <Card className="bg-dark bg-opacity-75 text-white shadow border-0 border-start border-success border-4">
                        <Card.Body className="d-flex justify-content-between align-items-center">
                            <h5 className="mb-0 text-success fw-bold">Студентов без долгов</h5>
                            <h2 className="mb-0 fw-bold display-5">{data.studentsWithoutDebts}</h2>
                        </Card.Body>
                    </Card>
                </Col>
                <Col md={6}>
                    <Card className="bg-dark bg-opacity-75 text-white shadow border-0 border-start border-danger border-4">
                        <Card.Body className="d-flex justify-content-between align-items-center">
                            <h5 className="mb-0 text-danger fw-bold">Студентов с долгами</h5>
                            <h2 className="mb-0 fw-bold display-5">{data.studentsWithDebts}</h2>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </div>
    );
};