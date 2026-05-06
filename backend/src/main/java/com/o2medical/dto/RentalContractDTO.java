package com.o2medical.dto;

import com.o2medical.domain.enums.RentalContractStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalContractDTO {
    private Long id;
    private String contractNumber;
    private Long clientId;
    private String clientName;
    private Long deviceId;
    private String deviceSerialNumber;
    private String deviceType;
    private Long assignedAgentId;
    private RentalContractStatus contractStatus;
    private LocalDate rentalStartDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private LocalDate deploymentDate;
    private LocalDate pickupDate;
    private Integer rentalDurationDays;
    private Integer actualRentalDurationDays;
    private BigDecimal dailyRentalRate;
    private BigDecimal totalRentalCost;
    private BigDecimal depositAmount;
    private BigDecimal additionalFees;
    private Boolean isOverdue;
    private Integer daysOverdue;
    private Integer daysUntilPickup;
    private String deploymentNotes;
    private String pickupNotes;
}
