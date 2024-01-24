// Stores the value of the value entered into the "number of lines" input box into a variable
let titleInputBoxValueLength = document.getElementById('assetTitle').size;
// Storing the "Create" button into a variable
const createButton = document.getElementById('createButton');

// NOT CURRENTLY WORKING!
createButton.addEventListener('click', () => {
    // The functions required to allow the alert(s) to appear above the form if input validation fails
    const alertPlaceholder = document.getElementById('alertPlaceholder');
    const appendAlert = (message, type) => {
        const wrapper = document.createElement('div');
        wrapper.innerHTML = [
            `<div class="alert alert-${type} alert-dismissible" role="alert">
                <div>${message}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close alert"></button>
            </div>`
        ].join('');

        alertPlaceholder.append(wrapper);
    }

    if(titleInputBoxValueLength > 10) {
        appendAlert('You cannot enter a title longer than 10 characters!', 'danger');
    }
});