package com.o2medical.api.controller;

import com.o2medical.dto.RentalContractDTO;
import com.o2medical.service.RentalContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/rental-contracts")
@Tag(name = "Rental Contracts", description = "Manage rental contracts and deployment workflow")
public class RentalContractController {

    @Autowired
    private RentalContractService rentalContractService;

    // =====================================================================
    // CREATE OPERATIONS
    // =====================================================================

    @PostMapping
    @Operation(summary = "Create new rental contract")
    public ResponseEntity<RentalContractDTO> createRentalContract(
            @RequestParam Long clientId,
            @RequestParam Long deviceId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam BigDecimal dailyRate,
            @RequestParam Long agentId) {

        var contract = rentalContractService.createRentalContract(
            clientId, deviceId, startDate, endDate, dailyRate, agentId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalContractService.toDTO(contract));
    }

    // =====================================================================
    // WORKFLOW OPERATIONS
    // =====================================================================

    @PostMapping("/{id}/deploy")
    @Operation(summary = "Deploy device to client")
    public ResponseEntity<RentalContractDTO> deployDevice(
            @PathVariable Long id,
            @RequestParam(required = false) String deploymentNotes) {

        var contract = rentalContractService.deployDevice(id, deploymentNotes);
        return ResponseEntity.ok(rentalContractService.toDTO(contract));
    }

    @PostMapping("/{id}/schedule-pickup")
    @Operation(summary = "Schedule pickup/retrieval date")
    public ResponseEntity<RentalContractDTO> schedulePickup(
            @PathVariable Long id,
            @RequestParam LocalDate pickupDate,
            @RequestParam(required = false) String notes) {

        var contract = rentalContractService.schedulePickup(id, pickupDate, notes);
        return ResponseEntity.ok(rentalContractService.toDTO(contract));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete rental contract")
    public ResponseEntity<RentalContractDTO> completeRental(
            @PathVariable Long id,
            @RequestParam LocalDate actualReturnDate) {

        var contract = rentalContractService.completeRental(id, actualReturnDate);
        return ResponseEntity.ok(rentalContractService.toDTO(contract));
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get rental contract by ID")
    public ResponseEntity<RentalContractDTO> getRentalContractById(@PathVariable Long id) {
        var contract = rentalContractService.getRentalContractById(id);
        return ResponseEntity.ok(rentalContractService.toDTO(contract));
    }

    @GetMapping("/number/{contractNumber}")
    @Operation(summary = "Get rental contract by contract number")
    public ResponseEntity<RentalContractDTO> getRentalContractByNumber(@PathVariable String contractNumber) {
        var contract = rentalContractService.getRentalContractByNumber(contractNumber);
        return ResponseEntity.ok(rentalContractService.toDTO(contract));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active rentals")
    public ResponseEntity<List<RentalContractDTO>> getAllActiveRentals() {
        var contracts = rentalContractService.getAllActiveRentals();
        return ResponseEntity.ok(contracts.stream().map(rentalContractService::toDTO).toList());
    }

    @GetMapping("/pending-deployment")
    @Operation(summary = "Get rentals pending deployment")
    public ResponseEntity<List<RentalContractDTO>> getPendingDeployments() {
        var contracts = rentalContractService.getPendingDeployments();
        return ResponseEntity.ok(contracts.stream().map(rentalContractService::toDTO).toList());
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue rentals")
    public ResponseEntity<List<RentalContractDTO>> getOverdueRentals() {
        var contracts = rentalContractService.getOverdueRentals();
        return ResponseEntity.ok(contracts.stream().map(rentalContractService::toDTO).toList());
    }

    @GetMapping("/pending-pickup")
    @Operation(summary = "Get rentals pending pickup")
    public ResponseEntity<List<RentalContractDTO>> getPendingPickups() {
        var contracts = rentalContractService.getPendingPickups();
        return ResponseEntity.ok(contracts.stream().map(rentalContractService::toDTO).toList());
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get client rental history")
    public ResponseEntity<List<RentalContractDTO>> getClientRentals(@PathVariable Long clientId) {
        var contracts = rentalContractService.getClientRentals(clientId);
        return ResponseEntity.ok(contracts.stream().map(rentalContractService::toDTO).toList());
    }

    @GetMapping("/upcoming-returns")
    @Operation(summary = "Get rentals due back in next 7 days")
    public ResponseEntity<List<RentalContractDTO>> getUpcomingReturnsIn7Days() {
        var contracts = rentalContractService.getUpcomingReturnsInNext7Days();
        return ResponseEntity.ok(contracts.stream().map(rentalContractService::toDTO).toList());
    }

    // =====================================================================
    // ANALYTICS
    // =====================================================================

    @GetMapping("/stats/active-count")
    @Operation(summary = "Count of active rentals")
    public ResponseEntity<Long> getActiveRentalsCount() {
        return ResponseEntity.ok(rentalContractService.getActiveRentalsCount());
    }

    @GetMapping("/stats/overdue-count")
    @Operation(summary = "Count of overdue rentals")
    public ResponseEntity<Long> getOverdueRentalsCount() {
        return ResponseEntity.ok(rentalContractService.getOverdueRentalsCount());
    }
}
