package com.o2medical.service;

import com.o2medical.domain.entities.Alert;
import com.o2medical.domain.entities.Invoice;
import com.o2medical.domain.entities.RentalContract;
import com.o2medical.domain.entities.Device;
import com.o2medical.domain.enums.RentalContractStatus;
import com.o2medical.domain.enums.PaymentStatus;
import com.o2medical.dto.AlertDTO;
import com.o2medical.repository.AlertRepository;
import com.o2medical.repository.InvoiceRepository;
import com.o2medical.repository.RentalContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private RentalContractRepository rentalContractRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // =====================================================================
    // ALERT CREATION
    // =====================================================================

    public Alert createAlert(String alertType, String severity, String title, String description,
                             String entityType, Long entityId) {
        Alert alert = new Alert();
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setRelatedEntityType(entityType);
        alert.setRelatedEntityId(entityId);
        alert.setIsResolved(false);
        return alertRepository.save(alert);
    }

    public void generateSystemAlerts() {
        // Clear old resolved alerts (optional)
        // Generate overdue pickup alerts
        checkOverduePickups();
        
        // Generate unpaid invoice alerts
        checkUnpaidInvoices();
        
        // Generate low stock alerts (for consumables)
        checkLowStock();
        
        // Generate maintenance due alerts
        checkMaintenanceDue();
    }

    private void checkOverduePickups() {
        List<RentalContract> overdueRentals = rentalContractRepository.findOverdueRentals();
        
        for (RentalContract contract : overdueRentals) {
            // Check if alert already exists
            List<Alert> existing = alertRepository.findByRelatedEntityTypeAndRelatedEntityId(
                "RENTAL_CONTRACT", contract.getId()
            );
            
            boolean alertExists = existing.stream()
                .anyMatch(a -> "OVERDUE_PICKUP".equals(a.getAlertType()) && !a.getIsResolved());
            
            if (!alertExists) {
                createAlert(
                    "OVERDUE_PICKUP",
                    "CRITICAL",
                    "Overdue Pickup",
                    String.format("Device %s from %s is %d days overdue for pickup",
                        contract.getDevice().getSerialNumber(),
                        contract.getClient().getFullName(),
                        contract.getDaysOverdue()
                    ),
                    "RENTAL_CONTRACT",
                    contract.getId()
                );
            }
        }
    }

    private void checkUnpaidInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueUnpaidInvoices();
        
        for (Invoice invoice : overdueInvoices) {
            List<Alert> existing = alertRepository.findByRelatedEntityTypeAndRelatedEntityId(
                "INVOICE", invoice.getId()
            );
            
            boolean alertExists = existing.stream()
                .anyMatch(a -> "UNPAID_INVOICE".equals(a.getAlertType()) && !a.getIsResolved());
            
            if (!alertExists) {
                createAlert(
                    "UNPAID_INVOICE",
                    "WARNING",
                    "Unpaid Invoice",
                    String.format("Invoice %s from %s is %d days overdue. Balance: €%.2f",
                        invoice.getInvoiceNumber(),
                        invoice.getClient().getFullName(),
                        invoice.getDaysOverdue(),
                        invoice.getBalanceDue()
                    ),
                    "INVOICE",
                    invoice.getId()
                );
            }
        }
    }

    private void checkLowStock() {
        // TODO: Implement low stock logic based on device quantity
    }

    private void checkMaintenanceDue() {
        // TODO: Query devices requiring maintenance from DeviceUsageHours
    }

    // =====================================================================
    // ALERT MANAGEMENT
    // =====================================================================

    public void resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.resolve();
        alertRepository.save(alert);
    }

    public List<Alert> getUnresolvedAlerts() {
        return alertRepository.findByIsResolvedFalse();
    }

    public List<Alert> getCriticalActiveAlerts() {
        return alertRepository.findCriticalActiveAlerts();
    }

    public Long getUnresolvedAlertsCount() {
        return alertRepository.countUnresolvedAlerts();
    }

    public Long getAlertCountByType(String alertType) {
        return alertRepository.countByAlertType(alertType);
    }

    // =====================================================================
    // DTO CONVERSION
    // =====================================================================

    public AlertDTO toDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setAlertType(alert.getAlertType());
        dto.setSeverity(alert.getSeverity());
        dto.setTitle(alert.getTitle());
        dto.setDescription(alert.getDescription());
        dto.setRelatedEntityType(alert.getRelatedEntityType());
        dto.setRelatedEntityId(alert.getRelatedEntityId());
        dto.setIsResolved(alert.getIsResolved());
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setCreatedAt(alert.getCreatedAt());
        return dto;
    }

    public List<AlertDTO> toDtoList(List<Alert> alerts) {
        return alerts.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
