package com.sanket.gstbilling_backend.repository;
 // Ensure this package matches your project structure

import com.sanket.gstbilling_backend.model.InvoiceItem; // Import the InvoiceItem entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for InvoiceItem entities.
 * Provides automatic CRUD operations.
 */
@Repository // Marks this interface as a Spring repository component
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    // JpaRepository automatically provides methods like:
    // save(InvoiceItem entity)
    // findById(Long id)
    // findAll()
    // deleteById(Long id)
}
