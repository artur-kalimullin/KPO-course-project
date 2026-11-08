const API_URL = "/api";

export const apiFetch = async (endpoint, options = {}) => {
    const token = localStorage.getItem("token");
    
    const headers = {
        "Content-Type": "application/json",
        ...options.headers,
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}${endpoint}`, { ...options, headers });

    if (!response.ok) {
        if (response.status === 401 && window.location.pathname !== "/login") {
            localStorage.removeItem("token");
            localStorage.removeItem("role");
            window.location.href = "/login";
        }
        
        const errorData = await response.json().catch(() => ({}));
        
        if (response.status === 401) {
            throw new Error("Неверный логин или пароль");
        }
        
        throw new Error(errorData.message || "Ошибка запроса");
    }

    return response.json();
};