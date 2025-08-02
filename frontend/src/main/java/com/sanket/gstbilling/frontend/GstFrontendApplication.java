package com.sanket.gstbilling.frontend; // Ensure this package matches your project structure

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox; // Using VBox as the root for a simple layout
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the GST Billing System Frontend (JavaFX).
 * This class sets up the primary stage and loads the main UI layout.
 */
public class GstFrontendApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main-view.fxml file
            FXMLLoader loader = new FXMLLoader();
            // Ensure this path correctly points to your main-view.fxml in resources
            loader.setLocation(getClass().getResource("/com/sanket/gstbilling/frontend/main-view.fxml"));
            VBox rootLayout = loader.load(); // Assuming main-view.fxml has a VBox root

            Scene scene = new Scene(rootLayout, 1000, 700); // Set initial window size
            primaryStage.setScene(scene);
            primaryStage.setTitle("GST Billing System"); // Set the window title
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading main-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
