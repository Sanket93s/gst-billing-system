package com.sanket.gstbilling_backend.repository; // Ensure this package matches your project structure

import com.sanket.gstbilling_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Product entities.
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository // Marks this interface as a Spring repository component
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring Data JPA automatically provides methods like:
    // save(Product entity)
    // findById(Long id)
    // findAll()
    // deleteById(Long id)

    // You can define custom query methods by following Spring Data JPA naming conventions
    Optional<Product> findByName(String name); // Example: find a product by its name
    List<Product> findByGstRate(Double gstRate); // Example: find products by GST rate
}
