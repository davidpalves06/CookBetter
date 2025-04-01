import { getAuthUsername, isLogged, logout } from "./auth.js";
import { Recipes, Recipe } from "./recipes.js";

export { };

const url = new URL(window.location.href);

const pathParts = url.pathname.split('/').filter(part => part !== '');
const profileUsername = pathParts[1] || null;
let profileUserId: string = "";

document.title = `@${profileUsername} - CookBetter`

const profileIcon = document.getElementById('profileIcon') as HTMLElement;
const authenticationBtnDiv = document.getElementById('authenticationBtnDiv') as HTMLDivElement;
const loggedInDiv = document.getElementById('loggedInDiv') as HTMLDivElement;
const editProfileBtn = document.getElementById('editProfileBtn') as HTMLButtonElement;

async function handleAuthenticationState() {
	let loggedIn: boolean = await isLogged();
	if (loggedIn) {
		let username = getAuthUsername();
		authenticationBtnDiv.classList.add('hidden');
		loggedInDiv.classList.remove('hidden');
		document.querySelectorAll(".profile-btn").forEach((item) => {
			let anchorTag = item as HTMLAnchorElement;
			anchorTag.href = `/profile/${username}`;
		});
		const manageRecipeAnchor = document.getElementById('manageRecipeAnchor') as HTMLAnchorElement
		manageRecipeAnchor.classList.remove('hidden')
		if (username === profileUsername) {
			editProfileBtn.classList.remove('hidden')
		}
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

export interface ProfileInfo {
	userId: string,
	name: string,
	username: string,
	description: string,
	avatarPhoto?: string,
	followers: number,
	following: number,
	recipes: number,
}

async function updateProfileInfo() {
	const response = await fetch(`/api/profile?username=${profileUsername}`, {
		method: "GET"
	});

	if (response.status == 200) {
		let profileInfo: ProfileInfo = await response.json();

		profileUserId = profileInfo.userId;
		const profileAvatar = document.getElementById('profileAvatar') as HTMLImageElement
		const profileContent = document.getElementById('profileContent') as HTMLDivElement
		const profileName = document.getElementById('profileName') as HTMLHeadingElement
		const profileUsername = document.getElementById('profileUsername') as HTMLParagraphElement
		const profileDescription = document.getElementById('profileDescription') as HTMLParagraphElement
		const profileRecipes = document.getElementById('profileRecipes') as HTMLParagraphElement
		const profileFollowers = document.getElementById('profileFollowers') as HTMLParagraphElement
		const profileFollowing = document.getElementById('profileFollowing') as HTMLParagraphElement
		const editBioArea = document.getElementById('editBioArea') as HTMLTextAreaElement;

		if (profileInfo.avatarPhoto != undefined) {
			profileAvatar.src = profileInfo.avatarPhoto
		} else {
			profileAvatar.src = "/avatar-default.svg"
		}
		profileName.textContent = profileInfo.name
		profileUsername.textContent = `@${profileInfo.username}`
		profileDescription.textContent = `${profileInfo.description}`
		editBioArea.value = profileDescription.textContent
		profileRecipes.textContent = `${profileInfo.recipes}`
		profileFollowers.textContent = `${profileInfo.followers}`
		profileFollowing.textContent = `${profileInfo.following}`
		profileContent.classList.remove('hidden')
		getProfileRecipes()

	} else if (response.status == 404) {
		const profileError = document.getElementById('profileError') as HTMLDivElement
		const errorStatus = document.getElementById('errorStatus') as HTMLHeadingElement
		const errorDescription = document.getElementById('errorDescription') as HTMLParagraphElement

		errorStatus.textContent = `@${profileUsername} not found`
		errorDescription.textContent = `User ${profileUsername} was not found. Please review the username and try again.`
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

const editProfileModal = document.getElementById('editProfileModal') as HTMLDivElement;
const closeModalBtn = document.getElementById('closeModalBtn') as HTMLButtonElement;
const cancelModalBtn = document.getElementById('cancelModalBtn') as HTMLButtonElement;
const editProfileForm = document.getElementById('editProfileForm') as HTMLFormElement;

editProfileBtn.addEventListener('click', () => {
	editProfileModal.classList.remove('hidden');
	editProfileModal.classList.add('flex');
});

cancelModalBtn.addEventListener('click', () => {
	editProfileModal.classList.remove('flex');
	editProfileModal.classList.add('hidden');

});

closeModalBtn.addEventListener('click', () => {
	editProfileModal.classList.remove('flex');
	editProfileModal.classList.add('hidden');

});

editProfileForm.addEventListener('submit', (e: Event) => {
	e.preventDefault();

	let errorMessage = document.getElementById('updateProfileErrorMessage') as HTMLDivElement;

	const formData = new FormData(editProfileForm);

	fetch(`/api/profile?username=${profileUsername}`, {
		method: 'PUT',
		body: formData
	})
		.then(response => {
			if (response.ok) {
				window.location.reload();
			} else {
				errorMessage.classList.remove('hidden');
				setTimeout(() => errorMessage.classList.add('hidden'), 5000)
			}
		})
		.catch(error => console.error('Error updating profile:', error));
});

function getProfileRecipes() {
	fetch(`/api/recipes/user/${profileUserId}`, {
		method: 'GET',
	})
		.then(response => {
			if (response.ok) {
				return response.json() as Promise<Recipes>
			} else {
				throw new Error("Failure while getting recipes");
			}
		}).then(recipeJson => {
			let userRecipes = recipeJson.recipes
            const recipeGrid = document.getElementById('recipeGrid') as HTMLDivElement
			if (userRecipes.length === 0) {
				recipeGrid.innerHTML = `<p class="text-gray-600 text-lg">This profile has no recipes!</p>`
			}
			userRecipes.forEach(recipe => {
				let recipeDiv = document.createElement('div');
				recipeDiv.className = "w-full bg-gray-50 rounded-lg overflow-hidden hover:shadow-md hover:scale-105 transition-shadow cursor-pointer";
				recipeDiv.innerHTML = `
                <img src="${recipe.imageUrl || '/default-recipe.svg'}" alt="${recipe.title}" class="w-full h-40 object-contain">
                <div class="p-4">
				<h4 class="text-lg font-semibold text-gray-800">${recipe.title}</h4>
				<p class="text-gray-600 text-sm">${recipe.description}</p>
				</div>
				`;
				recipeDiv.addEventListener('click', () => {
					window.location.href = `/recipe/${recipe.id}`
				})
				recipeGrid.appendChild(recipeDiv);
			})
			const recipesSection = document.getElementById('recipesSection') as HTMLDivElement;
			recipeGrid.classList.remove('hidden')
		})
		.catch(error => {
			const recipeError = document.getElementById('recipeError') as HTMLDivElement
			recipeError.innerHTML = `<h2 class="text-3xl font-bold text-gray-800" >Error loading recipes</h2>
            <p class="text-gray-600 mt-1">An error occurred while fetching recipes. Please try again later</p>`
		});
	const recipesLoadingSpinner = document.getElementById("recipesLoadingSpinner") as HTMLDivElement
	recipesLoadingSpinner.classList.add('hidden')
}
