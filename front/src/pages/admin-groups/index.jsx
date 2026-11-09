import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Modal, Button, Table, Form, Spinner, Alert, Pagination, Row, Col } from "react-bootstrap";

export const AdminGroupsPage = () => {
    const [groups, setGroups] = useState([]);
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [modalError, setModalError] = useState("");

    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [filterCourse, setFilterCourse] = useState("");
    const [filterLevel, setFilterLevel] = useState("");
    const [searchQuery, setSearchQuery] = useState("");

    const [showGroupModal, setShowGroupModal] = useState(false);
    const [currentGroup, setCurrentGroup] = useState({ id: null, prefix: "", course: "1", subgroup: "1", educationLevel: "BACHELOR" });
    
    const [showStudentsModal, setShowStudentsModal] = useState(false);
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [selectedStudentIds, setSelectedStudentIds] = useState([]);

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [groupToDelete, setGroupToDelete] = useState(null);

    useEffect(() => {
        loadStudents();
    }, []);

    useEffect(() => {
        loadGroups();
    }, [page, filterCourse, filterLevel, searchQuery]);

    const loadStudents = async () => {
        try {
            const data = await apiFetch("/admin/users/students");
            setStudents(data || []);
        } catch (err) {
            console.error("Ошибка загрузки студентов", err);
        }
    };

    const loadGroups = async () => {
        try {
            setLoading(true);
            let url = `/admin/groups?page=${page}`;
            if (filterCourse) url += `&course=${filterCourse}`;
            if (filterLevel) url += `&level=${filterLevel}`;
            if (searchQuery) url += `&search=${searchQuery}`;

            const data = await apiFetch(url);
            setGroups(data?.content || []); // Защита от null
            setTotalPages(data?.totalPages || 0);
        } catch (err) {
            setError("Ошибка загрузки данных");
        } finally {
            setLoading(false);
        }
    };

    const handleSaveGroup = async (e) => {
        e.preventDefault();
        setModalError("");
        try {
            const method = currentGroup.id ? "PUT" : "POST";
            const url = currentGroup.id ? `/admin/groups/${currentGroup.id}` : "/admin/groups";
            
            await apiFetch(url, {
                method: method,
                body: JSON.stringify({
                    prefix: currentGroup.prefix,
                    course: currentGroup.course,
                    subgroup: currentGroup.subgroup,
                    educationLevel: currentGroup.educationLevel
                })
            });
            setShowGroupModal(false);
            loadGroups();
        } catch (err) {
            setModalError(err.message);
        }
    };

    const handleOpenDelete = (group) => {
        setGroupToDelete(group);
        setShowDeleteModal(true);
    };

    const confirmDeleteGroup = async () => {
        try {
            await apiFetch(`/admin/groups/${groupToDelete.id}`, { method: "DELETE" });
            setShowDeleteModal(false);
            loadGroups();
        } catch (err) {
            setError("Ошибка удаления: " + err.message);
            setShowDeleteModal(false);
        }
    };

    const handleSaveStudents = async () => {
        try {
            await apiFetch(`/admin/groups/${selectedGroup.id}/students`, {
                method: "POST",
                body: JSON.stringify(selectedStudentIds)
            });
            setShowStudentsModal(false);
            loadGroups();
        } catch (err) {
            setError("Ошибка привязки студентов: " + err.message);
        }
    };

    const handleOpenStudentsModal = (group) => {
        setSelectedGroup(group);
        setSelectedStudentIds(group.students?.map(s => s.id) || []);
        setShowStudentsModal(true);
    };

    const toggleStudent = (id) => {
        setSelectedStudentIds(prev => prev.includes(id) ? prev.filter(sId => sId !== id) : [...prev, id]);
    };

    const renderPagination = () => {
        let items = [];
        for (let number = 1; number <= totalPages; number++) {
            items.push(
                <Pagination.Item key={number} active={number === page} onClick={() => setPage(number)}>
                    {number}
                </Pagination.Item>
            );
        }
        return <Pagination className="justify-content-center mt-3">{items}</Pagination>;
    };

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="text-white">Управление группами</h2>
                <Button variant="warning" className="fw-bold" onClick={() => { 
                    setCurrentGroup({ id: null, prefix: "", course: "1", subgroup: "1", educationLevel: "BACHELOR" }); 
                    setModalError("");
                    setShowGroupModal(true); 
                }}>
                    <i className="bi bi-plus-lg me-2"></i>Добавить группу
                </Button>
            </div>

            <Row className="mb-4 g-2 bg-dark bg-opacity-50 p-3 rounded">
                <Col md={3}>
                    <Form.Control 
                        placeholder="Поиск по названию" 
                        value={searchQuery} 
                        onChange={e => { setSearchQuery(e.target.value); setPage(1); }} 
                    />
                </Col>
                <Col md={3}>
                    <Form.Select value={filterLevel} onChange={e => { setFilterLevel(e.target.value); setPage(1); }}>
                        <option value="">Все уровни образования</option>
                        <option value="BACHELOR">Бакалавриат</option>
                        <option value="MASTER">Магистратура</option>
                    </Form.Select>
                </Col>
                <Col md={3}>
                    <Form.Select value={filterCourse} onChange={e => { setFilterCourse(e.target.value); setPage(1); }}>
                        <option value="">Все курсы</option>
                        {[1,2,3,4,5].map(c => <option key={c} value={c}>{c} курс</option>)}
                    </Form.Select>
                </Col>
            </Row>

            {error && <Alert variant="danger">{error}</Alert>}

            {loading ? (
                <div className="text-center mt-5"><Spinner animation="border" variant="warning" /></div>
            ) : (
                <div className="card bg-dark bg-opacity-75 text-white shadow border-0">
                    <Table hover responsive variant="dark" className="mb-0">
                        <thead>
                            <tr>
                                <th>Название группы</th>
                                <th>Уровень образования</th>
                                <th>Кол-во студентов</th>
                                <th className="text-end">Действия</th>
                            </tr>
                        </thead>
                        <tbody>
                            {groups?.map(g => (
                                <tr key={g.id}>
                                    <td className="fw-bold">{g.fullName}</td>
                                    <td>{g.educationLevel === "BACHELOR" ? "Бакалавриат" : "Магистратура"}</td>
                                    <td>{g.studentsCount}</td>
                                    <td className="text-end">
                                        <Button variant="outline-info" size="sm" className="me-2" onClick={() => handleOpenStudentsModal(g)}>
                                            <i className="bi bi-people-fill"></i> Состав
                                        </Button>
                                        <Button variant="outline-warning" size="sm" className="me-2" onClick={() => 
                                            { setCurrentGroup(g); setModalError(""); setShowGroupModal(true); }}>
                                            <i className="bi bi-pencil"></i>
                                        </Button>
                                        <Button variant="outline-danger" size="sm" onClick={() => handleOpenDelete(g)}>
                                            <i className="bi bi-trash"></i>
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                            {(!groups || groups.length === 0) && <tr><td colSpan="4" className="text-center py-4">Групп не найдено</td></tr>}
                        </tbody>
                    </Table>
                </div>
            )}
            
            {!loading && renderPagination()}

            <Modal show={showGroupModal} onHide={() => setShowGroupModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>{currentGroup.id ? "Редактировать группу" : "Новая группа"}</Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSaveGroup}>
                    <Modal.Body>
                        {modalError && <Alert variant="danger">{modalError}</Alert>}
                        
                        <Row>
                            <Col md={6} className="mb-3">
                                <Form.Label>Уровень образования</Form.Label>
                                <Form.Select value={currentGroup.educationLevel} onChange={(e) => setCurrentGroup({...currentGroup, educationLevel: e.target.value, course: "1"})}>
                                    <option value="BACHELOR">Бакалавриат</option>
                                    <option value="MASTER">Магистратура</option>
                                </Form.Select>
                            </Col>
                            <Col md={6} className="mb-3">
                                <Form.Label>Курс</Form.Label>
                                <Form.Select value={currentGroup.course} onChange={(e) => setCurrentGroup({...currentGroup, course: e.target.value})}>
                                    {[1,2,3,4,5].filter(c => currentGroup.educationLevel === "MASTER" ? c <= 2 : true).map(c => (
                                        <option key={c} value={c}>{c}</option>
                                    ))}
                                </Form.Select>
                            </Col>
                        </Row>
                        <Row>
                            <Col md={8} className="mb-3">
                                <Form.Label>Префикс (например: ПИбд)</Form.Label>
                                <Form.Control type="text" required value={currentGroup.prefix} onChange={(e) => setCurrentGroup({...currentGroup, prefix: e.target.value})} />
                            </Col>
                            <Col md={4} className="mb-3">
                                {/* ВОЗВРАЩАЕМ ЗАГОЛОВОК ПОДГРУППЫ */}
                                <Form.Label>Подгруппа</Form.Label>
                                <Form.Select 
                                    value={currentGroup.subgroup} 
                                    onChange={(e) => setCurrentGroup({...currentGroup, subgroup: e.target.value})}
                                >
                                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(s => (
                                        <option key={s} value={s}>{s}</option>
                                    ))}
                                </Form.Select>
                            </Col>
                        </Row>
                        <Alert variant="info" className="mt-2 py-2">
                            Итоговое название: <strong>{currentGroup.prefix || "..."}-{currentGroup.course}{currentGroup.subgroup}</strong>
                        </Alert>
                    </Modal.Body>
                    <Modal.Footer className="border-secondary">
                        <Button variant="secondary" onClick={() => setShowGroupModal(false)}>Отмена</Button>
                        <Button variant="warning" type="submit" className="fw-bold">Сохранить</Button>
                    </Modal.Footer>
                </Form>
            </Modal>

            <Modal show={showStudentsModal} onHide={() => setShowStudentsModal(false)} centered contentClassName="bg-dark text-white" size="lg">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Состав группы: {selectedGroup?.fullName}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div className="row">
                        {students?.map(s => (
                            <div className="col-md-6 mb-2" key={s.id}>
                                <Form.Check type="checkbox" id={`student-${s.id}`} label={s.fullName} checked={selectedStudentIds?.includes(s.id)} onChange={() => toggleStudent(s.id)} className="text-white" />
                            </div>
                        ))}
                        {(!students || students.length === 0) && <p className="text-center text-muted">Студентов пока нет</p>}
                    </div>
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowStudentsModal(false)}>Отмена</Button>
                    <Button variant="success" onClick={handleSaveStudents}>Сохранить состав</Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Удаление группы</Modal.Title>
                </Modal.Header>
                <Modal.Body className="fs-5">
                    Вы уверены, что хотите удалить группу <strong>{groupToDelete?.fullName}</strong>?
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>Отмена</Button>
                    <Button variant="danger" onClick={confirmDeleteGroup} className="fw-bold">Да, удалить</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};