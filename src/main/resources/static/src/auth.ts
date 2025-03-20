import { storage } from "./storage.js";

const verifyAuthAJAX = async () => {
    const verifyAuthResponse = await fetch("/api/auth/verify", {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
    if (verifyAuthResponse.ok) {
        const verifyAuthResponseJSON = await verifyAuthResponse.json();
        storage.setItem<string>("logged", verifyAuthResponseJSON.username, 300)
        return true;
    }
    return false;
};

async function isLogged() {
    return storage.getItem<string>("logged") != null || await verifyAuthAJAX();
}

function getAuthUsername() {
    return storage.getItem<string>("logged");
}

function isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function hasUppercaseAndNumber(input: string): boolean {
    return /(?=.*[A-Z])(?=.*[0-9])/.test(input);
}

async function logout() {
    const logoutResponse = await fetch("/api/auth/logout", {
        method: "GET"
    });
    if (logoutResponse.ok) {
        window.location.reload();
        storage.removeItem("logged")
    }
};

export { verifyAuthAJAX, isLogged, getAuthUsername, isValidEmail, hasUppercaseAndNumber, logout };