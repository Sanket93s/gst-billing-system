package com.sanket.gstbilling_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // Keep this

import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
// REMOVED @JsonIdentityInfo here
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "contact_no", nullable = false)
    private String contactNo;

    @Column(nullable = true)
    private String email;

    @Column(nullable = false)
    private String address;

    @Column(nullable = true, unique = true)
    private String gstin;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Keep this to prevent infinite recursion
    private List<Invoice> invoices;
}
    