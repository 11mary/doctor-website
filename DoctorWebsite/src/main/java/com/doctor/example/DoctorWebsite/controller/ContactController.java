package com.doctor.example.DoctorWebsite.controller;

import com.doctor.example.DoctorWebsite.entity.ContactMessage;
import com.doctor.example.DoctorWebsite.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactMessage> submitMessage(
            @RequestBody ContactMessage contactMessage) {

        return ResponseEntity.ok(
                contactService.saveMessage(contactMessage)
        );
    }

    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        return ResponseEntity.ok(contactService.getAllMessages());
    }
}