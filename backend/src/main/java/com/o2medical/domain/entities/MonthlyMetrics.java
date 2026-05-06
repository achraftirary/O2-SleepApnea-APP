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
@Table(name = "monthly_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MonthlyMetrics extends BaseEntity {

    @Column(nullable = false)
    private LocalDate metricMonth; // First day of the month

    @Column(nullable = false)
    private Integer totalContracts = 0;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal averageContractValue;

    @Column(precision = 5, scale = 2)
    private BigDecimal deviceUtilizationRate; // percentage

    public BigDecimal getCollectionRate() {
        if (this.totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.totalPaid
            .divide(this.totalRevenue, 2, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getOutstandingRevenue() {
        return this.totalRevenue.subtract(this.totalPaid);
    }
}
