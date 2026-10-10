package com.doctor.example.DoctorWebsite.repository;

import com.doctor.example.DoctorWebsite.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalServiceRepository
        extends JpaRepository<MedicalService, Long> {
}