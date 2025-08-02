package com.sanket.gstbilling_backend.controller;
// Ensure this package matches your project structure

import com.sanket.gstbilling_backend.model.Customer;
import com.sanket.gstbilling_backend.model.Invoice;
import com.sanket.gstbilling_backend.model.InvoiceItem;
import com.sanket.gstbilling_backend.model.Product;
import com.sanket.gstbilling_backend.repository.CustomerRepository;
import com.sanket.gstbilling_backend.repository.InvoiceRepository;
import com.sanket.gstbilling_backend.repository.ProductRepository;
import com.sanket.gstbilling_backend.service.PdfGenerationService; // Import the new service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // Import HttpHeaders
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional; // For transactional operations

import java.io.IOException; // Import IOException
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Invoice entities.
 * Provides API endpoints for CRUD operations on invoices,
 * including their associated invoice items, and handles calculations.
 */
@RestController // Marks this class as a REST controller
@RequestMapping("/api/invoices") // Base path for all invoice-related endpoints
@CrossOrigin(origins = "http://localhost:8080") // Allow requests from your frontend
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository; // To fetch customer details

    @Autowired
    private ProductRepository productRepository; // To fetch product details

    @Autowired
    private PdfGenerationService pdfGenerationService; // Autowire the PDF service

    /**
     * Get all invoices.
     * GET /api/invoices
     * @return A list of all Invoice objects.
     */
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return new ResponseEntity<>(invoiceRepository.findAll(), HttpStatus.OK);
    }

    /**
     * Get an invoice by ID.
     * GET /api/invoices/{id}
     * @param id The ID of the invoice to retrieve.
     * @return The Invoice object if found (200 OK), or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        Optional<Invoice> invoice = invoiceRepository.findById(id);
        return invoice.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Create a new invoice.
     * This method handles the creation of an invoice and its associated invoice items.
     * It also performs calculations for GST and total amounts.
     * POST /api/invoices
     * @param invoice The Invoice object to add (sent in request body).
     * It should contain the customer ID and a list of InvoiceItems (with product IDs and quantities).
     * @return The saved Invoice object (201 Created).
     */
    @PostMapping
    @Transactional // Ensures that the entire operation (invoice + items) is atomic
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        // 1. Validate and set Customer
        if (invoice.getCustomer() == null || invoice.getCustomer().getId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Customer ID is required
        }
        Optional<Customer> customerOptional = customerRepository.findById(invoice.getCustomer().getId());
        if (customerOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Customer not found
        }
        invoice.setCustomer(customerOptional.get()); // Set the managed Customer entity

        // 2. Set invoice date if not provided (default to today)
        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }

        // 3. Initialize totals
        double totalAmountBeforeGst = 0.0;
        double totalGstAmount = 0.0;

        // 4. Process Invoice Items
        if (invoice.getInvoiceItems() != null && !invoice.getInvoiceItems().isEmpty()) {
            for (InvoiceItem item : invoice.getInvoiceItems()) {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Product ID is required for each item
                }
                Optional<Product> productOptional = productRepository.findById(item.getProduct().getId());
                if (productOptional.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Product not found for an item
                }
                Product product = productOptional.get();

                // Set the managed Product entity
                item.setProduct(product);
                // Link item back to the invoice
                item.setInvoice(invoice);

                // Perform calculations for each item
                double itemPrice = product.getUnitPrice() * item.getQuantity();
                double itemGstAmount = itemPrice * (product.getGstRate() / 100.0);
                double itemTotal = itemPrice + itemGstAmount;

                item.setItemPrice(itemPrice);
                item.setGstRate(product.getGstRate()); // Store GST rate at time of invoice
                item.setItemGstAmount(itemGstAmount);
                item.setItemTotal(itemTotal);

                totalAmountBeforeGst += itemPrice;
                totalGstAmount += itemGstAmount;
            }
        } else {
            // If no items, totals are 0
            invoice.setInvoiceItems(List.of()); // Ensure it's not null
        }

        // 5. Set invoice totals
        invoice.setTotalAmountBeforeGst(totalAmountBeforeGst);
        invoice.setTotalGstAmount(totalGstAmount);
        invoice.setGrandTotal(totalAmountBeforeGst + totalGstAmount);

        // 6. Set default payment status if not provided
        if (invoice.getPaymentStatus() == null || invoice.getPaymentStatus().isEmpty()) {
            invoice.setPaymentStatus("Pending");
        }

        // 7. Save the invoice (this will cascade save invoice items due to CascadeType.ALL)
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return new ResponseEntity<>(savedInvoice, HttpStatus.CREATED);
    }


    /**
     * Update an existing invoice.
     * This method allows updating invoice details and its items.
     * PUT /api/invoices/{id}
     * @param id The ID of the invoice to update.
     * @param invoiceDetails The updated Invoice object details.
     * @return The updated Invoice object (200 OK), or 404 Not Found if ID doesn't exist.
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Invoice> updateInvoice(@PathVariable Long id, @RequestBody Invoice invoiceDetails) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
        if (invoiceOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Invoice existingInvoice = invoiceOptional.get();

        // Update basic invoice details
        existingInvoice.setInvoiceNumber(invoiceDetails.getInvoiceNumber());
        existingInvoice.setInvoiceDate(invoiceDetails.getInvoiceDate());
        existingInvoice.setPaymentStatus(invoiceDetails.getPaymentStatus());
        existingInvoice.setNotes(invoiceDetails.getNotes());

        // Update Customer if provided (optional, but good for flexibility)
        if (invoiceDetails.getCustomer() != null && invoiceDetails.getCustomer().getId() != null) {
            Optional<Customer> customerOptional = customerRepository.findById(invoiceDetails.getCustomer().getId());
            if (customerOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // New customer ID not found
            }
            existingInvoice.setCustomer(customerOptional.get());
        }

        // Handle Invoice Items update (more complex: remove old, add new, update existing)
        // Clear existing items (orphanRemoval=true will delete them from DB)
        existingInvoice.getInvoiceItems().clear();
        double totalAmountBeforeGst = 0.0;
        double totalGstAmount = 0.0;

        if (invoiceDetails.getInvoiceItems() != null && !invoiceDetails.getInvoiceItems().isEmpty()) {
            for (InvoiceItem item : invoiceDetails.getInvoiceItems()) {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Product ID is required
                }
                Optional<Product> productOptional = productRepository.findById(item.getProduct().getId());
                if (productOptional.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Product not found
                }
                Product product = productOptional.get();

                // Set relationships and recalculate
                item.setProduct(product);
                item.setInvoice(existingInvoice); // Link to the existing invoice

                double itemPrice = product.getUnitPrice() * item.getQuantity();
                double itemGstAmount = itemPrice * (product.getGstRate() / 100.0);
                double itemTotal = itemPrice + itemGstAmount;

                item.setItemPrice(itemPrice);
                item.setGstRate(product.getGstRate());
                item.setItemGstAmount(itemGstAmount);
                item.setItemTotal(itemTotal);

                totalAmountBeforeGst += itemPrice;
                totalGstAmount += itemGstAmount;

                // Add the (potentially new) item to the existing invoice's collection
                existingInvoice.getInvoiceItems().add(item);
            }
        }

        // Update invoice totals
        existingInvoice.setTotalAmountBeforeGst(totalAmountBeforeGst);
        existingInvoice.setTotalGstAmount(totalGstAmount);
        existingInvoice.setGrandTotal(totalAmountBeforeGst + totalGstAmount);

        Invoice updatedInvoice = invoiceRepository.save(existingInvoice);
        return new ResponseEntity<>(updatedInvoice, HttpStatus.OK);
    }


    /**
     * Delete an invoice by ID.
     * DELETE /api/invoices/{id}
     * @param id The ID of the invoice to delete.
     * @return 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (Exception e) {
            // Handle case where ID might not exist or other DB issues
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Or INTERNAL_SERVER_ERROR
        }
    }

    // NEW ENDPOINT: Generate PDF for a specific invoice
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long id) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(id);
        if (optionalInvoice.isPresent()) {
            Invoice invoice = optionalInvoice.get();
            try {
                // IMPORTANT: When fetching an Invoice for PDF generation, its associated Customer
                // and InvoiceItems (and their Products) must be initialized if they are LAZY loaded.
                // Accessing them here will trigger their loading if not already loaded by JPA.
                // Alternatively, you can use a JOIN FETCH query in your InvoiceRepository
                // to eagerly load them when fetching the invoice by ID.
                // For now, we'll access them to trigger lazy loading:
                invoice.getCustomer().getName(); // Access customer to initialize proxy
                if (invoice.getInvoiceItems() != null) {
                    invoice.getInvoiceItems().forEach(item -> item.getProduct().getName()); // Access product in each item
                }


                byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                String filename = "invoice_" + invoice.getInvoiceNumber() + ".pdf";
                headers.setContentDispositionFormData("attachment", filename); // "attachment" to prompt download

                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            } catch (IOException e) {
                System.err.println("Error generating PDF for invoice " + id + ": " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
