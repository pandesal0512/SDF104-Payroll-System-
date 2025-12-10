package controllers;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;

import java.sql.SQLException;

/**
 * Controller for the Login Screen
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Set up Enter key listener for password field
        passwordField.setOnAction(event -> handleLogin());

        // Set up Enter key listener for username field
        usernameField.setOnAction(event -> passwordField.requestFocus());
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Disable login button during authentication
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            // Authenticate user
            User user = userDAO.authenticateUser(username, password);

            if (user != null) {
                // Login successful
                System.out.println("✓ Login successful: " + user.getFullName() + " (" + user.getRole() + ")");
                openMainWindow(user);
            } else {
                // Login failed
                showError("Invalid username or password");
                loginButton.setDisable(false);
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            loginButton.setDisable(false);
            e.printStackTrace();
        }
    }

    /**
     * Open the main application window
     */
    private void openMainWindow(User user) {
        try {
            // Load main window FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/main.fxml"));
            Parent root = loader.load();

            // Get the controller and set the logged-in user
            MainController mainController = loader.getController();
            mainController.setCurrentUser(user);

            // Create new stage for main window
            Stage mainStage = new Stage();
            Scene scene = new Scene(root, 1920, 1080);
            mainStage.setScene(scene);
            mainStage.setTitle("HR Payroll System - " + user.getFullName());
            mainStage.setMaximized(true);
            mainStage.show();

            // Close login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            showError("Failed to open main window: " + e.getMessage());
            loginButton.setDisable(false);
            e.printStackTrace();
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Clear form fields
     */
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
    }
}