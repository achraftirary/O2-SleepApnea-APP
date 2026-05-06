package com.o2medical.dto;

import com.o2medical.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Long id;
    private String invoiceNumber;
    private Long rentalContractId;
    private String contractNumber;
    private Long clientId;
    private String clientName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private Boolean isOverdue;
    private Integer daysOverdue;
    private String notes;
}
