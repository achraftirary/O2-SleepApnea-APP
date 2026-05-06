package com.o2medical.repository;

import com.o2medical.domain.entities.RentalContract;
import com.o2medical.domain.entities.Client;
import com.o2medical.domain.entities.Device;
import com.o2medical.domain.enums.RentalContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
    Optional<RentalContract> findByContractNumber(String contractNumber);
    List<RentalContract> findByClient(Client client);
    List<RentalContract> findByDevice(Device device);
    List<RentalContract> findByContractStatus(RentalContractStatus status);
    
    @Query("SELECT rc FROM RentalContract rc WHERE rc.contractStatus = 'ACTIVE_RENTAL'")
    List<RentalContract> findAllActiveRentals();

    @Query("SELECT rc FROM RentalContract rc WHERE rc.contractStatus = 'PENDING_DEPLOYMENT'")
    List<RentalContract> findPendingDeployments();

    @Query("SELECT rc FROM RentalContract rc WHERE rc.contractStatus = 'ACTIVE_RENTAL' AND rc.expectedReturnDate < CURRENT_DATE")
    List<RentalContract> findOverdueRentals();

    @Query("SELECT rc FROM RentalContract rc WHERE rc.contractStatus = 'PENDING_PICKUP'")
    List<RentalContract> findPendingPickups();

    @Query("SELECT rc FROM RentalContract rc WHERE rc.rentalStartDate BETWEEN :startDate AND :endDate")
    List<RentalContract> findByRentalDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT rc FROM RentalContract rc WHERE rc.expectedReturnDate BETWEEN CURRENT_DATE AND :endDate")
    List<RentalContract> findUpcomingReturnsInNext7Days(@Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(rc) FROM RentalContract rc WHERE rc.contractStatus = 'ACTIVE_RENTAL'")
    Long countActiveRentals();

    @Query("SELECT COUNT(rc) FROM RentalContract rc WHERE rc.contractStatus = 'ACTIVE_RENTAL' AND rc.expectedReturnDate < CURRENT_DATE")
    Long countOverdueRentals();

    List<RentalContract> findByClientAndContractStatus(Client client, RentalContractStatus status);
}
