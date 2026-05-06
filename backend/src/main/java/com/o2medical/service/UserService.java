package com.o2medical.service;

import com.o2medical.domain.entities.User;
import com.o2medical.domain.enums.UserRole;
import com.o2medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder;

    public UserService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Initialize default users if they don't exist
     */
    @PostConstruct
    public void initializeDefaultUsers() {
        // Check if Hamouda already exists
        if (!userRepository.existsByUsername("hamouda")) {
            User agent = new User();
            agent.setUsername("hamouda");
            agent.setEmail("hamouda.elamrani@o2medical.com");
            agent.setPasswordHash(passwordEncoder.encode("hamouda123")); // Encrypted: hamouda123
            agent.setFirstName("Hamouda");
            agent.setLastName("El Amrani");
            agent.setRole(UserRole.AGENT);
            agent.setIsActive(true);
            userRepository.save(agent);
            System.out.println("✓ Created user: hamouda (password: hamouda123)");
        }

        // Check if Ahmed Berrada already exists
        if (!userRepository.existsByUsername("ahmed")) {
            User doctor = new User();
            doctor.setUsername("ahmed");
            doctor.setEmail("ahmed.berrada@o2medical.com");
            doctor.setPasswordHash(passwordEncoder.encode("ahmed123")); // Encrypted: ahmed123
            doctor.setFirstName("Ahmed");
            doctor.setLastName("Berrada");
            doctor.setRole(UserRole.DOCTOR);
            doctor.setIsActive(true);
            userRepository.save(doctor);
            System.out.println("✓ Created user: ahmed (password: ahmed123)");
        }
    }

    /**
     * Validate user credentials
     */
    public boolean validateCredentials(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        if (!user.getIsActive()) {
            return false;
        }
        
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find all users by role
     */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }

    /**
     * Get all active users
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();
    }

    /**
     * Create a new user
     */
    public User createUser(String username, String email, String firstName, String lastName, UserRole role, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    /**
     * Update user password
     */
    public User updateUserPassword(Long id, String newRawPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPasswordHash(passwordEncoder.encode(newRawPassword));
            return userRepository.save(user);
        }
        return null;
    }

    /**
     * Update user
     */
    public User updateUser(Long id, String firstName, String lastName, UserRole role, Boolean isActive) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(role);
            user.setIsActive(isActive);
            return userRepository.save(user);
        }
        return null;
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            userRepository.save(user);
        }
    }
}
