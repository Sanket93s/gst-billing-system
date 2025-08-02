package com.sanket.gstbilling.frontend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * Main controller for the GST Billing System dashboard.
 * Manages tab switching and initializes sub-controllers.
 */
public class MainController {

    @FXML
    private TabPane mainTabPane;

    /**
     * Initializes the controller. This method is called automatically after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Add a listener to the tab pane selection model
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                String tabText = newTab.getText();
                System.out.println("Switched to tab: " + tabText); // Debugging

                // Load content for the selected tab if it's not already loaded or needs refresh
                switch (tabText) {
                    case "Customers":
                        loadTabContent("customer-view.fxml", newTab);
                        break;
                    case "Products":
                        loadTabContent("product-view.fxml", newTab);
                        break;
                    case "Invoices":
                        // Special handling for Invoices to ensure customers and products are reloaded
                        loadInvoiceTabContent(newTab);
                        break;
                    case "Reports":
                        // For reports, we might want to trigger a refresh too
                        loadReportTabContent(newTab);
                        break;
                    default:
                        break;
                }
            }
        });

        // Manually load the initial tab content (e.g., Customers tab)
        // This ensures the content is loaded even before the first tab switch
        loadTabContent("customer-view.fxml", mainTabPane.getTabs().get(0)); // Assuming Customers is the first tab
    }

    /**
     * Loads the FXML content into a given tab.
     * @param fxmlFileName The name of the FXML file (e.g., "customer-view.fxml").
     * @param tab The Tab object to load the content into.
     */
    private void loadTabContent(String fxmlFileName, Tab tab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sanket/gstbilling/frontend/" + fxmlFileName));
            AnchorPane content = loader.load();
            tab.setContent(content);
            System.out.println("Loaded " + fxmlFileName + " into " + tab.getText() + " tab."); // Debugging
        } catch (IOException e) {
            System.err.println("Error loading FXML for " + fxmlFileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Specifically loads the Invoice tab content and ensures its controller reloads data.
     * @param tab The Invoice Tab object.
     */
    private void loadInvoiceTabContent(Tab tab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sanket/gstbilling/frontend/invoice-view.fxml"));
            AnchorPane content = loader.load();
            InvoiceController invoiceController = loader.getController(); // Get the controller instance
            tab.setContent(content);
            System.out.println("Loaded invoice-view.fxml into Invoices tab."); // Debugging

            // Explicitly call load methods on the InvoiceController to refresh data
            if (invoiceController != null) {
                // These methods are private, so we need to make them public or create public wrappers
                // For now, let's assume they are called during initialize() which happens on load().
                // If you need to force a refresh *after* load, you'd need public methods in InvoiceController.
                // However, the problem was that it wasn't loading *at all* on tab switch.
                // The load() call above will re-initialize the controller and thus call its initialize() method.
                // This should be sufficient.
            }
        } catch (IOException e) {
            System.err.println("Error loading FXML for invoice-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Specifically loads the Report tab content and ensures its controller reloads data.
     * @param tab The Report Tab object.
     */
    private void loadReportTabContent(Tab tab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sanket/gstbilling/frontend/report-view.fxml"));
            AnchorPane content = loader.load();
            ReportController reportController = loader.getController(); // Get the controller instance
            tab.setContent(content);
            System.out.println("Loaded report-view.fxml into Reports tab."); // Debugging

            if (reportController != null) {
                // The ReportController's initialize() method already calls handleGenerateReport(),
                // so simply reloading the FXML (which happens via loadTabContent) is sufficient.
                // No explicit call needed here unless you want to pass parameters.
            }
        } catch (IOException e) {
            System.err.println("Error loading FXML for report-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
