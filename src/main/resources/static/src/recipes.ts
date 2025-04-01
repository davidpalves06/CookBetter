import { getAuthUserID, getAuthUsername, isLogged, logout } from "./auth.js";

export { }

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

export interface Recipe {
    id: string;
    userId: string;
    title: string;
    description: string;
    ingredients: string[];
    instructions: string[];
    tags: string[];
    imageUrl?: string;
}

export interface Recipes {
    recipes: Recipe[]
}

function loadRecipes() {
    const recipesList = document.getElementById('recipesList') as HTMLElement;
    const loadingSpinner = document.getElementById('loadingSpinner') as HTMLElement;
    const userId = getAuthUserID();
    
    fetch(`/api/recipes/user/${userId}`, {
        method: 'GET'
    })
        .then(response => response.json())
        .then((recipes: Recipes) => {
            let recipeList = recipes.recipes;
            recipesList.innerHTML = '';
            if (recipeList.length === 0) {
                recipesList.innerHTML = '<p class="col-start-1 col-end-3 text-gray-800 font-medium">No recipes yet. Start sharing your recipes now.</p>'
            } else {
                recipeList.forEach(recipe => {
                    const recipeCard = document.createElement('div');
                    recipeCard.className = ' bg-gray-50 rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow';
                    recipeCard.innerHTML = `
                    <div class="mt-2 flex justify-center gap-2 relative">
                    <img src="${recipe.imageUrl || '/default-recipe.svg'}" alt="${recipe.title}" class="flex h-40 object-cover rounded-sm">
                    <div class="absolute -inset-0.5 bg-gradient-to-t from-gray-50 via-transparent to-transparent"></div>
                    </div>
                    <div class="p-4">
                    <h4 class="text-lg font-semibold text-gray-800">${recipe.title}</h4>
                    <p class="text-gray-600 text-sm line-clamp-2 overflow-ellipsis">${recipe.description || 'No description'}</p>
                    <div class="mt-2 flex justify-end gap-2">
                        <button class="view-btn px-2 py-1 text-orange-600 hover:text-orange-800 hover:font-semibold cursor-pointer" data-id="${recipe.id}">View</button>
                        <button class="delete-btn px-2 py-1 text-red-600 hover:text-red-800 hover:font-semibold cursor-pointer" data-id="${recipe.id}">Delete</button>
                    </div>
                    </div>
                    `;
                    recipesList.appendChild(recipeCard);
                });
            }
            loadingSpinner.classList.add('hidden');

            document.querySelectorAll('.view-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = (e.target as HTMLButtonElement).dataset.id;
                    window.location.href = `/recipe/${id}`
                });
            });

            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = (e.target as HTMLButtonElement).dataset.id;
                    if (confirm('Are you sure you want to delete this recipe?')) {
                        deleteRecipe(Number(id));
                    }
                });
            });
        })
        .catch(error => {
            console.error('Error loading recipes:', error);
            loadingSpinner.classList.add('hidden');
            recipesList.innerHTML = '<p class="text-red-600">Failed to load recipes.</p>';
        });
}

function deleteRecipe(id: number) {
    fetch(`/api/recipes/${id}`, {
        method: 'DELETE'
    })
        .then((response) => {
            if (response.ok) {
                window.location.reload()
            } else
                throw new Error("Error response from server");
        }).catch(error => console.error('Error deleting recipe:', error));
}

const createRecipeBtn = document.getElementById('createRecipeBtn') as HTMLButtonElement;
const createRecipeModal = document.getElementById('createRecipeModal') as HTMLElement;
const closeCreateModalBtn = document.getElementById('closeCreateModalBtn') as HTMLButtonElement;
const cancelCreateModalBtn = document.getElementById('cancelCreateModalBtn') as HTMLButtonElement;
const createRecipeForm = document.getElementById('createRecipeForm') as HTMLFormElement;
const createImageInput = document.getElementById('createImage') as HTMLInputElement;

