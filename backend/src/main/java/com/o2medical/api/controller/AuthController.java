package com.o2medical.api.controller;

import com.o2medical.domain.entities.User;
import com.o2medical.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validate credentials against database with password hashing
            boolean isValidUser = userService.validateCredentials(request.getUsername(), request.getPassword());
            
            if (!isValidUser) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid username or password"));
            }

            Optional<User> userOpt = userService.findByUsername(request.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid username or password"));
            }

            User foundUser = userOpt.get();

            if (!foundUser.getIsActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User account is inactive"));
            }

            // Create response with user info
            Map<String, Object> response = new HashMap<>();
            response.put("token", "token_" + foundUser.getId() + "_" + System.currentTimeMillis());
            response.put("username", foundUser.getUsername());
            response.put("firstName", foundUser.getFirstName());
            response.put("lastName", foundUser.getLastName());
            response.put("role", foundUser.getRole().toString());
            response.put("id", foundUser.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login error: " + e.getMessage()));
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
