package controllers;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    // Store logged-in user for session management
    private static User currentUser;

    @FXML
    public void initialize() {
        // Enter key triggers login
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        try {
            // Authenticate
            User user = userDAO.authenticateUser(username, password);

            if (user != null) {
                // Store current user
                currentUser = user;

                // Login successful - open main window
                openMainWindow();
            } else {
                showError("Invalid username or password");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openMainWindow() {
        try {
            // Load main.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            // Get the main controller and pass user info
            MainController mainController = loader.getController();
            mainController.setCurrentUser(currentUser);

            // Create new stage
            Stage stage = new Stage();
            stage.setTitle("HR Payroll & Attendance System - " + currentUser.getFullName());
            stage.setScene(new Scene(root, 1200, 800));
            stage.setMaximized(true);
            stage.show();

            // Close login window
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            showError("Failed to load main window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    // Static method to get current logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }
}