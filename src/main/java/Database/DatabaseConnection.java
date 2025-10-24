package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void createTables() {
        // Same as before, just with package declaration
        try {
            Connection conn = getConnection();

            // ... (same SQL code as before)

            conn.close();
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }
}