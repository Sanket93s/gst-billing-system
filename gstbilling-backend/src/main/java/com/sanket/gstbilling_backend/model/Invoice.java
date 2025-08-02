package com.sanket.gstbilling_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Keep this for invoiceItems

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
// REMOVED @JsonIdentityInfo here
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @ManyToOne(fetch = FetchType.LAZY) // Keep LAZY, Hibernate6Module will handle it
    @JoinColumn(name = "customer_id", nullable = false)
    // REMOVED @JsonIgnore here, as Hibernate6Module will handle the proxy and we need customer data
    private Customer customer;

    @Column(nullable = false)
    private Double totalAmountBeforeGst;

    @Column(nullable = false)
    private Double totalGstAmount;

    @Column(nullable = false)
    private Double grandTotal;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Keep this to manage bidirectional serialization with InvoiceItem
    private List<InvoiceItem> invoiceItems;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(nullable = true)
    private String notes;
}
    