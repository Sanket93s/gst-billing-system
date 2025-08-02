package com.sanket.gstbilling_backend.controller;
 // Ensure this package matches your project structure

import com.sanket.gstbilling_backend.model.Customer;
import com.sanket.gstbilling_backend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Customer entities.
 * Provides API endpoints for CRUD operations on customers.
 */
@RestController // Marks this class as a REST controller
@RequestMapping("/api/customers") // Base path for all customer-related endpoints
@CrossOrigin(origins = "http://localhost:8080") // Allow requests from your frontend (adjust if frontend runs on different port/domain)
public class CustomerController {

    @Autowired // Injects the CustomerRepository dependency
    private CustomerRepository customerRepository;

    /**
     * Get all customers.
     * GET /api/customers
     * @return A list of all Customer objects.
     */
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    /**
     * Get a customer by ID.
     * GET /api/customers/{id}
     * @param id The ID of the customer to retrieve.
     * @return The Customer object if found (200 OK), or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Add a new customer.
     * POST /api/customers
     * @param customer The Customer object to add (sent in request body).
     * @return The saved Customer object with its generated ID (201 Created).
     */
    @PostMapping
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer customer) {
        // Ensure ID is null for new entities, so JPA generates it
        customer.setId(null);
        Customer savedCustomer = customerRepository.save(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }

    /**
     * Update an existing customer.
     * PUT /api/customers/{id}
     * @param id The ID of the customer to update.
     * @param customerDetails The updated Customer object details (sent in request body).
     * @return The updated Customer object (200 OK), or 404 Not Found if ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isPresent()) {
            Customer existingCustomer = customerOptional.get();
            existingCustomer.setName(customerDetails.getName());
            existingCustomer.setContactNo(customerDetails.getContactNo());
            existingCustomer.setEmail(customerDetails.getEmail());
            existingCustomer.setAddress(customerDetails.getAddress());
            existingCustomer.setGstin(customerDetails.getGstin());
            Customer updatedCustomer = customerRepository.save(existingCustomer);
            return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a customer by ID.
     * DELETE /api/customers/{id}
     * @param id The ID of the customer to delete.
     * @return 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCustomer(@PathVariable Long id) {
        try {
            customerRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (Exception e) {
            // Handle case where ID might not exist or other DB issues
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Or INTERNAL_SERVER_ERROR
        }
    }
}
