package com.sanket.gstbilling.frontend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.gstbilling.frontend.model.Customer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Customer Management tab in the frontend.
 * Handles UI interactions and communicates with the Spring Boot backend API.
 */
public class CustomerController {

    // FXML UI elements injected from customer-view.fxml
    @FXML private TextField nameField;
    @FXML private TextField contactNoField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField gstinField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Long> idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> contactNoColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, String> gstinColumn;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    private static final String API_BASE_URL = "http://localhost:8080/api/customers";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private Customer selectedCustomerForEdit; // Field to hold the selected customer

    /**
     * Initializes the controller. This method is called automatically after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Set up table column cell value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactNoColumn.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        gstinColumn.setCellValueFactory(new PropertyValueFactory<>("gstin"));

        // Set the ObservableList to the TableView
        customerTable.setItems(customerList);

        // Load customers when the controller initializes
        loadCustomers();

        // Add listener to the table selection to populate fields when a row is selected
        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println("Table selection changed. New value: " + newValue); // Debugging
                    showCustomerDetails(newValue);
                });
        System.out.println("CustomerController initialized. Table selection listener attached."); // Debugging

        // Initialize button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);
        System.out.println("Initial button states: Add=" + !addButton.isDisable() + ", Update=" + !updateButton.isDisable() + ", Delete=" + !deleteButton.isDisable()); // Debugging
    }

    /**
     * Loads all customers from the backend API and updates the TableView.
     */
    private void loadCustomers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<Customer> customers = objectMapper.readValue(response.body(), new TypeReference<List<Customer>>() {});
                Platform.runLater(() -> {
                    customerList.setAll(customers);
                    System.out.println("Customers loaded: " + customers.size()); // Debugging
                    // After loading, ensure no selection is active and buttons are reset
                    handleClearFields();
                });
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load customers: " + response.body());
                System.err.println("Failed to load customers. Status: " + response.statusCode() + ", Body: " + response.body()); // Debugging
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }

    /**
     * Populates the input fields with details of the selected customer from the table.
     *
     * @param customer The selected Customer object.
     */
    private void showCustomerDetails(Customer customer) {
        if (customer != null) {
            selectedCustomerForEdit = customer; // Set the selected customer
            nameField.setText(customer.getName());
            contactNoField.setText(customer.getContactNo());
            emailField.setText(customer.getEmail());
            addressField.setText(customer.getAddress());
            gstinField.setText(customer.getGstin());
            // Enable Update/Delete buttons when a customer is selected
            updateButton.setDisable(false);
            deleteButton.setDisable(false);
            addButton.setDisable(true); // Disable Add when editing
            System.out.println("Selected customer for edit: ID=" + customer.getId() + ", Name=" + customer.getName()); // Debugging
            System.out.println("Button states after selection: Add=" + !addButton.isDisable() + ", Update=" + !updateButton.isDisable() + ", Delete=" + !deleteButton.isDisable()); // Debugging
        } else {
            handleClearFields(); // Clear fields if no customer is selected
            System.out.println("No customer selected, clearing fields."); // Debugging
        }
    }

    /**
     * Handles adding a new customer.
     * Called when the "Add" button is clicked.
     */
    @FXML
    private void handleAddCustomer() {
        System.out.println("handleAddCustomer called. selectedCustomerForEdit: " + selectedCustomerForEdit); // Debugging
        Customer newCustomer = new Customer();
        newCustomer.setName(nameField.getText());
        newCustomer.setContactNo(contactNoField.getText());
        newCustomer.setEmail(emailField.getText());
        newCustomer.setAddress(addressField.getText());
        newCustomer.setGstin(gstinField.getText());

        try {
            String json = objectMapper.writeValueAsString(newCustomer);
            System.out.println("Adding customer JSON: " + json); // Debugging
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) { // 201 Created
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");
                loadCustomers(); // Reload table data, which will also call handleClearFields()
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add customer: " + response.body());
                System.err.println("Failed to add customer. Status: " + response.statusCode() + ", Body: " + response.body()); // Debugging
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }

    /**
     * Handles updating an existing customer.
     * Called when the "Update" button is clicked.
     */
    @FXML
    private void handleUpdateCustomer() {
        System.out.println("handleUpdateCustomer called. selectedCustomerForEdit: " + selectedCustomerForEdit); // Debugging
        // Use the stored selectedCustomerForEdit
        if (selectedCustomerForEdit == null || selectedCustomerForEdit.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer to update.");
            System.out.println("Update attempted with no customer selected or null ID."); // Debugging
            return;
        }

        selectedCustomerForEdit.setName(nameField.getText());
        selectedCustomerForEdit.setContactNo(contactNoField.getText());
        selectedCustomerForEdit.setEmail(emailField.getText());
        selectedCustomerForEdit.setAddress(addressField.getText());
        selectedCustomerForEdit.setGstin(gstinField.getText());

        try {
            String json = objectMapper.writeValueAsString(selectedCustomerForEdit);
            System.out.println("Updating customer ID " + selectedCustomerForEdit.getId() + " with JSON: " + json); // Debugging
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/" + selectedCustomerForEdit.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) { // 200 OK
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer updated successfully!");
                loadCustomers(); // Reload table data, which will also call handleClearFields()
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update customer: " + response.body());
                System.err.println("Failed to update customer. Status: " + response.statusCode() + ", Body: " + response.body()); // Debugging
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }

    /**
     * Handles deleting a selected customer.
     * Called when the "Delete" button is clicked.
     */
    @FXML
    private void handleDeleteCustomer() {
        System.out.println("handleDeleteCustomer called. selectedCustomerForEdit: " + selectedCustomerForEdit); // Debugging
        // Use the stored selectedCustomerForEdit
        if (selectedCustomerForEdit == null || selectedCustomerForEdit.getId() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer to delete.");
            System.out.println("Delete attempted with no customer selected or null ID."); // Debugging
            return;
        }

        try {
            System.out.println("Deleting customer ID: " + selectedCustomerForEdit.getId()); // Debugging
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/" + selectedCustomerForEdit.getId()))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 204) { // 204 No Content
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer deleted successfully!");
                loadCustomers(); // Reload table data, which will also call handleClearFields()
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete customer: " + response.body());
                System.err.println("Failed to delete customer. Status: " + response.statusCode() + ", Body: " + response.body()); // Debugging
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }

    /**
     * Clears all input fields.
     * Called when the "Clear" button is clicked.
     */
    @FXML
    private void handleClearFields() {
        nameField.clear();
        contactNoField.clear();
        emailField.clear();
        addressField.clear();
        gstinField.clear();
        customerTable.getSelectionModel().clearSelection(); // Deselect table row
        selectedCustomerForEdit = null; // Clear the selected customer
        // Reset button states
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        System.out.println("Fields cleared and selection reset. Button states: Add=" + !addButton.isDisable() + ", Update=" + !updateButton.isDisable() + ", Delete=" + !deleteButton.isDisable()); // Debugging
    }

    /**
     * Helper method to display an alert message.
     * @param alertType Type of alert (INFORMATION, WARNING, ERROR).
     * @param title Title of the alert.
     * @param message Content message of the alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
