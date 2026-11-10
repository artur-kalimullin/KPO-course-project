import { useState, useEffect } from "react";
import { apiFetch } from "../../shared/api";
import { Card, Form, Button, Row, Col, Alert, Table, Spinner } from "react-bootstrap";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export const TeacherStatsPage = () => {
    const [groups, setGroups] = useState([]);
    const [subjects, setSubjects] = useState([]);
    
    const [selectedGroup, setSelectedGroup] = useState("");
    const [selectedSubject, setSelectedSubject] = useState("");
    const [filteredSubjects, setFilteredSubjects] = useState([]);
    
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadData = async () => {
            try {
                const [groupsData, subjectsData] = await Promise.all([
                    apiFetch("/teacher/my-groups"),
                    apiFetch("/teacher/grades/my-subjects")
                ]);
                setGroups(groupsData || []);
                setSubjects(subjectsData || []);
            } catch (err) {
                setError("Ошибка загрузки данных");
            }
        };
        loadData();
    }, []);

    useEffect(() => {
        if (!selectedGroup) {
            setFilteredSubjects(subjects);
            setSelectedSubject("");
            return;
        }

        const loadSubjectsForGroup = async () => {
            try {
                const data = await apiFetch(`/teacher/groups/${selectedGroup}/subjects`);
                setFilteredSubjects(data || []);
                setSelectedSubject("");
            } catch (err) {
                setError("Ошибка загрузки предметов для группы");
            }
        };
        loadSubjectsForGroup();
    }, [selectedGroup, subjects]);

    const handleGenerate = async () => {
        if (!selectedGroup || !selectedSubject) {
            setError("Выберите и группу, и предмет");
            return;
        }
        setError(null);
        setLoading(true);
        setStats(null);

        try {
            const data = await apiFetch(`/teacher/stats?groupId=${selectedGroup}&subjectId=${selectedSubject}`);
            setStats(data);
        } catch (err) {
            setError("Ошибка формирования статистики: " + err.message);
        } finally {
            setLoading(false);
        }
    };

    const chartData = stats ? [
        { name: '5', count: stats.gradeDistribution[5] || 0, fill: '#198754' },
        { name: '4', count: stats.gradeDistribution[4] || 0, fill: '#0dcaf0' },
        { name: '3', count: stats.gradeDistribution[3] || 0, fill: '#ffc107' },
        { name: '2', count: stats.gradeDistribution[2] || 0, fill: '#dc3545' },
        { name: 'Неявка', count: stats.gradeDistribution[0] || 0, fill: '#6c757d' }
    ] : [];

    return (
        <div className="container py-4">
            <h2 className="text-white mb-4">Статистика успеваемости</h2>
            
            <Card className="bg-dark bg-opacity-75 text-white shadow border-0 mb-4 p-3">
                <Row className="g-2 align-items-end">
                    <Col md={4}>
                        <Form.Label>Группа</Form.Label>
                        <Form.Select value={selectedGroup} onChange={e => setSelectedGroup(e.target.value)}>
                            <option value="">Выберите группу...</option>
                            {groups.map(g => <option key={g.id} value={g.id}>{g.fullName}</option>)}
                        </Form.Select>
                    </Col>
                    <Col md={4}>
                        <Form.Label>Предмет</Form.Label>
                        <Form.Select 
                            value={selectedSubject} 
                            onChange={e => setSelectedSubject(e.target.value)}
                            disabled={!selectedGroup}
                        >
                            <option value="">
                                {selectedGroup ? "Выберите предмет..." : "Сначала выберите группу"}
                            </option>
                            {filteredSubjects.map(s => (
                                <option key={s.id} value={s.id}>{s.name}</option>
                            ))}
                        </Form.Select>
                    </Col>
                    <Col md={4}>
                        <Button variant="warning" className="w-100 fw-bold" onClick={handleGenerate}>
                            <i className="bi bi-bar-chart-fill me-2"></i>Сформировать
                        </Button>
                    </Col>
                </Row>
            </Card>

            {error && <Alert variant="danger">{error}</Alert>}
            {loading && <div className="text-center mt-5"><Spinner animation="border" variant="warning" /></div>}

            {stats && !loading && (
                <div className="bg-white text-dark p-4 rounded shadow">
                    <h4 className="mb-4">Статистика по группе: <strong>{stats.groupName}</strong></h4>
                    
                    <Row className="mb-4 text-center">
                        <Col md={4}>
                            <div className="p-3 border rounded bg-light">
                                <h5 className="text-muted mb-2">Количество студентов</h5>
                                <h2 className="mb-0">{stats.totalStudents}</h2>
                            </div>
                        </Col>
                        <Col md={4}>
                            <div className="p-3 border rounded bg-light">
                                <h5 className="text-muted mb-2">Аттестовано</h5>
                                <h2 className="text-success mb-0">{stats.certifiedCount}</h2>
                            </div>
                        </Col>
                        <Col md={4}>
                            <div className="p-3 border rounded bg-light">
                                <h5 className="text-muted mb-2">Не сдали</h5>
                                <h2 className="text-danger mb-0">{stats.failedCount}</h2>
                            </div>
                        </Col>
                    </Row>

                    <h5 className="mb-3 text-center">Распределение оценок</h5>
                    <div style={{ width: '100%', height: 300 }} className="mb-5">
                        <ResponsiveContainer>
                            <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                <XAxis dataKey="name" />
                                <YAxis allowDecimals={false} />
                                <Tooltip />
                                <Bar dataKey="count" name="Студентов" fill="#8884d8">
                                    {chartData.map((entry, index) => (
                                        <rect key={`cell-${index}`} fill={entry.fill} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    <h5 className="mb-3">Неаттестованные студенты</h5>
                    {stats.failedStudents.length > 0 ? (
                        <Table striped bordered hover>
                            <thead>
                                <tr>
                                    <th>ФИО</th>
                                    <th>Оценка</th>
                                </tr>
                            </thead>
                            <tbody>
                                {stats.failedStudents.map((s, idx) => (
                                    <tr key={idx}>
                                        <td>{s.fullName}</td>
                                        <td className="text-danger fw-bold">
                                            {s.grade === -1 ? "Нет оценки" : s.grade === 0 ? "Неявка" : s.grade}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    ) : (
                        <Alert variant="success">Все студенты аттестованы! Отличная работа.</Alert>
                    )}
                </div>
            )}
        </div>
    );
};