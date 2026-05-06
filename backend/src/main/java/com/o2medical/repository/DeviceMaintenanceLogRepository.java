package com.o2medical.repository;

import com.o2medical.domain.entities.Device;
import com.o2medical.domain.entities.DeviceMaintenanceLog;
import com.o2medical.domain.enums.MaintenancePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeviceMaintenanceLogRepository extends JpaRepository<DeviceMaintenanceLog, Long> {
    List<DeviceMaintenanceLog> findByDevice(Device device);
    List<DeviceMaintenanceLog> findByMaintenancePriority(MaintenancePriority priority);
    
    @Query("SELECT d FROM DeviceMaintenanceLog d WHERE d.endDate IS NULL")
    List<DeviceMaintenanceLog> findOngoingMaintenance();

    @Query("SELECT d FROM DeviceMaintenanceLog d WHERE d.startDate BETWEEN :startDate AND :endDate")
    List<DeviceMaintenanceLog> findByMaintenanceDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DeviceMaintenanceLog d WHERE d.maintenancePriority IN ('HIGH', 'CRITICAL') AND d.endDate IS NULL")
    List<DeviceMaintenanceLog> findCriticalOngoingMaintenance();
}
