
import { API_BASE_URL } from "../config/config.js";

const PATIENT_API = `${API_BASE_URL}/patient`;

/* Patient Signup */
export async function patientSignup(data) {
  try {
    const response = await fetch(`${PATIENT_API}/signup`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    const result = await response.json();

    return {
      success: response.ok,
      message: result.message || "Patient registered successfully."
    };
  } catch (error) {
    console.error("Patient Signup Error:", error);

    return {
      success: false,
      message: "Unable to register patient."
    };
  }
}

/* Patient Login */
export async function patientLogin(data) {
  try {
    console.log(data);

    const response = await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    return await response.json();
  } catch (error) {
    console.error("Patient Login Error:", error);
    return null;
  }
}

/* Get Logged-in Patient Data */
export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/profile`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (!response.ok) {
      throw new Error("Unable to fetch patient data.");
    }

    return await response.json();
  } catch (error) {
    console.error("Get Patient Data Error:", error);
    return null;
  }
}

/* Get Patient Appointments */
export async function getPatientAppointments(id, token, user) {
  try {
    const response = await fetch(
        `${PATIENT_API}/${user}/appointments/${id}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
    );

    if (!response.ok) {
      throw new Error("Unable to fetch appointments.");
    }

    return await response.json();
  } catch (error) {
    console.error("Get Appointments Error:", error);
    return null;
  }
}

/* Filter Appointments */
export async function filterAppointments(condition, name, token) {
  try {
    const query = new URLSearchParams({
      condition,
      name
    });

    const response = await fetch(
        `${PATIENT_API}/appointments/filter?${query.toString()}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
    );

    if (!response.ok) {
      throw new Error("Unable to filter appointments.");
    }

    return await response.json();
  } catch (error) {
    console.error("Filter Appointments Error:", error);
    alert("Unable to filter appointments.");
    return [];
  }
}

