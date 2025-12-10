package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import models.User;

import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;

    // Buttons for highlighting
    @FXML private Button dashboardButton;
    @FXML private Button employeesButton;
    @FXML private Button attendanceButton;
    @FXML private Button payrollButton;
    @FXML private Button departmentsButton;

    private User currentUser;
    private Button activeButton;

    @FXML
    public void initialize() {
        loadDashboard();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");

        // Note: Window is already maximized in LoginController
        // No need to maximize here
    }

    @FXML
    private void loadDashboard() {
        loadView("/resources/dashboard.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    private void loadEmployees() {
        loadView("/resources/employees.fxml");
        setActiveButton(employeesButton);
    }

    @FXML
    private void loadAttendance() {
        loadView("/resources/attendance.fxml");
        setActiveButton(attendanceButton);
    }

    @FXML
    private void loadPayroll() {
        loadView("/resources/payroll.fxml");
        setActiveButton(payrollButton);
    }

    @FXML
    private void loadDepartments() {
        loadView("/resources/departments-positions.fxml");
        setActiveButton(departmentsButton);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333;");
        }

        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        activeButton = button;
    }
}