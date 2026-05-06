package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import com.o2medical.domain.enums.DeviceStatus;
import com.o2medical.domain.enums.DeviceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Device extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Column(unique = true, nullable = false, length = 100)
    private String serialNumber;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus deviceStatus = DeviceStatus.AVAILABLE;

    @Column
    private LocalDate purchaseDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private LocalDate acquisitionDate;

    @Column
    private LocalDate decommissionDate;

    @Column
    private Integer quantityInStock = 1;

    @Column(nullable = false)
    private Boolean isConsumable = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private DeviceUsageHours usageHours;

    public Boolean isAvailable() {
        return DeviceStatus.AVAILABLE.equals(this.deviceStatus);
    }

    public Boolean isDeployed() {
        return DeviceStatus.DEPLOYED.equals(this.deviceStatus);
    }

    public String getDisplayName() {
        return deviceType + " - " + serialNumber;
    }
}
