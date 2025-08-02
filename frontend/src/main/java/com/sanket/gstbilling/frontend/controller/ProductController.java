package com.sanket.gstbilling.frontend.controller;
 // Ensure this package matches your project structure

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.gstbilling.frontend.model.Product; // Import the frontend Product model
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
 * Controller for the Product Management tab in the frontend.
 * Handles UI interactions and communicates with the Spring Boot backend API.
 */
public class ProductController {

    // FXML UI elements injected from product-view.fxml
    @FXML private TextField nameField;
    @FXML private TextField hsnSacField;
    @FXML private TextField unitPriceField;
    @FXML private TextField gstRateField;
    @FXML private TextField descriptionField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Long> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> hsnSacColumn;
    @FXML private TableColumn<Product, Double> unitPriceColumn;
    @FXML private TableColumn<Product, Double> gstRateColumn;
    @FXML private TableColumn<Product, String> descriptionColumn;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    private static final String API_BASE_URL = "http://localhost:8080/api/products"; // Backend API URL
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON serialization/deserialization

    // ObservableList to hold product data for the TableView
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    /**
     * Initializes the controller. This method is called automatically after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Set up table column cell value factories
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        hsnSacColumn.setCellValueFactory(new PropertyValueFactory<>("hsnSac"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        gstRateColumn.setCellValueFactory(new PropertyValueFactory<>("gstRate"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Set the ObservableList to the TableView
        productTable.setItems(productList);

        // Load products when the controller initializes
        loadProducts();

        // Add listener to the table selection to populate fields when a row is selected
        productTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProductDetails(newValue));
    }

    /**
     * Loads all products from the backend API and updates the TableView.
     */
    private void loadProducts() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<Product> products = objectMapper.readValue(response.body(), new TypeReference<List<Product>>() {});
                productList.setAll(products); // Update the ObservableList
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays product details in the input fields when a row is selected in the table.
     * @param product The selected Product object.
     */
    private void showProductDetails(Product product) {
        if (product != null) {
            nameField.setText(product.getName());
            hsnSacField.setText(product.getHsnSac());
            unitPriceField.setText(String.valueOf(product.getUnitPrice()));
            gstRateField.setText(String.valueOf(product.getGstRate()));
            descriptionField.setText(product.getDescription());
        } else {
            handleClearFields(); // Clear fields if no product is selected
        }
    }

    /**
     * Handles adding a new product.
     * Called when the "Add" button is clicked.
     */
    @FXML
    private void handleAddProduct() {
        Product newProduct = new Product();
        newProduct.setName(nameField.getText());
        newProduct.setHsnSac(hsnSacField.getText());
        try {
            newProduct.setUnitPrice(Double.parseDouble(unitPriceField.getText()));
            newProduct.setGstRate(Double.parseDouble(gstRateField.getText()));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Unit Price and GST Rate must be valid numbers.");
            return;
        }
        newProduct.setDescription(descriptionField.getText());

        try {
            String json = objectMapper.writeValueAsString(newProduct);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) { // 201 Created
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
                loadProducts(); // Reload table data
                handleClearFields(); // Clear input fields
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles updating an existing product.
     * Called when the "Update" button is clicked.
     */
    @FXML
    private void handleUpdateProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to update.");
            return;
        }

        selectedProduct.setName(nameField.getText());
        selectedProduct.setHsnSac(hsnSacField.getText());
        try {
            selectedProduct.setUnitPrice(Double.parseDouble(unitPriceField.getText()));
            selectedProduct.setGstRate(Double.parseDouble(gstRateField.getText()));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Unit Price and GST Rate must be valid numbers.");
            return;
        }
        selectedProduct.setDescription(descriptionField.getText());

        try {
            String json = objectMapper.writeValueAsString(selectedProduct);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/" + selectedProduct.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) { // 200 OK
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                loadProducts(); // Reload table data
                handleClearFields(); // Clear input fields
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles deleting a selected product.
     * Called when the "Delete" button is clicked.
     */
    @FXML
    private void handleDeleteProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/" + selectedProduct.getId()))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 204) { // 204 No Content
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                loadProducts(); // Reload table data
                handleClearFields(); // Clear input fields
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all input fields.
     * Called when the "Clear" button is clicked.
     */
    @FXML
    private void handleClearFields() {
        nameField.clear();
        hsnSacField.clear();
        unitPriceField.clear();
        gstRateField.clear();
        descriptionField.clear();
        productTable.getSelectionModel().clearSelection(); // Deselect table row
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
