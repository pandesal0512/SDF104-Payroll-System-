package database;

import dao.ShiftDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * ALL-IN-ONE Database Setup
 * Creates all tables, columns, and default data
 * Run this ONCE when setting up the system
 */
public class DatabaseSetup {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    public static void main(String[] args) {
        System.out.println("    HR PAYROLL SYSTEM - DATABASE SETUP");
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();

            // Step 1: Create all core tables
            createCoreTables(stmt);

            // Step 2: Create feature tables
            createFeatureTables(stmt);

            // Step 3: Add additional columns to existing tables
            addAdditionalColumns(stmt);

            // Step 4: Create indexes for performance
            createIndexes(stmt);

            // Step 5: Insert default data
            insertDefaultData(stmt);

            stmt.close();
            conn.close();

            // Step 6: Initialize default shifts using DAO
            initializeShifts();

            System.out.println("    ✓ DATABASE SETUP COMPLETED SUCCESSFULLY!");
            System.out.println("\n Your system is ready to use!");
            System.out.println("   Login: admin / admin123\n");

        } catch (SQLException e) {
            System.err.println("\n DATABASE SETUP FAILED!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nPlease fix the error above and run again.");
        }
    }

    /**
     * Create all core tables
     */
    private static void createCoreTables(Statement stmt) throws SQLException {
        System.out.println(" Creating core tables...");

        // 1. Users table (for login system)
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL, " +
                        "full_name TEXT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "status TEXT DEFAULT 'active'" +
                        ")"
        );
        System.out.println("  users");

        // 2. Departments table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS departments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "description TEXT" +
                        ")"
        );
        System.out.println(" departments");

        // 3. Shifts table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS shifts (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "start_time TEXT NOT NULL, " +
                        "end_time TEXT NOT NULL, " +
                        "description TEXT, " +
                        "is_active INTEGER DEFAULT 1" +
                        ")"
        );
        System.out.println("  shifts");

        // 4. Positions table (with hourly_rate and shift_id)
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS positions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "department_id INTEGER, " +
                        "base_salary REAL NOT NULL, " +
                        "hourly_rate REAL DEFAULT 0.0, " +
                        "shift_id INTEGER, " +
                        "description TEXT, " +
                        "FOREIGN KEY(department_id) REFERENCES departments(id), " +
                        "FOREIGN KEY(shift_id) REFERENCES shifts(id)" +
                        ")"
        );
        System.out.println(" positions");

        // 5. Employees table (with all extended fields)
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS employees (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "age INTEGER, " +
                        "position_id INTEGER, " +
                        "department_id INTEGER, " +
                        "hire_date TEXT, " +
                        "contact_info TEXT, " +
                        "qr_code TEXT UNIQUE, " +
                        "status TEXT DEFAULT 'active', " +
                        "emergency_contact_name TEXT, " +
                        "emergency_contact_phone TEXT, " +
                        "profile_picture_path TEXT, " +
                        "shift_id INTEGER, " +
                        "FOREIGN KEY(position_id) REFERENCES positions(id), " +
                        "FOREIGN KEY(department_id) REFERENCES departments(id), " +
                        "FOREIGN KEY(shift_id) REFERENCES shifts(id)" +
                        ")"
        );
        System.out.println("  employees");

        // 6. Attendance table (with hours_worked and shift_id)
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS attendance (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "employee_id INTEGER, " +
                        "date TEXT NOT NULL, " +
                        "time_in TEXT, " +
                        "time_out TEXT, " +
                        "status TEXT, " +
                        "hours_worked REAL DEFAULT 0.0, " +
                        "shift_id INTEGER, " +
                        "FOREIGN KEY(employee_id) REFERENCES employees(id), " +
                        "FOREIGN KEY(shift_id) REFERENCES shifts(id)" +
                        ")"
        );
        System.out.println("  attendance");

        // 7. Payroll table (with all deduction columns)
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS payroll (" +
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
                        "notes TEXT, " +
                        "sss_deduction REAL DEFAULT 0, " +
                        "pagibig_deduction REAL DEFAULT 0, " +
                        "philhealth_deduction REAL DEFAULT 0, " +
                        "is_held INTEGER DEFAULT 0, " +
                        "total_adjustments REAL DEFAULT 0, " +
                        "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                        ")"
        );
        System.out.println("   ✓ payroll");

        System.out.println();
    }

    /**
     * Create feature tables (adjustments, holds, deductions)
     */
    private static void createFeatureTables(Statement stmt) throws SQLException {
        System.out.println("Creating feature tables...");

        // 1. Payroll Adjustments (bonuses, deductions, etc.)
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS payroll_adjustments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "payroll_id INTEGER, " +
                        "employee_id INTEGER NOT NULL, " +
                        "adjustment_type TEXT NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "reason TEXT, " +
                        "added_by INTEGER NOT NULL, " +
                        "approved_by INTEGER, " +
                        "date_added TEXT NOT NULL, " +
                        "date_approved TEXT, " +
                        "status TEXT DEFAULT 'pending', " +
                        "notes TEXT, " +
                        "FOREIGN KEY(payroll_id) REFERENCES payroll(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(employee_id) REFERENCES employees(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(added_by) REFERENCES users(id), " +
                        "FOREIGN KEY(approved_by) REFERENCES users(id)" +
                        ")"
        );
        System.out.println("  payroll_adjustments");

        // 2. Salary Holds (hold/release salary)
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS salary_holds (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "employee_id INTEGER NOT NULL, " +
                        "reason TEXT NOT NULL, " +
                        "hold_date TEXT NOT NULL, " +
                        "release_date TEXT, " +
                        "held_by INTEGER NOT NULL, " +
                        "released_by INTEGER, " +
                        "status TEXT DEFAULT 'active', " +
                        "notes TEXT, " +
                        "created_at TEXT DEFAULT (datetime('now')), " +
                        "FOREIGN KEY(employee_id) REFERENCES employees(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(held_by) REFERENCES users(id), " +
                        "FOREIGN KEY(released_by) REFERENCES users(id)" +
                        ")"
        );
        System.out.println("   ✓ salary_holds");

        // 3. Government Deductions Configuration
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS government_deductions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "deduction_type TEXT NOT NULL, " +
                        "salary_range_min REAL, " +
                        "salary_range_max REAL, " +
                        "employee_share REAL NOT NULL, " +
                        "employer_share REAL, " +
                        "is_active INTEGER DEFAULT 1, " +
                        "effective_date TEXT, " +
                        "notes TEXT" +
                        ")"
        );
        System.out.println("   ✓ government_deductions");

        System.out.println();
    }

    /**
     * Add additional columns to existing tables (for upgrades)
     */
    private static void addAdditionalColumns(Statement stmt) {
        System.out.println("Adding additional columns (if needed)...");

        // These are for systems upgrading from older versions
        // The CREATE TABLE statements above already include these,
        // but this ensures backward compatibility

        addColumnSafely(stmt, "attendance", "hours_worked", "REAL DEFAULT 0.0");
        addColumnSafely(stmt, "attendance", "shift_id", "INTEGER");
        addColumnSafely(stmt, "positions", "hourly_rate", "REAL DEFAULT 0.0");
        addColumnSafely(stmt, "positions", "shift_id", "INTEGER");
        addColumnSafely(stmt, "employees", "emergency_contact_name", "TEXT");
        addColumnSafely(stmt, "employees", "emergency_contact_phone", "TEXT");
        addColumnSafely(stmt, "employees", "profile_picture_path", "TEXT");
        addColumnSafely(stmt, "employees", "shift_id", "INTEGER");
        addColumnSafely(stmt, "payroll", "notes", "TEXT");
        addColumnSafely(stmt, "payroll", "sss_deduction", "REAL DEFAULT 0");
        addColumnSafely(stmt, "payroll", "pagibig_deduction", "REAL DEFAULT 0");
        addColumnSafely(stmt, "payroll", "philhealth_deduction", "REAL DEFAULT 0");
        addColumnSafely(stmt, "payroll", "is_held", "INTEGER DEFAULT 0");
        addColumnSafely(stmt, "payroll", "total_adjustments", "REAL DEFAULT 0");

        System.out.println("   ✓ All columns verified");
        System.out.println();
    }

    /**
     * Create indexes for better performance
     */
    private static void createIndexes(Statement stmt) throws SQLException {
        System.out.println("⚡ Creating indexes for performance...");

        // Shift indexes
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_shifts_active ON shifts(is_active)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_positions_shift ON positions(shift_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_employees_shift ON employees(shift_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_shift ON attendance(shift_id)");

        // Adjustment indexes
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_adjustments_employee ON payroll_adjustments(employee_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_adjustments_payroll ON payroll_adjustments(payroll_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_adjustments_status ON payroll_adjustments(status)");

        // Salary hold indexes
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_holds_employee ON salary_holds(employee_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_holds_status ON salary_holds(status)");

        System.out.println("   ✓ All indexes created");
        System.out.println();
    }

    /**
     * Insert default data (admin user, government rates)
     */
    private static void insertDefaultData(Statement stmt) throws SQLException {
        System.out.println(" Inserting default data...");

        // 1. Create default admin user
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.execute(
                    "INSERT INTO users (username, password, full_name, role, status) " +
                            "VALUES ('admin', 'admin123', 'System Administrator', 'admin', 'active')"
            );
            System.out.println("   ✓ Default admin user created");
        } else {
            System.out.println("   ⚠ Users already exist (skipped)");
        }

        // 2. Insert government deduction rates
        rs = stmt.executeQuery("SELECT COUNT(*) FROM government_deductions");
        if (rs.next() && rs.getInt(1) == 0) {
            // SSS rates (simplified brackets)
            stmt.execute(
                    "INSERT INTO government_deductions (deduction_type, salary_range_min, salary_range_max, employee_share, is_active) VALUES " +
                            "('SSS', 0, 4250, 180, 1)," +
                            "('SSS', 4250, 8750, 360, 1)," +
                            "('SSS', 8750, 13250, 585, 1)," +
                            "('SSS', 13250, 17750, 810, 1)," +
                            "('SSS', 17750, 22250, 990, 1)," +
                            "('SSS', 22250, 999999, 1125, 1)"
            );

            // PhilHealth rates
            stmt.execute(
                    "INSERT INTO government_deductions (deduction_type, salary_range_min, salary_range_max, employee_share, is_active, notes) VALUES " +
                            "('PHILHEALTH', 0, 10000, 200, 1, 'Minimum contribution')," +
                            "('PHILHEALTH', 10000, 999999, 0, 1, '2% of salary, max 1800')"
            );

            // Pag-IBIG rates
            stmt.execute(
                    "INSERT INTO government_deductions (deduction_type, salary_range_min, salary_range_max, employee_share, is_active, notes) VALUES " +
                            "('PAGIBIG', 0, 1500, 0, 1, '1% of salary')," +
                            "('PAGIBIG', 1500, 999999, 0, 1, '2% of salary, max 100')"
            );

            System.out.println("  Government deduction rates inserted");
        } else {
            System.out.println("  Deduction rates already exist (skipped)");
        }

        // 3. Calculate hourly rates for existing positions
        int updated = stmt.executeUpdate(
                "UPDATE positions SET hourly_rate = base_salary / 160.0 " +
                        "WHERE hourly_rate = 0.0 OR hourly_rate IS NULL"
        );
        if (updated > 0) {
            System.out.println("   ✓ Calculated hourly rates for " + updated + " position(s)");
        }

        System.out.println();
    }

    /**
     * Initialize default shifts using ShiftDAO
     */
    private static void initializeShifts() {
        System.out.println("Initializing default shifts...");
        try {
            ShiftDAO shiftDAO = new ShiftDAO();
            shiftDAO.initializeDefaultShifts();
            System.out.println("  Default shifts created");
        } catch (Exception e) {
            System.out.println("   Could not initialize shifts: " + e.getMessage());
        }
    }

    /**
     * Safely add column (checks if exists first)
     */
    private static void addColumnSafely(Statement stmt, String table, String column, String type) {
        try {
            stmt.execute(String.format("ALTER TABLE %s ADD COLUMN %s %s", table, column, type));
            System.out.println(String.format("   ✓ Added %s.%s", table, column));
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate column")) {
                // Column already exists - this is fine
            } else {
                System.err.println(String.format("   ⚠ Warning for %s.%s: %s", table, column, e.getMessage()));
            }
        }
    }

    /**
     * Test database connection
     */
    public static void testConnection() {
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("✓ Database connection successful!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("✗ Database connection failed: " + e.getMessage());
        }
    }
}