export { };

const profileTitle = document.getElementById('profileTitle') as HTMLHeadingElement;
const url = new URL(window.location.href);

const pathParts = url.pathname.split('/').filter(part => part !== '');
const username = pathParts[0] || null;

if (username) {
  console.log('Username from URL:', username);
  profileTitle.textContent = username + " Profile";
} else {
  console.log('No username found in URL');
}
