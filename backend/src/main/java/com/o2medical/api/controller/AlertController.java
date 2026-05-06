package com.o2medical.api.controller;

import com.o2medical.dto.AlertDTO;
import com.o2medical.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@Tag(name = "Alerts & Notifications", description = "System alerts for overdue pickups, unpaid invoices, low stock, etc.")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping("/unresolved")
    @Operation(summary = "Get all unresolved alerts")
    public ResponseEntity<List<AlertDTO>> getUnresolvedAlerts() {
        var alerts = alertService.getUnresolvedAlerts();
        return ResponseEntity.ok(alertService.toDtoList(alerts));
    }

    @GetMapping("/critical")
    @Operation(summary = "Get critical and warning alerts")
    public ResponseEntity<List<AlertDTO>> getCriticalAlerts() {
        var alerts = alertService.getCriticalActiveAlerts();
        return ResponseEntity.ok(alertService.toDtoList(alerts));
    }

    @PostMapping("/generate-system-alerts")
    @Operation(summary = "Generate system alerts (scheduled job)")
    public ResponseEntity<Void> generateSystemAlerts() {
        alertService.generateSystemAlerts();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{alertId}/resolve")
    @Operation(summary = "Mark alert as resolved")
    public ResponseEntity<Void> resolveAlert(@PathVariable Long alertId) {
        alertService.resolveAlert(alertId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/unresolved-count")
    @Operation(summary = "Count of unresolved alerts")
    public ResponseEntity<Long> getUnresolvedAlertsCount() {
        return ResponseEntity.ok(alertService.getUnresolvedAlertsCount());
    }

    @GetMapping("/stats/by-type/{alertType}")
    @Operation(summary = "Count alerts by type")
    public ResponseEntity<Long> getAlertCountByType(@PathVariable String alertType) {
        return ResponseEntity.ok(alertService.getAlertCountByType(alertType));
    }
}
