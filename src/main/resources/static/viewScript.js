// Updates the placeholder values on the offcanvas screen to represent the asset.
function replacePlaceholder() {
    var assetTitle = document.getElementById("assetName");
    var assetVariables = document.getElementById("assetVariables");

    var formData = new FormData();
    formData.append("asset", document.getElementById("tempButton").value);

    // Creates a POST requests that retrieves the asset information for the database.
    fetch('/view', {
        method: 'POST',
        body: formData

    }).then(response => {
        if(response.ok){
            return response.json();
        }else{
            return response.json().then(errorMessage => { 
                throw new Error(errorMessage.error);

            })
        }
    }).then( 
        data => {assetTitle.innerText = data.title;
    }).catch(
        error => {console.log(error);
    });

}