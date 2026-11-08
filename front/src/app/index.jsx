import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "../shared/auth";
import { Header } from "../widgets/header";
import { Footer } from "../widgets/footer";
import { MainPage } from "../pages/main-page";
import { LoginPage } from "../pages/login-page";
import { AdminDashboard } from "../pages/admin-dashboard";

const GroupsPage = () => <div className="text-white text-center mt-5"><h2>Группы (в разработке)</h2></div>;
const SubjectsPage = () => <div className="text-white text-center mt-5"><h2>Предметы (в разработке)</h2></div>;
const UsersPage = () => <div className="text-white text-center mt-5"><h2>Пользователи (в разработке)</h2></div>;

const ProtectedRoute = ({ children, allowedRole }) => {
    const { user } = useAuth();
    if (!user) return <Navigate to="/login" replace />;
    if (allowedRole && user.role !== allowedRole) return <Navigate to="/" replace />;
    return children;
};

export const App = () => {
    return (
        <AuthProvider>
            <BrowserRouter>
                <div className="d-flex flex-column min-vh-100">
                    <Header />
                    <main className="flex-grow-1 d-flex flex-column">
                        <Routes>
                            <Route path="/" element={<div className="d-flex align-items-center flex-grow-1"><MainPage /></div>} />
                            <Route path="/login" element={<div className="d-flex align-items-center flex-grow-1"><LoginPage /></div>} />

                            <Route path="/admin" element={
                                <ProtectedRoute allowedRole="ROLE_ADMIN">
                                    <AdminDashboard />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/groups" element={
                                <ProtectedRoute allowedRole="ROLE_ADMIN">
                                    <GroupsPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/subjects" element={
                                <ProtectedRoute allowedRole="ROLE_ADMIN">
                                    <SubjectsPage />
                                </ProtectedRoute>
                            } />
                            <Route path="/admin/users" element={
                                <ProtectedRoute allowedRole="ROLE_ADMIN">
                                    <UsersPage />
                                </ProtectedRoute>
                            } />

                            <Route path="*" element={<Navigate to="/" replace />} />
                        </Routes>
                    </main>
                    <Footer />
                </div>
            </BrowserRouter>
        </AuthProvider>
    );
};