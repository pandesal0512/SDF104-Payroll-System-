package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    /**
     * Initialize method - called after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Load dashboard by default
        loadDashboard();
    }

    /**
     * Load Dashboard view
     */
    @FXML
    private void loadDashboard() {
        loadView("/fxml/dashboard.fxml");
    }

    /**
     * Load Employees view
     */
    @FXML
    private void loadEmployees() {
        loadView("/fxml/employees.fxml");
    }

    /**
     * Load Attendance view
     */
    @FXML
    private void loadAttendance() {
        loadView("/fxml/attendance.fxml");
    }

    /**
     * Load Payroll view
     */
    @FXML
    private void loadPayroll() {
        loadView("/fxml/payroll.fxml");
    }

    /**
     * Load Departments view
     */
    @FXML
    private void loadDepartments() {
        loadView("/fxml/departments.fxml");
    }

    /**
     * Load Positions view
     */
    @FXML
    private void loadPositions() {
        loadView("/fxml/positions.fxml");
    }

    /**
     * Helper method to load any FXML view into the content area
     */
    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load view", "Could not load " + fxmlPath);
        }
    }

    /**
     * Exit application
     */
    @FXML
    private void handleExit() {
        // Get the current stage
        Stage stage = (Stage) contentArea.getScene().getWindow();

        // Confirm exit
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Exit Application");
        alert.setContentText("Are you sure you want to exit?");

        alert.showAndWait().ifPresent(response -> {
            if (response.getText().equals("OK")) {
                stage.close();
            }
        });
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}