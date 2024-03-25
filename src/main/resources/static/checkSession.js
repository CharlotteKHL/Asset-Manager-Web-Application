// When the page is started up, a check is carried out to confirm if the user is logged in or not
// If the user is logged in, then their username is displayed with a welcome message
// If the user is not logged in, then the Login/Register buttons are displayed instead

// Following this, if the user is not an admin then the "Manage Asset Types" button on the navigation bar does not appear 
document.addEventListener('DOMContentLoaded', function() {
    const appendAlert = (message, type, placeholder, alertId) => {
        const placeholderDiv = document.getElementById(`${placeholder}`);
        const wrapper = document.createElement('div');
        // Setting an id allows us to identify whether a specific alert already exists, thus preventing alerts from stacking
        wrapper.id = alertId;
        wrapper.classList.add("alertMsg");
        wrapper.innerHTML = [
            `<div class="alert ${type} alert-dismissible" role="alert">
                <div>${message}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>`
        ].join('');
        placeholderDiv.append(wrapper);
    }
fetch('/check', {
    method: 'POST',
}).then(response => {
    if(response.ok) {
        return response.json();
    } else {
        return response.json().then(errorMessage => {
            throw new Error(errorMessage.error);
        });
    }
}).then(async data => {
    if(data.username != "You are no longer logged in") {
        var loginRegisterButtons = document.getElementById('login-register-buttons');
        loginRegisterButtons.style.display = 'none';
        var welcomeUserMessage = document.getElementById('welcome-user-message');
        welcomeUserMessage.style.display = 'block';
        var usernameText = document.getElementById('username');
        usernameText.innerHTML = "Logged in as: " + data.username;
    } else {
     appendAlert('<i class="bi bi-exclamation-triangle"></i> ' + data.username + '.', 'alert-danger', 'successAlertPlaceholder');
         await sleep(1000);
         window.location.replace('login.html');
    }
});

fetch('/adminCheck', {
    method: 'POST'
}).then(response => {
    if(response.ok) {
        return response.json();
    } else {
        return response.json().then(errorMessage => {
            throw new Error(errorMessage.error);
        });
    }
}).then(data => {
    if(data.adminCheckResult == "ADMIN") {
        var manageAssetTypesButton = document.getElementById('manage-asset-types-button');
        var auditButton = document.getElementById('audit-log-button');
        var manageUserButton = document.getElementById('manage-users-button');

        if(manageAssetTypesButton != null){
            manageAssetTypesButton.style.display = 'inline-block';
        }
        
        if(auditButton != null){
            auditButton.style.display = 'inline-block';
        }
        
        if(manageUserButton != null){
			manageUserButton.style.display = 'inline-block';
		}
    } else {
        //check if user on page they are not allowed on
        let pathname = window.location.pathname;
        if((pathname == "/create-type.html") || (pathname == "/audit-trail.html" || (pathname == "/manage-users.html"))){
            window.location.replace("index.html");
        }
    }
});
});


// Variable to represent the sleep interval
var sleepInterval;

// Function that allows an action to be performed, then pause before performing another action
function sleep(ms) {
clearInterval(sleepInterval);
return new Promise(resolve => sleepInterval = setTimeout(resolve, ms));
}

// Function allowing users to log out
function logout() {
fetch('/logout', {
    method: 'POST'
});
}