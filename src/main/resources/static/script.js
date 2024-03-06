// Appends an alert to a (placeholder) div given an id specified by the placeholder parameter
// Aside from the placeholder, this function requires that the message, Bootstrap class type, and desired id for the alert is given
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

// All appended alerts are assigned the class alertMsg, hence we remove each element with the alertMsg class from the page.
function resetAlerts() {
    const alerts = Array.from(document.getElementsByClassName('alertMsg'));
    alerts.forEach(alert => {
        alert.remove();
    });
}

// Obtains the entries from the type management form, sends POST request to create a new row in the database table "type"
function createType() {
    // Resets the page, removing all previously given alerts
    resetAlerts();
    // Boolean used to keep track of whether an invalid entry has been detected
    let isValid = true;
    // Defining a JavaScript object to populate with pairs in the form attribute name to attribute datatype
    let pairs = {};
    // Checks whether a name has been provided for the new asset type
    if (document.getElementById("customType").value == '') {
        if (document.getElementById("noNameAlert") == null) {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Please enter a name for the new asset type.', 'alert-danger', 'errorAlertPlaceholder', 'noNameAlert');
        }
        isValid = false;
    }
    // Creates the initial pairing - this is for the name of the new asset type
    pairs["type"] = document.getElementById("customType").value;
    const spans = document.querySelectorAll('#attributesContainer span');
    // Iterates over each pair of form fields, obtaining the attribute name and the attribute datatype
    spans.forEach(span => {
        const inputVal = span.querySelector('input').value.trim();
        const selectVal = span.querySelector('select').value.trim();
        // Checks whether the name for an attribute has been left blank or is greater than 50 characters long
        if (!inputVal || inputVal.length > 50) {
            if (document.getElementById("invalidLengthAlert") == null) {
                appendAlert('<i class="bi bi-exclamation-triangle"></i> Please check the length of each attribute name.', 'alert-danger', 'errorAlertPlaceholder', 'invalidLengthAlert');
            }
            isValid = false;
            return;
        }
        // Creates the subsequent pairings
        pairs[inputVal] = selectVal;
    });
    // All entries detected to be valid - convert the JavaScript object to a JSON string and send as the body of the POST request
    if (isValid) {
        pairs = JSON.stringify(pairs);
        fetch('/createType', {
            method: 'POST',
            body: pairs,
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorMessage => {
                    throw new Error(errorMessage.error);
                });
            }
        })
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'alert-success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'alert-danger', 'successAlertPlaceholder');
        });
    }
}

function deleteType() {
    resetAlerts();
    selectedType = document.getElementById("type").value;
    
    fetch('/deleteType', {
            method: 'POST',
            body: selectedType,
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorMessage => {
                    throw new Error(errorMessage.error);
                });
            }
        })
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'alert-success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'alert-danger', 'successAlertPlaceholder');
        });
}

// Obtains the entries from the type management form, sends POST request to update an existing row in the database table "type"
function updateType() {
    // Resets the page, removing all previously given alerts
    resetAlerts();
    let isValid = true;
    let pairs = {};
    pairs["type"] = document.getElementById("type").value;
    const spans = document.querySelectorAll('#attributesContainer span');
    spans.forEach(span => {
        const inputVal = span.querySelector('input').value.trim();
        const selectVal = span.querySelector('select').value.trim();
        if (!inputVal || inputVal.length > 50) {
            if (document.getElementById("invalidLengthAlert") == null) {
                appendAlert('<i class="bi bi-exclamation-triangle"></i> Please check the length of each attribute name.', 'alert-danger', 'errorAlertPlaceholder', 'invalidLengthAlert');
            }
            isValid = false;
            return;
        }
        pairs[inputVal] = selectVal;
    });
    if (isValid) {
        pairs = JSON.stringify(pairs);
        fetch('/updateType', {
            method: 'POST',
            body: pairs,
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorMessage => {
                    throw new Error(errorMessage.error);
                });
            }
        })
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'alert-success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'alert-danger', 'successAlertPlaceholder');
        });
    }
}

