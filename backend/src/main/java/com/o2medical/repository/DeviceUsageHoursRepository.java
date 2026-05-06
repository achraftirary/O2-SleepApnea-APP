package com.o2medical.repository;

import com.o2medical.domain.entities.Device;
import com.o2medical.domain.entities.DeviceUsageHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceUsageHoursRepository extends JpaRepository<DeviceUsageHours, Long> {
    Optional<DeviceUsageHours> findByDevice(Device device);
    
    @Query("SELECT d FROM DeviceUsageHours d WHERE d.requiresMaintenance = true")
    List<DeviceUsageHours> findDevicesRequiringMaintenance();
}
