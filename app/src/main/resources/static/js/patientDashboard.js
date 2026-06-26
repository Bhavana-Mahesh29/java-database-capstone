
import { createDoctorCard } from "./components/doctorCard.js";
import { openModal, closeModal } from "./components/modals.js";
import {
    getDoctors,
    filterDoctors
} from "./services/doctorServices.js";
import {
    patientLogin,
    patientSignup
} from "./services/patientServices.js";

document.addEventListener("DOMContentLoaded", () => {

    loadDoctorCards();

    const signupBtn = document.getElementById("patientSignup");
    if (signupBtn) {
        signupBtn.addEventListener("click", () => {
            openModal("patientSignup");
        });
    }

    const loginBtn = document.getElementById("patientLogin");
    if (loginBtn) {
        loginBtn.addEventListener("click", () => {
            openModal("patientLogin");
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

});

async function loadDoctorCards() {
    try {

        const doctors = await getDoctors();

        renderDoctorCards(doctors);

    } catch (error) {

        console.error(error);

        document.getElementById("content").innerHTML =
            "<p>Unable to load doctors.</p>";

    }
}

function renderDoctorCards(doctors) {

    const contentDiv = document.getElementById("content");

    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML =
            "<p>No doctors found with the given filters.</p>";
        return;
    }

    doctors.forEach((doctor) => {
        contentDiv.appendChild(createDoctorCard(doctor));
    });

}

async function filterDoctorsOnChange() {

    const name =
        document.getElementById("searchBar").value.trim();

    const time =
        document.getElementById("filterTime").value;

    const specialty =
        document.getElementById("filterSpecialty").value;

    try {

        const doctors = await filterDoctors(
            name,
            time,
            specialty
        );

        renderDoctorCards(doctors);

    } catch (error) {

        console.error(error);

        document.getElementById("content").innerHTML =
            "<p>No doctors found with the given filters.</p>";

    }
}

window.signupPatient = async function () {

    const patient = {
        name: document.getElementById("signupName").value,
        email: document.getElementById("signupEmail").value,
        password: document.getElementById("signupPassword").value,
        phone: document.getElementById("signupPhone").value,
        address: document.getElementById("signupAddress").value
    };

    try {

        await patientSignup(patient);

        alert("Signup successful.");

        closeModal();

        loadDoctorCards();

    } catch (error) {

        console.error(error);

        alert("Unable to signup.");

    }

};

window.loginPatient = async function () {

    const credentials = {
        email: document.getElementById("loginEmail").value,
        password: document.getElementById("loginPassword").value
    };

    try {

        const response = await patientLogin(credentials);

        localStorage.setItem("token", response.token);
        localStorage.setItem("userRole", "loggedPatient");

        window.location.href = "/pages/loggedPatientDashboard.html";

    } catch (error) {

        console.error(error);

        alert("Invalid email or password.");

    }

};

