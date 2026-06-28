package com.project.back_end_completed.services;

import com.project.back_end_completed.DTO.AppointmentDTO;
import com.project.back_end_completed.models.Appointment;
import com.project.back_end_completed.models.Patient;
import com.project.back_end_completed.repo.AppointmentRepository;
import com.project.back_end_completed.repo.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 3. createPatient - saves a new patient, returns 1 on success, 0 on failure
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            logger.error("Error creating patient: {}", e.getMessage());
            return 0;
        }
    }

    // 4. getPatientAppointment - retrieves all appointments for a patient as DTOs
    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
            List<AppointmentDTO> dtos = appointments.stream()
                    .map(a -> new AppointmentDTO(
                            a.getId(),
                            a.getDoctor().getId(),
                            a.getDoctor().getName(),
                            a.getPatient().getId(),
                            a.getPatient().getName(),
                            a.getPatient().getEmail(),
                            a.getPatient().getPhone(),
                            a.getPatient().getAddress(),
                            a.getAppointmentTime(),
                            a.getStatus()))
                    .collect(Collectors.toList());
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching patient appointments: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. filterByCondition - filters appointments by "past" (status=1) or "future" (status=0)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if (condition.equalsIgnoreCase("past")) {
                status = 1;
            } else if (condition.equalsIgnoreCase("future")) {
                status = 0;
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            List<Appointment> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
            List<AppointmentDTO> dtos = toDTO(appointments);
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering appointments by condition: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. filterByDoctor - filters appointments by doctor name for a specific patient
    public ResponseEntity<Map<String, Object>> filterByDoctor(String doctorName, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientId(doctorName, patientId);
            List<AppointmentDTO> dtos = toDTO(appointments);
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering appointments by doctor: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 7. filterByDoctorAndCondition - filters by both doctor name and condition (past/future)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String doctorName,
                                                                          String condition,
                                                                          Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if (condition.equalsIgnoreCase("past")) {
                status = 1;
            } else if (condition.equalsIgnoreCase("future")) {
                status = 0;
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
            List<AppointmentDTO> dtos = toDTO(appointments);
            response.put("appointments", dtos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering appointments by doctor and condition: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 8. getPatientDetails - extracts email from token, returns patient info
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            Patient patient = patientRepository.findByEmail(email);
            response.put("patient", patient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching patient details: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper: convert Appointment list to AppointmentDTO list
    private List<AppointmentDTO> toDTO(List<Appointment> appointments) {
        return appointments.stream()
                .map(a -> new AppointmentDTO(
                        a.getId(),
                        a.getDoctor().getId(),
                        a.getDoctor().getName(),
                        a.getPatient().getId(),
                        a.getPatient().getName(),
                        a.getPatient().getEmail(),
                        a.getPatient().getPhone(),
                        a.getPatient().getAddress(),
                        a.getAppointmentTime(),
                        a.getStatus()))
                .collect(Collectors.toList());
    }
}
