package com.project.back_end_completed.controllers;

import com.project.back_end_completed.DTO.Login;
import com.project.back_end_completed.models.Patient;
import com.project.back_end_completed.repo.PatientRepository;
import com.project.back_end_completed.services.PatientService;
import com.project.back_end_completed.services.Service;
import com.project.back_end_completed.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;

    public PatientController(PatientService patientService,
                             Service service,
                             TokenService tokenService,
                             PatientRepository patientRepository) {
        this.patientService = patientService;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
    }

    // GET /{token} — get patient details
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        Map<String, String> validation = service.validateToken(token, "patient");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.copyOf(validation));
        }
        return patientService.getPatientDetails(token);
    }

    // POST / — register new patient
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        boolean isValid = service.validatePatient(patient.getEmail(), patient.getPhone());
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Patient with this email or phone already exists"));
        }
        int result = patientService.createPatient(patient);
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Patient registered successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to register patient"));
    }

    // POST /login — patient login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login.getIdentifier(), login.getPassword());
    }

    // GET /{patientId}/{token}/{user} — get patient's appointments
    @GetMapping("/{patientId}/{token}/{user}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable Long patientId,
            @PathVariable String token,
            @PathVariable String user) {

        Map<String, String> validation = service.validateToken(token, user);
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.copyOf(validation));
        }
        return patientService.getPatientAppointment(patientId);
    }

    // GET /filter/{condition}/{name}/{token} — filter patient's appointments
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        Map<String, String> validation = service.validateToken(token, "patient");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.copyOf(validation));
        }
        return service.filterPatient(condition, name, token);
    }
}
