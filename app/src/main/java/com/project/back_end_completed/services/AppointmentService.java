package com.project.back_end_completed.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end_completed.models.Appointment;
import com.project.back_end_completed.repo.AppointmentRepository;
import com.project.back_end_completed.repo.PatientRepository;
import com.project.back_end_completed.repo.DoctorRepository;

@Service
public class AppointmentService {

    // 2. Constructor Injection for Dependencies
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService
            ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    /**
     * 4. Book Appointment Method
     * Saves a new appointment. Returns 1 if successful, 0 if it fails.
     */
    @Transactional // 3. Modifies database, needs @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            // Business logic/validation could go here before saving
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            // Log the exception in a real application
            return 0;
        }
    }

    /**
     * 5. Update Appointment Method
     * Updates an existing appointment if validations pass.
     * Returns a success message or an error message.
     */
    @Transactional // 3. Modifies database, needs @Transactional
    public String updateAppointment(Long appointmentId, Appointment updatedDetails, Long patientId) {
        Optional<Appointment> existingAppointmentOpt = appointmentRepository.findById(appointmentId);

        if (existingAppointmentOpt.isEmpty()) {
            return "Error: Appointment not found.";
        }

        Appointment existingAppointment = existingAppointmentOpt.get();

        // Validate whether the patient ID matches the owner of the appointment
        if (!existingAppointment.getPatient().getId().equals(patientId)) {
            return "Error: You are not authorized to update this appointment.";
        }

        // Additional validation: Check if doctor is available at the new time
        // (Assuming you have a business method on doctorRepository or doctor entity to check availability)
        boolean isDoctorAvailable = true; // Placeholder for actual verification logic
        if (!isDoctorAvailable) {
            return "Error: Doctor is not available at the specified time.";
        }

        // Apply updates
        existingAppointment.setAppointmentTime(updatedDetails.getAppointmentTime());
        existingAppointment.setStatus(updatedDetails.getStatus());
        // Map other necessary fields...

        try {
            appointmentRepository.save(existingAppointment);
            return "Appointment updated successfully.";
        } catch (Exception e) {
            return "Error: Failed to save the updated appointment.";
        }
    }

    /**
     * 6. Cancel Appointment Method
     * Deletes an appointment from the database after verifying ownership.
     */
    @Transactional // 3. Modifies database, needs @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isEmpty()) {
            return "Error: Appointment does not exist.";
        }

        Appointment appointment = appointmentOpt.get();

        // Ensure the patient who owns the appointment is trying to cancel it
        if (!appointment.getPatient().getId().equals(patientId)) {
            return "Error: Authorization failed. You cannot cancel this appointment.";
        }

        try {
            appointmentRepository.delete(appointment);
            return "Appointment canceled successfully.";
        } catch (Exception e) {
            return "Error: An error occurred while canceling the appointment.";
        }
    }

    /**
     * 7. Get Appointments Method
     * Retrieves a list of appointments for a doctor on a specific day,
     * optionally filtered by the patient's name.
     */
    @Transactional(readOnly = true) // 3. Read-only transaction optimization
    public List<Appointment> getAppointments(Long doctorId, LocalDate date, String patientName) {
        try {
            // Define boundaries for the given day (00:00:00 to 23:59:59)
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            if (patientName != null && !patientName.trim().isEmpty()) {
                // Filter by doctor, time range, AND patient name (case-insensitive)
                return appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                        doctorId, patientName, startOfDay, endOfDay);
            } else {
                // Filter only by doctor and time range
                return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                        doctorId, startOfDay, endOfDay);
            }
        } catch (Exception e) {
            // Handle query exceptions gracefully
            return Collections.emptyList();
        }
    }

    /**
     * 8. Change Status Method
     * Updates only the status field of an appointment.
     */
    @Transactional // 3 & 8. Ensures atomicity when updating status
    public boolean changeStatus(Long appointmentId, int newStatus) {
        try {
            // Uses the custom modifying update query from your Repository
            appointmentRepository.updateStatus(newStatus, appointmentId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}