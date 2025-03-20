import { getAuthUsername, isLogged, logout } from "./auth.js";

export { }

const profileIcon = document.getElementById('profileIcon') as HTMLElement;

async function handleAuthenticationState() {
    let loggedIn: boolean = await isLogged();
    if (loggedIn) {
        let username = getAuthUsername();
        document.querySelectorAll(".profile-btn").forEach((item) => {
            let anchorTag = item as HTMLAnchorElement;
            anchorTag.href = `/${username}`;
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

interface Recipe {
    id: string;
    title: string;
    description: string;
    ingredients: string[];
    instructions: string[];
    tags?: string[];
    image?: string;
}

interface Recipes {
    recipes: Recipe[]
}

function loadRecipes() {
    const recipesList = document.getElementById('recipesList') as HTMLElement;
    const loadingSpinner = document.getElementById('loadingSpinner') as HTMLElement;

    fetch('/api/recipes/user', {
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
                    recipeCard.className = 'bg-gray-50 rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow';
                    recipeCard.innerHTML = `
                    <img src="${recipe.image || '/default-recipe.jpg'}" alt="${recipe.title}" class="w-full h-40 object-cover">
                    <div class="p-4">
                    <h4 class="text-lg font-semibold text-gray-800">${recipe.title}</h4>
                    <p class="text-gray-600 text-sm">${recipe.description || 'No description'}</p>
                    <div class="mt-2 flex justify-end gap-2">
                        <button class="edit-btn px-2 py-1 text-orange-600 hover:text-orange-800" data-id="${recipe.id}">Edit</button>
                        <button class="delete-btn px-2 py-1 text-red-600 hover:text-red-800" data-id="${recipe.id}">Delete</button>
                    </div>
                    </div>
                    `;
                    recipesList.appendChild(recipeCard);
                });
            }
            loadingSpinner.classList.add('hidden');

            document.querySelectorAll('.edit-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = (e.target as HTMLButtonElement).dataset.id;
                    alert(`Edit recipe ID: ${id} (Edit functionality TBD)`);
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
        .then(() => window.location.reload())
        .catch(error => console.error('Error deleting recipe:', error));
}

const createRecipeBtn = document.getElementById('createRecipeBtn') as HTMLButtonElement;
const createRecipeModal = document.getElementById('createRecipeModal') as HTMLElement;
const closeCreateModalBtn = document.getElementById('closeCreateModalBtn') as HTMLButtonElement;
const cancelCreateModalBtn = document.getElementById('cancelCreateModalBtn') as HTMLButtonElement;
const createRecipeForm = document.getElementById('createRecipeForm') as HTMLFormElement;

createRecipeBtn.addEventListener('click', () => {
    createRecipeModal.classList.remove('hidden');
});

closeCreateModalBtn.addEventListener('click', () => {
    createRecipeModal.classList.add('hidden');
});

cancelCreateModalBtn.addEventListener('click', () => {
    createRecipeModal.classList.add('hidden');
});


createRecipeForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const formData = new FormData(createRecipeForm);
    
    fetch('/api/recipes', {
        method: 'POST',
        body: formData
    }).then(response => {
        if (response.ok) window.location.reload();}
    ).catch(error => {
        console.error('Error creating recipe:', error);
        alert('Failed to create recipe.');
    });
});

const ingredientsList = document.getElementById('ingredientsList') as HTMLElement;
const addIngredientBtn = document.getElementById('addIngredientBtn') as HTMLButtonElement;
const instructionsList = document.getElementById('instructionsList') as HTMLElement;
const addInstructionBtn = document.getElementById('addInstructionBtn') as HTMLButtonElement;
const tagsList = document.getElementById('tagsList') as HTMLElement;
const addTagBtn = document.getElementById('addTagBtn') as HTMLButtonElement;

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

function addTagField() {
    const tagDiv = document.createElement('div');
    tagDiv.className = 'flex items-center gap-2';
    tagDiv.innerHTML = `
        <input type="text" name="tags" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-600" placeholder="e.g., 1 lb chicken">
        <button type="button" class="remove-tag px-2 py-1 text-red-600 hover:text-red-800">Remove</button>
    `;
    tagsList.appendChild(tagDiv);
    tagDiv.querySelector('.remove-tag')!.addEventListener('click', () => {
        if (tagsList.children.length > 1) {
            tagsList.removeChild(tagDiv);
        }
    });
}

addIngredientBtn.addEventListener('click', addIngredientField);
addInstructionBtn.addEventListener('click', addInstructionField);
addTagBtn.addEventListener('click', addTagField);

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

tagsList.querySelector('.remove-tag')!.addEventListener('click', () => {
    if (tagsList.children.length > 1) {
        tagsList.removeChild(tagsList.children[0]);
    }
});

loadRecipes();