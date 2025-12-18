package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple Database Connection Manager
 * Use DatabaseSetup.java to initialize the database
 */
public class DatabaseConnection {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    /**
     * Get connection to database
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    /**
     * Test database connection
     */
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println(" Database connection successful!");
            conn.close();
        } catch (SQLException e) {
            System.out.println(" Database connection failed: " + e.getMessage());
        }
    }
}