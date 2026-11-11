import { createContext, useContext, useState, useEffect } from "react";
import { apiFetch } from "./api";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem("token");
        const role = localStorage.getItem("role");
        const username = localStorage.getItem("username");
        if (token && role) {
            setUser({ token, role, username });
        }
        setLoading(false);
    }, []);

    const login = async (username, password) => {
        const data = await apiFetch("/auth/login", {
            method: "POST",
            body: JSON.stringify({ username, password }),
        });
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);
        localStorage.setItem("username", data.username);
        setUser({ token: data.token, role: data.role, username: data.username });
        return data.role;
    };

    const logout = async () => {
        try {
            await apiFetch("/auth/logout", { method: "POST" });
        } catch (e) {
            console.error(e);
        }
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("username");
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, loading }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);