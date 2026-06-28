package com.project.back_end_completed.controllers;

import com.project.back_end_completed.models.Appointment;
import com.project.back_end_completed.repo.DoctorRepository;
import com.project.back_end_completed.repo.PatientRepository;
import com.project.back_end_completed.services.AppointmentService;
import com.project.back_end_completed.services.ClinicService;
import com.project.back_end_completed.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ClinicService clinicservice;
    private final TokenService tokenService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentController(AppointmentService appointmentService,
                                 ClinicService clinicservice,
                                 TokenService tokenService,
                                 DoctorRepository doctorRepository,
                                 PatientRepository patientRepository) {
        this.appointmentService = appointmentService;
        this.clinicservice = clinicservice;
        this.tokenService = tokenService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // GET /{token}/{date}/{patientName} — fetch appointments for a doctor (doctor access)
    @GetMapping("/{token}/{date}/{patientName}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String token,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String patientName) {

        Map<String, String> validation = clinicservice.validateToken(token, "doctor");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.copyOf(validation));
        }

        String email = tokenService.extractEmail(token);
        var doctor = doctorRepository.findByEmail(email);
        List<Appointment> appointments = appointmentService.getAppointments(doctor.getId(), date, patientName);
        return ResponseEntity.ok(Map.of("appointments", appointments));
    }

    // POST /{token} — book a new appointment (patient access)
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {

        Map<String, String> validation = clinicservice.validateToken(token, "patient");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        int validSlot = clinicservice.validateAppointment(
                appointment.getDoctor().getId(),
                appointment.getAppointmentTime());

        if (validSlot == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor not found"));
        }
        if (validSlot == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "The selected time slot is not available"));
        }

        int result = appointmentService.bookAppointment(appointment);
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Appointment booked successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to book appointment"));
    }

    // PUT /{token} — update an existing appointment (patient access)
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {

        Map<String, String> validation = clinicservice.validateToken(token, "patient");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        String email = tokenService.extractEmail(token);
        Long patientId = patientRepository.findByEmail(email).getId();

        String result = appointmentService.updateAppointment(appointment.getId(), appointment, patientId);
        if (result.startsWith("Error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", result));
        }
        return ResponseEntity.ok(Map.of("message", result));
    }

    // DELETE /{id}/{token} — cancel an appointment (patient access)
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        Map<String, String> validation = clinicservice.validateToken(token, "patient");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        String email = tokenService.extractEmail(token);
        Long patientId = patientRepository.findByEmail(email).getId();

        String result = appointmentService.cancelAppointment(id, patientId);
        if (result.startsWith("Error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", result));
        }
        return ResponseEntity.ok(Map.of("message", result));
    }
}
