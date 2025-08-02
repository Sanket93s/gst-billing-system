package com.sanket.gstbilling_backend.repository;
// Ensure this package matches your project structure

import com.sanket.gstbilling_backend.model.Invoice; // Import the Invoice entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Invoice entities.
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository // Marks this interface as a Spring repository component
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Custom query method: find an invoice by its unique invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}

