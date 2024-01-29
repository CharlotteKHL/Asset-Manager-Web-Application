// Storing the "Create" button into a variable
const createButton = document.getElementById('createButton');

createButton.addEventListener('click', () => {
    console.log(parseInt(document.querySelector('#assetNoOfLines').value));
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

    // Performs input validation to make sure that the asset title is less than 15 characters
    if(document.querySelector('#assetTitle').value.length > 15 && parseInt(document.querySelector('#assetNoOfLines').value) > 0) {
        appendAlert('You cannot enter a title longer than 15 characters!', 'danger');
    } else if(document.querySelector('#assetTitle').value.length < 15 && parseInt(document.querySelector('#assetNoOfLines').value) < 0) {
        appendAlert('You cannot enter a negative number of lines!', 'danger');
    } else if(document.querySelector('#assetTitle').value.length > 15 && parseInt(document.querySelector('#assetNoOfLines').value) < 0) {
        appendAlert('You cannot enter a title longer than 15 characters and you cannot enter a negative number of lines!', 'danger');
    }
});