# Smart Clinic Management System – User Stories

## Admin User Stories

### User Story 1

**Title:**
*As an admin, I want to log into the portal with my username and password, so that I can securely manage the platform.*

**Acceptance Criteria:**

1. The admin can enter a valid username and password.
2. Invalid login credentials display an error message.
3. The admin is redirected to the dashboard after successful login.

**Priority:** High
**Story Points:** 3

**Notes:**

* Only authenticated admins can access admin features.

---

### User Story 2

**Title:**
*As an admin, I want to log out of the portal, so that I can protect system access.*

**Acceptance Criteria:**

1. The admin can log out from any page.
2. The current session is terminated.
3. The admin is redirected to the login page.

**Priority:** High
**Story Points:** 1

**Notes:**

* Logged-out users cannot access protected pages.

---

### User Story 3

**Title:**
*As an admin, I want to add doctors to the portal, so that they can manage appointments.*

**Acceptance Criteria:**

1. The admin can enter doctor details.
2. Required fields are validated.
3. The doctor profile is successfully saved.

**Priority:** High
**Story Points:** 5

**Notes:**

* Email addresses should be unique.

---

### User Story 4

**Title:**
*As an admin, I want to delete a doctor's profile, so that inactive doctors are removed from the system.*

**Acceptance Criteria:**

1. The admin can select a doctor.
2. A confirmation is shown before deletion.
3. The doctor profile is removed successfully.

**Priority:** Medium
**Story Points:** 3

**Notes:**

* Deletion should not affect completed appointment records.

---

### User Story 5

**Title:**
*As an admin, I want to run a stored procedure to view monthly appointment statistics, so that I can monitor clinic usage.*

**Acceptance Criteria:**

1. The stored procedure executes successfully.
2. Monthly appointment counts are displayed.
3. Errors are shown if execution fails.

**Priority:** Medium
**Story Points:** 2

**Notes:**

* Executed through the MySQL CLI.

---

## Patient User Stories

### User Story 1

**Title:**
*As a patient, I want to view the list of doctors without logging in, so that I can explore available doctors before registering.*

**Acceptance Criteria:**

1. The doctor list is publicly accessible.
2. Doctor name and specialization are displayed.
3. No login is required.

**Priority:** Medium
**Story Points:** 2

**Notes:**

* Contact details may be limited.

---

### User Story 2

**Title:**
*As a patient, I want to sign up using my email and password, so that I can create an account.*

**Acceptance Criteria:**

1. The patient enters valid registration details.
2. Duplicate email addresses are rejected.
3. A new account is created successfully.

**Priority:** High
**Story Points:** 3

**Notes:**

* Password validation should be enforced.

---

### User Story 3

**Title:**
*As a patient, I want to log into the portal, so that I can manage my appointments.*

**Acceptance Criteria:**

1. Valid credentials allow login.
2. Invalid credentials display an error.
3. The patient is redirected to the dashboard.

**Priority:** High
**Story Points:** 3

**Notes:**

* Session management should be secure.

---

### User Story 4

**Title:**
*As a patient, I want to book a one-hour appointment with a doctor, so that I can receive medical consultation.*

**Acceptance Criteria:**

1. Available time slots are displayed.
2. The selected slot is reserved.
3. A confirmation message is shown after booking.

**Priority:** High
**Story Points:** 5

**Notes:**

* Double booking should not be allowed.

---

### User Story 5

**Title:**
*As a patient, I want to view my upcoming appointments, so that I can prepare accordingly.*

**Acceptance Criteria:**

1. Upcoming appointments are listed.
2. Appointment details include doctor, date, and time.
3. Past appointments are excluded.

**Priority:** Medium
**Story Points:** 2

**Notes:**

* Display appointments in chronological order.

---

## Doctor User Stories

### User Story 1

**Title:**
*As a doctor, I want to log into the portal, so that I can manage my appointments.*

**Acceptance Criteria:**

1. Valid credentials allow login.
2. Invalid credentials display an error.
3. The doctor is redirected to the dashboard.

**Priority:** High
**Story Points:** 3

**Notes:**

* Authentication is required.

---

### User Story 2

**Title:**
*As a doctor, I want to log out of the portal, so that I can protect my data.*

**Acceptance Criteria:**

1. The doctor can log out successfully.
2. The current session is terminated.
3. The login page is displayed.

**Priority:** High
**Story Points:** 1

**Notes:**

* Protected pages require login after logout.

---

### User Story 3

**Title:**
*As a doctor, I want to view my appointment calendar, so that I can stay organized.*

**Acceptance Criteria:**

1. Upcoming appointments are displayed.
2. Date and time are shown.
3. The calendar updates when appointments change.

**Priority:** High
**Story Points:** 3

**Notes:**

* Appointments should be sorted by date.

---

### User Story 4

**Title:**
*As a doctor, I want to mark my unavailable time slots, so that patients can only book available appointments.*

**Acceptance Criteria:**

1. The doctor can select unavailable dates and times.
2. Unavailable slots cannot be booked.
3. Availability updates immediately.

**Priority:** High
**Story Points:** 5

**Notes:**

* Existing appointments remain unchanged.

---

### User Story 5

**Title:**
*As a doctor, I want to update my profile with my specialization and contact information, so that patients have accurate information.*

**Acceptance Criteria:**

1. The doctor can edit profile details.
2. Updated information is saved successfully.
3. Patients can view the latest profile information.

**Priority:** Medium
**Story Points:** 3

**Notes:**

* Required fields must be validated.
