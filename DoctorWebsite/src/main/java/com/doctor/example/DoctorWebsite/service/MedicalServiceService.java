package com.doctor.example.DoctorWebsite.service;

import com.doctor.example.DoctorWebsite.entity.MedicalService;
import com.doctor.example.DoctorWebsite.repository.MedicalServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicalServiceService {

    private final MedicalServiceRepository medicalServiceRepository;

    public MedicalServiceService(
            MedicalServiceRepository medicalServiceRepository) {
        this.medicalServiceRepository = medicalServiceRepository;
    }

    // Add a new medical service
    public MedicalService addService(MedicalService medicalService) {
        return medicalServiceRepository.save(medicalService);
    }

    // Get all medical services
    public List<MedicalService> getAllServices() {
        return medicalServiceRepository.findAll();
    }

    // Get one service by ID
    public Optional<MedicalService> getServiceById(Long id) {
        return medicalServiceRepository.findById(id);
    }

    // Update an existing service
    public MedicalService updateService(
            Long id, MedicalService updatedService) {

        MedicalService existingService =
                medicalServiceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Medical service not found"));

        existingService.setName(updatedService.getName());
        existingService.setDescription(updatedService.getDescription());
        existingService.setDuration(updatedService.getDuration());
        existingService.setPrice(updatedService.getPrice());

        return medicalServiceRepository.save(existingService);
    }

    // Delete a service
    public void deleteService(Long id) {
        if (!medicalServiceRepository.existsById(id)) {
            throw new RuntimeException("Medical service not found");
        }

        medicalServiceRepository.deleteById(id);
    }
}