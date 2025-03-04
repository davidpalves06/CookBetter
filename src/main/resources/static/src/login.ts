export {};

const loginForm = document.getElementById('loginForm') as HTMLFormElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;
const loginBtn = document.getElementById('loginBtn') as HTMLButtonElement;
const errorMessageDiv = document.getElementById('errorMessage') as HTMLDivElement;
const loginBtnText = document.getElementById('loginBtnText') as HTMLSpanElement;
const loadingSpinner = document.getElementById("loadingSpinner") as HTMLElement;


interface LoginFormData {
  email : string,
  password : string
}

loginForm.addEventListener('submit', async (event: Event) => {
  event.preventDefault();


  const email: string = emailInput.value.trim();
  const password: string = passwordInput.value.trim();
  const loginFormData : LoginFormData = {
    email,
    password
  }

  if (!email || !password) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on login form. Please fill all the fields'
    return;
  }

  if (!isValidEmail(email)) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on login form. Enter a valid email'
    return;
  }

  if (password.length < 8 || password.length > 32) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on login form. Invalid password'
    return;
  }

  if (!hasUppercaseAndNumber(password)) {
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerText = 'Error on login form. Invalid password'
    return;
  }

  loginBtnText.classList.add('hidden')
  loadingSpinner.classList.remove('hidden')
  loginBtn.disabled = true

  const response = await fetch("/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(loginFormData),
  })
  if (response.ok) {
    window.location.href = "/";
    errorMessageDiv.style.display = 'none'
  } else {
    let errorMessage : string
    if (response.status != 500) {
      errorMessage = (await response.text())
    } else {
      errorMessage = "Registration failed. Try again later"
    }
    errorMessageDiv.style.display = 'block'
    errorMessageDiv.innerHTML = errorMessage
    loginBtnText.classList.remove('hidden')
    loadingSpinner.classList.add('hidden')
    loginBtn.disabled = false
    return
  }
});

function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

function hasUppercaseAndNumber(input: string): boolean {
  return /(?=.*[A-Z])(?=.*[0-9])/.test(input);
}