import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Table, Form, Spinner, Alert, Row, Col, Card } from "react-bootstrap";
import { CustomPagination } from "../../widgets/pagination";

export const TeacherStudentsPage = () => {
    const [groups, setGroups] = useState([]);
    const [selectedGroup, setSelectedGroup] = useState("");
    const [students, setStudents] = useState([]);
    
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [searchQuery, setSearchQuery] = useState("");

    useEffect(() => {
        const loadGroups = async () => {
            try {
                const data = await apiFetch("/teacher/my-groups");
                setGroups(data || []);
            } catch (err) {
                setError("Ошибка загрузки ваших групп");
            }
        };
        loadGroups();
    }, []);

    useEffect(() => {
        if (selectedGroup) {
            loadStudents();
        } else {
            setStudents([]);
            setTotalPages(0);
        }
    }, [selectedGroup, page, searchQuery]);

    const loadStudents = async () => {
        try {
            setLoading(true);
            let url = `/teacher/groups/${selectedGroup}/students?page=${page}`;
            if (searchQuery) url += `&search=${encodeURIComponent(searchQuery)}`;

            const data = await apiFetch(url);
            setStudents(data?.content || []);
            setTotalPages(data?.totalPages || 0);
        } catch (err) {
            setError("Ошибка загрузки студентов: " + err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container py-4">
            <h2 className="text-white mb-4">Списки студентов</h2>
            
            <Card className="bg-dark bg-opacity-75 text-white shadow border-0 mb-4 p-3">
                <Row className="g-2">
                    <Col md={6}>
                        <Form.Select 
                            value={selectedGroup} 
                            onChange={e => { 
                                setSelectedGroup(e.target.value); 
                                setPage(1); 
                                setSearchQuery(""); 
                            }}
                        >
                            <option value="">Выберите вашу группу...</option>
                            {groups.map(g => (
                                <option key={g.id} value={g.id}>{g.fullName}</option>
                            ))}
                        </Form.Select>
                    </Col>
                    {selectedGroup && (
                        <Col md={6}>
                            <Form.Control 
                                placeholder="Поиск по ФИО студента..." 
                                value={searchQuery} 
                                onChange={e => { setSearchQuery(e.target.value); setPage(1); }} 
                            />
                        </Col>
                    )}
                </Row>
            </Card>

            {error && <Alert variant="danger">{error}</Alert>}

            {selectedGroup ? (
                loading ? (
                    <div className="text-center mt-5"><Spinner animation="border" variant="warning" /></div>
                ) : (
                    <div className="card bg-dark bg-opacity-75 text-white shadow border-0">
                        <Table hover responsive variant="dark" className="mb-0">
                            <thead>
                                <tr>
                                    <th>ФИО студента</th>
                                </tr>
                            </thead>
                            <tbody>
                                {students.map(s => (
                                    <tr key={s.id}>
                                        <td className="fw-bold">{s.fullName}</td>
                                    </tr>
                                ))}
                                {students.length === 0 && (
                                    <tr><td className="text-center py-4">Студентов не найдено</td></tr>
                                )}
                            </tbody>
                        </Table>
                    </div>
                )
            ) : (
                <Alert variant="info" className="text-center">
                    Выберите группу из списка выше, чтобы увидеть список студентов.
                </Alert>
            )}

            {selectedGroup && !loading && (
                <CustomPagination page={page} totalPages={totalPages} onPageChange={setPage} />
            )}
        </div>
    );
};
