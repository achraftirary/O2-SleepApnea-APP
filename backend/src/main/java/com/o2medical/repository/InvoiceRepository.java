package com.o2medical.repository;

import com.o2medical.domain.entities.Invoice;
import com.o2medical.domain.entities.Client;
import com.o2medical.domain.entities.RentalContract;
import com.o2medical.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByClient(Client client);
    List<Invoice> findByRentalContract(RentalContract contract);
    List<Invoice> findByPaymentStatus(PaymentStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.paymentStatus IN ('UNPAID', 'PARTIAL')")
    List<Invoice> findUnpaidOrPartialInvoices();

    @Query("SELECT i FROM Invoice i WHERE i.paymentStatus = 'UNPAID' AND i.dueDate < CURRENT_DATE")
    List<Invoice> findOverdueUnpaidInvoices();

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < CURRENT_DATE AND i.paymentStatus != 'PAID'")
    List<Invoice> findAllOverdueInvoices();

    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByInvoiceDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.paymentStatus = 'UNPAID'")
    BigDecimal calculateTotalUnpaidAmount();

    @Query("SELECT SUM(i.paidAmount) FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPaidInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.paymentStatus = 'UNPAID' AND i.dueDate < CURRENT_DATE")
    Long countOverdueInvoices();
}
