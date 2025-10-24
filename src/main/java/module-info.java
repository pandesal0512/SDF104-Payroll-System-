module com.example.payrollattendance {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // Optional UI styling library (BootstrapFX)
    requires org.kordamp.bootstrapfx.core;

    // Database access
    requires java.sql;

    // If you plan to use QR generation later:
    // requires com.google.zxing;
    // requires com.google.zxing.javase;

    // Allow FXML to access controller classes inside this package
    opens com.example.payrollattendance to javafx.fxml;
    opens com.example.payrollattendance.controllers to javafx.fxml;


}
