
package com.project.back_end_completed.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.back_end_completed.models.Appointment;

import jakarta.transaction.Transactional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = {"doctor.availableTimes"})
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end);

    @EntityGraph(attributePaths = {"doctor.availableTimes", "patient"})
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId,
            String patientName,
            LocalDateTime start,
            LocalDateTime end);

    @Transactional
    @Modifying
    void deleteAllByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
            Long patientId,
            int status);

    @Query("""
            SELECT a
            FROM Appointment a
            WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
            AND a.patient.id = :patientId
            ORDER BY a.appointmentTime ASC
            """)
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId);

    @Query("""
            SELECT a
            FROM Appointment a
            WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
            AND a.patient.id = :patientId
            AND a.status = :status
            ORDER BY a.appointmentTime ASC
            """)
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status);

    @Transactional
    @Modifying
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(
            @Param("status") int status,
            @Param("id") long id);

}

