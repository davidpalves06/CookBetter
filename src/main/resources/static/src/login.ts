export {};
// Define form and input elements with type assertions
const loginForm = document.getElementById('loginForm') as HTMLFormElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;

// Form submission handler
loginForm.addEventListener('submit', (event: Event) => {
  event.preventDefault(); // Prevent default form submission

  const email: string = emailInput.value.trim();
  const password: string = passwordInput.value.trim();

  // Basic validation
  if (!email || !password) {
    alert('Please fill in both email and password.');
    return;
  }

  if (!isValidEmail(email)) {
    alert('Please enter a valid email address.');
    return;
  }

  // Simulate login (replace with actual API call later)
  console.log('Login attempt:', { email, password });
  alert('Login successful! (This is a demo)');
  loginForm.reset(); // Clear the form
});

// Simple email validation function
function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}