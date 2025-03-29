import { storage } from "./storage.js";

interface AuthenticationInfo {
    userId:string,
    username:string
}
const verifyAuthAJAX = async () => {
    const verifyAuthResponse = await fetch("/api/auth/verify", {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
    if (verifyAuthResponse.ok) {
        const verifyAuthResponseJSON = await verifyAuthResponse.json();
        storage.setItem<string>("logged", JSON.stringify(verifyAuthResponseJSON), 300)
        return true;
    }
    return false;
};

async function isLogged() {
    return storage.getItem<string>("logged") != null || await verifyAuthAJAX();
}

function getAuthUsername() {
    let authString = storage.getItem<string>("logged");
    if (authString != null) {
        let authInfo = JSON.parse(storage.getItem<string>("logged") as string) as AuthenticationInfo;
        return authInfo.username
    }
    return null;
}

function getAuthUserID() {
    let authString = storage.getItem<string>("logged");
    if (authString != null) {
        let authInfo = JSON.parse(storage.getItem<string>("logged") as string) as AuthenticationInfo;
        return authInfo.userId
    }
    return null;
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

export { verifyAuthAJAX, isLogged, getAuthUsername, getAuthUserID, isValidEmail, hasUppercaseAndNumber, logout };