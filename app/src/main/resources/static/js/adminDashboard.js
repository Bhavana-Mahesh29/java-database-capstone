/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/

import { openModal, closeModal } from "./components/modals.js";
import {
    getDoctors,
    filterDoctors,
    saveDoctor
} from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

document.addEventListener("DOMContentLoaded", () => {

    loadDoctorCards();

    const addDoctorBtn = document.getElementById("addDocBtn");
    if (addDoctorBtn) {
        addDoctorBtn.addEventListener("click", () => {
            openModal("addDoctor");
        });
    }

    const searchBar = document.getElementById("searchBar");
    if (searchBar) {
        searchBar.addEventListener("input", filterDoctorsOnChange);
    }

    const filterTime = document.getElementById("filterTime");
    if (filterTime) {
        filterTime.addEventListener("change", filterDoctorsOnChange);
    }

    const filterSpecialty = document.getElementById("filterSpecialty");
    if (filterSpecialty) {
        filterSpecialty.addEventListener("change", filterDoctorsOnChange);
    }

    const addDoctorForm = document.getElementById("addDoctorForm");
    if (addDoctorForm) {
        addDoctorForm.addEventListener("submit", adminAddDoctor);
    }

});

async function loadDoctorCards() {
    try {

        const doctors = await getDoctors();

        renderDoctorCards(doctors);

    } catch (error) {

        console.error(error);

        alert("Unable to load doctors.");

    }
}

function renderDoctorCards(doctors) {

    const contentDiv = document.getElementById("content");

    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {

        contentDiv.innerHTML = "<h3>No doctors found</h3>";

        return;
    }

    doctors.forEach((doctor) => {

        const card = createDoctorCard(doctor);

        contentDiv.appendChild(card);

    });

}

async function filterDoctorsOnChange() {

    const search =
        document.getElementById("searchBar")?.value.trim() || "";

    const time =
        document.getElementById("filterTime")?.value || "";

    const specialty =
        document.getElementById("filterSpecialty")?.value || "";

    try {

        const doctors = await filterDoctors(
            search,
            specialty,
            time
        );

        renderDoctorCards(doctors);

    } catch (error) {

        console.error(error);

        alert("Unable to filter doctors.");

    }
}

async function adminAddDoctor(event) {

    event.preventDefault();

    const token = localStorage.getItem("token");

    if (!token) {

        alert("Please login as Admin.");

        return;

    }

    const availability = [];

    document
        .querySelectorAll("input[name='availability']:checked")
        .forEach((checkbox) => {
            availability.push(checkbox.value);
        });

    const doctor = {
        name: document.getElementById("doctorName").value,
        specialization: document.getElementById("specialization").value,
        email: document.getElementById("doctorEmail").value,
        password: document.getElementById("doctorPassword").value,
        mobile: document.getElementById("doctorMobile").value,
        availability: availability
    };

    try {

        await saveDoctor(doctor, token);

        alert("Doctor added successfully.");

        closeModal();

        loadDoctorCards();

    } catch (error) {

        console.error(error);

        alert("Unable to add doctor.");

    }
}

