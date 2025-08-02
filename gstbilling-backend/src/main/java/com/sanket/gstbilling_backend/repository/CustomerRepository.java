package com.sanket.gstbilling_backend.repository; // Ensure this package matches your project structure

import com.sanket.gstbilling_backend.model.Customer; // Import the Customer entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List; // Added for findByGstRate example, though not used for Customer

/**
 * Spring Data JPA Repository for Customer entities.
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository // Marks this interface as a Spring repository component
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Spring Data JPA automatically provides methods like:
    // save(Customer entity)
    // findById(Long id)
    // findAll()
    // deleteById(Long id)

    // You can define custom query methods by following Spring Data JPA naming conventions
    Optional<Customer> findByName(String name); // Example: find a customer by their name
    Optional<Customer> findByGstin(String gstin); // Example: find a customer by their GSTIN
}

