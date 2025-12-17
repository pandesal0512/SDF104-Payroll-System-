import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.ResponsiveUI;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize database
        DatabaseConnection.createTables();
        DatabaseConnection.createDefaultUser();

        // Load login screen
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root, 500, 550);

        // Apply CSS if available
        try {
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            System.out.println(" CSS loaded");
        } catch (Exception e) {
            System.out.println("CSS not found - using default styles");
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

    public static void main(String[] args) {
        launch(args);
    }
}