package com.doctor.example.DoctorWebsite.service;

import com.doctor.example.DoctorWebsite.entity.Appointment;
import com.doctor.example.DoctorWebsite.repository.AppointmentRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private static final LocalTime OPEN_TIME =
            LocalTime.of(9, 0);

    private static final LocalTime CLOSE_TIME =
            LocalTime.of(17, 0);

    private static final int SLOT_MINUTES = 30;

    public AppointmentService(
            AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<LocalTime> getAvailableSlots(LocalDate date) {

        if (date == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select an appointment date."
            );
        }

        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Past dates cannot be booked."
            );
        }

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime slot = OPEN_TIME;

        while (!slot.plusMinutes(SLOT_MINUTES).isAfter(CLOSE_TIME)) {

            boolean alreadyBooked =
                    appointmentRepository
                            .existsByAppointmentDateAndAppointmentTime(
                                    date, slot
                            );

            boolean isPastTime =
                    date.equals(LocalDate.now())
                            && !slot.isAfter(LocalTime.now());

            if (!alreadyBooked && !isPastTime) {
                availableSlots.add(slot);
            }

            slot = slot.plusMinutes(SLOT_MINUTES);
        }

        return availableSlots;
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment) {

        if (appointment == null
                || appointment.getPatientName() == null
                || appointment.getPhone() == null
                || appointment.getEmail() == null
                || appointment.getService() == null
                || appointment.getAppointmentDate() == null
                || appointment.getAppointmentTime() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please complete all required appointment fields."
            );
        }

        appointment.setPatientName(
                appointment.getPatientName().trim()
        );
        appointment.setPhone(appointment.getPhone().trim());
        appointment.setEmail(appointment.getEmail().trim());
        appointment.setService(appointment.getService().trim());

        if (appointment.getPatientName().isBlank()
                || appointment.getPhone().isBlank()
                || appointment.getEmail().isBlank()
                || appointment.getService().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Required fields cannot be empty."
            );
        }

        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Past dates cannot be booked."
            );
        }

        List<LocalTime> availableSlots =
                getAvailableSlots(appointment.getAppointmentDate());

        if (!availableSlots.contains(appointment.getAppointmentTime())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This time slot is unavailable. Please select another."
            );
        }

        try {
            return appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This time slot has just been booked. Please select another."
            );
        }
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment not found."
                ));
    }

    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }
}