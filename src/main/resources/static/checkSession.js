// When the page is started up, a check is carried out to confirm if the user is logged in or not
// If the user is logged in, then their username is displayed with a welcome message
// If the user is not logged in, then the Login/Register buttons are displayed instead
document.addEventListener('DOMContentLoaded', function() {
    fetch('/check', {
        method: 'POST',
    }).then(response => {
        if(response.ok) {
            return response.json();
        } else {
            return response.json().then(errorMessage => {
                throw new Error(errorMessage.error);
            })
        }
    }).then(data => {
        if(data.username != null) {
            var loginRegisterButtons = document.getElementById('login-register-buttons');
            loginRegisterButtons.style.display = 'none';
            var welcomeUserMessage = document.getElementById('welcome-user-message');
            welcomeUserMessage.style.display = 'block';
            var usernameText = document.getElementById('username');
            usernameText.innerHTML = data.username;
        }
    })
 });