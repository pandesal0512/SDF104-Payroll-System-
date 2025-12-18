import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.ResponsiveUI;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Check if database is initialized, if not show setup message
        if (!isDatabaseInitialized()) {
            showDatabaseSetupRequired();
            return;
        }

        // Load login screen
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 500, 550);

        // Apply CSS if available
        try {
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            System.out.println("✓ CSS loaded");
        } catch (Exception e) {
            System.out.println("⚠ CSS not found - using default styles");
        }

        stage.setTitle("HR Payroll System - Login");
        stage.setScene(scene);
        stage.setResizable(false); // Login window shouldn't resize

        // Center the login window
        ResponsiveUI.initializeStage(stage, "HR Payroll System - Login", 500, 550);

        stage.show();

        // Print screen info for debugging
        ResponsiveUI.printScreenInfo();
    }

    /**
     * Check if database is properly initialized
     */
    private boolean isDatabaseInitialized() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if users table exists and has data
            ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='users'"
            );

            if (!rs.next()) {
                return false; // users table doesn't exist
            }

            // Check if admin user exists
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Database is initialized
            }

            return false;

        } catch (SQLException e) {
            return false; // Database not initialized or error
        }
    }

    /**
     * Show error message if database is not initialized
     */
    private void showDatabaseSetupRequired() {
        System.err.println(" DATABASE NOT INITIALIZED");
        // Exit the application
        javafx.application.Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}