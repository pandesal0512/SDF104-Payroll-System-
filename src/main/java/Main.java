import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Test database
        DatabaseConnection.createTables();

        //test UI
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 50; -fx-alignment: center;");

        Label label = new Label("Backend is working!");
        label.setStyle("-fx-font-size: 20px;");

        Button testButton = new Button("Test Database Connection");
        testButton.setOnAction(e -> {
            DatabaseConnection.testConnection();
            label.setText("Database connection tested! Check console.");
        });

        root.getChildren().addAll(label, testButton);

        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("HR Payroll - Backend Testing");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}