import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Modal, Button, Table, Form, Spinner, Alert } from "react-bootstrap";

export const AdminSubjectsPage = () => {
    const [subjects, setSubjects] = useState([]);
    const [teachers, setTeachers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showModal, setShowModal] = useState(false);
    const [currentSubject, setCurrentSubject] = useState({ id: null, name: "" });
    const [modalError, setModalError] = useState("");

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [subjectToDelete, setSubjectToDelete] = useState(null);

    const [showTeachersModal, setShowTeachersModal] = useState(false);
    const [selectedSubject, setSelectedSubject] = useState(null);
    const [selectedTeacherIds, setSelectedTeacherIds] = useState([]);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const [subjectsData, teachersData] = await Promise.all([
                apiFetch("/admin/subjects"),
                apiFetch("/admin/users/teachers")
            ]);
            setSubjects(subjectsData);
            setTeachers(teachersData);
        } catch (err) {
            setError("Ошибка загрузки данных");
        } finally {
            setLoading(false);
        }
    };

    const handleSaveSubject = async (e) => {
        e.preventDefault();
        setModalError("");
        try {
            if (currentSubject.id) {
                await apiFetch(`/admin/subjects/${currentSubject.id}`, {
                    method: "PUT",
                    body: JSON.stringify({ name: currentSubject.name })
                });
            } else {
                await apiFetch("/admin/subjects", {
                    method: "POST",
                    body: JSON.stringify({ name: currentSubject.name })
                });
            }
            setShowModal(false);
            loadData();
        } catch (err) {
            setModalError(err.message);
        }
    };

    const handleOpenDelete = (subject) => {
        setSubjectToDelete(subject);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        try {
            await apiFetch(`/admin/subjects/${subjectToDelete.id}`, { method: "DELETE" });
            setShowDeleteModal(false);
            loadData();
        } catch (err) {
            alert("Ошибка удаления: " + err.message);
        }
    };

    const handleOpenTeachersModal = (subject) => {
        setSelectedSubject(subject);
        setSelectedTeacherIds(subject.teachers.map(t => t.id));
        setShowTeachersModal(true);
    };

    const handleSaveTeachers = async () => {
        try {
            await apiFetch(`/admin/subjects/${selectedSubject.id}/teachers`, {
                method: "POST",
                body: JSON.stringify(selectedTeacherIds)
            });
            setShowTeachersModal(false);
            loadData();
        } catch (err) {
            alert("Ошибка привязки преподавателей: " + err.message);
        }
    };

    const toggleTeacher = (id) => {
        setSelectedTeacherIds(prev => 
            prev.includes(id) ? prev.filter(tId => tId !== id) : [...prev, id]
        );
    };

    if (loading) return <div className="text-center mt-5"><Spinner animation="border" variant="warning" /></div>;

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="text-white">Управление предметами</h2>
                <Button variant="warning" className="fw-bold" onClick={() => { setCurrentSubject({ id: null, name: "" }); setModalError(""); setShowModal(true); }}>
                    <i className="bi bi-plus-lg me-2"></i>Добавить предмет
                </Button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <div className="card bg-dark bg-opacity-75 text-white shadow border-0">
                <Table hover responsive variant="dark" className="mb-0">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Название предмета</th>
                            <th>Кол-во преподавателей</th>
                            <th className="text-end">Действия</th>
                        </tr>
                    </thead>
                    <tbody>
                        {subjects.map(s => (
                            <tr key={s.id}>
                                <td>{s.id}</td>
                                <td className="fw-bold">{s.name}</td>
                                <td>{s.teachersCount}</td>
                                <td className="text-end">
                                    <Button variant="outline-info" size="sm" className="me-2" onClick={() => handleOpenTeachersModal(s)}>
                                        <i className="bi bi-person-video3"></i> Преподаватели
                                    </Button>
                                    <Button variant="outline-warning" size="sm" className="me-2" onClick={() => { setCurrentSubject(s); setModalError(""); setShowModal(true); }}>
                                        <i className="bi bi-pencil"></i>
                                    </Button>
                                    <Button variant="outline-danger" size="sm" onClick={() => handleOpenDelete(s)}>
                                        <i className="bi bi-trash"></i>
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        {subjects.length === 0 && (
                            <tr><td colSpan="4" className="text-center py-4">Предметов пока нет</td></tr>
                        )}
                    </tbody>
                </Table>
            </div>

            <Modal show={showModal} onHide={() => setShowModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>{currentSubject.id ? "Редактировать предмет" : "Новый предмет"}</Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSaveSubject}>
                    <Modal.Body>
                        {modalError && <Alert variant="danger">{modalError}</Alert>}
                        <Form.Group>
                            <Form.Label>Название предмета</Form.Label>
                            <Form.Control 
                                type="text" 
                                required 
                                value={currentSubject.name}
                                onChange={(e) => setCurrentSubject({...currentSubject, name: e.target.value})}
                                placeholder="Например: Базы данных"
                            />
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer className="border-secondary">
                        <Button variant="secondary" onClick={() => setShowModal(false)}>Отмена</Button>
                        <Button variant="warning" type="submit" className="fw-bold">Сохранить</Button>
                    </Modal.Footer>
                </Form>
            </Modal>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Удаление предмета</Modal.Title>
                </Modal.Header>
                <Modal.Body className="fs-5">
                    Вы уверены, что хотите удалить предмет <strong>{subjectToDelete?.name}</strong>?
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>Отмена</Button>
                    <Button variant="danger" onClick={confirmDelete} className="fw-bold">Да, удалить</Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showTeachersModal} onHide={() => setShowTeachersModal(false)} centered contentClassName="bg-dark text-white" size="lg">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Преподаватели предмета: {selectedSubject?.name}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p className="text-light opacity-75">Отметьте преподавателей, которые ведут этот предмет:</p>
                    <div className="row">
                        {teachers.map(t => (
                            <div className="col-md-6 mb-2" key={t.id}>
                                <Form.Check 
                                    type="checkbox"
                                    id={`teacher-${t.id}`}
                                    label={t.fullName}
                                    checked={selectedTeacherIds.includes(t.id)}
                                    onChange={() => toggleTeacher(t.id)}
                                    className="text-white"
                                />
                            </div>
                        ))}
                        {teachers.length === 0 && (
                            <div className="col-12 text-center py-3">
                                <p className="text-warning mb-0">В системе пока нет преподавателей</p>
                                <small className="text-muted">Добавьте их в разделе "Пользователи".</small>
                            </div>
                        )}
                    </div>
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowTeachersModal(false)}>Отмена</Button>
                    <Button variant="success" onClick={handleSaveTeachers}>Сохранить</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};