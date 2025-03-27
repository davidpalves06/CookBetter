import { isLogged, getAuthUsername, logout } from "./auth.js";
import { Recipe } from "./recipes.js";
export {}

const profileIcon = document.getElementById('profileIcon') as HTMLElement;

async function handleAuthenticationState() {
    let loggedIn: boolean = await isLogged();
    if (loggedIn) {
        let username = getAuthUsername();
        document.querySelectorAll(".profile-btn").forEach((item) => {
            let anchorTag = item as HTMLAnchorElement;
            anchorTag.href = `/profile/${username}`;
        });
    } else {
        window.location.href = "/login";
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

function loadRecipe() {
    const recipeDetail = document.getElementById('recipeDetail') as HTMLElement;
    const loadingSpinner = document.getElementById('loadingSpinner') as HTMLElement;
    const errorMessage = document.getElementById('errorMessage') as HTMLElement;

    const pathSegments = window.location.pathname.split('/');
    const recipeId = pathSegments[pathSegments.length - 1];

    if (!recipeId || isNaN(Number(recipeId))) {
        loadingSpinner.classList.add('hidden');
        errorMessage.classList.remove('hidden');
        errorMessage.textContent = 'Invalid recipe ID.';
        return;
    }

    fetch(`/api/recipes/${recipeId}`, {
        method: 'GET'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Recipe not found');
        }
        return response.json();
    })
    .then((recipe: Recipe) => {
        loadingSpinner.classList.add('hidden');
        recipeDetail.innerHTML = `
            <div class="relative rounded-lg overflow-hidden shadow-md">
                <img src="${recipe.imageUrl || '/default-recipe.jpg'}" alt="${recipe.title}" class="w-full h-64 object-contain">
                <div class="absolute inset-0 bg-gradient-to-t from-gray-900 to-transparent opacity-75"></div>
                <h1 class="absolute bottom-4 left-4 text-3xl font-bold text-white">${recipe.title}</h1>
            </div>
            <p class="text-gray-700 italic">${recipe.description || 'No description provided'}</p>
            <div>
            <h2 class="text-xl font-semibold text-gray-800 mb-2">Ingredients</h2>
            <ul class="list-disc list-inside space-y-1 text-gray-700">
            ${recipe.ingredients.map(ingredient => `<li>${ingredient}</li>`).join('')}
            </ul>
            </div>
            <div>
            <h2 class="text-xl font-semibold text-gray-800 mb-2">Instructions</h2>
            <ol class="list-decimal list-inside space-y-1 text-gray-700">
            ${recipe.instructions.map(instruction => `<li>${instruction}</li>`).join('')}
            </ol>
            </div>
            <div class="flex flex-wrap gap-2">
            ${recipe.tags != null ? recipe.tags.map(tag => `<span class="px-3 py-1 bg-orange-100 text-orange-800 rounded-full text-sm">${tag}</span>`).join('') : ""}
            </div>
        `;
    })
    .catch(error => {
        console.error('Error loading recipe:', error);
        loadingSpinner.classList.add('hidden');
        errorMessage.classList.remove('hidden');
        errorMessage.textContent = 'Failed to load recipe. Please try again.';
    });
}

loadRecipe();