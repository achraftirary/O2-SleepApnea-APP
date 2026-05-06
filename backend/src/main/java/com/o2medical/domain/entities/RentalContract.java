package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import com.o2medical.domain.enums.RentalContractStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"invoices"})
public class RentalContract extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String contractNumber; // e.g., RC-2024-001

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne
    @JoinColumn(name = "assigned_agent_id", nullable = false)
    private User assignedAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalContractStatus contractStatus = RentalContractStatus.PENDING_DEPLOYMENT;

    // Core dates
    @Column(nullable = false)
    private LocalDate rentalStartDate;

    @Column(nullable = false)
    private LocalDate expectedReturnDate;

    @Column
    private LocalDate actualReturnDate;

    // Field operations
    @Column
    private LocalDate deploymentDate;

    @Column(columnDefinition = "TEXT")
    private String deploymentNotes;

    @Column
    private LocalDate pickupDate;

    @Column(columnDefinition = "TEXT")
    private String pickupNotes;

    // Duration tracking
    @Column
    private Integer rentalDurationDays;

    @Column
    private Integer actualRentalDurationDays;

    // Financial tracking
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRentalRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalRentalCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal additionalFees = BigDecimal.ZERO;

    @OneToMany(mappedBy = "rentalContract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();

    // =====================================================================
    // Business Logic Methods
    // =====================================================================

    public void calculateRentalDuration() {
        if (this.rentalStartDate != null && this.expectedReturnDate != null) {
            this.rentalDurationDays = Math.toIntExact(
                ChronoUnit.DAYS.between(this.rentalStartDate, this.expectedReturnDate)
            );
        }
    }

    public void calculateActualRentalDuration() {
        if (this.deploymentDate != null && this.pickupDate != null) {
            this.actualRentalDurationDays = Math.toIntExact(
                ChronoUnit.DAYS.between(this.deploymentDate, this.pickupDate)
            );
        }
    }

    public void calculateTotalRentalCost() {
        if (this.rentalDurationDays != null && this.dailyRentalRate != null) {
            this.totalRentalCost = this.dailyRentalRate
                .multiply(BigDecimal.valueOf(this.rentalDurationDays))
                .add(this.additionalFees != null ? this.additionalFees : BigDecimal.ZERO);
        }
    }

    public Boolean isOverdue() {
        if (RentalContractStatus.ACTIVE_RENTAL.equals(this.contractStatus)) {
            return LocalDate.now().isAfter(this.expectedReturnDate);
        }
        return false;
    }

    public Integer getDaysOverdue() {
        if (isOverdue()) {
            return Math.toIntExact(ChronoUnit.DAYS.between(this.expectedReturnDate, LocalDate.now()));
        }
        return 0;
    }

    public Integer getDaysUntilPickup() {
        if (RentalContractStatus.ACTIVE_RENTAL.equals(this.contractStatus)) {
            return Math.toIntExact(ChronoUnit.DAYS.between(LocalDate.now(), this.expectedReturnDate));
        }
        return null;
    }

    public void transitionToActive(LocalDate deploymentDate) {
        if (RentalContractStatus.PENDING_DEPLOYMENT.equals(this.contractStatus)) {
            this.contractStatus = RentalContractStatus.ACTIVE_RENTAL;
            this.deploymentDate = deploymentDate;
        }
    }

    public void transitionToPendingPickup(LocalDate pickupDate) {
        if (RentalContractStatus.ACTIVE_RENTAL.equals(this.contractStatus)) {
            this.contractStatus = RentalContractStatus.PENDING_PICKUP;
            this.pickupDate = pickupDate;
        }
    }

    public void transitionToCompleted(LocalDate actualReturnDate) {
        if (RentalContractStatus.PENDING_PICKUP.equals(this.contractStatus)) {
            this.contractStatus = RentalContractStatus.COMPLETED;
            this.actualReturnDate = actualReturnDate;
            this.calculateActualRentalDuration();
        }
    }

    public String getContractSummary() {
        return String.format("%s - %s (%s to %s)",
            this.contractNumber,
            this.client.getFullName(),
            this.rentalStartDate,
            this.expectedReturnDate
        );
    }
}
