import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize database and create tables
        DatabaseConnection.createTables();
        DatabaseConnection.createDefaultUser(); // Create default admin user

        // Load login screen instead of main window
        Parent root = FXMLLoader.load(getClass().getResource("/resources/login.fxml"));
        Scene scene = new Scene(root, 500, 550);
        stage.setTitle("HR Payroll System - Login");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}