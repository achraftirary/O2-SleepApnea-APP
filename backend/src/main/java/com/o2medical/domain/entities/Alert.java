package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Alert extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String alertType; // e.g., OVERDUE_PICKUP, UNPAID_INVOICE, LOW_STOCK, MAINTENANCE_DUE

    @Column(nullable = false, length = 50)
    private String severity; // INFO, WARNING, CRITICAL

    @Column(length = 50)
    private String relatedEntityType; // RENTAL_CONTRACT, INVOICE, DEVICE, CLIENT

    @Column
    private Long relatedEntityId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isResolved = false;

    @Column
    private LocalDateTime resolvedAt;

    public void resolve() {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
    }

    public String getAlertSummary() {
        return String.format("[%s] %s - %s", this.severity, this.alertType, this.title);
    }
}
