package com.o2medical.repository;

import com.o2medical.domain.entities.Client;
import com.o2medical.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByPhone(String phone);
    Optional<Client> findByEmail(String email);
    List<Client> findByIsActiveTrue();
    List<Client> findByAssignedDoctor(User doctor);
    
    @Query("SELECT c FROM Client c WHERE CONCAT(c.firstName, ' ', c.lastName) LIKE %:name%")
    List<Client> findByNameContaining(@Param("name") String name);

    @Query("SELECT c FROM Client c WHERE c.isActive = true AND c.assignedDoctor = :doctor")
    List<Client> findActiveClientsByDoctor(@Param("doctor") User doctor);

    @Query("SELECT c FROM Client c WHERE c.isActive = false")
    List<Client> findInactiveClients();

    Boolean existsByPhone(String phone);
    Boolean existsByEmail(String email);
}
