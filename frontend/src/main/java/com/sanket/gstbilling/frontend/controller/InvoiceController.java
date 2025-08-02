package com.sanket.gstbilling.frontend.controller;
import javafx.application.Platform;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature; // IMPORTANT: New import for DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanket.gstbilling.frontend.model.Customer;
import com.sanket.gstbilling.frontend.model.Invoice;
import com.sanket.gstbilling.frontend.model.InvoiceItem;
import com.sanket.gstbilling.frontend.model.Product;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Invoice Generation tab in the frontend.
 * Handles UI interactions, calculations, and communicates with the Spring Boot backend API.
 */
public class InvoiceController {

    // FXML UI elements for Invoice details
    @FXML private TextField invoiceNumberField;
    @FXML private DatePicker invoiceDatePicker;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private ComboBox<String> paymentStatusComboBox;
    @FXML private TextArea notesArea;

    // FXML UI elements for Invoice Items
    @FXML private ComboBox<Product> productComboBox;
    @FXML private TextField quantityField;
    @FXML private TableView<InvoiceItem> invoiceItemTable;
    @FXML private TableColumn<InvoiceItem, String> itemProductColumn;
    @FXML private TableColumn<InvoiceItem, Integer> itemQuantityColumn;
    @FXML private TableColumn<InvoiceItem, Double> itemUnitPriceColumn;
    @FXML private TableColumn<InvoiceItem, Double> itemPriceColumn;
    @FXML private TableColumn<InvoiceItem, Double> itemGstRateColumn;
    @FXML private TableColumn<InvoiceItem, Double> itemGstAmountColumn;
    @FXML private TableColumn<InvoiceItem, Double> itemTotalColumn;

    // FXML UI elements for Totals
    @FXML private Label totalBeforeGstLabel;
    @FXML private Label totalGstLabel;
    @FXML private Label grandTotalLabel;

    // FXML UI elements for Buttons
    @FXML private Button createInvoiceButton;
    @FXML private Button updateInvoiceButton;
    @FXML private Button deleteInvoiceButton;
    @FXML private Button printInvoiceButton;
    @FXML private Button clearInvoiceButton;

    private static final String API_BASE_URL_INVOICES = "http://localhost:8080/api/invoices";
    private static final String API_BASE_URL_CUSTOMERS = "http://localhost:8080/api/customers";
    private static final String API_BASE_URL_PRODUCTS = "http://localhost:8080/api/products";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<InvoiceItem> currentInvoiceItems = FXCollections.observableArrayList();

    private Invoice selectedInvoiceForEdit; // To hold the invoice selected for update/delete

