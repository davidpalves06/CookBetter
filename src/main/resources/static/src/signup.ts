import { hasUppercaseAndNumber, isLogged, isValidEmail } from "./auth.js";

export { };

const signupForm = document.getElementById('signupForm') as HTMLFormElement;
const usernameInput = document.getElementById('username') as HTMLInputElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const nameInput = document.getElementById('name') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;
const confirmPasswordInput = document.getElementById('confirmPassword') as HTMLInputElement;
const errorMessageDiv = document.getElementById('errorMessage') as HTMLDivElement;
const signupBtn = document.getElementById('signupBtn') as HTMLButtonElement;
const signupBtnText = document.getElementById('signupBtnText') as HTMLSpanElement;
const loadingSpinner = document.getElementById("loadingSpinner") as HTMLElement;
const currentDomain = window.location.hostname;
const passwordToggleButton = document.getElementById("togglePassword") as HTMLButtonElement;

passwordToggleButton.addEventListener('click', (event : Event) => {
  event.preventDefault();
  
  passwordToggleButton.blur();
  const eyeIcon = document.getElementById("eyeIcon") as HTMLElement;
  const eyeSlashIcon = document.getElementById("eyeSlashIcon") as HTMLElement;

  if (passwordInput.type === 'password') {
    passwordInput.type = 'text';
    eyeIcon.classList.add('hidden');
    eyeSlashIcon.classList.remove('hidden');
  } else {
    passwordInput.type = 'password';
    eyeIcon.classList.remove('hidden');
    eyeSlashIcon.classList.add('hidden');
  }
});

interface SignupFormData {
  name: string;
  email: string;
  username: string;
  password: string;
}

async function handleAuthenticationState() {
  let loggedIn: boolean = await isLogged();
  if (loggedIn) {
    if (document.referrer) {
      const referrerDomain = new URL(document.referrer).hostname;
      if (referrerDomain === currentDomain && window.history.length > 1) {
        window.history.back();
      }
    } else {
      window.location.href = "/";
      errorMessageDiv.style.display = 'none'
    }
  }
}

handleAuthenticationState();

signupForm.addEventListener('submit', async (event: Event) => {
  event.preventDefault();


  const username: string = usernameInput.value.trim();
  const email: string = emailInput.value.trim();
  const password: string = passwordInput.value.trim();
  const confirmPassword: string = confirmPasswordInput.value.trim();
  const name: string = nameInput.value.trim();


  if (!username || !email || !password || !name || !confirmPassword) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on registration form. Please fill all the fields'
    return;
  }

  if (username.length < 6 || username.length > 16) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on registration form. Username should be between 6 and 16 characters'
    return;
  }

  if (!isValidEmail(email)) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on registration form. Email is not valid'
    return;
  }

  if (password.length < 8 || password.length > 32) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on registration form. Password should be between 8 and 32 characters'
    return;
  }

  if (!hasUppercaseAndNumber(password)) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on registration form. Password should contain at least one uppercase and one number'
    return;
  }

  if (password != confirmPassword) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on registration form. Passwords do not match'
    return;
  }


  if (name.split(" ").length < 2) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on registration form. Name should contain at least first and last name'
    return;
  }

  let formData: SignupFormData = {
    username, email, password, name
  }

  signupBtnText.classList.add('hidden')
  loadingSpinner.classList.remove('hidden')

  signupBtn.disabled = true

  const response = await fetch("/auth/signup", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(formData),
  })
  if (response.ok) {
    window.location.href = "/";
    errorMessageDiv.style.display = 'none'
  } else {
    let errorMessage: string
    if (response.status != 500) {
      errorMessage = (await response.text())
    } else {
      errorMessage = "Registration failed. Try again later"
    }
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerHTML = errorMessage
    signupBtnText.classList.remove('hidden')
    loadingSpinner.classList.add('hidden')
    signupBtn.disabled = false
    return
  }
});