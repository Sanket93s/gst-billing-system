package com.sanket.gstbilling.frontend.model;
 // Ensure this package matches your project structure
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.util.List;

// This is a simple POJO for the frontend, no JPA annotations
public class Invoice {
    private Long id;
    private String invoiceNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate invoiceDate;

    private Customer customer; // Frontend Customer object
    private Double totalAmountBeforeGst;
    private Double totalGstAmount;
    private Double grandTotal;
    private List<InvoiceItem> invoiceItems; // List of frontend InvoiceItem objects
    private String paymentStatus;
    private String notes;

    // Constructors
    public Invoice() {}

    public Invoice(Long id, String invoiceNumber, LocalDate invoiceDate, Customer customer,
                   Double totalAmountBeforeGst, Double totalGstAmount, Double grandTotal,
                   List<InvoiceItem> invoiceItems, String paymentStatus, String notes) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.customer = customer;
        this.totalAmountBeforeGst = totalAmountBeforeGst;
        this.totalGstAmount = totalGstAmount;
        this.grandTotal = grandTotal;
        this.invoiceItems = invoiceItems;
        this.paymentStatus = paymentStatus;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Double getTotalAmountBeforeGst() { return totalAmountBeforeGst; }
    public void setTotalAmountBeforeGst(Double totalAmountBeforeGst) { this.totalAmountBeforeGst = totalAmountBeforeGst; }
    public Double getTotalGstAmount() { return totalGstAmount; }
    public void setTotalGstAmount(Double totalGstAmount) { this.totalGstAmount = totalGstAmount; }
    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }
    public List<InvoiceItem> getInvoiceItems() { return invoiceItems; }
    public void setInvoiceItems(List<InvoiceItem> invoiceItems) { this.invoiceItems = invoiceItems; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", invoiceDate=" + invoiceDate +
                ", customer=" + (customer != null ? customer.getName() : "null") +
                ", grandTotal=" + grandTotal +
                '}';
    }
}
