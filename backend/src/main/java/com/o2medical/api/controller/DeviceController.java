package com.o2medical.api.controller;

import com.o2medical.domain.enums.DeviceStatus;
import com.o2medical.domain.enums.DeviceType;
import com.o2medical.dto.DeviceDTO;
import com.o2medical.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/devices")
@Tag(name = "Devices & Inventory", description = "Manage inventory of oxygen concentrators and sleep apnea devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    // =====================================================================
    // CREATE OPERATIONS
    // =====================================================================

    @PostMapping
    @Operation(summary = "Register new device")
    public ResponseEntity<DeviceDTO> registerDevice(
            @RequestParam DeviceType deviceType,
            @RequestParam String serialNumber,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal purchasePrice) {

        var device = deviceService.registerDevice(deviceType, serialNumber, manufacturer, model, purchasePrice);
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.toDTO(device));
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable Long id) {
        var device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(deviceService.toDTO(device));
    }

    @GetMapping("/serial/{serialNumber}")
    @Operation(summary = "Get device by serial number")
    public ResponseEntity<DeviceDTO> getDeviceBySerialNumber(@PathVariable String serialNumber) {
        var device = deviceService.getDeviceBySerialNumber(serialNumber);
        return ResponseEntity.ok(deviceService.toDTO(device));
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available devices")
    public ResponseEntity<List<DeviceDTO>> getAvailableDevices() {
        var devices = deviceService.getAvailableDevices();
        return ResponseEntity.ok(deviceService.toDtoList(devices));
    }

    @GetMapping("/deployed")
    @Operation(summary = "Get all deployed devices")
    public ResponseEntity<List<DeviceDTO>> getDeployedDevices() {
        var devices = deviceService.getDeployedDevices();
        return ResponseEntity.ok(deviceService.toDtoList(devices));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get devices by type")
    public ResponseEntity<List<DeviceDTO>> getDevicesByType(@PathVariable DeviceType type) {
        var devices = deviceService.getDevicesByType(type);
        return ResponseEntity.ok(deviceService.toDtoList(devices));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get devices by status")
    public ResponseEntity<List<DeviceDTO>> getDevicesByStatus(@PathVariable DeviceStatus status) {
        var devices = deviceService.getDevicesByStatus(status);
        return ResponseEntity.ok(deviceService.toDtoList(devices));
    }

    @GetMapping("/consumables")
    @Operation(summary = "Get consumable items (masks)")
    public ResponseEntity<List<DeviceDTO>> getConsumables() {
        var devices = deviceService.getConsumables();
        return ResponseEntity.ok(deviceService.toDtoList(devices));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get consumables with low stock (< 5 units)")
    public ResponseEntity<List<DeviceDTO>> getLowStockConsumables() {
        var devices = deviceService.getLowStockConsumables();
        return ResponseEntity.ok(deviceService.toDtoList(devices));
    }

    @GetMapping("/maintenance-required")
    @Operation(summary = "Get devices requiring maintenance")
    public ResponseEntity<List<DeviceDTO>> getDevicesRequiringMaintenance() {
        var devices = deviceService.getDevicesRequiringMaintenance();
        return ResponseEntity.ok(deviceService.toDtoList(devices));
    }

    // =====================================================================
    // UPDATE OPERATIONS
    // =====================================================================

    @PutMapping("/{id}/decommission")
    @Operation(summary = "Decommission device (retire from service)")
    public ResponseEntity<Void> decommissionDevice(@PathVariable Long id) {
        deviceService.decommissionDevice(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/flag-maintenance")
    @Operation(summary = "Flag device as requiring maintenance")
    public ResponseEntity<Void> flagMaintenanceRequired(@PathVariable Long id) {
        deviceService.flagMaintenanceRequired(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // ANALYTICS & INVENTORY STATS
    // =====================================================================

    @GetMapping("/stats/available-count")
    @Operation(summary = "Count of available devices")
    public ResponseEntity<Long> getAvailableDevicesCount() {
        return ResponseEntity.ok(deviceService.getAvailableDevicesCount());
    }

    @GetMapping("/stats/deployed-count")
    @Operation(summary = "Count of deployed devices")
    public ResponseEntity<Long> getDeployedDevicesCount() {
        return ResponseEntity.ok(deviceService.getDeployedDevicesCount());
    }

    @GetMapping("/stats/maintenance-count")
    @Operation(summary = "Count of devices in maintenance")
    public ResponseEntity<Long> getMaintenanceDevicesCount() {
        return ResponseEntity.ok(deviceService.getMaintenanceDevicesCount());
    }

    @GetMapping("/stats/by-type-and-status")
    @Operation(summary = "Count devices by type and status")
    public ResponseEntity<Long> countByTypeAndStatus(
            @RequestParam DeviceType type,
            @RequestParam DeviceStatus status) {
        return ResponseEntity.ok(deviceService.countByTypeAndStatus(type, status));
    }
}
