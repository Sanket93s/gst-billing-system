package com.sanket.gstbilling.frontend.model;
 // Ensure this package matches your project structure

// No JPA annotations here, this is a simple POJO for the frontend
public class Customer {
    private Long id;
    private String name;
    private String contactNo;
    private String email;
    private String address;
    private String gstin;

    // Constructors
    public Customer() {}

    public Customer(Long id, String name, String contactNo, String email, String address, String gstin) {
        this.id = id;
        this.name = name;
        this.contactNo = contactNo;
        this.email = email;
        this.address = address;
        this.gstin = gstin;
    }

    // Getters and Setters (Manually written or use Lombok if you add it to frontend pom)
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

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", gstin='" + gstin + '\'' +
                '}';
    }
}
