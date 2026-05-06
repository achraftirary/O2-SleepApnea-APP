package com.o2medical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    // KPIs
    private Integer totalActiveRentals;
    private Integer totalOverduePickups;
    private Integer totalUnpaidInvoices;
    private BigDecimal totalUnpaidAmount;
    private Integer lowStockAlerts;
    
    // Financial metrics
    private BigDecimal dailyRevenueExpected;
    private BigDecimal dailyRevenueCollected;
    private BigDecimal collectionRate; // percentage
    
    // Urgent items
    private List<RentalContractDTO> pickupsToday;
    private List<RentalContractDTO> overdueRentals;
    private List<InvoiceDTO> overdueInvoices;
    private List<AlertDTO> criticalAlerts;
    
    // Device inventory
    private List<DeviceDTO> lowStockDevices;
    private List<DeviceDTO> devicesNeedingMaintenance;
    
    // Quick status
    private Integer totalDevicesAvailable;
    private Integer totalDevicesDeployed;
    private Integer totalDevicesInMaintenance;
}
