// Client-side asset form validation - this is called upon form submission.
function validateEntries() {

    const assetForm = document.getElementById("assetForm");
    // Regular expression to ensure date entered is valid i.e. dd/MM/yyyy.
    const dateRegex = /^(0[1-9]|[12][0-9]|3[01])\/(0[1-9]|1[0-2])\/\d{4}$/;
    // Creates an array containing all entries entered / selected by the user.
    let entries = [...assetForm.elements]
        .filter(element => element.tagName === 'INPUT' || element.tagName === 'SELECT')
        .map(input => input.value);

    // The code below "resets" any previously displayed alerts.
    const alerts = Array.from(document.getElementsByClassName('alertMsg'));
    alerts.forEach(alert => {
        alert.remove();
    });

    let alertTriggered = false;

    /* The block of code below is used to generate and display the necessary (Bootstrap) alert.
    Each alert has its own specific id.
    All alerts share the same class: "alertMsg". */
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

    // Checks whether the asset title is empty.
    if(entries[0].length == 0 && document.getElementById('titleAlert') === null) {
        alertTriggered = true;
        appendAlert('You must enter an asset title!', 'danger', 'titleAlertPlaceholder', 'titleAlert');
    }

    // Checks whether the asset title is over 50 characters long.
    if(entries[0].length > 50 && document.getElementById('titleLengthAlert') === null) {
        alertTriggered = true;
        appendAlert('You cannot enter a title longer than 50 characters!', 'danger', 'titleAlertPlaceholder', 'titleLengthAlert');
    }

    // Checks whether the asset type has been left empty.
    if(entries[1].length == 0 && document.getElementById('typeAlert') === null) {
        alertTriggered = true;
        appendAlert('You must enter an asset type!', 'danger', 'typeAlertPlaceholder', 'typeAlert');
    }

    // Checks whether the number of lines has been left empty / is less than or equal to 0.
    if((entries[2] == "" || entries[2] <= 0) && document.getElementById('noOfLinesAlert') === null) {
        alertTriggered = true;
        appendAlert('You must enter a positive number of lines!', 'danger', 'noOfLinesAlertPlaceholder', 'noOfLinesAlert');
    }

    // Checks whether the asset link is empty.
    if(entries[3].length == 0 && document.getElementById('linkAlert') === null) {
        alertTriggered = true;
        appendAlert('You must enter an asset link!', 'danger', 'linkAlertPlaceholder', 'linkAlert');
    }

    // Checks whether the programming language field is empty.
    if(entries[4].length == 0 && document.getElementById('progLanguageAlert') === null) {
        alertTriggered = true;
        appendAlert('You must enter an asset programming language!', 'danger', 'progLanguageAlertPlaceholder', 'progLanguageAlert');
    }

    // Checks whether the associations field is empty.
    if(entries[5].length == 0 && document.getElementById('associationsAlert') === null) {
        alertTriggered = true;
        appendAlert('You must enter asset association(s)!', 'danger', 'associationsAlertPlaceholder', 'associationsAlert');
    }

    // Checks whether date field has been left empty or does not conform to the date regular expression.
    if((entries[6].length == 0 || !dateRegex.test(entries[6])) && document.getElementById('dateAlert') === null) {
        alertTriggered = true;
        appendAlert('You must enter an asset creation date in the dd/mm/yyyy format!', 'danger', 'dateAlertPlaceholder', 'dateAlert');
    }

    /* If no alerts were triggered, we can proceed to submit a POST request.
    If an error (from the server / backend) is caught, this is appropriately displayed.
    Otherwise a successful message is displayed to the user. */
    if(!alertTriggered) {

        var formData = new FormData();

        formData.append("title", entries[0]);
        // N.B. Type is skipped as this will be implemented later...
        formData.append("lines", entries[2]);
        formData.append("link", entries[3]);
        formData.append("lang", entries[4]);
        formData.append("assoc", entries[5]);
        formData.append("date", entries[6]);

        fetch('/submit', {
            method: 'POST',
            body: formData,
        })
        .then(response => response.json())
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i>  Asset created successfully!', 'success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i>  Error: ' + error.message, 'danger', 'successAlertPlaceholder');
        });
    }

}