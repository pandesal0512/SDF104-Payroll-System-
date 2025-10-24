import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Database.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize database tables on first run
        DatabaseConnection.createTables();

        // Load main window
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(root);

        stage.setTitle("HR Payroll & Attendance System");
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}