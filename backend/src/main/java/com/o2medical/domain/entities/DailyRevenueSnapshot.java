package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_revenue_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DailyRevenueSnapshot extends BaseEntity {

    @Column(unique = true, nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private Integer totalRentalsActive = 0;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenueExpected = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenuePaid = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalUnpaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer totalOverduePickups = 0;

    @Column(nullable = false)
    private Integer lowStockAlerts = 0;

    public BigDecimal getCollectionRate() {
        if (this.totalRevenueExpected.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.totalRevenuePaid
            .divide(this.totalRevenueExpected, 2, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
