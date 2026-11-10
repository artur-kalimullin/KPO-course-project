import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Modal, Button, Table, Form, Spinner, Alert, Tabs, Tab, Row, Col } from "react-bootstrap";
import { CustomPagination } from "../../widgets/pagination";

export const AdminUsersPage = () => {
    const [activeTab, setActiveTab] = useState("students");

    const [students, setStudents] = useState([]);
    const [teachers, setTeachers] = useState([]);
    const [allGroups, setAllGroups] = useState([]);
    
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [studentPage, setStudentPage] = useState(1);
    const [studentTotalPages, setStudentTotalPages] = useState(0);
    const [studentSearch, setStudentSearch] = useState("");
    const [studentFilterCourse, setStudentFilterCourse] = useState("");
    const [studentFilterLevel, setStudentFilterLevel] = useState("");

    const [teacherPage, setTeacherPage] = useState(1);
    const [teacherTotalPages, setTeacherTotalPages] = useState(0);
    const [teacherSearch, setTeacherSearch] = useState("");
    const [teacherFilterGroup, setTeacherFilterGroup] = useState("");

    const [showStudentModal, setShowStudentModal] = useState(false);
    const [showTeacherModal, setShowTeacherModal] = useState(false);
    const [modalError, setModalError] = useState("");
    
    const [editingStudentId, setEditingStudentId] = useState(null);
    const [editingTeacherId, setEditingTeacherId] = useState(null);

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [userToDelete, setUserToDelete] = useState(null);

    const [studentData, setStudentData] = useState({ 
        lastName: "", firstName: "", middleName: "", username: "", password: "", 
        course: "1", groupId: "", recordBookNumber: "", educationForm: "Очная", educationLevel: "Бакалавриат" 
    });
    
    const [teacherData, setTeacherData] = useState({ 
        lastName: "", firstName: "", middleName: "", username: "", password: "", groupIds: [] 
    });

    // Загружаем группы 1 раз при старте
    useEffect(() => {
        const loadGroups = async () => {
            try {
                const data = await apiFetch("/admin/groups?page=1");
                setAllGroups(data?.content || []);
            } catch (err) {
                console.error("Ошибка загрузки групп", err);
            }
        };
        loadGroups();
    }, []);

    useEffect(() => {
        if (activeTab === "students") loadStudents();
    }, [activeTab, studentPage, studentSearch, studentFilterCourse, studentFilterLevel]);

    useEffect(() => {
        if (activeTab === "teachers") loadTeachers();
    }, [activeTab, teacherPage, teacherSearch, teacherFilterGroup]);

    const loadStudents = async () => {
        try {
            setLoading(true);
            let url = `/admin/users/students?page=${studentPage}`;
            if (studentSearch) url += `&search=${encodeURIComponent(studentSearch)}`; 
            if (studentFilterCourse) url += `&course=${studentFilterCourse}`;
            if (studentFilterLevel) url += `&level=${studentFilterLevel}`;

            const data = await apiFetch(url);
            setStudents(data?.content || []);
            setStudentTotalPages(data?.totalPages || 0);
        } catch (err) {
            setError("Ошибка загрузки студентов");
        } finally {
            setLoading(false);
        }
    };

    const loadTeachers = async () => {
        try {
            setLoading(true);
            let url = `/admin/users/teachers?page=${teacherPage}`;
            if (teacherSearch) url += `&search=${encodeURIComponent(teacherSearch)}`;
            if (teacherFilterGroup) url += `&groupId=${teacherFilterGroup}`;

            const data = await apiFetch(url);
            setTeachers(data?.content || []);
            setTeacherTotalPages(data?.totalPages || 0);
        } catch (err) {
            setError("Ошибка загрузки преподавателей");
        } finally {
            setLoading(false);
        }
    };

    const handleSaveStudent = async (e) => {
        e.preventDefault();
        setModalError("");
        try {
            const url = editingStudentId ? `/admin/users/students/${editingStudentId}` : "/admin/users/students";
            await apiFetch(url, {
                method: editingStudentId ? "PUT" : "POST",
                body: JSON.stringify(studentData)
            });
            setShowStudentModal(false);
            loadStudents();
        } catch (err) {
            setModalError(err.message);
        }
    };

    const handleSaveTeacher = async (e) => {
        e.preventDefault();
        setModalError("");
        try {
            const url = editingTeacherId ? `/admin/users/teachers/${editingTeacherId}` : "/admin/users/teachers";
            await apiFetch(url, {
                method: editingTeacherId ? "PUT" : "POST",
                body: JSON.stringify(teacherData)
            });
            setShowTeacherModal(false);
            loadTeachers();
        } catch (err) {
            setModalError(err.message);
        }
    };

    const confirmDelete = async () => {
        try {
            await apiFetch(`/admin/users/${userToDelete.id}`, { method: "DELETE" });
            setShowDeleteModal(false);
            activeTab === "students" ? loadStudents() : loadTeachers();
        } catch (err) {
            setError("Ошибка удаления: " + err.message);
            setShowDeleteModal(false);
        }
    };

    const openEditStudent = (student) => {
        setEditingStudentId(student.id);
        const groupId = student.groupIds && student.groupIds.length > 0 ? student.groupIds[0] : "";
        
        setStudentData({
            lastName: student.fullName?.split(" ")[0] || "",
            firstName: student.fullName?.split(" ")[1] || "",
            middleName: student.fullName?.split(" ")[2] || "",
            username: student.username,
            password: "", 
            course: student.course || "1",
            groupId: groupId,
            recordBookNumber: student.recordBookNumber || "",
            educationForm: student.educationForm || "Очная",
            educationLevel: student.educationLevel || "Бакалавриат"
        });
        setModalError("");
        setShowStudentModal(true);
    };

    const openEditTeacher = (teacher) => {
        setEditingTeacherId(teacher.id);
        setTeacherData({
            lastName: teacher.fullName?.split(" ")[0] || "",
            firstName: teacher.fullName?.split(" ")[1] || "",
            middleName: teacher.fullName?.split(" ")[2] || "",
            username: teacher.username,
            password: "",
            groupIds: teacher.groupIds || [] 
        });
        setModalError("");
        setShowTeacherModal(true);
    };

    const toggleTeacherGroup = (id) => {
        setTeacherData(prev => ({
            ...prev,
            groupIds: prev.groupIds.includes(id) ? prev.groupIds.filter(gId => gId !== id) : [...prev.groupIds, id]
        }));
    };

    const availableGroupsForStudent = allGroups.filter(g => {
        const backendLevel = studentData.educationLevel === "Бакалавриат" ? "BACHELOR" : "MASTER";
        
        if (studentData.educationLevel && g.educationLevel !== backendLevel) return false;
        if (studentData.course && g.course !== studentData.course) return false;
        return true;
    });

    return (
        <div className="container py-4">
            <h2 className="text-white mb-4">Управление пользователями</h2>
            {error && <Alert variant="danger">{error}</Alert>}

            <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} className="mb-3 custom-tabs">
                <Tab eventKey="students" title={<span><i className="bi bi-mortarboard me-2"></i>Студенты</span>}>
                                        <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                        <Row className="g-2 flex-grow-1">
                            <Col md={4}>
                                <Form.Control 
                                    placeholder="Поиск по ФИО..." 
                                    value={studentSearch} 
                                    onChange={e => { setStudentSearch(e.target.value); setStudentPage(1); }} 
                                />
                            </Col>
                            <Col md={3}>
                                <Form.Select value={studentFilterCourse} onChange={e => { setStudentFilterCourse(e.target.value); setStudentPage(1); }}>
                                    <option value="">Все курсы</option>
                                    {[1,2,3,4,5].map(c => <option key={c} value={c}>{c} курс</option>)}
                                </Form.Select>
                            </Col>
                            <Col md={4}>
                                <Form.Select value={studentFilterLevel} onChange={e => { setStudentFilterLevel(e.target.value); setStudentPage(1); }}>
                                    <option value="">Все уровни образования</option>
                                    <option value="Бакалавриат">Бакалавриат</option>
                                    <option value="Магистратура">Магистратура</option>
                                </Form.Select>
                            </Col>
                        </Row>
                        <Button variant="warning" className="fw-bold" onClick={() => { 
                            setEditingStudentId(null); 
                            setStudentData({ lastName: "", firstName: "", middleName: "", username: "", password: "", course: "1", groupId: "", recordBookNumber: "", educationForm: "Очная", educationLevel: "Бакалавриат" }); 
                            setModalError(""); 
                            setShowStudentModal(true); 
                        }}>
                            <i className="bi bi-plus-lg me-2"></i>Добавить студента
                        </Button>
                    </div>
                    
                    <div className="card bg-dark bg-opacity-75 text-white shadow border-0">
                        <Table hover responsive variant="dark" className="mb-0">
                            <thead>
                                <tr>
                                    <th>ФИО</th>
                                    <th>Логин</th>
                                    <th>Курс</th>
                                    <th>Зачетка</th>
                                    <th>Группа</th>
                                    <th>Форма / Уровень</th>
                                    <th className="text-end">Действия</th>
                                </tr>
                            </thead>
                            <tbody>
                                {students.map(s => (
                                    <tr key={s.id}>
                                        <td>{s.fullName}</td>
                                        <td>{s.username}</td>
                                        <td>{s.course}</td>
                                        <td>{s.recordBookNumber}</td>
                                        <td>{s.groupNames?.join(", ")}</td>
                                        <td>{s.educationForm} / {s.educationLevel}</td>
                                        <td className="text-end">
                                            <Button variant="outline-warning" size="sm" className="me-2" onClick={() => openEditStudent(s)}>
                                                <i className="bi bi-pencil"></i>
                                            </Button>
                                            <Button variant="outline-danger" size="sm" onClick={() => { setUserToDelete(s); setShowDeleteModal(true); }}>
                                                <i className="bi bi-trash"></i>
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                                {students.length === 0 && <tr><td colSpan="7" className="text-center py-3">Студентов не найдено</td></tr>}
                            </tbody>
                        </Table>
                    </div>
                    
                    {!loading && <CustomPagination page={studentPage} totalPages={studentTotalPages} onPageChange={setStudentPage} />}
                </Tab>

                <Tab eventKey="teachers" title={<span><i className="bi bi-person-video3 me-2"></i>Преподаватели</span>}>
                                        <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                        <Row className="g-2 flex-grow-1">
                            <Col md={6}>
                                <Form.Control 
                                    placeholder="Поиск по ФИО..." 
                                    value={teacherSearch} 
                                    onChange={e => { setTeacherSearch(e.target.value); setTeacherPage(1); }} 
                                />
                            </Col>
                            <Col md={6}>
                                <Form.Select value={teacherFilterGroup} onChange={e => { setTeacherFilterGroup(e.target.value); setTeacherPage(1); }}>
                                    <option value="">Все группы</option>
                                    {allGroups.map(g => <option key={g.id} value={g.id}>{g.fullName}</option>)}
                                </Form.Select>
                            </Col>
                        </Row>
                        <Button variant="warning" className="fw-bold" onClick={() => { 
                            setEditingTeacherId(null); 
                            setTeacherData({ lastName: "", firstName: "", middleName: "", username: "", password: "", groupIds: [] }); 
                            setModalError(""); 
                            setShowTeacherModal(true); 
                        }}>
                            <i className="bi bi-plus-lg me-2"></i>Добавить преподавателя
                        </Button>
                    </div>

                    <div className="card bg-dark bg-opacity-75 text-white shadow border-0">
                        <Table hover responsive variant="dark" className="mb-0">
                            <thead>
                                <tr>
                                    <th>ФИО</th>
                                    <th>Логин</th>
                                    <th>Ведет занятия у групп</th>
                                    <th className="text-end">Действия</th>
                                </tr>
                            </thead>
                            <tbody>
                                {teachers.map(t => (
                                    <tr key={t.id}>
                                        <td>{t.fullName}</td>
                                        <td>{t.username}</td>
                                        <td>{t.groupNames?.length > 0 ? t.groupNames.join(", ") : <span className="text-muted">Нет групп</span>}</td>
                                        <td className="text-end">
                                            <Button variant="outline-warning" size="sm" className="me-2" onClick={() => openEditTeacher(t)}>
                                                <i className="bi bi-pencil"></i>
                                            </Button>
                                            <Button variant="outline-danger" size="sm" onClick={() => { setUserToDelete(t); setShowDeleteModal(true); }}>
                                                <i className="bi bi-trash"></i>
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                                {teachers.length === 0 && <tr><td colSpan="4" className="text-center py-3">Преподавателей не найдено</td></tr>}
                            </tbody>
                        </Table>
                    </div>

                    {!loading && <CustomPagination page={teacherPage} totalPages={teacherTotalPages} onPageChange={setTeacherPage} />}
                </Tab>
            </Tabs>

            <Modal show={showStudentModal} onHide={() => setShowStudentModal(false)} centered contentClassName="bg-dark text-white" size="lg">
                <Modal.Header closeButton closeVariant="white"><Modal.Title>{editingStudentId ? "Редактировать студента" : "Новый студент"}</Modal.Title></Modal.Header>
                <Form onSubmit={handleSaveStudent}>
                    <Modal.Body>
                        {modalError && <Alert variant="danger">{modalError}</Alert>}
                        <Row>
                            <Col md={4} className="mb-3"><Form.Label>Фамилия</Form.Label><Form.Control required maxLength={60} value={studentData.lastName} onChange={e => setStudentData({...studentData, lastName: e.target.value})} /></Col>
                            <Col md={4} className="mb-3"><Form.Label>Имя</Form.Label><Form.Control required maxLength={60} value={studentData.firstName} onChange={e => setStudentData({...studentData, firstName: e.target.value})} /></Col>
                            <Col md={4} className="mb-3"><Form.Label>Отчество</Form.Label><Form.Control maxLength={60} value={studentData.middleName} onChange={e => setStudentData({...studentData, middleName: e.target.value})} /></Col>
                            
                            <Col md={6} className="mb-3"><Form.Label>Логин (6-40 симв.)</Form.Label><Form.Control required minLength={6} maxLength={40} value={studentData.username} onChange={e => setStudentData({...studentData, username: e.target.value})} /></Col>
                            <Col md={6} className="mb-3"><Form.Label>Пароль {editingStudentId && "(оставьте пустым, чтобы не менять)"}</Form.Label><Form.Control type="password" required={!editingStudentId} minLength={6} maxLength={40} value={studentData.password} onChange={e => setStudentData({...studentData, password: e.target.value})} /></Col>

                            <Col md={6} className="mb-3">
                                <Form.Label>Уровень образования</Form.Label>
                                <Form.Select value={studentData.educationLevel} onChange={e => setStudentData({...studentData, educationLevel: e.target.value, course: "1", groupId: ""})}>
                                    <option value="Бакалавриат">Бакалавриат</option>
                                    <option value="Магистратура">Магистратура</option>
                                </Form.Select>
                            </Col>
                            <Col md={6} className="mb-3">
                                <Form.Label>Курс</Form.Label>
                                <Form.Select required value={studentData.course} onChange={e => setStudentData({...studentData, course: e.target.value, groupId: ""})}>
                                    {[1,2,3,4,5].filter(c => studentData.educationLevel === "Магистратура" ? c <= 2 : true).map(c => (
                                        <option key={c} value={c.toString()}>{c} курс</option>
                                    ))}
                                </Form.Select>
                            </Col>

                            <Col md={4} className="mb-3">
                                <Form.Label>Зачетка (6 цифр)</Form.Label>
                                <Form.Control required pattern="\d{6}" maxLength={6} value={studentData.recordBookNumber} onChange={e => setStudentData({...studentData, recordBookNumber: e.target.value})} />
                            </Col>
                            <Col md={4} className="mb-3">
                                <Form.Label>Форма обучения</Form.Label>
                                <Form.Select value={studentData.educationForm} onChange={e => setStudentData({...studentData, educationForm: e.target.value})}>
                                    <option value="Очная">Очная</option>
                                    <option value="Очно-заочная">Очно-заочная</option>
                                    <option value="Заочная">Заочная</option>
                                </Form.Select>
                            </Col>
                            <Col md={4} className="mb-3">
                                <Form.Label>Группа</Form.Label>
                                <Form.Select required value={studentData.groupId} onChange={e => setStudentData({...studentData, groupId: Number(e.target.value)})}>
                                    <option value="">Выберите</option>
                                    {availableGroupsForStudent.map(g => <option key={g.id} value={g.id}>{g.fullName}</option>)}
                                </Form.Select>
                                {availableGroupsForStudent.length === 0 && <small className="text-danger">Нет подходящих групп</small>}
                            </Col>
                        </Row>
                    </Modal.Body>
                    <Modal.Footer className="border-secondary">
                        <Button variant="secondary" onClick={() => setShowStudentModal(false)}>Отмена</Button>
                        <Button variant="warning" type="submit" className="fw-bold">Сохранить</Button>
                    </Modal.Footer>
                </Form>
            </Modal>

            <Modal show={showTeacherModal} onHide={() => setShowTeacherModal(false)} centered contentClassName="bg-dark text-white" size="lg">
                <Modal.Header closeButton closeVariant="white"><Modal.Title>{editingTeacherId ? "Редактировать преподавателя" : "Новый преподаватель"}</Modal.Title></Modal.Header>
                <Form onSubmit={handleSaveTeacher}>
                    <Modal.Body>
                        {modalError && <Alert variant="danger">{modalError}</Alert>}
                        <Row>
                            <Col md={4} className="mb-3"><Form.Label>Фамилия</Form.Label><Form.Control required maxLength={60} value={teacherData.lastName} onChange={e => setTeacherData({...teacherData, lastName: e.target.value})} /></Col>
                            <Col md={4} className="mb-3"><Form.Label>Имя</Form.Label><Form.Control required maxLength={60} value={teacherData.firstName} onChange={e => setTeacherData({...teacherData, firstName: e.target.value})} /></Col>
                            <Col md={4} className="mb-3"><Form.Label>Отчество</Form.Label><Form.Control maxLength={60} value={teacherData.middleName} onChange={e => setTeacherData({...teacherData, middleName: e.target.value})} /></Col>
                            
                            <Col md={6} className="mb-3"><Form.Label>Логин (6-40 симв.)</Form.Label><Form.Control required minLength={6} maxLength={40} value={teacherData.username} onChange={e => setTeacherData({...teacherData, username: e.target.value})} /></Col>
                            <Col md={6} className="mb-3"><Form.Label>Пароль {editingTeacherId && "(оставьте пустым, чтобы не менять)"}</Form.Label><Form.Control type="password" required={!editingTeacherId} minLength={6} maxLength={40} value={teacherData.password} onChange={e => setTeacherData({...teacherData, password: e.target.value})} /></Col>

                            <Col md={12} className="mb-3">
                                <Form.Label>Группы, в которых ведет занятия</Form.Label>
                                <div className="border border-secondary rounded p-3" style={{ maxHeight: "150px", overflowY: "auto" }}>
                                    {allGroups.map(g => (
                                        <Form.Check 
                                            key={g.id} type="checkbox" id={`group-${g.id}`} label={g.fullName}
                                            checked={teacherData.groupIds.includes(g.id)} onChange={() => toggleTeacherGroup(g.id)}
                                            className="text-white mb-1"
                                        />
                                    ))}
                                    {allGroups.length === 0 && <p className="text-muted mb-0">Сначала создайте группы.</p>}
                                </div>
                            </Col>
                        </Row>
                    </Modal.Body>
                    <Modal.Footer className="border-secondary">
                        <Button variant="secondary" onClick={() => setShowTeacherModal(false)}>Отмена</Button>
                        <Button variant="warning" type="submit" className="fw-bold">Сохранить</Button>
                    </Modal.Footer>
                </Form>
            </Modal>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Удаление пользователя</Modal.Title>
                </Modal.Header>
                <Modal.Body className="fs-5">
                    Вы уверены, что хотите удалить <strong>{userToDelete?.fullName}</strong>?
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>Отмена</Button>
                    <Button variant="danger" onClick={confirmDelete} className="fw-bold">Да, удалить</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};