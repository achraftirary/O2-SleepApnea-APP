package com.o2medical.repository;

import com.o2medical.domain.entities.DailyRevenueSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyRevenueSnapshotRepository extends JpaRepository<DailyRevenueSnapshot, Long> {
    Optional<DailyRevenueSnapshot> findBySnapshotDate(LocalDate date);
}
