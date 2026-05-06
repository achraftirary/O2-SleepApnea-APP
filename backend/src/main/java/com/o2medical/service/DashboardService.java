package com.o2medical.service;

import com.o2medical.domain.entities.*;
import com.o2medical.domain.enums.DeviceStatus;
import com.o2medical.dto.*;
import com.o2medical.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private RentalContractRepository rentalContractRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DailyRevenueSnapshotRepository revenueSnapshotRepository;

    @Autowired
    private RentalContractService rentalContractService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DeviceService deviceService;

    // =====================================================================
    // MAIN DASHBOARD ASSEMBLY
    // =====================================================================

    public DashboardDTO getAgentDashboard() {
        DashboardDTO dashboard = DashboardDTO.builder()
            // KPIs
            .totalActiveRentals(Math.toIntExact(rentalContractRepository.countActiveRentals()))
            .totalOverduePickups(Math.toIntExact(rentalContractRepository.countOverdueRentals()))
            .totalUnpaidInvoices(Math.toIntExact(invoiceRepository.countOverdueInvoices()))
            .totalUnpaidAmount(invoiceService.getTotalUnpaidAmount())
            .lowStockAlerts(Math.toIntExact(alertRepository.countByAlertType("LOW_STOCK")))

            // Financial metrics
            .dailyRevenueExpected(calculateDailyExpectedRevenue())
            .dailyRevenueCollected(getDailyRevenueCollected())
            .collectionRate(calculateCollectionRate())

            // Urgent items - PICKUPS TODAY
            .pickupsToday(getPickupsToday())
            
            // Urgent items - OVERDUE RENTALS
            .overdueRentals(rentalContractRepository.findOverdueRentals().stream()
                .map(rentalContractService::toDTO)
                .collect(Collectors.toList()))

            // Urgent items - OVERDUE INVOICES
            .overdueInvoices(invoiceRepository.findOverdueUnpaidInvoices().stream()
                .map(invoiceService::toDTO)
                .collect(Collectors.toList()))

            // Critical alerts
            .criticalAlerts(alertRepository.findCriticalActiveAlerts().stream()
                .map(alertService::toDTO)
                .collect(Collectors.toList()))

            // Device inventory
            .lowStockDevices(getLowStockDevices())
            .devicesNeedingMaintenance(getDevicesNeedingMaintenance())

            // Quick status
            .totalDevicesAvailable(Math.toIntExact(deviceRepository.findAll().stream()
                .filter(device -> device.getDeviceStatus() == DeviceStatus.AVAILABLE)
                .count()))
            .totalDevicesDeployed(Math.toIntExact(rentalContractRepository.countActiveRentals()))
            .totalDevicesInMaintenance(Math.toIntExact(deviceRepository.findAll().stream()
                .filter(device -> device.getDeviceStatus() == DeviceStatus.IN_MAINTENANCE)
                .count()))

            .build();

        return dashboard;
    }

    // =====================================================================
    // KPI CALCULATIONS
    // =====================================================================

    private BigDecimal calculateDailyExpectedRevenue() {
        List<RentalContract> activeRentals = rentalContractRepository.findAllActiveRentals();
        return activeRentals.stream()
            .map(RentalContract::getDailyRentalRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getDailyRevenueCollected() {
        LocalDate today = LocalDate.now();
        BigDecimal dailyPayments = invoiceService.getTotalPaidInRange(today, today);
        return dailyPayments;
    }

    private BigDecimal calculateCollectionRate() {
        BigDecimal expected = calculateDailyExpectedRevenue();
        BigDecimal collected = getDailyRevenueCollected();

        if (expected.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return collected.divide(expected, 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    // =====================================================================
    // URGENT ITEMS
    // =====================================================================

    private List<RentalContractDTO> getPickupsToday() {
        LocalDate today = LocalDate.now();
        return rentalContractRepository.findPendingPickups().stream()
            .filter(contract -> contract.getPickupDate() != null && contract.getPickupDate().equals(today))
            .map(rentalContractService::toDTO)
            .collect(Collectors.toList());
    }

    private List<DeviceDTO> getLowStockDevices() {
        // Get all consumable devices with low quantity
        return deviceRepository.findByIsConsumableTrue().stream()
            .filter(device -> device.getQuantityInStock() < 5) // Low stock threshold
            .map(deviceService::toDTO)
            .collect(Collectors.toList());
    }

    private List<DeviceDTO> getDevicesNeedingMaintenance() {
        // Query devices with requiresMaintenance = true
        List<Device> devicesNeedingMaintenance = deviceRepository.findByIsConsumableFalse();
        return devicesNeedingMaintenance.stream()
            .filter(device -> device.getUsageHours() != null && device.getUsageHours().getRequiresMaintenance())
            .map(deviceService::toDTO)
            .collect(Collectors.toList());
    }

    // =====================================================================
    // SNAPSHOT & METRICS
    // =====================================================================

    @Transactional
    public void generateDailySnapshot() {
        LocalDate today = LocalDate.now();
        
        DailyRevenueSnapshot snapshot = revenueSnapshotRepository.findBySnapshotDate(today)
            .orElse(new DailyRevenueSnapshot());

        snapshot.setSnapshotDate(today);
        snapshot.setTotalRentalsActive(Math.toIntExact(rentalContractRepository.countActiveRentals()));
        snapshot.setTotalRevenueExpected(calculateDailyExpectedRevenue());
        snapshot.setTotalRevenuePaid(getDailyRevenueCollected());
        snapshot.setTotalUnpaid(invoiceService.getTotalUnpaidAmount());
        snapshot.setTotalOverduePickups(Math.toIntExact(rentalContractRepository.countOverdueRentals()));
        snapshot.setLowStockAlerts(Math.toIntExact(alertRepository.countByAlertType("LOW_STOCK")));

        revenueSnapshotRepository.save(snapshot);
    }
}
