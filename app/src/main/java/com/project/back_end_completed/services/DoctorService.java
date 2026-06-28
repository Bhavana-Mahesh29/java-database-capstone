package com.project.back_end_completed.services;

import com.project.back_end_completed.models.Appointment;
import com.project.back_end_completed.models.Doctor;
import com.project.back_end_completed.repo.AppointmentRepository;
import com.project.back_end_completed.repo.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 4. getDoctorAvailability - returns available time slots for a doctor on a given date
    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return Collections.emptyList();

        List<String> allSlots = new ArrayList<>(doctor.getAvailableTimes());

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> booked = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, startOfDay, endOfDay);

        Set<String> bookedTimes = booked.stream()
                .map(a -> a.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toSet());

        allSlots.removeAll(bookedTimes);
        return allSlots;
    }

    // 5. saveDoctor - saves a new doctor, returns -1 if email exists, 1 on success, 0 on error
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            logger.error("Error saving doctor: {}", e.getMessage());
            return 0;
        }
    }

    // 6. updateDoctor - updates an existing doctor's details, returns -1 if not found
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            logger.error("Error updating doctor: {}", e.getMessage());
            return 0;
        }
    }

    // 7. getDoctors - fetches all doctors with eagerly loaded available times
    @Transactional
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Doctor> doctors = doctorRepository.findAll();
            doctors.forEach(d -> d.getAvailableTimes().size()); // force eager load
            response.put("doctors", doctors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching doctors: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 8. deleteDoctor - deletes a doctor and all their appointments
    public int deleteDoctor(Long doctorId) {
        try {
            if (!doctorRepository.existsById(doctorId)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(doctorId);
            doctorRepository.deleteById(doctorId);
            return 1;
        } catch (Exception e) {
            logger.error("Error deleting doctor: {}", e.getMessage());
            return 0;
        }
    }

    // 9. validateDoctor - validates doctor login, returns token on success or error message
    public ResponseEntity<Map<String, String>> validateDoctor(String email, String password) {
        Map<String, String> response = new HashMap<>();
        try {
            Doctor doctor = doctorRepository.findByEmail(email);
            if (doctor == null) {
                response.put("message", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            if (!doctor.getPassword().equals(password)) {
                response.put("message", "Incorrect password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(email);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating doctor: {}", e.getMessage());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 10. findDoctorByName - finds doctors by partial name match, eagerly loads available times
    @Transactional
    public List<Doctor> findDoctorByName(String name) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors.forEach(d -> d.getAvailableTimes().size());
        return doctors;
    }

    // 11. filterDoctorsByNameSpecilityandTime - filters by name, specialty, and AM/PM
    @Transactional
    public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        return filterDoctorByTime(doctors, time);
    }

    // 12. filterDoctorByTime - filters a doctor list by AM/PM availability
    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String time) {
        return doctors.stream().filter(doctor -> {
            for (String slot : doctor.getAvailableTimes()) {
                LocalTime t = LocalTime.parse(slot);
                if (time.equalsIgnoreCase("AM") && t.getHour() < 12) return true;
                if (time.equalsIgnoreCase("PM") && t.getHour() >= 12) return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    // 13. filterDoctorByNameAndTime - filters doctors by name and AM/PM
    @Transactional
    public List<Doctor> filterDoctorByNameAndTime(String name, String time) {
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors.forEach(d -> d.getAvailableTimes().size());
        return filterDoctorByTime(doctors, time);
    }

    // 14. filterDoctorByNameAndSpecility - filters doctors by name and specialty
    @Transactional
    public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
    }

    // 15. filterDoctorByTimeAndSpecility - filters doctors by specialty and AM/PM
    @Transactional
    public List<Doctor> filterDoctorByTimeAndSpecility(String specialty, String time) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        doctors.forEach(d -> d.getAvailableTimes().size());
        return filterDoctorByTime(doctors, time);
    }

    // 16. filterDoctorBySpecility - filters doctors by specialty (case-insensitive)
    @Transactional
    public List<Doctor> filterDoctorBySpecility(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }

    // 17. filterDoctorsByTime - filters all doctors by AM/PM availability
    @Transactional
    public List<Doctor> filterDoctorsByTime(String time) {
        List<Doctor> doctors = doctorRepository.findAll();
        doctors.forEach(d -> d.getAvailableTimes().size());
        return filterDoctorByTime(doctors, time);
    }
}
