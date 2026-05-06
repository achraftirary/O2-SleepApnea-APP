package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import com.o2medical.domain.enums.MaintenancePriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "device_maintenance_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeviceMaintenanceLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false, length = 100)
    private String maintenanceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenancePriority maintenancePriority = MaintenancePriority.MEDIUM;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(length = 100)
    private String technicianName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Boolean isCompleted() {
        return this.endDate != null;
    }

    public Integer getDurationDays() {
        if (isCompleted()) {
            return Math.toIntExact(java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate));
        }
        return Math.toIntExact(java.time.temporal.ChronoUnit.DAYS.between(startDate, LocalDate.now()));
    }

    public String getMaintenanceSummary() {
        return String.format("%s - %s (%s) [%s]",
            this.device.getSerialNumber(),
            this.maintenanceType,
            this.maintenancePriority,
            isCompleted() ? "Completed" : "In Progress"
        );
    }
}
