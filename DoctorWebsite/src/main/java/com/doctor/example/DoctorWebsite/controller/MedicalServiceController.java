
package com.doctor.example.DoctorWebsite.controller;

import com.doctor.example.DoctorWebsite.entity.MedicalService;
import com.doctor.example.DoctorWebsite.service.MedicalServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    public MedicalServiceController(
            MedicalServiceService medicalServiceService) {
        this.medicalServiceService = medicalServiceService;
    }

    @PostMapping
    public ResponseEntity<MedicalService> addService(
            @RequestBody MedicalService medicalService) {
        return ResponseEntity.ok(
                medicalServiceService.addService(medicalService));
    }

    @GetMapping
    public ResponseEntity<List<MedicalService>> getAllServices() {
        return ResponseEntity.ok(
                medicalServiceService.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalService> getServiceById(
            @PathVariable Long id) {
        return medicalServiceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalService> updateService(
            @PathVariable Long id,
            @RequestBody MedicalService medicalService) {
        return ResponseEntity.ok(
                medicalServiceService.updateService(id, medicalService));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long id) {
        medicalServiceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
