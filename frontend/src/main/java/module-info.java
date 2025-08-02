module com.sanket.gstbilling.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http; // For HttpClient, HttpRequest, HttpResponse
    requires javafx.base; // Add this for StringConverter, ReadOnlyStringWrapper, ReadOnlyObjectWrapper
    requires java.base;
    requires java.desktop; // ADDED THIS LINE FOR PDF OPENING

    // Explicitly require Jackson modules
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation; // For @JsonFormat, @JsonDeserialize, @JsonSerialize
    requires com.fasterxml.jackson.datatype.jsr310; // For LocalDate support

    opens com.sanket.gstbilling.frontend to javafx.fxml;
    opens com.sanket.gstbilling.frontend.controller to javafx.fxml; // Open controller package for FXML access
    opens com.sanket.gstbilling.frontend.model to com.fasterxml.jackson.databind; // Needed for Jackson deserialization

    exports com.sanket.gstbilling.frontend;
    exports com.sanket.gstbilling.frontend.controller;
    exports com.sanket.gstbilling.frontend.model; // Export model package
}
