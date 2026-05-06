package com.o2medical.api.controller;

import com.o2medical.dto.InvoiceDTO;
import com.o2medical.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices & Payments", description = "Manage invoices and payment tracking")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    // =====================================================================
    // CREATE OPERATIONS
    // =====================================================================

    @PostMapping("/from-contract/{contractId}")
    @Operation(summary = "Create invoice from rental contract")
    public ResponseEntity<InvoiceDTO> createInvoiceFromContract(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "30") Integer paymentTermsDays) {

        var invoice = invoiceService.createInvoiceFromRentalContract(contractId, paymentTermsDays);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.toDTO(invoice));
    }

    // =====================================================================
    // PAYMENT OPERATIONS
    // =====================================================================

    @PostMapping("/{invoiceId}/payments")
    @Operation(summary = "Record payment against invoice")
    public ResponseEntity<Void> recordPayment(
            @PathVariable Long invoiceId,
            @RequestParam BigDecimal amount,
            @RequestParam String method,
            @RequestParam Long recordedByUserId,
            @RequestParam(required = false) String transactionReference) {

        invoiceService.recordPayment(invoiceId, amount, method, recordedByUserId, transactionReference);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        var invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoiceService.toDTO(invoice));
    }

    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Get invoice by invoice number")
    public ResponseEntity<InvoiceDTO> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        var invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(invoiceService.toDTO(invoice));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all invoices for a client")
    public ResponseEntity<List<InvoiceDTO>> getClientInvoices(@PathVariable Long clientId) {
        var invoices = invoiceService.getClientInvoices(clientId);
        return ResponseEntity.ok(invoiceService.toDtoList(invoices));
    }

    @GetMapping("/unpaid")
    @Operation(summary = "Get all unpaid or partially paid invoices")
    public ResponseEntity<List<InvoiceDTO>> getUnpaidInvoices() {
        var invoices = invoiceService.getUnpaidInvoices();
        return ResponseEntity.ok(invoiceService.toDtoList(invoices));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue invoices")
    public ResponseEntity<List<InvoiceDTO>> getOverdueInvoices() {
        var invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(invoiceService.toDtoList(invoices));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get invoices by date range")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        var invoices = invoiceService.getInvoicesByDateRange(startDate, endDate);
        return ResponseEntity.ok(invoiceService.toDtoList(invoices));
    }

    // =====================================================================
    // FINANCIAL REPORTING
    // =====================================================================

    @GetMapping("/stats/total-unpaid")
    @Operation(summary = "Get total unpaid invoice amount")
    public ResponseEntity<BigDecimal> getTotalUnpaidAmount() {
        return ResponseEntity.ok(invoiceService.getTotalUnpaidAmount());
    }

    @GetMapping("/stats/total-paid")
    @Operation(summary = "Get total paid in date range")
    public ResponseEntity<BigDecimal> getTotalPaidInRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(invoiceService.getTotalPaidInRange(startDate, endDate));
    }

    @GetMapping("/stats/overdue-count")
    @Operation(summary = "Count of overdue invoices")
    public ResponseEntity<Long> getOverdueInvoicesCount() {
        return ResponseEntity.ok(invoiceService.getOverdueInvoicesCount());
    }
}
