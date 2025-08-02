package com.sanket.gstbilling.frontend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature; // NEW IMPORT
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanket.gstbilling.frontend.model.Invoice;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop; // For opening PDF (requires java.desktop module)
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Reports and Analytics tab in the frontend.
 * Handles UI interactions and communicates with the Spring Boot backend API for invoice data.
 */
public class ReportController {

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label totalInvoicesLabel;
    @FXML private Label totalGrandAmountLabel;
    @FXML private Label totalGstCollectedLabel;
    @FXML private TableView<Invoice> reportInvoiceTable;
    @FXML private TableColumn<Invoice, Long> colInvoiceId;
    @FXML private TableColumn<Invoice, String> colInvoiceNumber;
    @FXML private TableColumn<Invoice, LocalDate> colInvoiceDate;
    @FXML private TableColumn<Invoice, String> colCustomerName;
    @FXML private TableColumn<Invoice, Double> colTotalAmountBeforeGst;
    @FXML private TableColumn<Invoice, Double> colTotalGstAmount;
    @FXML private TableColumn<Invoice, Double> colGrandTotal;
    @FXML private TableColumn<Invoice, String> colPaymentStatus;

    private static final String API_BASE_URL_INVOICES = "http://localhost:8080/api/invoices";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ObservableList<Invoice> invoiceReportList = FXCollections.observableArrayList();

    /**
     * Initializes the controller. This method is called automatically after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Configure ObjectMapper for LocalDate and to ignore unknown properties
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ADDED THIS LINE

        // Set up table column cell value factories
        colInvoiceId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInvoiceNumber.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        colInvoiceDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        // For nested properties like customer name, use a custom CellValueFactory
        colCustomerName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getName() : "N/A"
        ));
        colTotalAmountBeforeGst.setCellValueFactory(new PropertyValueFactory<>("totalAmountBeforeGst"));
        colTotalGstAmount.setCellValueFactory(new PropertyValueFactory<>("totalGstAmount"));
        colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        reportInvoiceTable.setItems(invoiceReportList);

        // Set default date range (e.g., last 30 days or current month)
        toDate.setValue(LocalDate.now());
        fromDate.setValue(LocalDate.now().minusMonths(1)); // Example: last month

        // Load initial report
        handleGenerateReport();
    }

    /**
     * Handles generating the report based on the selected date range.
     * Fetches invoices from the backend and updates the table and summary labels.
     */
    @FXML
    private void handleGenerateReport() {
        LocalDate start = fromDate.getValue();
        LocalDate end = toDate.getValue();

        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Date Selection Error", "Please select both From and To dates for the report.");
            return;
        }
        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "Date Order Error", "From Date cannot be after To Date.");
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL_INVOICES))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<Invoice> allInvoices = objectMapper.readValue(response.body(), new TypeReference<List<Invoice>>() {});

                // Filter invoices by date range
                List<Invoice> filteredInvoices = allInvoices.stream()
                        .filter(invoice -> !invoice.getInvoiceDate().isBefore(start) && !invoice.getInvoiceDate().isAfter(end))
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    invoiceReportList.setAll(filteredInvoices); // Update table
                    updateReportSummary(filteredInvoices); // Calculate and update summary labels
                    System.out.println("Report generated. Invoices found: " + filteredInvoices.size());
                });

            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load invoices for report: " + response.body());
                System.err.println("Failed to load invoices for report. Status: " + response.statusCode() + ", Body: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error during report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the summary labels based on the filtered list of invoices.
     * @param invoices The list of invoices to summarize.
     */
    private void updateReportSummary(List<Invoice> invoices) {
        double totalGrandAmount = 0.0;
        double totalGstCollected = 0.0;

        for (Invoice invoice : invoices) {
            totalGrandAmount += invoice.getGrandTotal();
            totalGstCollected += invoice.getTotalGstAmount();
        }

        totalInvoicesLabel.setText(String.valueOf(invoices.size()));
        totalGrandAmountLabel.setText(String.format("₹%.2f", totalGrandAmount));
        totalGstCollectedLabel.setText(String.format("₹%.2f", totalGstCollected));
    }

    /**
     * Handles printing the report. This now generates and opens a PDF for the selected invoice.
     */
    @FXML
    private void handlePrintReport() {
        Invoice selectedInvoice = reportInvoiceTable.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "No Invoice Selected", "Please select an invoice from the table to print.");
            return;
        }

        // Call the backend endpoint to generate the PDF
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL_INVOICES + "/" + selectedInvoice.getId() + "/pdf"))
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                // Save the PDF bytes to a temporary file
                String fileName = "Invoice_" + selectedInvoice.getInvoiceNumber() + ".pdf";
                File pdfFile = new File(fileName);
                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    fos.write(response.body());
                }

                // Open the PDF file using the default system PDF viewer
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                    showAlert(Alert.AlertType.INFORMATION, "PDF Generated", "Invoice PDF generated and opened successfully!");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "PDF Generated", "Invoice PDF generated as " + fileName + ". Please open it manually.");
                }
            } else if (response.statusCode() == 404) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invoice not found on server for PDF generation.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate PDF: " + response.statusCode());
                System.err.println("Failed to generate PDF. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Network or communication error during PDF generation: " + e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Desktop operations not supported on this system to open PDF automatically.");
            e.printStackTrace();
        }
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
