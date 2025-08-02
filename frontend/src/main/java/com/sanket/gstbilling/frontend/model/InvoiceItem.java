package com.sanket.gstbilling.frontend.model;
 // Ensure this package matches your project structure

// This is a simple POJO for the frontend, no JPA annotations
public class InvoiceItem {
    private Long id;
    private Product product; // Frontend Product object
    private Integer quantity;
    private Double itemPrice; // Price before GST for this item (unitPrice * quantity)
    private Double gstRate;
    private Double itemGstAmount;
    private Double itemTotal; // Price after GST for this item

    // Constructors
    public InvoiceItem() {}

    public InvoiceItem(Long id, Product product, Integer quantity, Double itemPrice, Double gstRate, Double itemGstAmount, Double itemTotal) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.gstRate = gstRate;
        this.itemGstAmount = itemGstAmount;
        this.itemTotal = itemTotal;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getItemPrice() { return itemPrice; }
    public void setItemPrice(Double itemPrice) { this.itemPrice = itemPrice; }
    public Double getGstRate() { return gstRate; }
    public void setGstRate(Double gstRate) { this.gstRate = gstRate; }
    public Double getItemGstAmount() { return itemGstAmount; }
    public void setItemGstAmount(Double itemGstAmount) { this.itemGstAmount = itemGstAmount; }
    public Double getItemTotal() { return itemTotal; }
    public void setItemTotal(Double itemTotal) { this.itemTotal = itemTotal; }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : "null") +
                ", quantity=" + quantity +
                ", itemTotal=" + itemTotal +
                '}';
    }
}
