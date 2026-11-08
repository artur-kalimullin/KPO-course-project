import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Modal, Button, Table, Form, Spinner, Alert } from "react-bootstrap";

export const AdminGroupsPage = () => {
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showGroupModal, setShowGroupModal] = useState(false);
    const [currentGroup, setCurrentGroup] = useState({ id: null, name: "" });
    const [modalError, setModalError] = useState("");

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [groupToDelete, setGroupToDelete] = useState(null);

    const [showStudentsModal, setShowStudentsModal] = useState(false);
    const [selectedGroup, setSelectedGroup] = useState(null);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const groupsData = await apiFetch("/admin/groups");
            setGroups(groupsData);
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
            if (currentGroup.id) {
                await apiFetch(`/admin/groups/${currentGroup.id}`, {
                    method: "PUT",
                    body: JSON.stringify({ name: currentGroup.name })
                });
            } else {
                await apiFetch("/admin/groups", {
                    method: "POST",
                    body: JSON.stringify({ name: currentGroup.name })
                });
            }
            setShowGroupModal(false);
            loadData();
        } catch (err) {
            setModalError(err.message);
        }
    };

    const handleOpenDelete = (group) => {
        setGroupToDelete(group);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        try {
            await apiFetch(`/admin/groups/${groupToDelete.id}`, { method: "DELETE" });
            setShowDeleteModal(false);
            loadData();
        } catch (err) {
            alert("Ошибка удаления: " + err.message);
        }
    };

    if (loading) return <div className="text-center mt-5"><Spinner animation="border" variant="warning" /></div>;

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="text-white">Управление группами</h2>
                <Button variant="warning" className="fw-bold" onClick={() => { setCurrentGroup({ id: null, name: "" }); setModalError(""); setShowGroupModal(true); }}>
                    <i className="bi bi-plus-lg me-2"></i>Добавить группу
                </Button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <div className="card bg-dark bg-opacity-75 text-white shadow border-0">
                <Table hover responsive variant="dark" className="mb-0">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Название группы</th>
                            <th>Кол-во студентов</th>
                            <th className="text-end">Действия</th>
                        </tr>
                    </thead>
                    <tbody>
                        {groups.map(g => (
                            <tr key={g.id}>
                                <td>{g.id}</td>
                                <td className="fw-bold">{g.name}</td>
                                <td>{g.studentsCount}</td>
                                <td className="text-end">
                                    <Button variant="outline-info" size="sm" className="me-2" onClick={() => { setSelectedGroup(g); setShowStudentsModal(true); }}>
                                        <i className="bi bi-people-fill"></i> Состав
                                    </Button>
                                    <Button variant="outline-warning" size="sm" className="me-2" onClick={() => { setCurrentGroup(g); setModalError(""); setShowGroupModal(true); }}>
                                        <i className="bi bi-pencil"></i>
                                    </Button>
                                    <Button variant="outline-danger" size="sm" onClick={() => handleOpenDelete(g)}>
                                        <i className="bi bi-trash"></i>
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        {groups.length === 0 && (
                            <tr><td colSpan="4" className="text-center py-4">Групп пока нет</td></tr>
                        )}
                    </tbody>
                </Table>
            </div>

            <Modal show={showGroupModal} onHide={() => setShowGroupModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>{currentGroup.id ? "Редактировать группу" : "Новая группа"}</Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSaveGroup}>
                    <Modal.Body>
                        {modalError && <Alert variant="danger">{modalError}</Alert>}
                        
                        <Form.Group>
                            <Form.Label>Название группы</Form.Label>
                            <Form.Control 
                                type="text" 
                                required 
                                value={currentGroup.name}
                                onChange={(e) => setCurrentGroup({...currentGroup, name: e.target.value})}
                                placeholder="Например: ПИбд-41"
                            />
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer className="border-secondary">
                        <Button variant="secondary" onClick={() => setShowGroupModal(false)}>Отмена</Button>
                        <Button variant="warning" type="submit" className="fw-bold">Сохранить</Button>
                    </Modal.Footer>
                </Form>
            </Modal>

            <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Удаление группы</Modal.Title>
                </Modal.Header>
                <Modal.Body className="fs-5">
                    Вы уверены, что хотите удалить группу <strong>{groupToDelete?.name}</strong>?
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
                        Отмена
                    </Button>
                    <Button variant="danger" onClick={confirmDelete} className="fw-bold">
                        Да, удалить
                    </Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showStudentsModal} onHide={() => setShowStudentsModal(false)} centered contentClassName="bg-dark text-white">
                <Modal.Header closeButton closeVariant="white">
                    <Modal.Title>Состав группы: {selectedGroup?.name}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {selectedGroup?.students && selectedGroup.students.length > 0 ? (
                        <ul className="list-group list-group-flush bg-transparent">
                            {selectedGroup.students.map(s => (
                                <li key={s.id} className="list-group-item bg-transparent text-white border-secondary">
                                    <i className="bi bi-person-fill me-2 text-warning"></i>
                                    {s.fullName}
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <div className="text-center py-4">
                            <p className="text-light mb-0 fs-5">
                                Нет студентов в группе
                            </p>
                        </div>
                    )}
                </Modal.Body>
                <Modal.Footer className="border-secondary">
                    <Button variant="secondary" onClick={() => setShowStudentsModal(false)}>Выйти</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};