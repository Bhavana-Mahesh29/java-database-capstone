# MySQL Database Design

The Smart Clinic Management System stores structured and relational data in MySQL. The following tables represent the core operational data of the clinic.

---

## Table: patients

* id: INT, Primary Key, AUTO_INCREMENT
* full_name: VARCHAR(100), NOT NULL
* email: VARCHAR(100), UNIQUE, NOT NULL
* phone: VARCHAR(15), UNIQUE, NOT NULL
* date_of_birth: DATE, NOT NULL
* gender: VARCHAR(10), NOT NULL
* address: VARCHAR(255)
* password: VARCHAR(255), NOT NULL

---

## Table: doctors

* id: INT, Primary Key, AUTO_INCREMENT
* full_name: VARCHAR(100), NOT NULL
* specialization: VARCHAR(100), NOT NULL
* email: VARCHAR(100), UNIQUE, NOT NULL
* phone: VARCHAR(15), UNIQUE, NOT NULL
* availability_status: BOOLEAN, DEFAULT TRUE

---

## Table: appointments

* id: INT, Primary Key, AUTO_INCREMENT
* doctor_id: INT, Foreign Key → doctors(id)
* patient_id: INT, Foreign Key → patients(id)
* appointment_time: DATETIME, NOT NULL
* duration: INT, DEFAULT 60
* status: INT, DEFAULT 0

  * 0 = Scheduled
  * 1 = Completed
  * 2 = Cancelled

---

## Table: admin

* id: INT, Primary Key, AUTO_INCREMENT
* username: VARCHAR(50), UNIQUE, NOT NULL
* password: VARCHAR(255), NOT NULL
* email: VARCHAR(100), UNIQUE, NOT NULL

---

## Table: clinic_locations

* id: INT, Primary Key, AUTO_INCREMENT
* clinic_name: VARCHAR(100), NOT NULL
* address: VARCHAR(255), NOT NULL
* phone: VARCHAR(15), NOT NULL

---

## Table: payments

* id: INT, Primary Key, AUTO_INCREMENT
* appointment_id: INT, Foreign Key → appointments(id)
* amount: DECIMAL(10,2), NOT NULL
* payment_method: VARCHAR(50), NOT NULL
* payment_status: VARCHAR(20), NOT NULL
* payment_date: DATETIME

---

## Constraints and Design Decisions

* Primary keys use **AUTO_INCREMENT** to generate unique IDs automatically.
* Email addresses are marked **UNIQUE** to prevent duplicate accounts.
* Required fields such as names, email, password, and appointment time are marked **NOT NULL**.
* Phone number and email format validation should be handled in the application using Spring Boot validation.
* A doctor should **not** have overlapping appointments. This should be validated in the service layer before saving a new appointment.
* Patient appointment history should be retained for future reference; therefore, deleting a patient should not automatically delete appointment records. Instead, patient accounts can be marked as inactive or soft deleted.
* Each appointment is associated with one patient and one doctor through foreign key relationships.
* Prescriptions are stored separately in MongoDB because they contain flexible document-based data and are linked to a specific appointment using the appointment ID.


# MongoDB Collection Design

The Smart Clinic Management System uses MongoDB to store **prescription documents**. Unlike relational tables, prescription records may contain different fields for different patients, making MongoDB an ideal choice.

## Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc1234567890abcdef12')",
  "appointmentId": 101,
  "patientId": 25,
  "doctorId": 8,
  "diagnosis": "Viral Fever",
  "medications": [
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Twice a day",
      "duration": "5 days"
    },
    {
      "name": "Vitamin C",
      "dosage": "1000mg",
      "frequency": "Once a day",
      "duration": "7 days"
    }
  ],
  "doctorNotes": "Drink plenty of water and take adequate rest.",
  "followUpDate": "2026-07-05",
  "attachments": [
    "blood_test_report.pdf",
    "xray_image.jpg"
  ],
  "tags": [
    "fever",
    "viral",
    "follow-up"
  ],
  "metadata": {
    "createdAt": "2026-06-25T10:30:00Z",
    "lastUpdated": "2026-06-25T10:45:00Z",
    "createdBy": "Dr. Smith"
  }
}
```

## Design Decisions

* The document stores **patientId**, **doctorId**, and **appointmentId** instead of the complete patient or doctor information to avoid data duplication.
* The **medications** field is an array of embedded documents, allowing multiple medicines to be stored in a single prescription.
* The **attachments** array stores references to uploaded medical reports or images.
* The **tags** field makes it easier to search prescriptions by diagnosis or condition.
* The **metadata** object stores audit information such as creation and update timestamps.
* MongoDB's flexible schema allows new fields (such as allergies, lab results, or additional notes) to be added in the future without modifying existing documents.
