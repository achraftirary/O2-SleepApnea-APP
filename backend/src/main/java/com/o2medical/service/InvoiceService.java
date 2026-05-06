package com.o2medical.service;

import com.o2medical.domain.entities.*;
import com.o2medical.domain.enums.PaymentStatus;
import com.o2medical.dto.InvoiceDTO;
import com.o2medical.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalContractRepository rentalContractRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertService alertService;

    // =====================================================================
    // INVOICE CREATION
    // =====================================================================

    public Invoice createInvoiceFromRentalContract(Long contractId, Integer paymentTermsDays) {
        RentalContract contract = rentalContractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Rental contract not found"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setRentalContract(contract);
        invoice.setClient(contract.getClient());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(paymentTermsDays));

        // Calculate totals based on rental contract
        invoice.setSubtotal(contract.getTotalRentalCost());
        invoice.setTaxAmount(calculateTax(contract.getTotalRentalCost()));
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(
            invoice.getSubtotal()
                .add(invoice.getTaxAmount())
                .subtract(invoice.getDiscountAmount())
        );
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.updateBalance();
        invoice.setPaymentStatus(PaymentStatus.UNPAID);

        Invoice saved = invoiceRepository.save(invoice);
        alertService.createAlert("INVOICE_CREATED", "INFO", "Invoice Created",
            String.format("Invoice %s created for %s (€%.2f)", invoice.getInvoiceNumber(), contract.getClient().getFullName(), invoice.getTotalAmount()),
            "INVOICE", saved.getId());

        return saved;
    }

    // =====================================================================
    // PAYMENT RECORDING
    // =====================================================================

    public Payment recordPayment(Long invoiceId, BigDecimal amount, String method,
                                 Long recordedByUserId, String transactionReference) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (amount.compareTo(invoice.getRemainingBalance()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining balance");
        }

        User recordedBy = userRepository.findById(recordedByUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(method);
        payment.setTransactionReference(transactionReference);
        payment.setRecordedByUser(recordedBy);

        Payment savedPayment = paymentRepository.save(payment);
        invoice.addPayment(savedPayment);
        invoiceRepository.save(invoice);

        alertService.createAlert("PAYMENT_RECORDED", "INFO", "Payment Recorded",
            String.format("Payment of €%.2f recorded for invoice %s", amount, invoice.getInvoiceNumber()),
            "INVOICE", invoice.getId());

        return savedPayment;
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
    }

    public List<Invoice> getClientInvoices(Long clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        return invoiceRepository.findByClient(client);
    }

    public List<Invoice> getUnpaidInvoices() {
        return invoiceRepository.findUnpaidOrPartialInvoices();
    }

    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findAllOverdueInvoices();
    }

    // =====================================================================
    // FINANCIAL REPORTING
    // =====================================================================

    public BigDecimal getTotalUnpaidAmount() {
        BigDecimal total = invoiceRepository.calculateTotalUnpaidAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalPaidInRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = invoiceRepository.calculateTotalPaidInRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Long getOverdueInvoicesCount() {
        return invoiceRepository.countOverdueInvoices();
    }

    public List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByInvoiceDateRange(startDate, endDate);
    }

    // =====================================================================
    // DTO CONVERSION
    // =====================================================================

    public InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setRentalContractId(invoice.getRentalContract().getId());
        dto.setContractNumber(invoice.getRentalContract().getContractNumber());
        dto.setClientId(invoice.getClient().getId());
        dto.setClientName(invoice.getClient().getFullName());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaymentStatus(invoice.getPaymentStatus());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setBalanceDue(invoice.getBalanceDue());
        dto.setIsOverdue(invoice.isOverdue());
        dto.setDaysOverdue(invoice.getDaysOverdue());
        dto.setNotes(invoice.getNotes());
        return dto;
    }

    public List<InvoiceDTO> toDtoList(List<Invoice> invoices) {
        return invoices.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // =====================================================================
    // HELPER METHODS
    // =====================================================================

    private BigDecimal calculateTax(BigDecimal amount) {
        // French VAT (TVA) - typically 20%
        return amount.multiply(BigDecimal.valueOf(0.20));
    }

    private String generateInvoiceNumber() {
        String year = String.valueOf(java.time.Year.now().getValue());
        long count = invoiceRepository.count() + 1;
        return String.format("INV-%s-%06d", year, count);
    }
}