createRecipeBtn.addEventListener('click', () => {
    createRecipeModal.classList.remove('hidden');
    createRecipeModal.classList.add('flex');
});

closeCreateModalBtn.addEventListener('click', () => {
    createRecipeModal.classList.add('hidden');
});

cancelCreateModalBtn.addEventListener('click', () => {
    createRecipeModal.classList.add('hidden');
});


createRecipeForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const tagsInput = (createRecipeForm.querySelector('input[name="tags"]') as HTMLInputElement).value;
    const tags = tagsInput ? tagsInput.split(',').map(tag => tag.trim()) : [];

    const formData = new FormData(createRecipeForm);
    formData.delete('tags');

    tags.forEach(tag => formData.append('tags', tag));
    
    fetch('/api/recipes', {
        method: 'POST',
        body: formData
    }).then(async response => {
        if (response.ok) window.location.reload();
        if (response.status == 400) {
            let errorText = await response.text()
            throw new Error(errorText);
        }
        throw new Error("Could not create recipe. Check your input and try later")
    }
    ).catch(error => {
        const errorMessage = document.getElementById('errorMessage') as HTMLDivElement
        errorMessage.innerHTML = error.message
        errorMessage.classList.remove('hidden')
    });
}); 

createImageInput.addEventListener('change', function(e:Event) {
    const input = e.target as HTMLInputElement;
    let fileName = 'No file chosen';
    if (input.files && input.files.length > 0) {
        fileName = input.files[0].name;
    }
    let fileNameDiv = document.getElementById('file-name') as HTMLSpanElement; 
    fileNameDiv.textContent = fileName;
});

const ingredientsList = document.getElementById('ingredientsList') as HTMLElement;
const addIngredientBtn = document.getElementById('addIngredientBtn') as HTMLButtonElement;
const instructionsList = document.getElementById('instructionsList') as HTMLElement;
const addInstructionBtn = document.getElementById('addInstructionBtn') as HTMLButtonElement;

function addIngredientField() {
    const ingredientDiv = document.createElement('div');
    ingredientDiv.className = 'flex items-center gap-2';
    ingredientDiv.innerHTML = `
        <input type="text" name="ingredients" required class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-600" placeholder="e.g., 1 lb chicken">
        <button type="button" class="remove-ingredient px-2 py-1 text-red-600 hover:text-red-800">Remove</button>
    `;
    ingredientsList.appendChild(ingredientDiv);
    ingredientDiv.querySelector('.remove-ingredient')!.addEventListener('click', () => {
        if (ingredientsList.children.length > 1) {
            ingredientsList.removeChild(ingredientDiv);
        }
    });
}

function addInstructionField() {
    const instructionDiv = document.createElement('div');
    instructionDiv.className = 'flex items-center gap-2';
    instructionDiv.innerHTML = `
        <input type="text" name="instructions" required class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-600" placeholder="e.g., Preheat oven to 350°F">
        <button type="button" class="remove-instruction px-2 py-1 text-red-600 hover:text-red-800">Remove</button>
    `;
    instructionsList.appendChild(instructionDiv);
    instructionDiv.querySelector('.remove-instruction')!.addEventListener('click', () => {
        if (instructionsList.children.length > 1) {
            instructionsList.removeChild(instructionDiv);
        }
    });
}

addIngredientBtn.addEventListener('click', addIngredientField);
addInstructionBtn.addEventListener('click', addInstructionField);

ingredientsList.querySelector('.remove-ingredient')!.addEventListener('click', () => {
    if (ingredientsList.children.length > 1) {
        ingredientsList.removeChild(ingredientsList.children[0]);
    }
});

instructionsList.querySelector('.remove-instruction')!.addEventListener('click', () => {
    if (instructionsList.children.length > 1) {
        instructionsList.removeChild(instructionsList.children[0]);
    }
});

loadRecipes();