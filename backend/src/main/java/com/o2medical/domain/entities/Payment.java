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
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, length = 50)
    private String paymentMethod; // e.g., CASH, BANK_TRANSFER, CHECK

    @Column(length = 100)
    private String transactionReference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedByUser;

    public String getPaymentSummary() {
        return String.format("%s - %s (€%.2f on %s)",
            this.invoice.getInvoiceNumber(),
            this.paymentMethod,
            this.paymentAmount,
            this.paymentDate
        );
    }
}
