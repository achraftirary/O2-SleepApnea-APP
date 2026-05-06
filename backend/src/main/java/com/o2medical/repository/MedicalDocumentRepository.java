package com.o2medical.repository;

import com.o2medical.domain.entities.Client;
import com.o2medical.domain.entities.MedicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    List<MedicalDocument> findByClient(Client client);
    List<MedicalDocument> findByDocumentType(String documentType);
    List<MedicalDocument> findByClientAndDocumentType(Client client, String documentType);
}
