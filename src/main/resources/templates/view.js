function replacePlaceholder() {
    var assetTitle = document.getElementById("assetName");
    var assetVariables = document.getElementById("assetVariables");

    assetName.innerText = "Test Asset";
    assetVariables.innerText = "Test Variable 1 \n Test variable 2."

    var formData = new FormData();
    formData.append("asset", document.getElementById("tempButton").value);

    fetch('/view', {
        method: 'POST',
        body: formData

    }).then(res => {
        if(res.ok){
            console.log('Success');
        }else{
            console.log('Error');
        }
    }).then( 
        data => console.log(data)
    );

}