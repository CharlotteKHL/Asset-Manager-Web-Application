const filterDate = document.querySelector("#filterDate");
const dateInput = document.querySelector("#dateInput");
dateInput.style.visibility = "hidden";

filterDate.addEventListener("change", () => {
  if (filterDate.checked) {
    dateInput.style.visibility = "visible";
    otherText.value = "";
  } else {
    dateInput.style.visibility = "hidden";
  }