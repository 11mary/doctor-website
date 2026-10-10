package com.doctor.example.DoctorWebsite.repository;

import com.doctor.example.DoctorWebsite.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(
            LocalDate appointmentDate
    );

    boolean existsByAppointmentDateAndAppointmentTime(
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );
}