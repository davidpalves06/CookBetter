import { isLogged, getAuthUsername } from "./auth.js";

export { };

async function handleAuthenticationState() {
    let loggedIn: boolean = await isLogged();
    if (!loggedIn) {
        window.location.href = "/login";
    }
}

handleAuthenticationState();