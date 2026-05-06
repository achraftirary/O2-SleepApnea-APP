package com.o2medical.service;

import com.o2medical.domain.entities.Device;
import com.o2medical.domain.entities.DeviceUsageHours;
import com.o2medical.domain.enums.DeviceStatus;
import com.o2medical.domain.enums.DeviceType;
import com.o2medical.dto.DeviceDTO;
import com.o2medical.repository.DeviceRepository;
import com.o2medical.repository.DeviceUsageHoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceUsageHoursRepository usageHoursRepository;

    // =====================================================================
    // CREATE & UPDATE OPERATIONS
    // =====================================================================

    public Device registerDevice(DeviceType type, String serialNumber, String manufacturer,
                                 String model, BigDecimal purchasePrice) {
        if (deviceRepository.findBySerialNumber(serialNumber).isPresent()) {
            throw new IllegalArgumentException("Device with this serial number already exists");
        }

        Device device = new Device();
        device.setDeviceType(type);
        device.setSerialNumber(serialNumber);
        device.setManufacturer(manufacturer);
        device.setModel(model);
        device.setPurchasePrice(purchasePrice);
        device.setAcquisitionDate(LocalDate.now());
        device.setDeviceStatus(DeviceStatus.AVAILABLE);
        device.setIsConsumable(type == DeviceType.OXYGEN_MASK);

        Device saved = deviceRepository.save(device);

        // Create usage tracking
        if (!device.getIsConsumable()) {
            try {
                DeviceUsageHours usageHours = new DeviceUsageHours();
                usageHours.setDevice(saved);
                usageHoursRepository.save(usageHours);
            } catch (Exception ex) {
                // Device creation must remain successful even if usage tracking cannot be initialized.
                System.err.println("Failed to initialize usage tracking for device " + saved.getSerialNumber() + ": " + ex.getMessage());
            }
        }

        return saved;
    }

    public void decommissionDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setDeviceStatus(DeviceStatus.RETIRED);
        device.setDecommissionDate(LocalDate.now());
        deviceRepository.save(device);
    }

    public void flagMaintenanceRequired(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        DeviceUsageHours usage = usageHoursRepository.findByDevice(device)
            .orElseGet(() -> {
                DeviceUsageHours created = new DeviceUsageHours();
                created.setDevice(device);
                return created;
            });

        usage.setRequiresMaintenance(true);
        usageHoursRepository.save(usage);
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Device not found"));
    }

    public Device getDeviceBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> new IllegalArgumentException("Device not found"));
    }

    public List<Device> getAvailableDevices() {
        return deviceRepository.findAvailableDevices();
    }

    public List<Device> getDeployedDevices() {
        return deviceRepository.findDeployedDevices();
    }

    public List<Device> getDevicesByType(DeviceType type) {
        return deviceRepository.findByDeviceType(type);
    }

    public List<Device> getDevicesByStatus(DeviceStatus status) {
        return deviceRepository.findByDeviceStatus(status);
    }

    public List<Device> getConsumables() {
        return deviceRepository.findByIsConsumableTrue();
    }

    public List<Device> getDevicesRequiringMaintenance() {
        return usageHoursRepository.findDevicesRequiringMaintenance().stream()
            .peek(usageHours -> usageHours.getDevice().setUsageHours(usageHours))
            .map(DeviceUsageHours::getDevice)
            .collect(Collectors.toList());
    }

    public List<Device> getActiveDevices() {
        return deviceRepository.findByDecommissionDateIsNull();
    }

    // =====================================================================
    // INVENTORY ANALYTICS
    // =====================================================================

    public Long getAvailableDevicesCount() {
        return countByStatus(DeviceStatus.AVAILABLE);
    }

    public Long getDeployedDevicesCount() {
        return countByStatus(DeviceStatus.DEPLOYED);
    }

    public Long getMaintenanceDevicesCount() {
        return countByStatus(DeviceStatus.IN_MAINTENANCE);
    }

    public Long countByTypeAndStatus(DeviceType type, DeviceStatus status) {
        return deviceRepository.findAll().stream()
            .filter(device -> type == null || device.getDeviceType() == type)
            .filter(device -> status == null || device.getDeviceStatus() == status)
            .count();
    }

    public List<Device> getLowStockConsumables() {
        return deviceRepository.findByIsConsumableTrue().stream()
            .filter(device -> device.getQuantityInStock() < 5)
            .collect(Collectors.toList());
    }

    private Long countByStatus(DeviceStatus status) {
        return deviceRepository.findAll().stream()
            .filter(device -> device.getDeviceStatus() == status)
            .count();
    }

    // =====================================================================
    // DTO CONVERSION
    // =====================================================================

    public DeviceDTO toDTO(Device device) {
        if (device.getUsageHours() == null) {
            usageHoursRepository.findByDevice(device).ifPresent(device::setUsageHours);
        }

        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setDeviceType(device.getDeviceType());
        dto.setSerialNumber(device.getSerialNumber());
        dto.setManufacturer(device.getManufacturer());
        dto.setModel(device.getModel());
        dto.setDeviceStatus(device.getDeviceStatus());
        dto.setPurchaseDate(device.getPurchaseDate());
        dto.setPurchasePrice(device.getPurchasePrice());
        dto.setAcquisitionDate(device.getAcquisitionDate());
        dto.setDecommissionDate(device.getDecommissionDate());
        dto.setQuantityInStock(device.getQuantityInStock());
        dto.setIsConsumable(device.getIsConsumable());
        dto.setNotes(device.getNotes());
        dto.setDisplayName(device.getDisplayName());

        if (device.getUsageHours() != null) {
            dto.setTotalRentalHours(device.getUsageHours().getTotalRentalHours());
            dto.setTotalRentalDays(device.getUsageHours().getTotalRentalDays());
            dto.setRequiresMaintenance(device.getUsageHours().getRequiresMaintenance());
            dto.setDaysUntilMaintenance(device.getUsageHours().getDaysUntilMaintenance());
        }

        return dto;
    }

    public List<DeviceDTO> toDtoList(List<Device> devices) {
        return devices.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
