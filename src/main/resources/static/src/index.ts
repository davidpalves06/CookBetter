import { isLogged, getAuthUsername, logout } from "./auth.js";

export { };

const authenticationBtnDiv = document.getElementById('authenticationBtnDiv') as HTMLDivElement;
const loggedInDiv = document.getElementById('loggedInDiv') as HTMLDivElement;
const profileIcon = document.getElementById('profileIcon') as HTMLElement;
const getStartedBtn = document.getElementById('getStartedBtn') as HTMLButtonElement;

async function handleAuthenticationState() {
    let loggedIn: boolean = await isLogged();
    if (loggedIn) {
        let username = getAuthUsername();
        authenticationBtnDiv.classList.add('hidden');
        loggedInDiv.classList.remove('hidden');
        getStartedBtn.addEventListener('click', () => {
            window.location.href = "/explore";
        });
        document.querySelectorAll(".profile-btn").forEach((item) => {
            let anchorTag = item as HTMLAnchorElement;
            anchorTag.href = `/profile/${username}`;
        });
    } else {
        authenticationBtnDiv.classList.remove('hidden');
        loggedInDiv.classList.add('hidden');
        getStartedBtn.addEventListener('click', () => {
            window.location.href = "/login";
        });
    }
}

handleAuthenticationState();

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






