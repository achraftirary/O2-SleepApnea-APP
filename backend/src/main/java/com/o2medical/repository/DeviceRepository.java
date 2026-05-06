package com.o2medical.repository;

import com.o2medical.domain.entities.Device;
import com.o2medical.domain.enums.DeviceStatus;
import com.o2medical.domain.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findBySerialNumber(String serialNumber);
    List<Device> findByDeviceStatus(DeviceStatus status);
    List<Device> findByDeviceType(DeviceType type);
    List<Device> findByDeviceTypeAndDeviceStatus(DeviceType type, DeviceStatus status);
    List<Device> findByIsConsumableTrue();
    List<Device> findByIsConsumableFalse();
    
    @Query("SELECT d FROM Device d WHERE d.deviceStatus = 'AVAILABLE' AND d.isConsumable = false")
    List<Device> findAvailableDevices();

    @Query("SELECT d FROM Device d WHERE d.deviceStatus = 'DEPLOYED' AND d.isConsumable = false")
    List<Device> findDeployedDevices();

    @Query("SELECT COUNT(d) FROM Device d WHERE d.deviceType = :deviceType AND d.deviceStatus = :status")
    Long countByTypeAndStatus(@Param("deviceType") DeviceType type, @Param("status") DeviceStatus status);

    List<Device> findByDecommissionDateIsNull();
}