    /**
     * Initializes the controller. This method is called automatically after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Configure ObjectMapper for LocalDate and to ignore unknown properties
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // This is crucial because the backend Customer/Product might send 'invoices'/'invoiceItems' lists,
        // which the frontend models don't have.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Set up customer ComboBox
        customerComboBox.setItems(customers);
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer != null ? customer.getName() : "";
            }
            @Override
            public Customer fromString(String string) {
                // Not used for selection, only for display
                return null;
            }
        });

        // Set up product ComboBox
        productComboBox.setItems(products);
        productComboBox.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                return product != null ? product.getName() + " (₹" + product.getUnitPrice() + ", GST " + product.getGstRate() + "%)" : "";
            }
            @Override
            public Product fromString(String string) {
                return null;
            }
        });

        // Populate payment status ComboBox
        paymentStatusComboBox.setItems(FXCollections.observableArrayList("Pending", "Paid", "Cancelled"));
        paymentStatusComboBox.setValue("Pending"); // Default value

        // Set up Invoice Item TableView columns
        itemProductColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProduct().getName()));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemUnitPriceColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getUnitPrice()));
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));
        itemGstRateColumn.setCellValueFactory(new PropertyValueFactory<>("gstRate")); // Corrected from setCellValue
        itemGstAmountColumn.setCellValueFactory(new PropertyValueFactory<>("itemGstAmount"));
        itemTotalColumn.setCellValueFactory(new PropertyValueFactory<>("itemTotal"));
        invoiceItemTable.setItems(currentInvoiceItems);

        // Set default invoice date
        invoiceDatePicker.setValue(LocalDate.now());

        // Load initial data
        loadCustomers();
        loadProducts();

        // Add listener to quantity field for real-time calculation (optional, but good UX)
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> calculateOverallTotals());

        // Add listener to invoice item table selection (for potential editing/removal)
        invoiceItemTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // Logic to populate product/quantity fields if an item is selected for editing
                    // For now, just a placeholder.
                });
    }

    /**
     * Loads customers from the backend API into the customer ComboBox.
     */
    private void loadCustomers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL_CUSTOMERS))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<Customer> fetchedCustomers = objectMapper.readValue(response.body(), new TypeReference<List<Customer>>() {});
                // Ensure UI update happens on the JavaFX Application Thread
                Platform.runLater(() -> {
                    customers.setAll(fetchedCustomers); // Update the ObservableList
                });
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to load customers: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(AlertType.ERROR, "Error", "Network or communication error loading customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads products from the backend API into the product ComboBox.
     */
    private void loadProducts() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL_PRODUCTS))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<Product> fetchedProducts = objectMapper.readValue(response.body(), new TypeReference<List<Product>>() {});
                // Ensure UI update happens on the JavaFX Application Thread
                Platform.runLater(() -> {
                    products.setAll(fetchedProducts); // Update the ObservableList
                    System.out.println("Products loaded: " + fetchedProducts.size()); // Debugging
                });
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to load products: " + response.body());
                System.err.println("Failed to load products. Status: " + response.statusCode() + ", Body: " + response.body()); // Debugging
            }
        } catch (IOException | InterruptedException e) {
            showAlert(AlertType.ERROR, "Error", "Network or communication error loading products: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }


    /**
     * Handles adding a selected product with quantity to the invoice items table.
     */
    @FXML
    private void handleAddInvoiceItem() {
        Product selectedProduct = productComboBox.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText();

        if (selectedProduct == null) {
            showAlert(AlertType.WARNING, "No Product Selected", "Please select a product from the dropdown.");
            return;
        }
        if (quantityText.isEmpty() || !quantityText.matches("\\d+")) {
            showAlert(AlertType.WARNING, "Invalid Quantity", "Please enter a valid positive integer for quantity.");
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        if (quantity <= 0) {
            showAlert(AlertType.WARNING, "Invalid Quantity", "Quantity must be greater than zero.");
            return;
        }

        // Check if item already exists and update quantity
        Optional<InvoiceItem> existingItem = currentInvoiceItems.stream()
                .filter(item -> item.getProduct().getId().equals(selectedProduct.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update existing item's quantity
            InvoiceItem itemToUpdate = existingItem.get();
            itemToUpdate.setQuantity(itemToUpdate.getQuantity() + quantity);
            // Recalculate totals for this item
            recalculateInvoiceItem(itemToUpdate);
            invoiceItemTable.refresh(); // Refresh table to show updated quantity
        } else {
            // Create new invoice item
            InvoiceItem newItem = new InvoiceItem();
            newItem.setProduct(selectedProduct);
            newItem.setQuantity(quantity);
            recalculateInvoiceItem(newItem); // Calculate totals for the new item
            currentInvoiceItems.add(newItem);
        }

        calculateOverallTotals(); // Recalculate grand totals for the invoice
        clearItemInputFields();
    }

    /**
     * Handles removing a selected item from the invoice items table.
     */
    @FXML
    private void handleRemoveInvoiceItem() {
        InvoiceItem selectedItem = invoiceItemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(AlertType.WARNING, "No Item Selected", "Please select an item from the table to remove.");
            return;
        }

        currentInvoiceItems.remove(selectedItem);
        calculateOverallTotals(); // Recalculate grand totals
    }

    /**
     * Recalculates price, GST amount, and total for a single invoice item.
     * @param item The InvoiceItem to recalculate.
     */
    private void recalculateInvoiceItem(InvoiceItem item) {
        Product product = item.getProduct();
        double unitPrice = product.getUnitPrice();
        double gstRate = product.getGstRate();
        int quantity = item.getQuantity();

        double itemPrice = unitPrice * quantity;
        double itemGstAmount = itemPrice * (gstRate / 100.0);
        double itemTotal = itemPrice + itemGstAmount;

        item.setItemPrice(itemPrice);
        item.setGstRate(gstRate); // Store actual GST rate at time of billing
        item.setItemGstAmount(itemGstAmount);
        item.setItemTotal(itemTotal);
    }

    /**
     * Calculates and updates the overall totals for the invoice (Total before GST, Total GST, Grand Total).
     */
    private void calculateOverallTotals() {
        double totalBeforeGst = 0.0;
        double totalGst = 0.0;
        double grandTotal = 0.0;

        for (InvoiceItem item : currentInvoiceItems) {
            totalBeforeGst += item.getItemPrice();
            totalGst += item.getItemGstAmount();
            grandTotal += item.getItemTotal();
        }

        totalBeforeGstLabel.setText(String.format("₹%.2f", totalBeforeGst));
        totalGstLabel.setText(String.format("₹%.2f", totalGst));
        grandTotalLabel.setText(String.format("₹%.2f", grandTotal));
    }

    /**
     * Clears the input fields for adding invoice items.
     */
    private void clearItemInputFields() {
        productComboBox.getSelectionModel().clearSelection();
        quantityField.clear();
    }

    /**
     * Handles creating a new invoice.
     */
    @FXML
    private void handleCreateInvoice() {
        System.out.println("--- handleCreateInvoice called ---"); // Debugging
        if (!isInvoiceInputValid()) {
            System.out.println("Invoice input is NOT valid. Aborting create invoice."); // Debugging
            return;
        }

        Invoice newInvoice = new Invoice();
        newInvoice.setInvoiceNumber(invoiceNumberField.getText());
        newInvoice.setInvoiceDate(invoiceDatePicker.getValue());
        newInvoice.setCustomer(customerComboBox.getSelectionModel().getSelectedItem());
        newInvoice.setPaymentStatus(paymentStatusComboBox.getSelectionModel().getSelectedItem());
        newInvoice.setNotes(notesArea.getText());
        newInvoice.setInvoiceItems(new ArrayList<>(currentInvoiceItems)); // Pass a copy of items

        // Totals are calculated by backend, but we can send current values for consistency
        newInvoice.setTotalAmountBeforeGst(Double.parseDouble(totalBeforeGstLabel.getText().replace("₹", "")));
        newInvoice.setTotalGstAmount(Double.parseDouble(totalGstLabel.getText().replace("₹", "")));
        newInvoice.setGrandTotal(Double.parseDouble(grandTotalLabel.getText().replace("₹", "")));

        try {
            String json = objectMapper.writeValueAsString(newInvoice);
            System.out.println("Sending Invoice JSON to backend: " + json); // Debugging: See the JSON being sent

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL_INVOICES))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) { // 201 Created
                showAlert(AlertType.INFORMATION, "Success", "Invoice created successfully!");
                handleClearInvoiceFields(); // Clear form after successful creation
                // Optionally, reload a list of invoices if you add an invoice table to this view
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to create invoice: " + response.body());
                System.err.println("Failed to create invoice. Status: " + response.statusCode() + ", Body: " + response.body()); // Debugging
            }
        } catch (IOException | InterruptedException e) {
            showAlert(AlertType.ERROR, "Error", "Network or communication error: " + e.getMessage());
            e.printStackTrace(); // Debugging
        }
    }

    /**
     * Handles updating an existing invoice. (Implementation will be similar to create, but with PUT and ID)
     */
    @FXML
    private void handleUpdateInvoice() {
        // This method will be implemented later when we have a way to select an existing invoice for editing.
        // It will involve fetching the invoice by ID, populating the fields, and then sending a PUT request.
        showAlert(AlertType.INFORMATION, "Coming Soon", "Update Invoice functionality will be implemented.");
    }

    /**
     * Handles deleting an invoice. (Implementation will be similar to create, but with DELETE and ID)
     */
    @FXML
    private void handleDeleteInvoice() {
        // This method will be implemented later when we have a way to select an existing invoice for deletion.
        showAlert(AlertType.INFORMATION, "Coming Soon", "Delete Invoice functionality will be implemented.");
    }

    /**
     * Handles printing an invoice. (Requires JasperReports integration)
     */
    @FXML
    private void handlePrintInvoice() {
        showAlert(AlertType.INFORMATION, "Coming Soon", "Invoice printing functionality will be implemented (requires JasperReports).");
    }

    /**
     * Clears all fields related to invoice creation.
     */
    @FXML
    private void handleClearInvoiceFields() {
        invoiceNumberField.clear();
        invoiceDatePicker.setValue(LocalDate.now());
        customerComboBox.getSelectionModel().clearSelection();
        paymentStatusComboBox.setValue("Pending");
        notesArea.clear();
        currentInvoiceItems.clear(); // Clear invoice items table
        calculateOverallTotals(); // Reset totals to zero
        clearItemInputFields();
        selectedInvoiceForEdit = null; // Clear selected invoice
    }

    /**
     * Validates invoice input fields.
     * @return true if input is valid, false otherwise.
     */
    private boolean isInvoiceInputValid() {
        System.out.println("--- Validating Invoice Input ---"); // Debugging
        String errorMessage = "";
        if (invoiceNumberField.getText() == null || invoiceNumberField.getText().isEmpty()) {
            errorMessage += "Invoice Number cannot be empty!\n";
        }
        if (invoiceDatePicker.getValue() == null) {
            errorMessage += "Invoice Date cannot be empty!\n";
        }
        if (customerComboBox.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Please select a Customer!\n";
        }
        if (currentInvoiceItems.isEmpty()) {
            errorMessage += "Invoice must have at least one item!\n";
        }
        // Add more validation as needed (e.g., quantity, amounts)

        if (errorMessage.isEmpty()) {
            System.out.println("Invoice input is valid."); // Debugging
            return true;
        } else {
            showAlert(AlertType.ERROR, "Invalid Input", errorMessage);
            System.out.println("Invoice input validation failed: " + errorMessage); // Debugging
            return false;
        }
    }

    /**
     * Helper method to display an alert message.
     * @param alertType Type of alert (INFORMATION, WARNING, ERROR).
     * @param title Title of the alert.
     * @param message Content message of the alert.
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
