package com.o2medical.repository;

import com.o2medical.domain.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByIsResolvedFalse();
    List<Alert> findByAlertType(String alertType);
    List<Alert> findBySeverity(String severity);
    
    @Query("SELECT a FROM Alert a WHERE a.isResolved = false ORDER BY a.severity DESC, a.createdAt DESC")
    List<Alert> findActiveAlertsOrderedBySeverity();

    @Query("SELECT a FROM Alert a WHERE a.isResolved = false AND a.severity IN ('WARNING', 'CRITICAL')")
    List<Alert> findCriticalActiveAlerts();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.isResolved = false")
    Long countUnresolvedAlerts();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.isResolved = false AND a.alertType = :alertType")
    Long countByAlertType(@Param("alertType") String alertType);

    List<Alert> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId);
}
