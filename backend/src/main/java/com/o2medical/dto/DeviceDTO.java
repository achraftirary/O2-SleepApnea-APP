package com.o2medical.dto;

import com.o2medical.domain.enums.DeviceStatus;
import com.o2medical.domain.enums.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {
    private Long id;
    private DeviceType deviceType;
    private String serialNumber;
    private String manufacturer;
    private String model;
    private DeviceStatus deviceStatus;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private LocalDate acquisitionDate;
    private LocalDate decommissionDate;
    private Integer quantityInStock;
    private Boolean isConsumable;
    private String notes;
    private String displayName;
    private Integer totalRentalHours;
    private Integer totalRentalDays;
    private Boolean requiresMaintenance;
    private Integer daysUntilMaintenance;
}
