package com.sanket.gstbilling_backend.controller;
 // Ensure this package matches your project structure

import com.sanket.gstbilling_backend.model.Product;
import com.sanket.gstbilling_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Product entities.
 * Provides API endpoints for CRUD operations on products.
 */
@RestController // Marks this class as a REST controller
@RequestMapping("/api/products") // Base path for all product-related endpoints
@CrossOrigin(origins = "http://localhost:8080") // Allow requests from your frontend (adjust if frontend runs on different port/domain)
public class ProductController {

    @Autowired // Injects the ProductRepository dependency
    private ProductRepository productRepository;

    /**
     * Get all products.
     * GET /api/products
     * @return A list of all Product objects.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Get a product by ID.
     * GET /api/products/{id}
     * @param id The ID of the product to retrieve.
     * @return The Product object if found (200 OK), or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Add a new product.
     * POST /api/products
     * @param product The Product object to add (sent in request body).
     * @return The saved Product object with its generated ID (201 Created).
     */
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        // Ensure ID is null for new entities, so JPA generates it
        product.setId(null);
        Product savedProduct = productRepository.save(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    /**
     * Update an existing product.
     * PUT /api/products/{id}
     * @param id The ID of the product to update.
     * @param productDetails The updated Product object details (sent in request body).
     * @return The updated Product object (200 OK), or 404 Not Found if ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isPresent()) {
            Product existingProduct = productOptional.get();
            existingProduct.setName(productDetails.getName());
            existingProduct.setHsnSac(productDetails.getHsnSac());
            existingProduct.setUnitPrice(productDetails.getUnitPrice());
            existingProduct.setGstRate(productDetails.getGstRate());
            existingProduct.setDescription(productDetails.getDescription());
            Product updatedProduct = productRepository.save(existingProduct);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a product by ID.
     * DELETE /api/products/{id}
     * @param id The ID of the product to delete.
     * @return 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteProduct(@PathVariable Long id) {
        try {
            productRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (Exception e) {
            // Handle case where ID might not exist or other DB issues
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Or INTERNAL_SERVER_ERROR
        }
    }
}
