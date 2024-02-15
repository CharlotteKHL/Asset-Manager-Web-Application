function checkRegister() {

    let valid = true;

    var formData = [];

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

    // validating the username 
    if((document.getElementById("exampleInputEmail1").value) == ""){
        valid = false;
        appendAlert("Please enter an email", 'danger', 'registerEmailAlertPlaceholder', 'exampleInputEmail1');
    }else{
        formData.push(document.getElementById("exampleInputEmail1").value);
    }

    // validating the password 
    if((document.getElementById("exampleInputPassword1").value) == ""){
        valid = false;
        appendAlert("Please enter a password", 'danger', 'registerPasswordAlertPlaceholder', 'exampleInputPassword1');
    }else{
        formData.push(document.getElementById("exampleInputPassword1").value);
    }

    // validating the reentered password 
    if(((document.getElementById("exampleInputPassword2").value) == "")){
        valid = false;
        appendAlert("Please re-enter your password", 'danger', 'registerRePasswordAlertPlaceholder', 'exampleInputPassword2');
        if(((document.getElementById("exampleInputPassword1").value) != (document.getElementById("exampleInputPassword2").value))){
            appendAlert("Make your passwords are the same", 'danger', 'registerRePassword2AlertPlaceholder', 'exampleInputPassword2');
        }
    }else{
        formData.push(document.getElementById("exampleInputPassword2").value);
    }

    // Portion that check if the server side validation has worked.
    if(valid){
        // formData = new formData();

        fetch('/submit', {
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
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'danger', 'successAlertPlaceholder');
        });

        window.location ="login.html";
    }
}