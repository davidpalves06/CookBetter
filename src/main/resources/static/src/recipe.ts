import { isLogged, logout, getAuthUserID, getAuthUsername } from "./auth.js";
import { Recipe } from "./recipes.js";
import { ProfileInfo } from "./profile.js";
export { }

const profileIcon = document.getElementById('profileIcon') as HTMLElement;
let authUserId : string | null;
async function handleAuthenticationState() {
    let loggedIn: boolean = await isLogged();
    if (loggedIn) {
        let username = getAuthUsername();
        authUserId = getAuthUserID();
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
            fetch(`/api/profile?userId=${recipe.userId}`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            }).then(response => {
                if (response.ok) {
                    return response.json() as Promise<ProfileInfo>;
                } else {
                    throw new Error("Error getting profile info");
                }
            }).then((user: ProfileInfo) => {
                recipeDetail.innerHTML = `
                <div class="relative rounded-lg overflow-hidden shadow-md">
                    <img src="${recipe.imageUrl || '/default-recipe.svg'}" alt="${recipe.title}" class="w-full h-64 object-contain">
                    <div class="absolute inset-0 bg-gradient-to-t from-gray-400 to-transparent opacity-75"></div>
                    <h1 class="absolute bottom-4 left-4 text-3xl font-bold text-white">${recipe.title}</h1>
                    </div>
                    <div class="flex items-center gap-4 mt-4">
                        <img src="${user.avatarPhoto || '/avatar-default.svg'}" alt="${user.username}" class="w-15 h-15 rounded-full object-contain">
                        <span class="text-gray-800 font-medium text-xl">${user.username}</span>
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
                <button id="editRecipeBtn" class="hidden cursor-pointer mt-4 px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700">Edit Recipe</button>
            `;
                

                const editRecipeModal = document.getElementById('editRecipeModal') as HTMLElement;
                const editRecipeForm = document.getElementById('editRecipeForm') as HTMLFormElement;
                const editIngredientsList = document.getElementById('editIngredientsList') as HTMLElement;
                const editInstructionsList = document.getElementById('editInstructionsList') as HTMLElement;

                const editRecipeBtn = document.getElementById('editRecipeBtn') as HTMLButtonElement;
                if (authUserId === user.userId) {
                    editRecipeBtn.classList.remove('hidden');
                }

                editRecipeBtn.addEventListener('click', () => {
                    editRecipeModal.classList.remove('hidden');
                    editRecipeModal.classList.add('flex');
                    populateEditForm(recipe);
                });

                const cancelEditBtn = document.getElementById('cancelEdit') as HTMLButtonElement;
                cancelEditBtn.addEventListener('click', () => {
                    editRecipeModal.classList.add('hidden');
                });

                const addIngredientBtn = document.getElementById('addEditIngredient') as HTMLButtonElement;
                addIngredientBtn.addEventListener('click', () => {
                    addInputField(editIngredientsList, 'ingredients');
                });

                const addInstructionBtn = document.getElementById('addEditInstruction') as HTMLButtonElement;
                addInstructionBtn.addEventListener('click', () => {
                    addInputField(editInstructionsList, 'instructions');
                });

                editRecipeForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    const formData = new FormData(editRecipeForm);
                    const ingredients = Array.from(editIngredientsList.querySelectorAll('input[name="ingredients"]'))
                        .map(input => (input as HTMLInputElement).value)
                        .filter(value => value.trim() !== '');
                    const instructions = Array.from(editInstructionsList.querySelectorAll('input[name="instructions"]'))
                        .map(input => (input as HTMLInputElement).value)
                        .filter(value => value.trim() !== '');
                    const tagsInput = (editRecipeForm.querySelector('input[name="tags"]') as HTMLInputElement).value;
                    const tags = tagsInput ? tagsInput.split(',').map(tag => tag.trim()) : [];

                    formData.delete('ingredients');
                    formData.delete('instructions');
                    formData.delete('tags');
                    ingredients.forEach(ingredient => formData.append('ingredients', ingredient));
                    instructions.forEach(instruction => formData.append('instructions', instruction));
                    tags.forEach(tag => formData.append('tags', tag));

                    fetch(`/api/recipes/${recipeId}`, {
                        method: 'PUT',
                        body: formData
                    })
                        .then(response => {
                            if (!response.ok) throw new Error('Failed to update recipe');
                            window.location.reload();
                        })
                        .then(() => {
                            editRecipeModal.classList.add('hidden');
                            loadRecipe();
                        })
                        .catch(error => {
                            console.error('Error updating recipe:', error);
                            alert('Failed to update recipe. Please try again.');
                        });
                });
                
                const updateImageInput = document.getElementById('updateImage') as HTMLInputElement;
                updateImageInput.addEventListener('change', function(e:Event) {
                    const input = e.target as HTMLInputElement;
                    let fileName = 'No file chosen';
                    if (input.files && input.files.length > 0) {
                        fileName = input.files[0].name;
                    }
                    let fileNameDiv = document.getElementById('file-name') as HTMLSpanElement; 
                    fileNameDiv.textContent = fileName;
                });
                loadingSpinner.classList.add('hidden');

            });
        })
        .catch(error => {
            console.error('Error loading recipe:', error);
            loadingSpinner.classList.add('hidden');
            errorMessage.classList.remove('hidden');
            errorMessage.textContent = 'Failed to load recipe. Please try again.';
        });
}

function populateEditForm(recipe: Recipe) {
    const form = document.getElementById('editRecipeForm') as HTMLFormElement;
    const editIngredientsList = document.getElementById('editIngredientsList') as HTMLElement;
    const editInstructionsList = document.getElementById('editInstructionsList') as HTMLElement;

    (form.querySelector('input[name="title"]') as HTMLInputElement).value = recipe.title;
    (form.querySelector('textarea[name="description"]') as HTMLTextAreaElement).value = recipe.description || '';
    (form.querySelector('input[name="tags"]') as HTMLInputElement).value = recipe.tags.join(', ');

    editIngredientsList.innerHTML = '';
    recipe.ingredients.forEach(ingredient => addInputField(editIngredientsList, 'ingredients', ingredient));

    editInstructionsList.innerHTML = '';
    recipe.instructions.forEach(instruction => addInputField(editInstructionsList, 'instructions', instruction));
}

function addInputField(container: HTMLElement, name: string, value: string = '') {
    const div = document.createElement('div');
    div.className = 'flex items-center space-x-2';
    div.innerHTML = `
        <input type="text" name="${name}" value="${value}" class="w-full px-3 py-2 border border-gray-300 rounded-lg">
        <button type="button" class="px-2 py-1 cursor-pointer text-red-600 hover:text-red-800">Remove</button>
    `;
    div.querySelector('button')?.addEventListener('click', () => {
        if (container.children.length > 1) {
            container.removeChild(div);
        }
    });
    container.appendChild(div);
}

loadRecipe();