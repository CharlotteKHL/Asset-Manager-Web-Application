// Storing the "Create" button into a variable
const createButton = document.getElementById('createButton');

createButton.addEventListener('click', () => {
    let valid = true;
    // The functions required to allow the alert(s) to appear above the form if input validation fails
    const appendAlert = (message, type, placeholder) => {
        const placeholderElement = document.getElementById(`${placeholder}`);
        const wrapper = document.createElement('div');
        wrapper.innerHTML = [
            `<div class="alert alert-${type} alert-dismissible" role="alert">
                <div>${message}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close alert"></button>
            </div>`
        ].join('');

        placeholderElement.append(wrapper);
    }

    // If the asset title is empty
    if(document.querySelector('#assetTitle').value.length == 0) {
        valid = false;
        appendAlert('You must enter an asset title!', 'danger', 'titleAlertPlaceholder');
    }

    // If the asset title is less than 50 characters
    if(document.querySelector('#assetTitle').value.length > 50) {
        valid = false;
        appendAlert('You cannot enter a title longer than 50 characters!', 'danger', 'titleAlertPlaceholder');
    }

    // If the asset type is empty
    if(document.querySelector('#assetType').value.length == 0) {
        valid = false;
        appendAlert('You must enter an asset type!', 'danger', 'typeAlertPlaceholder');
    }

    // If the asset number of lines is less than or equal to zero
    if(parseInt(document.querySelector('#assetNoOfLines').value) <= 0 || document.querySelector('#assetNoOfLines').value === "") {
        valid = false;
        appendAlert('You must enter a positive number of lines!', 'danger', 'noOfLinesAlertPlaceholder');
    }

    // If the asset link is empty
    if(document.querySelector('#assetLink').value.length == 0) {
        valid = false;
        appendAlert('You must enter an asset link!', 'danger', 'linkAlertPlaceholder');
    }

    // If the asset programming language is empty
    if(document.querySelector('#assetLanguage').value.length == 0) {
        valid = false;
        appendAlert('You must enter an asset programming language!', 'danger', 'progLanguageAlertPlaceholder');
    }

    // If the asset association(s) is empty
    if(document.querySelector('#assetAssociations').value.length == 0) {
        valid = false;
        appendAlert('You must enter asset association(s)!', 'danger', 'associationsAlertPlaceholder');
    }

    // If the asset link is empty
    if(document.querySelector('#assetDate').value.length == 0) {
        valid = false;
        appendAlert('You must enter an asset creation date!', 'danger', 'dateAlertPlaceholder');
    }

    // If the asset form is filled in correctly then asset creation notification is displayed
    if(valid) {
        appendAlert('<i class="bi bi-check-circle-fill"></i>  Asset created successfully!', 'success', 'successAlertPlaceholder');
    }
});