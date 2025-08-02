module com.sanket.gstbillingsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.sanket.gstbillingsystem to javafx.fxml;
    exports com.sanket.gstbillingsystem;
}