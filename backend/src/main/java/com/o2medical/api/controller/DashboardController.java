package com.o2medical.api.controller;

import com.o2medical.dto.DashboardDTO;
import com.o2medical.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Agent dashboard with KPIs and urgent alerts")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/agent")
    @Operation(summary = "Get Agent dashboard with all KPIs and urgent items")
    public ResponseEntity<DashboardDTO> getAgentDashboard() {
        DashboardDTO dashboard = dashboardService.getAgentDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/generate-daily-snapshot")
    @Operation(summary = "Generate daily revenue snapshot (scheduled job)")
    public ResponseEntity<Void> generateDailySnapshot() {
        dashboardService.generateDailySnapshot();
        return ResponseEntity.noContent().build();
    }
}
