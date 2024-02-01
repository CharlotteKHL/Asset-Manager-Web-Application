// Storing the "Create" button into a variable
const createButton = document.getElementById('createButton');

// Boolean variables for each form attribute to check whether it has already been validated or not
let titleEmpty = false;
let titleTooLong = false;
let type = false;
let noOfLinesNegativeOrZero = false;
let noOfLinesEmpty = false;
let link = false;
let progLanguage = false;
let associations = false;
let dateEmpty = false;
let dateIncorrectFormat = false;

// Boolean variable to check whether the asset has been successfully created yet or not
let assetSuccessfullyCreated = false;

// The code regex expected for the correct date format in the date field
const dateRegex = /[0-31]{2}\/[0-12]{2}\/[0-9999]{2}/;

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
    if(document.querySelector('#assetTitle').value.length == 0 && titleEmpty == false) {
        titleEmpty = true;
        valid = false;
        appendAlert('You must enter an asset title!', 'danger', 'titleAlertPlaceholder');
    }

    // If the asset title is less than 50 characters
    if(document.querySelector('#assetTitle').value.length > 50 && titleTooLong == false) {
        titleTooLong = true;
        valid = false;
        appendAlert('You cannot enter a title longer than 50 characters!', 'danger', 'titleAlertPlaceholder');
    }

    // If the asset type is empty
    if(document.querySelector('#assetType').value.length == 0 && type == false) {
        type = true;
        valid = false;
        appendAlert('You must enter an asset type!', 'danger', 'typeAlertPlaceholder');
    }

    // If the asset number of lines is empty
    if(document.querySelector('#assetNoOfLines').value === "" & noOfLinesEmpty == false) {
        noOfLinesEmpty = true;
        appendAlert('You must enter a number of lines!', 'danger', 'noOfLinesAlertPlaceholder');
    }

    // If the asset number of lines is less than or equal to zero
    if(parseInt(document.querySelector('#assetNoOfLines').value) <= 0 & noOfLinesNegativeOrZero == false) {
        noOfLinesNegativeOrZero = true
        valid = false;
        appendAlert('You must enter a positive number of lines!', 'danger', 'noOfLinesAlertPlaceholder');
    }

    // If the asset link is empty
    if(document.querySelector('#assetLink').value.length == 0 && link == false) {
        link = true;
        valid = false;
        appendAlert('You must enter an asset link!', 'danger', 'linkAlertPlaceholder');
    }

    // If the asset programming language is empty
    if(document.querySelector('#assetLanguage').value.length == 0 && progLanguage == false) {
        progLanguage = true;
        valid = false;
        appendAlert('You must enter an asset programming language!', 'danger', 'progLanguageAlertPlaceholder');
    }

    // If the asset association(s) is empty
    if(document.querySelector('#assetAssociations').value.length == 0 && associations == false) {
        associations = true;
        valid = false;
        appendAlert('You must enter asset association(s)!', 'danger', 'associationsAlertPlaceholder');
    }

    // If the asset date is empty
    if(document.querySelector('#assetDate').value.length == 0 && dateEmpty == false) {
        dateEmpty = true;
        valid = false;
        appendAlert('You must enter an asset creation date!', 'danger', 'dateAlertPlaceholder');
    }

    // If the asset date is entered in the incorrect format
    if(!dateRegex.test(document.querySelector('#assetDate').value) && dateIncorrectFormat == false) {
        dateIncorrectFormat = true;
        valid = false;
        appendAlert('You must enter the date in the format mm/dd/yy!', 'danger', 'dateAlertPlaceholder');
    }

    // If the asset form is filled in correctly then asset creation notification is displayed
    if(titleEmpty == false && titleTooLong == false && type == false && noOfLinesEmpty == false && noOfLinesNegativeOrZero == false & link == false && progLanguage == false && associations == false && dateEmpty == false && dateIncorrectFormat == false && assetSuccessfullyCreated == false) {
        assetSuccessfullyCreated = true;
        appendAlert('<i class="bi bi-check-circle-fill"></i>  Asset created successfully!', 'success', 'successAlertPlaceholder');
    }
});