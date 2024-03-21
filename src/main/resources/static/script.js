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

// All appended alerts are assigned the class alertMsg, hence we remove each element with the alertMsg class from the page
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
            }
        }
        // Checks whether the value of an input relating to a version number conforms to either the x.y or x.y.z format
        if (input.classList.contains("versionNumber")) {
            let pattern = /^(\d+)\.(\d+)(?:\.(\d+))?$/;
            let regex = new RegExp(pattern);
            let testValidity = regex.test(input.value);
            if (!testValidity) {
                if (document.getElementById("invalidVersionNumberAlert") == null) {
                    appendAlert('<i class="bi bi-exclamation-triangle"></i> Please enter a valid version number.', 'alert-danger', 'errorAlertPlaceholder', 'invalidVersionNumberAlert');
                }
                isValid = false;
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
    } else {
        return;
    }
}

// Function called upon the input changing in the keyword search field
function fetchAssets() {
    // Resets any alerts e.g. alerts given due to the lack of a start / end date
    resetAlerts();

    // Retrieves the keyword entered into the search field
    let assetToQuery = document.getElementById("searchField").value;

    // If the keyword search field is blank, do not display any results
    if (assetToQuery.trim() == '') {
        document.getElementById('resultsGrid').innerHTML = '';
        return;
    }

    // Obtain the selected choice from each filter
    let chosenType = document.getElementById("typeFilter").value;
    let dateFrom = document.getElementById("from").value;
    let dateTo = document.getElementById("to").value;
    let sortBy = document.getElementById("sortFilter").value;

    // Checks whether either the "From" date or "To" date has been left blank, providing an appropriate alert if so
    if ((dateFrom == '' && dateTo != '') && document.getElementById('incompleteDateAlert') == null) {
        appendAlert('<i class="bi bi-exclamation-triangle"></i> Please enter the starting date.', 'alert-danger', 'errorAlertPlaceholder', 'incompleteDateAlert');
    } else if ((dateFrom != '' && dateTo == '') && document.getElementById('incompleteDateAlert') == null) {
        appendAlert('<i class="bi bi-exclamation-triangle"></i> Please enter the ending date.', 'alert-danger', 'errorAlertPlaceholder', 'incompleteDateAlert');
    }

    // Define an array which starts with the entered keyword and is followed by the selected choices from each option within the filter section
    let searchData = [assetToQuery, chosenType, dateFrom, dateTo, sortBy];

    // Send as body of POST request
    fetch('/searchAssetQuery', {
        method: 'POST',
        body: JSON.stringify(searchData),
    })
    .then(response => response.json())
    .then(matchingAssets => {
        // If the list of matching assets is empty, inform the user that there are no matching assets
        if (matchingAssets.length == 0) {
            // Resets any previously given results
            document.getElementById('resultsGrid').innerHTML = '';
            let invisBtn = document.createElement('button');
            invisBtn.style.visibility = 'hidden';
            document.getElementById('resultsGrid').appendChild(invisBtn);
            let msg = document.createElement('span');
            msg.style.width = 'max-content';
            msg.innerText = "No matching assets.";
            document.getElementById('resultsGrid').appendChild(msg);
            return;
        }
        // Otherwise, display each matching asset as a button in the grid of results
        // N.B. Line below resets any previously given results
        document.getElementById('resultsGrid').innerHTML = '';
        matchingAssets.forEach(title => {
            const button = document.createElement('button');
            button.textContent = title;
            button.classList.add('btn', 'btn-primary');
            button.setAttribute('type', 'button');
            button.setAttribute('data-bs-toggle', 'offcanvas');
            button.setAttribute('data-bs-target', '#offcanvasRight');
            // Clicking on a button should replace the placeholders in the off canvas window with the actual asset data
            // N.B. The argument of the onclick function is dynamically assigned. Specifically, the name of the asset (as a string) is passed
            button.setAttribute('onclick', 'replacePlaceholder("' + button.textContent + '")');
            document.getElementById('resultsGrid').appendChild(button);
        });
    })
    .catch(error => {
        console.error('Error fetching assets:', error);
    });
    
}

// This function is called when the user clicks the "Reset" button on the Search for Assets page
function resetFilters() {
    document.getElementById("typeFilter").selectedIndex = -1;
    document.getElementById("from").value = null;
    document.getElementById("to").value = null;
    document.getElementById("sortFilter").selectedIndex = 0;
    fetchAssets();
}


function replacePlaceholder(assetName) {
    // POST request - linked to corresponding backend Java method getAssetData
    // Name of the asset is passed as the body
    fetch('/getAssetData', {
        method: 'POST',
        body: assetName,
    })
    .then(response => response.json())
    .then(assetData => {
        // Receives a JSON object representing the rest of the asset's data, including its name
        // Sets the text within the placeholder given by the id assetName
        document.getElementById('assetName').innerText = assetData.title;
        
        const additionalAttributes = JSON.parse(assetData.additional_attrs);

        // Construct a string used to set the HTML for each of the asset's attributes
        let attributesString = '';
        for (const [key, value] of Object.entries(additionalAttributes)) {
            attributesString += '<strong>' + key + '</strong>: ' + value + '<br>';
        }
        
        // Sets the HTML within the placeholder given by the id assetInfo
        // Displays the rest of the data besides the asset's name
        document.getElementById('assetInfo').innerHTML = "<strong>Type</strong>: " + assetData.type + "<br><br>" 
        + "<strong><u>Attributes</u></strong><br><br>" + attributesString + "<br>" 
        + "<strong>Last Updated</strong>: " + assetData.last_updated;
    })
    .catch(error => {
        console.error('Error fetching asset data:', error);
    });
}
function assetSelectError() {
    // Resets the page, removing all previously given alerts
    resetAlerts();
    // Create a new alert for the asset selection error
    if (document.getElementById("invalidAssetChoiceAlert") == null) {
        appendAlert('<i class="bi bi-exclamation-triangle"></i> Please select an asset.', 'alert-danger', 'errorAlertPlaceholder', 'invalidAssetChoiceAlert');
    }
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
        // Checks whether the value of an input relating to a version number conforms to either the x.y or x.y.z format
        if (input.classList.contains("versionNumber")) {
            let pattern = /^(\d+)\.(\d+)(?:\.(\d+))?$/;
            let regex = new RegExp(pattern);
            let testValidity = regex.test(input.value);
            if (!testValidity) {
                if (document.getElementById("invalidVersionNumberAlert") == null) {
                    appendAlert('<i class="bi bi-exclamation-triangle"></i> Please enter a valid version number.', 'alert-danger', 'errorAlertPlaceholder', 'invalidVersionNumberAlert');
                }
                isValid = false;
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
		if(!document.getElementById('changeAssetNameCheckbox').checked && labelArray[i] === 'Rename Asset' || !document.getElementById('changeAssetNameCheckbox').checked && labelArray[i] === 'Change asset name?' || labelArray[i] === 'Delete selected asset?') {
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

function deleteAsset(id) {
    resetAlerts();
    
    fetch(`/deleteAsset/${id}`, {
            method: 'POST',
            body: {},
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

function updateUser(userId, roleChoice) {
    resetAlerts();
	console.log(userId);
	console.log(roleChoice);
    
    fetch(`/updateUser/${userId}/${roleChoice}`, {
            method: 'POST',
            body: {},
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