package com.sanket.gstbilling_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference; // Keep this

/**
 * Represents an Invoice Item entity (line item) in an Invoice.
 * Maps to the 'invoice_items' table in the database.
 */
@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
// REMOVED @JsonIdentityInfo here
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Keep LAZY
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonBackReference // Keep this for bidirectional relationship management
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.EAGER) // Product is EAGER, should load fine
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double itemPrice;

    @Column(nullable = false)
    private Double gstRate;

    @Column(nullable = false)
    private Double itemGstAmount;

    @Column(nullable = false)
    private Double itemTotal;
}
    