package com.o2medical.repository;

import com.o2medical.domain.entities.User;
import com.o2medical.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    java.util.List<User> findByRole(UserRole role);
    java.util.List<User> findByRoleAndIsActiveTrue(UserRole role);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
