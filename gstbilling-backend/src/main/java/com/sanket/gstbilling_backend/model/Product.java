package com.sanket.gstbilling_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// REMOVED @JsonIdentityInfo here

/**
 * Represents a Product entity in the GST Billing System.
 * Maps to the 'products' table in the database.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
// REMOVED @JsonIdentityInfo here
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String hsnSac;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Double gstRate;

    @Column(nullable = true)
    private String description;
}
    