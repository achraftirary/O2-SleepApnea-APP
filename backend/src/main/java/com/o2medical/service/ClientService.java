package com.o2medical.service;

import com.o2medical.domain.entities.Client;
import com.o2medical.domain.entities.User;
import com.o2medical.dto.ClientDTO;
import com.o2medical.repository.ClientRepository;
import com.o2medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    // =====================================================================
    // CREATE & UPDATE OPERATIONS
    // =====================================================================

    public Client createClient(String firstName, String lastName, String phone, String email,
                               String streetAddress, String city, String postalCode, Long assignedDoctorId) {
        if (clientRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Client with this phone number already exists");
        }
        if (email != null && clientRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Client with this email already exists");
        }

        Client client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setPhone(phone);
        client.setEmail(email);
        client.setStreetAddress(streetAddress);
        client.setCity(city);
        client.setPostalCode(postalCode);
        client.setIsActive(true);

        if (assignedDoctorId != null) {
            User doctor = userRepository.findById(assignedDoctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
            client.setAssignedDoctor(doctor);
        }

        return clientRepository.save(client);
    }

    public Client updateClient(Long id, ClientDTO dto) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (dto.getFirstName() != null) client.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) client.setLastName(dto.getLastName());
        if (dto.getStreetAddress() != null) client.setStreetAddress(dto.getStreetAddress());
        if (dto.getCity() != null) client.setCity(dto.getCity());
        if (dto.getPostalCode() != null) client.setPostalCode(dto.getPostalCode());
        if (dto.getMedicalHistory() != null) client.setMedicalHistory(dto.getMedicalHistory());
        if (dto.getAllergies() != null) client.setAllergies(dto.getAllergies());
        if (dto.getIsActive() != null) client.setIsActive(dto.getIsActive());

        return clientRepository.save(client);
    }

    public void deactivateClient(Long id) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        client.setIsActive(false);
        clientRepository.save(client);
    }

    public void assignDoctor(Long clientId, Long doctorId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        
        User doctor = userRepository.findById(doctorId)
            .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        client.setAssignedDoctor(doctor);
        clientRepository.save(client);
    }

    // =====================================================================
    // READ OPERATIONS
    // =====================================================================

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }

    public Client getClientByPhone(String phone) {
        return clientRepository.findByPhone(phone)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }

    public Client getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));
    }

    public List<Client> getAllActiveClients() {
        return clientRepository.findByIsActiveTrue();
    }

    public List<Client> getClientsByDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
            .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        return clientRepository.findActiveClientsByDoctor(doctor);
    }

    public List<Client> searchClients(String name) {
        return clientRepository.findByNameContaining(name);
    }

    public List<Client> getInactiveClients() {
        return clientRepository.findInactiveClients();
    }

    // =====================================================================
    // ANALYTICS
    // =====================================================================

    public Long getTotalActiveClients() {
        return (long) clientRepository.findByIsActiveTrue().size();
    }

    public Long getTotalClientsByDoctor(Long doctorId) {
        return (long) getClientsByDoctor(doctorId).size();
    }

    // =====================================================================
    // DTO CONVERSION
    // =====================================================================

    public ClientDTO toDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setFullName(client.getFullName());
        dto.setDateOfBirth(client.getDateOfBirth());
        dto.setGender(client.getGender());
        dto.setPhone(client.getPhone());
        dto.setEmail(client.getEmail());
        dto.setStreetAddress(client.getStreetAddress());
        dto.setCity(client.getCity());
        dto.setPostalCode(client.getPostalCode());
        dto.setCountry(client.getCountry());
        dto.setFullAddress(client.getFullAddress());
        
        if (client.getAssignedDoctor() != null) {
            dto.setAssignedDoctorId(client.getAssignedDoctor().getId());
            dto.setAssignedDoctorName(client.getAssignedDoctor().getFullName());
        }

        dto.setMedicalHistory(client.getMedicalHistory());
        dto.setAllergies(client.getAllergies());
        dto.setIsActive(client.getIsActive());

        return dto;
    }

    public List<ClientDTO> toDtoList(List<Client> clients) {
        return clients.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
