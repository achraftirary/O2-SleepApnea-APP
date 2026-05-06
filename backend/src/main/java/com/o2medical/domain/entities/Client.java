package com.o2medical.domain.entities;

import com.o2medical.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"rentalContracts", "medicalDocuments"})
public class Client extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column
    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    @Column(unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String streetAddress;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country = "France";

    @ManyToOne
    @JoinColumn(name = "assigned_doctor_id")
    private User assignedDoctor;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<RentalContract> rentalContracts = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalDocument> medicalDocuments = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFullAddress() {
        return streetAddress + ", " + postalCode + " " + city + ", " + country;
    }

    public void addRentalContract(RentalContract contract) {
        rentalContracts.add(contract);
        contract.setClient(this);
    }

    public void addMedicalDocument(MedicalDocument document) {
        medicalDocuments.add(document);
        document.setClient(this);
    }
}