function renameType() {
    // Resets the page, removing all previously given alerts
    resetAlerts();
    // Boolean used to keep track of whether an invalid entry has been detected
    let isValid = true;
    // Defining a JavaScript object to populate with pairs in the form attribute name to attribute datatype
    let pairs = {};
    // Checks whether a name has been provided for the new asset type
    if (document.getElementById("customType").value == '') {
        if (document.getElementById("noNameAlert") == null) {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Please enter a new name for the asset type.', 'alert-danger', 'errorAlertPlaceholder', 'noNameAlert');
        }
        isValid = false;
    }
    // Creates the initial pairing - this is for the name of the new asset type
    pairs["customType"] = document.getElementById("customType").value;
    // Add the type value to the pairs object
    pairs["overarchingType"] = document.getElementById("type").value;
    const spans = document.querySelectorAll('#attributesContainer span');
    // Iterates over each pair of form fields, obtaining the attribute name and the attribute datatype
    spans.forEach(span => {
        const inputVal = span.querySelector('input').value.trim();
        const selectVal = span.querySelector('select').value.trim();
        // Checks whether the name for an attribute has been left blank or is greater than 50 characters long
        if (!inputVal || inputVal.length > 50) {
            if (document.getElementById("invalidLengthAlert") == null) {
                appendAlert('<i class="bi bi-exclamation-triangle"></i> Please check the length of each attribute name.', 'alert-danger', 'errorAlertPlaceholder', 'invalidLengthAlert');
            }
            isValid = false;
            return;
        }
        // Creates the subsequent pairings
        pairs[inputVal] = selectVal;
    });
    // All entries detected to be valid - convert the JavaScript object to a JSON string and send as the body of the POST request
    if (isValid) {
        pairs = JSON.stringify(pairs);
        fetch('/renameType', {
            method: 'POST',
            body: pairs,
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorMessage => {
                    throw new Error(errorMessage.error);
                });
            }
        })
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'alert-success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'alert-danger', 'successAlertPlaceholder');
        });
    }
}


// Obtains the entries from the asset creation form, sends POST request to create a new row in the database table "assets"
function validateEntries() {
    // Resets the page, removing all previously given alerts
    resetAlerts();
    let isValid = true;
    let obj = {};
    // Creates a list of the attribute names in order to construct the key-value pairs (JSON) later on
    const labels = document.querySelectorAll('label');
    const labelArray = [];
    labels.forEach(label => {
        labelArray.push(label.textContent.trim());
    });
    const assetForm = document.getElementById("assetForm");
    // Creates a list of all the user's entries into the asset creation form
    let entries = [...assetForm.elements]
    .filter(element => element.tagName === 'INPUT' || element.tagName === 'SELECT')
    .map(input => {
        // Whilst constructing the list, check whether any entries for list attributes do not conform to the pattern attribute (regular expression) 
        // of their corresponding input field
        if (input.classList.contains("listAttr")) {
            if (!input.checkValidity()) {
                if (document.getElementById("invalidListAlert") == null) {
                    appendAlert('<i class="bi bi-exclamation-triangle"></i> Please ensure list items are of the correct type and separated by a comma.', 'alert-danger', 'errorAlertPlaceholder', 'invalidListAlert');
                }
                isValid = false;
                return;
            }
        }
        // Handles associations field, where all selected options need to be collected
        if (input.tagName === 'SELECT' && input.multiple) {
            return [...input.options]
                .filter(option => option.selected)
                .map(option => option.value);
        } else {
            return input.value;
        }
    });
    // Iterates over the user's entries, checks whether they have been left blank / are over 50 characters long
    for (let i = 0; i < entries.length; i++) {
        if(entries[i] == '' || entries[i].length > 50) {
            if (document.getElementById("invalidLengthAlert") == null) {
                appendAlert('<i class="bi bi-exclamation-triangle"></i> Please ensure no fields are left blank / are over 50 characters long.', 'alert-danger', 'errorAlertPlaceholder', 'invalidLengthAlert');
            }
            isValid = false;
            return;
        } else {
            // Handles associations field, as trim() is not possible on an array
            if (Array.isArray(entries[i])) {
                obj[labelArray[i]] = entries[i];
            } else {
                obj[labelArray[i]] = entries[i].trim();
            }
        }
    }
    if (isValid) {
        obj = JSON.stringify(obj);
        fetch('/submitAsset', {
            method: 'POST',
            body: obj,
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorMessage => {
                    throw new Error(errorMessage.error);
                });
            }
        })
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'alert-success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'alert-danger', 'successAlertPlaceholder');
        });
    }
}

