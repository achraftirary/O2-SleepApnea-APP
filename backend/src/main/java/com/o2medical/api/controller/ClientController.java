package com.o2medical.api.controller;

import com.o2medical.dto.ClientDTO;
import com.o2medical.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@Tag(name = "Clients & Patients", description = "Manage client/patient profiles and medical records")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // =====================================================================
    // CREATE OPERATIONS
    // =====================================================================

    @PostMapping
    @Operation(summary = "Create new client/patient")
    public ResponseEntity<ClientDTO> createClient(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam String streetAddress,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam(required = false) Long assignedDoctorId) {

        var client = clientService.createClient(firstName, lastName, phone, email, 
            streetAddress, city, postalCode, assignedDoctorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.toDTO(client));
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        var client = clientService.getClientById(id);
        return ResponseEntity.ok(clientService.toDTO(client));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Get client by phone number")
    public ResponseEntity<ClientDTO> getClientByPhone(@PathVariable String phone) {
        var client = clientService.getClientByPhone(phone);
        return ResponseEntity.ok(clientService.toDTO(client));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get client by email")
    public ResponseEntity<ClientDTO> getClientByEmail(@PathVariable String email) {
        var client = clientService.getClientByEmail(email);
        return ResponseEntity.ok(clientService.toDTO(client));
    }

    @GetMapping
    @Operation(summary = "Get all active clients")
    public ResponseEntity<List<ClientDTO>> getAllActiveClients() {
        var clients = clientService.getAllActiveClients();
        return ResponseEntity.ok(clientService.toDtoList(clients));
    }

    @GetMapping("/search")
    @Operation(summary = "Search clients by name")
    public ResponseEntity<List<ClientDTO>> searchClients(@RequestParam String name) {
        var clients = clientService.searchClients(name);
        return ResponseEntity.ok(clientService.toDtoList(clients));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get all clients assigned to a doctor")
    public ResponseEntity<List<ClientDTO>> getClientsByDoctor(@PathVariable Long doctorId) {
        var clients = clientService.getClientsByDoctor(doctorId);
        return ResponseEntity.ok(clientService.toDtoList(clients));
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get inactive clients")
    public ResponseEntity<List<ClientDTO>> getInactiveClients() {
        var clients = clientService.getInactiveClients();
        return ResponseEntity.ok(clientService.toDtoList(clients));
    }

    // =====================================================================
    // UPDATE OPERATIONS
    // =====================================================================

    @PutMapping("/{id}")
    @Operation(summary = "Update client information")
    public ResponseEntity<ClientDTO> updateClient(
            @PathVariable Long id,
            @RequestBody ClientDTO dto) {

        var client = clientService.updateClient(id, dto);
        return ResponseEntity.ok(clientService.toDTO(client));
    }

    @PutMapping("/{id}/assign-doctor")
    @Operation(summary = "Assign doctor to client")
    public ResponseEntity<Void> assignDoctor(
            @PathVariable Long id,
            @RequestParam Long doctorId) {

        clientService.assignDoctor(id, doctorId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate client account")
    public ResponseEntity<Void> deactivateClient(@PathVariable Long id) {
        clientService.deactivateClient(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // ANALYTICS
    // =====================================================================

    @GetMapping("/stats/total-active")
    @Operation(summary = "Total count of active clients")
    public ResponseEntity<Long> getTotalActiveClients() {
        return ResponseEntity.ok(clientService.getTotalActiveClients());
    }

    @GetMapping("/stats/by-doctor/{doctorId}")
    @Operation(summary = "Count of clients assigned to a doctor")
    public ResponseEntity<Long> getTotalClientsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(clientService.getTotalClientsByDoctor(doctorId));
    }
}
