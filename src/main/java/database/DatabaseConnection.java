
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    /**
     * Get connection to database
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

            // 1. Create users table (for login system)
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " + // 'admin' or 'hr_staff'
                    "status TEXT DEFAULT 'active'" +
                    ")";

            // 2. Create departments table
            String createDepartments = "CREATE TABLE IF NOT EXISTS departments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT" +
                    ")";

            // 3. Create positions table
            String createPositions = "CREATE TABLE IF NOT EXISTS positions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "department_id INTEGER, " +
                    "base_salary REAL NOT NULL, " +
                    "description TEXT, " +
                    "FOREIGN KEY(department_id) REFERENCES departments(id)" +
                    ")";

            // 4. Create employees table
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

            // 5. Create attendance table
            String createAttendance = "CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "employee_id INTEGER, " +
                    "date TEXT NOT NULL, " +
                    "time_in TEXT, " +
                    "time_out TEXT, " +
                    "status TEXT, " + // 'on-time', 'late', 'absent'
                    "hours_worked REAL DEFAULT 0, " +
                    "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                    ")";

            // 6. Create payroll table
            String createPayroll = "CREATE TABLE IF NOT EXISTS payroll (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "employee_id INTEGER, " +
                    "month INTEGER NOT NULL, " +
                    "year INTEGER NOT NULL, " +
                    "base_salary REAL, " +
                    "total_deductions REAL, " +
                    "final_salary REAL, " +
                    "late_count INTEGER DEFAULT 0, " +
                    "absent_count INTEGER DEFAULT 0, " +
                    "date_processed TEXT, " +
                    "notes TEXT, " + // For manual adjustments
                    "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                    ")";

            // Execute all table creations
            Statement stmt = conn.createStatement();
            stmt.execute(createUsers);
            stmt.execute(createDepartments);
            stmt.execute(createPositions);
            stmt.execute(createEmployees);
            stmt.execute(createAttendance);
            stmt.execute(createPayroll);

            stmt.close();
            conn.close();

            System.out.println("All database tables created successfully");

        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create default admin user if no users exist
     */
    public static void createDefaultUser() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();

            // Check if any users exist
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                // No users exist, create default admin
                String insertAdmin = "INSERT INTO users (username, password, full_name, role, status) " +
                        "VALUES ('admin', 'admin123', 'System Administrator', 'admin', 'active')";
                stmt.execute(insertAdmin);
                System.out.println("✓ Default admin user created (username: admin, password: admin123)");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("✗ Error creating default user: " + e.getMessage());
        }
    }

    /**
     * Test database connection
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