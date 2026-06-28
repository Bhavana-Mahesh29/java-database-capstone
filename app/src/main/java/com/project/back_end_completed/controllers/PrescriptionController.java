package com.project.back_end_completed.controllers;

import com.project.back_end_completed.models.Prescription;
import com.project.back_end_completed.services.AppointmentService;
import com.project.back_end_completed.services.PrescriptionService;
import com.project.back_end_completed.services.ClinicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final ClinicService clinicservice;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  ClinicService clinicservice,
                                  AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.clinicservice = clinicservice;
        this.appointmentService = appointmentService;
    }

    // POST /{token} — save a prescription (doctor access)
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @Valid @RequestBody Prescription prescription,
            @PathVariable String token) {

        Map<String, String> validation = clinicservice.validateToken(token, "doctor");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        // Mark appointment as completed (status = 1)
        appointmentService.changeStatus(prescription.getAppointmentId(), 1);

        return prescriptionService.savePrescription(prescription);
    }

    // GET /{appointmentId}/{token} — get a prescription (doctor access)
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {

        Map<String, String> validation = clinicservice.validateToken(token, "doctor");
        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.copyOf(validation));
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}
