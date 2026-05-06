package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import com.o2medical.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"payments"})
public class Invoice extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String invoiceNumber; // e.g., INV-2024-001

    @ManyToOne
    @JoinColumn(name = "rental_contract_id", nullable = false)
    private RentalContract rentalContract;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    // Amount breakdown
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal balanceDue;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    // =====================================================================
    // Business Logic Methods
    // =====================================================================

    public void updateBalance() {
        this.balanceDue = this.totalAmount.subtract(this.paidAmount);
        
        if (this.balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            this.paymentStatus = PaymentStatus.PAID;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.paymentStatus = PaymentStatus.PARTIAL;
        } else {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
    }

    public void recordPayment(BigDecimal amount) {
        this.paidAmount = this.paidAmount.add(amount);
        updateBalance();
    }

    public Boolean isOverdue() {
        return LocalDate.now().isAfter(this.dueDate) 
            && !PaymentStatus.PAID.equals(this.paymentStatus);
    }

    public Integer getDaysOverdue() {
        if (isOverdue()) {
            return Math.toIntExact(java.time.temporal.ChronoUnit.DAYS.between(this.dueDate, LocalDate.now()));
        }
        return 0;
    }

    public Boolean isPaid() {
        return PaymentStatus.PAID.equals(this.paymentStatus);
    }

    public Boolean isPartiallyPaid() {
        return PaymentStatus.PARTIAL.equals(this.paymentStatus);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setInvoice(this);
        recordPayment(payment.getPaymentAmount());
    }

    public BigDecimal getRemainingBalance() {
        return this.totalAmount.subtract(this.paidAmount);
    }
}
