import { jwtDecode } from "jwt-decode";
import api from "./api";

const register = (username, email, password, roles, additionalData) => {
    return api.post("/auth/register", {
        username,
        email,
        password,
        roles,
        ...additionalData
    });
};

const login = (username, password) => {
    return api.post("/auth/login", { username, password }).then((response) => {
        if (response.data.token) {
            localStorage.setItem("user", JSON.stringify(response.data));
            localStorage.setItem("token", response.data.token);
        }
        return response.data;
    });
};

const logout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
};

const getCurrentUser = () => {
    return JSON.parse(localStorage.getItem("user"));
};

const isTokenValid = () => {
    const token = localStorage.getItem("token");
    if (!token) return false;

    try {
        const decoded = jwtDecode(token);
        const currentTime = Date.now() / 1000;
        return decoded.exp > currentTime;
    } catch (err) {
        return false;
    }
};

const hasRole = (role) => {
    const user = getCurrentUser();
    if (!user || !user.roles) return false;
    return user.roles.includes(`ROLE_${role}`) || user.roles.includes(role);
};

export default {
    register,
    login,
    logout,
    getCurrentUser,
    isTokenValid,
    hasRole
};
