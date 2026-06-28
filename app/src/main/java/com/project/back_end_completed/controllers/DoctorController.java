package com.project.back_end_completed.controllers;

import com.project.back_end_completed.DTO.Login;
import com.project.back_end_completed.models.Doctor;
import com.project.back_end_completed.services.DoctorService;
import com.project.back_end_completed.services.Service;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // GET /{user}/{doctorId}/{date}/{token} — check doctor availability
    @GetMapping("/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String token) {

        Map<String, String> validation = service.validateToken(token, user);
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.copyOf(validation));
        }

        List<String> availability = doctorService.getDoctorAvailability(doctorId, date);
        return ResponseEntity.ok(Map.of("availability", availability));
    }

    // GET / — get all doctors
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        return doctorService.getDoctors();
    }

    // POST /{token} — register a new doctor (admin access)
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, String> validation = service.validateToken(token, "admin");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Doctor with this email already exists"));
        }
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Doctor registered successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to register doctor"));
    }

    // POST /login — doctor login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@Valid @RequestBody Login login) {
        return doctorService.validateDoctor(login.getIdentifier(), login.getPassword());
    }

    // PUT /{token} — update doctor details (admin access)
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, String> validation = service.validateToken(token, "admin");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        int result = doctorService.updateDoctor(doctor);
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor not found"));
        }
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to update doctor"));
    }

    // DELETE /{id}/{token} — delete a doctor (admin access)
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {

        Map<String, String> validation = service.validateToken(token, "admin");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        int result = doctorService.deleteDoctor(id);
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor not found"));
        }
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to delete doctor"));
    }

    // GET /filter/{name}/{time}/{speciality} — filter doctors
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {

        return service.filterDoctor(name, time, speciality);
    }
}
