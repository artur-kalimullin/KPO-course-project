import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Table, Form, Spinner, Alert, Card, Button, Modal, Row, Col } from "react-bootstrap";

export const TeacherGradesPage = () => {
    const [subjects, setSubjects] = useState([]);
    const [selectedSubject, setSelectedSubject] = useState("");
    
    const [groupsWithStudents, setGroupsWithStudents] = useState([]);
    const [existingGrades, setExistingGrades] = useState({}); // { studentId: value }
    
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [showGradeModal, setShowGradeModal] = useState(false);
    const [currentStudent, setCurrentStudent] = useState(null);
    const [selectedGrade, setSelectedGrade] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    useEffect(() => {
        const loadSubjects = async () => {
            try {
                const data = await apiFetch("/teacher/grades/my-subjects");
                setSubjects(data || []);
            } catch (err) {
                setError("Ошибка загрузки ваших предметов");
            }
        };
        loadSubjects();
    }, []);

    useEffect(() => {
        if (selectedSubject) {
            loadData();
        } else {
            setGroupsWithStudents([]);
            setExistingGrades({});
        }
    }, [selectedSubject]);

    const loadData = async () => {
        try {
            setLoading(true);
            setError(null);
            
            const [gradesData, detailsData] = await Promise.all([
                apiFetch(`/teacher/grades/subject/${selectedSubject}`),
                apiFetch(`/teacher/grades/subject/${selectedSubject}/details`) // <-- НОВЫЙ ЗАПРОС
            ]);

            const gradesMap = {};
            gradesData.forEach(g => { gradesMap[g.studentId] = g.value; });
            setExistingGrades(gradesMap);

            setGroupsWithStudents(detailsData.groups || []);

        } catch (err) {
            setError("Ошибка загрузки данных: " + err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleOpenGradeModal = (student) => {
        setCurrentStudent(student);
        setSelectedGrade(existingGrades[student.id] || "");
        setSuccessMessage("");
        setShowGradeModal(true);
    };

    const handleSaveGrade = async () => {
        if (!selectedGrade) return;
        try {
            await apiFetch("/teacher/grades", {
                method: "POST",
                body: JSON.stringify({
                    studentId: currentStudent.id,
                    subjectId: Number(selectedSubject),
                    value: Number(selectedGrade)
                })
            });
            
            setExistingGrades(prev => ({ ...prev, [currentStudent.id]: Number(selectedGrade) }));
            setShowGradeModal(false);
            setSuccessMessage(`Оценка для ${currentStudent.fullName} успешно сохранена!`);
            
            setTimeout(() => setSuccessMessage(""), 3000);
        } catch (err) {
            setError("Ошибка сохранения оценки: " + err.message);
        }
    };

    const renderGrade = (value) => {
        if (value === 0) return <span className="fw-bold text-danger">Неявка</span>;
        if (value === 5) return <span className="fw-bold text-success fs-5">{value}</span>;
        if (value === 4) return <span className="fw-bold text-info fs-5">{value}</span>;
        if (value === 3) return <span className="fw-bold text-warning fs-5">{value}</span>;
        if (value === 2) return <span className="fw-bold text-danger fs-5">{value}</span>;
        return <span className="text-muted">-</span>;
    };

    return (
        <div className="container py-4">
            <h2 className="text-white mb-4">Выставление оценок</h2>
            
            <Card className="bg-dark bg-opacity-75 text-white shadow border-0 mb-4 p-3">
                <Row>
                    <Col md={6}>
                        <Form.Select 
                            value={selectedSubject} 
                            onChange={e => setSelectedSubject(e.target.value)}
                        >
                            <option value="">Выберите ваш предмет...</option>
                            {subjects.map(s => (
                                <option key={s.id} value={s.id}>{s.name}</option>
                            ))}
                        </Form.Select>
                    </Col>
                </Row>
            </Card>

            {successMessage && <Alert variant="success">{successMessage}</Alert>}
            {error && <Alert variant="danger">{error}</Alert>}

            {loading ? (
                <div className="text-center mt-5"><Spinner animation="border" variant="warning" /></div>
            ) : selectedSubject && (
                groupsWithStudents.length > 0 ? (
                    groupsWithStudents.map(group => (
                        <div key={group.id} className="mb-4">
                            <h5 className="text-warning mb-3">Группа {group.fullName}</h5>
                            <div className="card bg-dark bg-opacity-75 text-white shadow border-0">
                                <Table hover responsive variant="dark" className="mb-0 align-middle">
                                    <thead>
                                        <tr>
                                            <th style={{ width: "50%" }}>ФИО студента</th>
                                            <th style={{ width: "20%" }}>Оценка</th>
                                            <th style={{ width: "30%" }} className="text-end">Действие</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {group.students?.map(student => {
                                            const grade = existingGrades[student.id];
                                            return (
                                                <tr key={student.id}>
                                                    <td className="fw-bold">{student.fullName}</td>
                                                    <td className="fs-5">
                                                        {grade !== undefined && grade !== null ? renderGrade(grade) : <span className="text-muted">-</span>}
                                                    </td>
                                                    <td className="text-end">
                                                        <Button variant="outline-warning" size="sm" onClick={() => handleOpenGradeModal(student)}>
                                                            <i className="bi bi-pencil-square me-1"></i>
                                                            {grade ? "Редактировать" : "Выставить"}
                                                        </Button>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                        {(!group.students || group.students.length === 0) && (
                                            <tr><td colSpan="3" className="text-center py-4">В группе нет студентов</td></tr>
                                        )}
                                    </tbody>
                                </Table>
                            </div>
                        </div>
                    ))
                ) : (
                    <Alert variant="info" className="text-center">К этому предмету не привязано ни одной группы со студентами</Alert>
                )
            )}

            {!selectedSubject && (
                <Alert variant="info" className="text-center">Выберите предмет, чтобы увидеть список студентов</Alert>
            )}

            <Modal show={showGradeModal} onHide={() => setShowGradeModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Оценка студента</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p className="fs-5 mb-3">Студент: <strong>{currentStudent?.fullName}</strong></p>
                    <Form.Group>
                        <Form.Label>Выберите оценку</Form.Label>
                        <Form.Select value={selectedGrade} onChange={e => setSelectedGrade(e.target.value)}>
                            <option value="">-- Выберите --</option>
                            <option value="5" className="text-success fw-bold">5 (Отлично)</option>
                            <option value="4" className="text-info fw-bold">4 (Хорошо)</option>
                            <option value="3" className="text-warning fw-bold">3 (Удовлетворительно)</option>
                            <option value="2" className="text-danger fw-bold">2 (Неудовлетворительно)</option>
                            <option value="0" className="text-danger fw-bold">Неявка</option>
                        </Form.Select>
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowGradeModal(false)}>Отмена</Button>
                    <Button variant="warning" onClick={handleSaveGrade} disabled={!selectedGrade} className="fw-bold">
                        Сохранить
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};