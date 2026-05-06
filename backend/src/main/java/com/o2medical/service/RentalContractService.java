package com.o2medical.service;

import com.o2medical.domain.entities.*;
import com.o2medical.domain.enums.DeviceStatus;
import com.o2medical.domain.enums.RentalContractStatus;
import com.o2medical.dto.RentalContractDTO;
import com.o2medical.repository.RentalContractRepository;
import com.o2medical.repository.DeviceRepository;
import com.o2medical.repository.ClientRepository;
import com.o2medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RentalContractService {

    @Autowired
    private RentalContractRepository rentalContractRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertService alertService;

    @Autowired
    private InvoiceService invoiceService;

    // =====================================================================
    // CREATE & UPDATE OPERATIONS
    // =====================================================================

    public RentalContract createRentalContract(Long clientId, Long deviceId, LocalDate startDate,
                                               LocalDate endDate, java.math.BigDecimal dailyRate, Long agentId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        
        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

        if (!device.isAvailable()) {
            throw new IllegalStateException("Device is not available for rental");
        }

        RentalContract contract = new RentalContract();
        contract.setContractNumber(generateContractNumber());
        contract.setClient(client);
        contract.setDevice(device);
        contract.setAssignedAgent(agent);
        contract.setRentalStartDate(startDate);
        contract.setExpectedReturnDate(endDate);
        contract.setDailyRentalRate(dailyRate);
        contract.setContractStatus(RentalContractStatus.PENDING_DEPLOYMENT);
        contract.calculateRentalDuration();
        contract.calculateTotalRentalCost();

        RentalContract saved = rentalContractRepository.save(contract);
        alertService.createAlert("CONTRACT_CREATED", "INFO", "Rental Contract Created",
            String.format("New rental contract %s created for %s", contract.getContractNumber(), client.getFullName()),
            "RENTAL_CONTRACT", saved.getId());
        
        return saved;
    }

    public RentalContract deployDevice(Long contractId, String deploymentNotes) {
        RentalContract contract = rentalContractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if (!RentalContractStatus.PENDING_DEPLOYMENT.equals(contract.getContractStatus())) {
            throw new IllegalStateException("Contract must be in PENDING_DEPLOYMENT status");
        }

        contract.transitionToActive(LocalDate.now());
        contract.setDeploymentNotes(deploymentNotes);
        contract.getDevice().setDeviceStatus(DeviceStatus.DEPLOYED);

        RentalContract saved = rentalContractRepository.save(contract);
        deviceRepository.save(contract.getDevice());

        alertService.createAlert("DEVICE_DEPLOYED", "INFO", "Device Deployed",
            String.format("Device %s deployed to %s", contract.getDevice().getSerialNumber(), contract.getClient().getFullName()),
            "RENTAL_CONTRACT", saved.getId());

        return saved;
    }

    public RentalContract schedulePickup(Long contractId, LocalDate pickupDate, String notes) {
        RentalContract contract = rentalContractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if (!RentalContractStatus.ACTIVE_RENTAL.equals(contract.getContractStatus())) {
            throw new IllegalStateException("Contract must be in ACTIVE_RENTAL status");
        }

        // Call alertService to create a "PICKUP_SCHEDULED" alert with reminder for pickup date
        alertService.createAlert("PICKUP_SCHEDULED", "INFO", "Pickup Scheduled",
            String.format("Pickup scheduled for %s on %s", contract.getClient().getFullName(), pickupDate),
            "RENTAL_CONTRACT", contract.getId());

        contract.transitionToPendingPickup(pickupDate);
        contract.setPickupNotes(notes);

        return rentalContractRepository.save(contract);
    }

    public RentalContract completeRental(Long contractId, LocalDate actualReturnDate) {
        RentalContract contract = rentalContractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if (!RentalContractStatus.PENDING_PICKUP.equals(contract.getContractStatus())) {
            throw new IllegalStateException("Contract must be in PENDING_PICKUP status");
        }

        contract.transitionToCompleted(actualReturnDate);
        contract.getDevice().setDeviceStatus(DeviceStatus.AVAILABLE);

        // Update device usage hours
        if (contract.getDevice().getUsageHours() != null) {
            contract.getDevice().getUsageHours().updateUsage(contract.getActualRentalDurationDays());
        }

        RentalContract saved = rentalContractRepository.save(contract);
        deviceRepository.save(contract.getDevice());

        alertService.createAlert("RENTAL_COMPLETED", "INFO", "Rental Completed",
            String.format("Rental contract %s completed", contract.getContractNumber()),
            "RENTAL_CONTRACT", saved.getId());

        return saved;
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    public List<RentalContract> getAllActiveRentals() {
        return rentalContractRepository.findAllActiveRentals();
    }

    public List<RentalContract> getPendingDeployments() {
        return rentalContractRepository.findPendingDeployments();
    }

    public List<RentalContract> getOverdueRentals() {
        return rentalContractRepository.findOverdueRentals();
    }

    public List<RentalContract> getPendingPickups() {
        return rentalContractRepository.findPendingPickups();
    }

    public List<RentalContract> getClientRentals(Long clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return rentalContractRepository.findByClient(client);
    }

    public RentalContract getRentalContractById(Long id) {
        return rentalContractRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
    }

    public RentalContract getRentalContractByNumber(String contractNumber) {
        return rentalContractRepository.findByContractNumber(contractNumber)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
    }

    // =====================================================================
    // ANALYTICS & REPORTING
    // =====================================================================

    public Long getActiveRentalsCount() {
        return rentalContractRepository.countActiveRentals();
    }

    public Long getOverdueRentalsCount() {
        return rentalContractRepository.countOverdueRentals();
    }

    public List<RentalContract> getUpcomingReturnsInNext7Days() {
        return rentalContractRepository.findUpcomingReturnsInNext7Days(LocalDate.now().plusDays(7));
    }

    // =====================================================================
    // DTO CONVERSION
    // =====================================================================

    public RentalContractDTO toDTO(RentalContract contract) {
        RentalContractDTO dto = new RentalContractDTO();
        dto.setId(contract.getId());
        dto.setContractNumber(contract.getContractNumber());
        dto.setClientId(contract.getClient().getId());
        dto.setClientName(contract.getClient().getFullName());
        dto.setDeviceId(contract.getDevice().getId());
        dto.setDeviceSerialNumber(contract.getDevice().getSerialNumber());
        dto.setDeviceType(contract.getDevice().getDeviceType().toString());
        dto.setAssignedAgentId(contract.getAssignedAgent().getId());
        dto.setContractStatus(contract.getContractStatus());
        dto.setRentalStartDate(contract.getRentalStartDate());
        dto.setExpectedReturnDate(contract.getExpectedReturnDate());
        dto.setActualReturnDate(contract.getActualReturnDate());
        dto.setDeploymentDate(contract.getDeploymentDate());
        dto.setPickupDate(contract.getPickupDate());
        dto.setRentalDurationDays(contract.getRentalDurationDays());
        dto.setActualRentalDurationDays(contract.getActualRentalDurationDays());
        dto.setDailyRentalRate(contract.getDailyRentalRate());
        dto.setTotalRentalCost(contract.getTotalRentalCost());
        dto.setDepositAmount(contract.getDepositAmount());
        dto.setAdditionalFees(contract.getAdditionalFees());
        dto.setIsOverdue(contract.isOverdue());
        dto.setDaysOverdue(contract.getDaysOverdue());
        dto.setDaysUntilPickup(contract.getDaysUntilPickup());
        dto.setDeploymentNotes(contract.getDeploymentNotes());
        dto.setPickupNotes(contract.getPickupNotes());
        return dto;
    }

    // =====================================================================
    // HELPER METHODS
    // =====================================================================

    private String generateContractNumber() {
        // Format: RC-YYYY-NNNNNN
        String year = String.valueOf(java.time.Year.now().getValue());
        long count = rentalContractRepository.count() + 1;
        return String.format("RC-%s-%06d", year, count);
    }
}
