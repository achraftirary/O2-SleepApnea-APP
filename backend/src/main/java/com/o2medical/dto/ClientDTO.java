package com.o2medical.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String streetAddress;
    private String city;
    private String postalCode;
    private String country;
    private String fullAddress;
    private Long assignedDoctorId;
    private String assignedDoctorName;
    private String medicalHistory;
    private String allergies;
    private Boolean isActive;
}
