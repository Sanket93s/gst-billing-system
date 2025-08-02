package com.sanket.gstbilling.frontend.model;
 // Ensure this package matches your project structure

// No JPA annotations here, this is a simple POJO for the frontend
public class Product {
    private Long id;
    private String name;
    private String hsnSac;
    private Double unitPrice;
    private Double gstRate;
    private String description;

    // Constructors
    public Product() {}

    public Product(Long id, String name, String hsnSac, Double unitPrice, Double gstRate, String description) {
        this.id = id;
        this.name = name;
        this.hsnSac = hsnSac;
        this.unitPrice = unitPrice;
        this.gstRate = gstRate;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHsnSac() {
        return hsnSac;
    }

    public void setHsnSac(String hsnSac) {
        this.hsnSac = hsnSac;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getGstRate() {
        return gstRate;
    }

    public void setGstRate(Double gstRate) {
        this.gstRate = gstRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hsnSac='" + hsnSac + '\'' +
                ", unitPrice=" + unitPrice +
                ", gstRate=" + gstRate +
                ", description='" + description + '\'' +
                '}';
    }
}
