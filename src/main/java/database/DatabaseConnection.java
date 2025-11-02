package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Database file path
    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    /**
     * connection to database
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    /**
     * Creates all database tables if they don't exist
     */
    public static void createTables() {
        try {
            Connection conn = getConnection();

            // 1. Create departments table
            String createDepartments = "CREATE TABLE IF NOT EXISTS departments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "description TEXT" +
                    ")";

            // 2. Create positions table
            String createPositions = "CREATE TABLE IF NOT EXISTS positions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "department_id INTEGER, " +
                    "base_salary REAL NOT NULL, " +
                    "description TEXT, " +
                    "FOREIGN KEY(department_id) REFERENCES departments(id)" +
                    ")";

            // 3. Create employees table
            String createEmployees = "CREATE TABLE IF NOT EXISTS employees (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "age INTEGER, " +
                    "position_id INTEGER, " +
                    "department_id INTEGER, " +
                    "hire_date TEXT, " +
                    "contact_info TEXT, " +
                    "qr_code TEXT UNIQUE, " +
                    "status TEXT DEFAULT 'active', " +
                    "FOREIGN KEY(position_id) REFERENCES positions(id), " +
                    "FOREIGN KEY(department_id) REFERENCES departments(id)" +
                    ")";

            // 4. Create attendance table
            String createAttendance = "CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "employee_id INTEGER, " +
                    "date TEXT NOT NULL, " +
                    "time_in TEXT, " +
                    "time_out TEXT, " +
                    "status TEXT, " +
                    "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                    ")";

            // 5. Create payroll table
            String createPayroll = "CREATE TABLE IF NOT EXISTS payroll (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "employee_id INTEGER, " +
                    "month INTEGER NOT NULL, " +
                    "year INTEGER NOT NULL, " +
                    "base_salary REAL, " +
                    "total_deductions REAL, " +
                    "final_salary REAL, " +
                    "late_count INTEGER, " +
                    "absent_count INTEGER, " +
                    "date_processed TEXT, " +
                    "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                    ")";

            // Execute all table creations
            conn.createStatement().execute(createDepartments);
            conn.createStatement().execute(createPositions);
            conn.createStatement().execute(createEmployees);
            conn.createStatement().execute(createAttendance);
            conn.createStatement().execute(createPayroll);

            conn.close();

        } catch (SQLException e) {
            System.out.println(" Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test the database connection
     */
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("Database connection successful!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}