function checkRegister() {
	
    let valid = true;

    var formData = new FormData();

    // The functions required to have the placeholder allow the alert(s) to appear above the form if input validation fails
    const appendAlert = (message, type, placeholder, alertId) => {
        const placeholderElement = document.getElementById(`${placeholder}`);
        const wrapper = document.createElement('div');
        wrapper.id = alertId;
        wrapper.classList.add("alertMsg");
        wrapper.innerHTML = [
            `<div class="alert alert-${type} alert-dismissible" role="alert">
                <div>${message}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close alert"></button>
            </div>`
        ].join('');

        placeholderElement.append(wrapper);
    }
    
    let email = document.getElementById("email").value;
    
    // validating the username 
    if(email == ""){
		
        valid = false;
        appendAlert("Please enter your email", 'danger', 'registerEmailAlertPlaceholder', 'email');
        
    }else if(email.search("@") == -1){
		
        valid = false;
        appendAlert("Please enter a valid email, must include an '@'", 'danger', 'registerEmailAlertPlaceholder', 'email');
    
    }else{
        formData.append("username", document.getElementById("email").value);
    }

    // validating the password 
    const password = document.getElementById("password").value;
    const password2 = document.getElementById("password2").value;
    if(password == ""){
        
        valid = false;
        appendAlert("Please enter your password", 'danger', 'registerPasswordAlertPlaceholder', 'password');
        
    } else if(password != password2){
		
		valid = false;
		appendAlert("Your password does not match your re-entry", 'danger', 'registerPasswordAlertPlaceholder', 'password');
	
	} else {
        formData.append("password", password);
    }

    // Sends fetch to backend with user input to check if username unique and to input into database
    if(valid){
        
        fetch('/register', {
            method: 'POST',
            body: formData,
        }).then(response => {
            if(response.ok){
                return response.json();
            } else{
                return response.json().then(errorMessage =>{
                    throw new Error(errorMessage.error);
                })
            }
        })
        .then(async data => {
			if(data.message == "Registration successful"){
				appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message + ", redirecting to login", 'success', 'successAlertPlaceholder');
				await sleep(2000);
				window.location.replace('login.html');
			} else {
				appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'danger', 'successAlertPlaceholder');
			}
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'danger', 'successAlertPlaceholder');
        });
    }
}

// Variable to represent the sleep interval
var sleepInterval;

// Function that allows an action to be performed, then pause before performing another action
function sleep(ms) {
    clearInterval(sleepInterval);
    return new Promise(resolve => sleepInterval = setTimeout(resolve, ms));
}