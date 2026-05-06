package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_usage_hours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeviceUsageHours extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    @Column(nullable = false)
    private Integer totalRentalHours = 0;

    @Column(nullable = false)
    private Integer totalRentalDays = 0;

    @Column(nullable = false)
    private Integer lastMaintenanceHours = 0;

    @Column(nullable = false)
    private Integer lastMaintenanceDays = 0;

    @Column(nullable = false)
    private Integer thresholdMaintenanceHours = 500;

    @Column(nullable = false)
    private Integer thresholdMaintenanceDays = 180;

    @Column(nullable = false)
    private Boolean requiresMaintenance = false;

    public void updateUsage(Integer rentalDays) {
        this.totalRentalDays += rentalDays;
        this.totalRentalHours += (rentalDays * 24);
        checkMaintenanceRequirement();
    }

    public void checkMaintenanceRequirement() {
        this.requiresMaintenance = 
            this.totalRentalHours >= this.thresholdMaintenanceHours ||
            this.totalRentalDays >= this.thresholdMaintenanceDays;
    }

    public Integer getDaysUntilMaintenance() {
        return Math.max(0, this.thresholdMaintenanceDays - this.totalRentalDays);
    }

    public Integer getHoursUntilMaintenance() {
        return Math.max(0, this.thresholdMaintenanceHours - this.totalRentalHours);
    }
}
