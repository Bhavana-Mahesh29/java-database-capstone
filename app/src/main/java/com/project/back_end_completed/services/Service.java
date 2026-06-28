package com.project.back_end_completed.services;

import com.project.back_end_completed.models.Doctor;
import com.project.back_end_completed.repo.AdminRepository;
import com.project.back_end_completed.repo.DoctorRepository;
import com.project.back_end_completed.repo.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // 3. validateToken - validates a JWT token for a specific role
    public Map<String, String> validateToken(String token, String role) {
        return tokenService.validateToken(token, role);
    }

    // 4. validateAdmin - validates admin login credentials and returns a token
    public ResponseEntity<Map<String, String>> validateAdmin(String username, String password) {
        Map<String, String> response = new HashMap<>();
        try {
            var admin = adminRepository.findByUsername(username);
            if (admin == null) {
                response.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            if (!admin.getPassword().equals(password)) {
                response.put("message", "Incorrect password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(username);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating admin: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. filterDoctor - filters doctors by name, specialty, and/or time
    @Transactional
    public ResponseEntity<Map<String, Object>> filterDoctor(String name, String time, String specialty) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Doctor> doctors;
            boolean hasName = name != null && !name.isBlank();
            boolean hasTime = time != null && !time.isBlank();
            boolean hasSpecialty = specialty != null && !specialty.isBlank();

            if (hasName && hasTime && hasSpecialty) {
                doctors = doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
            } else if (hasName && hasTime) {
                doctors = doctorService.filterDoctorByNameAndTime(name, time);
            } else if (hasName && hasSpecialty) {
                doctors = doctorService.filterDoctorByNameAndSpecility(name, specialty);
            } else if (hasTime && hasSpecialty) {
                doctors = doctorService.filterDoctorByTimeAndSpecility(specialty, time);
            } else if (hasName) {
                doctors = doctorService.findDoctorByName(name);
            } else if (hasTime) {
                doctors = doctorService.filterDoctorsByTime(time);
            } else if (hasSpecialty) {
                doctors = doctorService.filterDoctorBySpecility(specialty);
            } else {
                doctors = doctorRepository.findAll();
            }

            response.put("doctors", doctors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering doctors: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. validateAppointment - checks if a doctor has availability at the requested time
    public int validateAppointment(Long doctorId, java.time.LocalDateTime appointmentTime) {
        try {
            var doctorOpt = doctorRepository.findById(doctorId);
            if (doctorOpt.isEmpty()) return -1;

            Doctor doctor = doctorOpt.get();
            LocalDate date = appointmentTime.toLocalDate();
            List<String> available = doctorService.getDoctorAvailability(doctorId, date);

            LocalTime requested = appointmentTime.toLocalTime();
            for (String slot : available) {
                LocalTime slotTime = LocalTime.parse(slot);
                if (slotTime.equals(requested)) return 1;
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error validating appointment: {}", e.getMessage());
            return 0;
        }
    }

    // 7. validatePatient - checks if a patient with same email/phone already exists
    public boolean validatePatient(String email, String phone) {
        return patientRepository.findByEmailOrPhone(email, phone) == null;
    }

    // 8. validatePatientLogin - validates patient login and returns a JWT token
    public ResponseEntity<Map<String, String>> validatePatientLogin(String email, String password) {
        Map<String, String> response = new HashMap<>();
        try {
            var patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            if (!patient.getPassword().equals(password)) {
                response.put("message", "Incorrect password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(email);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating patient login: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 9. filterPatient - filters a patient's appointments by condition and/or doctor name
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            var patient = patientRepository.findByEmail(email);
            if (patient == null) {
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Long patientId = patient.getId();

            boolean hasCondition = condition != null && !condition.isBlank();
            boolean hasName = name != null && !name.isBlank();

            if (hasCondition && hasName) {
                return patientService.filterByDoctorAndCondition(name, condition, patientId);
            } else if (hasCondition) {
                return patientService.filterByCondition(condition, patientId);
            } else if (hasName) {
                return patientService.filterByDoctor(name, patientId);
            } else {
                return patientService.getPatientAppointment(patientId);
            }
        } catch (Exception e) {
            logger.error("Error filtering patient appointments: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
