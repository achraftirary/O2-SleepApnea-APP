package com.o2medical.repository;

import com.o2medical.domain.entities.MonthlyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MonthlyMetricsRepository extends JpaRepository<MonthlyMetrics, Long> {
    Optional<MonthlyMetrics> findByMetricMonth(LocalDate month);
}
