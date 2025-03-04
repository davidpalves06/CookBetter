import { storage } from "./storage.js";

export { };

const authenticationBtnDiv = document.getElementById('authenticationBtnDiv') as HTMLDivElement;
const loggedInDiv = document.getElementById('loggedInDiv') as HTMLDivElement;
const profileIcon = document.getElementById('profileIcon') as HTMLElement;
const getStartedBtn = document.getElementById('getStartedBtn') as HTMLButtonElement;


profileIcon.addEventListener('click', (event: Event) => {
    const menu = document.getElementById("profileMenu") as HTMLDivElement;
    menu.classList.toggle("hidden");
    event.stopPropagation();
})

document.addEventListener('click', (event: Event) => {
    const menu = document.getElementById("profileMenu") as HTMLDivElement;

    if (!profileIcon.contains(event.target as Node) && !menu.contains(event.target as Node)) {
        menu.classList.add("hidden");
    }
})

async function logout() {
    const logoutResponse = await fetch("/auth/logout", {
        method: "GET"
    });
    if (logoutResponse.ok) {
        window.location.reload();
        storage.removeItem("logged")
    }
};

document.querySelectorAll(".logoutBtn").forEach((item) => {
    item.addEventListener("click", logout);
});

const hamburgerIcon = document.getElementById("hamburgerIcon") as SVGSVGElement | null;
const hamburgerMenu = document.getElementById("hamburgerMenu") as HTMLDivElement | null;
const closeIcon = document.getElementById("closeIcon") as SVGSVGElement | null;

hamburgerIcon?.addEventListener("click", (event: Event) => {
    hamburgerMenu?.classList.remove("hidden");
    setTimeout(() => hamburgerMenu?.classList.remove("translate-x-full"), 10);
    event.stopPropagation();
});

closeIcon?.addEventListener("click", () => {
    hamburgerMenu?.classList.add("translate-x-full");
    setTimeout(() => hamburgerMenu?.classList.add("hidden"), 300);
});

document.addEventListener("click", (event: Event) => {
    if (
        hamburgerMenu &&
        !hamburgerMenu.contains(event.target as Node) &&
        hamburgerIcon &&
        !hamburgerIcon.contains(event.target as Node)
    ) {
        hamburgerMenu.classList.add("hidden");
    }
});

const verifyAuthAJAX = async () => {
    const verifyAuthResponse = await fetch("/auth/verify", {
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
let username: string | null = null;
let loggedIn: boolean = storage.getItem<string>("logged") != null;

function handleLoginState() {
    if (loggedIn) {
        username = storage.getItem<string>("logged");
        authenticationBtnDiv.classList.add('hidden');
        loggedInDiv.classList.remove('hidden');
        getStartedBtn.addEventListener('click', () => {
            window.location.href = "/explore";
        });
        document.querySelectorAll(".profile-btn").forEach((item) => {
            let anchorTag = item as HTMLAnchorElement;
            anchorTag.href = `/${username}`;
        });
    } else {
        authenticationBtnDiv.classList.remove('hidden');
        loggedInDiv.classList.add('hidden');
        getStartedBtn.addEventListener('click', () => {
            window.location.href = "/login";
        });
    }
}

if (!loggedIn) {
    (async () => {
        loggedIn = await verifyAuthAJAX();
        handleLoginState();
    })();
} else {
    handleLoginState();
}





