import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Modal, Button, Table, Form, Spinner, Alert, Row, Col } from "react-bootstrap";
import { CustomPagination } from "../../widgets/pagination";

export const AdminSubjectsPage = () => {
    const [subjects, setSubjects] = useState([]);
    const [teachers, setTeachers] = useState([]);
    const [groups, setGroups] = useState([]); 
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [searchQuery, setSearchQuery] = useState("");
    const [filterTeacherId, setFilterTeacherId] = useState("");
    const [filterGroupId, setFilterGroupId] = useState(""); 

    const [showModal, setShowModal] = useState(false);
    const [currentSubject, setCurrentSubject] = useState({ id: null, name: "", teacherIds: [], groupIds: [] });
    const [modalError, setModalError] = useState("");
    const [teacherSearch, setTeacherSearch] = useState("");
    const [groupSearch, setGroupSearch] = useState(""); // Поиск в чекбоксах групп

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [subjectToDelete, setSubjectToDelete] = useState(null);

    useEffect(() => {
        loadDictionaries();
    }, []);

    useEffect(() => {
        loadSubjects();
    }, [page, searchQuery, filterTeacherId, filterGroupId]);

    const loadDictionaries = async () => {
        try {
            const [teachersData, groupsData] = await Promise.all([
                apiFetch("/admin/users/teachers?page=1"),
                apiFetch("/admin/groups?page=1") 
            ]);
            setTeachers(teachersData?.content || []);
            setGroups(groupsData?.content || []);
        } catch (err) {
            console.error("Ошибка загрузки справочников", err);
        }
    };

    const loadSubjects = async () => {
        try {
            setLoading(true);
            let url = `/admin/subjects?page=${page}`;
            if (searchQuery) url += `&search=${encodeURIComponent(searchQuery)}`;
            if (filterTeacherId) url += `&teacherId=${filterTeacherId}`;
            if (filterGroupId) url += `&groupId=${filterGroupId}`;

            const data = await apiFetch(url);
            setSubjects(data?.content || []);
            setTotalPages(data?.totalPages || 0);
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
            const method = currentSubject.id ? "PUT" : "POST";
            const url = currentSubject.id ? `/admin/subjects/${currentSubject.id}` : "/admin/subjects";
            
            await apiFetch(url, {
                method: method,
                body: JSON.stringify({ 
                    name: currentSubject.name,
                    teacherIds: currentSubject.teacherIds,
                    groupIds: currentSubject.groupIds
                })
            });
            setShowModal(false);
            loadSubjects();
        } catch (err) {
            setModalError(err.message);
        }
    };

    const toggleTeacher = (id) => {
        setCurrentSubject(prev => ({
            ...prev,
            teacherIds: prev.teacherIds.includes(id) ? prev.teacherIds.filter(tId => tId !== id) : [...prev.teacherIds, id]
        }));
    };

    const toggleGroup = (id) => {
        setCurrentSubject(prev => ({
            ...prev,
            groupIds: prev.groupIds.includes(id) ? prev.groupIds.filter(gId => gId !== id) : [...prev.groupIds, id]
        }));
    };

    const filteredTeachers = teachers.filter(t => t.fullName.toLowerCase().includes(teacherSearch.toLowerCase()));
    const filteredGroups = groups.filter(g => g.fullName.toLowerCase().includes(groupSearch.toLowerCase()));

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="text-white">Управление предметами</h2>
                <Button variant="warning" className="fw-bold" onClick={() => { 
                    setCurrentSubject({ id: null, name: "", teacherIds: [], groupIds: [] }); 
                    setModalError(""); 
                    setTeacherSearch("");
                    setGroupSearch("");
                    setShowModal(true); 
                }}>
                    <i className="bi bi-plus-lg me-2"></i>Добавить предмет
                </Button>
            </div>

            {/* Фильтры */}
            <Row className="mb-4 g-2 bg-dark bg-opacity-50 p-3 rounded">
                <Col md={3}>
                    <Form.Control 
                        placeholder="Поиск по названию" 
                        value={searchQuery} 
                        onChange={e => { setSearchQuery(e.target.value); setPage(1); }} 
                    />
                </Col>
                <Col md={3}>
                    <Form.Select value={filterGroupId} onChange={e => { setFilterGroupId(e.target.value); setPage(1); }}>
                        <option value="">Все группы</option>
                        {groups.map(g => <option key={g.id} value={g.id}>{g.fullName}</option>)}
                    </Form.Select>
                </Col>
                <Col md={3}>
                    <Form.Select value={filterTeacherId} onChange={e => { setFilterTeacherId(e.target.value); setPage(1); }}>
                        <option value="">Все преподаватели</option>
                        {teachers.map(t => <option key={t.id} value={t.id}>{t.fullName}</option>)}
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
                                <th>Название</th>
                                <th>Группы</th>
                                <th>Преподаватели</th>
                                <th className="text-end">Действия</th>
                            </tr>
                        </thead>
                        <tbody>
                            {subjects.map(s => (
                                <tr key={s.id}>
                                    <td className="fw-bold">{s.name}</td>
                                    <td>
                                        {s.groups?.length > 0 ? s.groups.map(g => g.fullName).join(", ") : <span className="text-muted">-</span>}
                                    </td>
                                    <td>
                                        {s.teachers?.length > 0 ? s.teachers.map(t => t.fullName).join(", ") : <span className="text-muted">-</span>}
                                    </td>
                                    <td className="text-end">
                                        <Button variant="outline-warning" size="sm" className="me-2" onClick={() => { 
                                            setCurrentSubject({ 
                                                id: s.id, 
                                                name: s.name, 
                                                teacherIds: s.teachers.map(t => t.id),
                                                groupIds: s.groups.map(g => g.id)
                                            }); 
                                            setModalError(""); 
                                            setTeacherSearch("");
                                            setGroupSearch("");
                                            setShowModal(true); 
                                        }}>
                                            <i className="bi bi-pencil"></i>
                                        </Button>
                                        <Button variant="outline-danger" size="sm" onClick={() => { setSubjectToDelete(s); setShowDeleteModal(true); }}>
                                            <i className="bi bi-trash"></i>
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                            {subjects.length === 0 && <tr><td colSpan="4" className="text-center py-4">Предметов не найдено</td></tr>}
                        </tbody>
                    </Table>
                </div>
            )}

            {!loading && <CustomPagination page={page} totalPages={totalPages} onPageChange={setPage} />}

            {/* Модалка создания/редактирования */}
            <Modal show={showModal} onHide={() => setShowModal(false)} centered contentClassName="bg-dark text-white" size="lg">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>{currentSubject.id ? "Редактировать предмет" : "Новый предмет"}</Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSaveSubject}>
                    <Modal.Body>
                        {modalError && <Alert variant="danger">{modalError}</Alert>}
                        
                        <Form.Group className="mb-3">
                            <Form.Label>Название предмета</Form.Label>
                            <Form.Control 
                                type="text" 
                                required 
                                value={currentSubject.name}
                                onChange={(e) => setCurrentSubject({...currentSubject, name: e.target.value})}
                            />
                        </Form.Group>

                        <Row>
                            {/* Блок групп */}
                            <Col md={6} className="mb-3">
                                <Form.Label className="d-flex justify-content-between">
                                    Группы <small className="text-muted">Выбрано: {currentSubject.groupIds.length}</small>
                                </Form.Label>
                                <Form.Control 
                                    placeholder="Поиск группы..." 
                                    size="sm" 
                                    className="mb-2" 
                                    value={groupSearch}
                                    onChange={e => setGroupSearch(e.target.value)}
                                />
                                <div className="border border-secondary rounded p-2" style={{ maxHeight: "200px", overflowY: "auto" }}>
                                    {filteredGroups.map(g => (
                                        <Form.Check 
                                            key={g.id} type="checkbox" id={`gr-${g.id}`} label={g.fullName}
                                            checked={currentSubject.groupIds.includes(g.id)} 
                                            onChange={() => toggleGroup(g.id)}
                                            className="text-white mb-1"
                                        />
                                    ))}
                                    {filteredGroups.length === 0 && <p className="text-muted small mb-0">Не найдено</p>}
                                </div>
                            </Col>

                            {/* Блок преподавателей */}
                            <Col md={6} className="mb-3">
                                <Form.Label className="d-flex justify-content-between">
                                    Преподаватели <small className="text-muted">Выбрано: {currentSubject.teacherIds.length}</small>
                                </Form.Label>
                                <Form.Control 
                                    placeholder="Поиск преподавателя..." 
                                    size="sm" 
                                    className="mb-2" 
                                    value={teacherSearch}
                                    onChange={e => setTeacherSearch(e.target.value)}
                                />
                                <div className="border border-secondary rounded p-2" style={{ maxHeight: "200px", overflowY: "auto" }}>
                                    {filteredTeachers.map(t => (
                                        <Form.Check 
                                            key={t.id} type="checkbox" id={`t-${t.id}`} label={t.fullName}
                                            checked={currentSubject.teacherIds.includes(t.id)} 
                                            onChange={() => toggleTeacher(t.id)}
                                            className="text-white mb-1"
                                        />
                                    ))}
                                    {filteredTeachers.length === 0 && <p className="text-muted small mb-0">Не найдено</p>}
                                </div>
                            </Col>
                        </Row>
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
                    <Button variant="danger" onClick={async () => {
                        try {
                            await apiFetch(`/admin/subjects/${subjectToDelete.id}`, { method: "DELETE" });
                            setShowDeleteModal(false);
                            loadSubjects();
                        } catch (err) {
                            setError("Ошибка удаления: " + err.message);
                            setShowDeleteModal(false);
                        }
                    }} className="fw-bold">Да, удалить</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};