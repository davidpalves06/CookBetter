import { getAuthUsername, isLogged, logout } from "./auth.js";

export { };

const profileTitle = document.getElementById('profileTitle') as HTMLHeadingElement;
const url = new URL(window.location.href);

const pathParts = url.pathname.split('/').filter(part => part !== '');
const username = pathParts[0] || null;

document.title = `@${username} - CookBetter`

const profileIcon = document.getElementById('profileIcon') as HTMLElement;
const authenticationBtnDiv = document.getElementById('authenticationBtnDiv') as HTMLDivElement;
const loggedInDiv = document.getElementById('loggedInDiv') as HTMLDivElement;

async function handleAuthenticationState() {
	let loggedIn: boolean = await isLogged();
	if (loggedIn) {
		let username = getAuthUsername();
		authenticationBtnDiv.classList.add('hidden');
		loggedInDiv.classList.remove('hidden');
		document.querySelectorAll(".profile-btn").forEach((item) => {
			let anchorTag = item as HTMLAnchorElement;
			anchorTag.href = `/${username}`;
		});
	} else {
		authenticationBtnDiv.classList.remove('hidden');
		loggedInDiv.classList.add('hidden');
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

interface ProfileInfo {
	name: string,
	username: string,
	description: string,
	avatarPhoto?: string,
	followers: number,
	following: number,
	recipes: number,
}

async function updateProfileInfo() {
	const response = await fetch(`/profile/${username}`, {
		method: "GET"
	});

	if (response.status == 200) {
		let profileInfo : ProfileInfo = await response.json();
		console.log(profileInfo);
		const profileContent = document.getElementById('profileContent') as HTMLDivElement
		const profileAvatar = document.getElementById('profileAvatar') as HTMLImageElement
		const defaultAvatar = document.getElementById('defaultAvatar') as HTMLImageElement
		const profileName = document.getElementById('profileName') as HTMLHeadingElement
		const profileUsername = document.getElementById('profileUsername') as HTMLParagraphElement
		const profileDescription = document.getElementById('profileDescription') as HTMLParagraphElement
		const profileRecipes = document.getElementById('profileRecipes') as HTMLParagraphElement
		const profileFollowers = document.getElementById('profileFollowers') as HTMLParagraphElement
		const profileFollowing = document.getElementById('profileFollowing') as HTMLParagraphElement
		
		if (profileInfo.avatarPhoto != undefined) {
			profileAvatar.src = profileInfo.avatarPhoto
			defaultAvatar.classList.add("hidden")
			profileAvatar.classList.remove("hidden")
		} else {
			defaultAvatar.classList.remove("hidden")
		}
		profileName.textContent = profileInfo.name
		profileUsername.textContent = `@${profileInfo.username}`
		profileDescription.textContent = `${profileInfo.description}`
		profileRecipes.textContent = `${profileInfo.recipes}`
		profileFollowers.textContent = `${profileInfo.followers}`
		profileFollowing.textContent = `${profileInfo.following}`
		profileContent.classList.remove('hidden')
	} else if (response.status == 404) {
		const profileError = document.getElementById('profileError') as HTMLDivElement
		const errorStatus = document.getElementById('errorStatus') as HTMLHeadingElement
		const errorDescription = document.getElementById('errorDescription') as HTMLParagraphElement

		errorStatus.textContent = `@${username} not found`
		errorDescription.textContent = `User ${username} was not found. Please review the username and try again.`
		profileError.classList.remove('hidden');
	}
	else {
		const profileError = document.getElementById('profileError') as HTMLDivElement
		const errorStatus = document.getElementById('errorStatus') as HTMLHeadingElement
		const errorDescription = document.getElementById('errorDescription') as HTMLParagraphElement

		errorStatus.textContent = `Server Error`;
		errorDescription.textContent = `Internal Server Error ocurred. Check your connection and try again later.`;
		profileError.classList.remove('hidden');
	}
	const loadingSpinner = document.getElementById("loadingSpinner") as HTMLDivElement
	loadingSpinner.classList.add('hidden')
}

updateProfileInfo()