function assetSelectError() {
    // Resets the page, removing all previously given alerts
    resetAlerts();

    // Create a new alert for the asset selection error
    const alertPlaceholder = document.getElementById('errorAlertPlaceholder');
    const alertDiv = document.createElement('div');
    alertDiv.classList.add('alert', 'alert-danger');
    alertDiv.innerHTML = '<i class="bi bi-exclamation-triangle"></i> Please select an asset.';
    alertPlaceholder.appendChild(alertDiv);
}

// Obtains the entries from the asset creation form, sends POST request to create a new row in the database table "assets"
function updateAsset(id) {
    // Resets the page, removing all previously given alerts
    resetAlerts();
    let isValid = true;
    let obj = {};
    // Creates a list of the attribute names in order to construct the key-value pairs (JSON) later on
    const labels = document.querySelectorAll('label');
    const labelArray = [];
    labels.forEach(label => {
        labelArray.push(label.textContent.trim());
    });
    const assetForm = document.getElementById("assetForm");
    // Creates a list of all the user's entries into the asset creation form
    let entries = [...assetForm.elements]
    .filter(element => element.tagName === 'INPUT' || element.tagName === 'SELECT')
    .map(input => {
        // Whilst constructing the list, check whether any entries for list attributes do not conform to the pattern attribute (regular expression) 
        // of their corresponding input field
        if (input.classList.contains("listAttr")) {
            if (!input.checkValidity()) {
                if (document.getElementById("invalidListAlert") == null) {
                    appendAlert('<i class="bi bi-exclamation-triangle"></i> Please ensure list items are of the correct type and separated by a comma.', 'alert-danger', 'errorAlertPlaceholder', 'invalidListAlert');
                }
                isValid = false;
                return;
            }
        }
        // Handles associations field, where all selected options need to be collected
        if (input.tagName === 'SELECT' && input.multiple) {
            return [...input.options]
                .filter(option => option.selected)
                .map(option => option.value);
        } else {
            return input.value;
        }
    });
    // Iterates over the user's entries, checks whether they have been left blank / are over 50 characters long
    for (let i = 0; i < entries.length; i++) {
		if(!document.getElementById('changeAssetNameCheckbox').checked && labelArray[i] === 'Re-name asset' || !document.getElementById('changeAssetNameCheckbox').checked && labelArray[i] === 'Change asset name?') {
			continue;
		}
        if(entries[i] == '' || entries[i].length > 50) {
            if (document.getElementById("invalidLengthAlert") == null) {
                appendAlert('<i class="bi bi-exclamation-triangle"></i> Please ensure no fields are left blank / are over 50 characters long.', 'alert-danger', 'errorAlertPlaceholder', 'invalidLengthAlert');
            }
            isValid = false;
            return;
        } else {
            // Handles associations field, as trim() is not possible on an array
            if (Array.isArray(entries[i])) {
                obj[labelArray[i]] = entries[i];
            } else {
                obj[labelArray[i]] = entries[i].trim();
            }
        }
    }
    if (isValid) {
        obj = JSON.stringify(obj);
        fetch(`/updateAsset/${id}`, {
            method: 'POST',
            body: obj,
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorMessage => {
                    throw new Error(errorMessage.error);
                });
            }
        })
        .then(data => {
            appendAlert('<i class="bi bi-check-circle-fill"></i> ' + data.message, 'alert-success', 'successAlertPlaceholder');
        })
        .catch(error => {
            appendAlert('<i class="bi bi-exclamation-triangle"></i> Error: ' + error.message, 'alert-danger', 'successAlertPlaceholder');
        });
    }
}
