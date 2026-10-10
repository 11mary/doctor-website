package com.doctor.example.DoctorWebsite.repository;

import com.doctor.example.DoctorWebsite.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository
        extends JpaRepository<ContactMessage, Long> {
}