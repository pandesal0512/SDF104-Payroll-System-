package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Migration for Revision Features
 * Run this ONCE to add new tables and columns
 */
public class RevisionMigration {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    public static void main(String[] args) {
        System.out.println("  HR PAYROLL SYSTEM - REVISION MIGRATION");

        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();

            // 1. Employee Emergency Contact & Profile Picture
            System.out.println("  Adding emergency contact fields to employees...");
            addColumnSafely(stmt, "employees", "emergency_contact_name", "TEXT");
            addColumnSafely(stmt, "employees", "emergency_contact_phone", "TEXT");
            addColumnSafely(stmt, "employees", "profile_picture_path", "TEXT");
            System.out.println("    Employee fields added\n");

            // 2. Payroll Adjustments Table
            System.out.println("Creating payroll adjustments table...");
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS payroll_adjustments (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    payroll_id INTEGER," +
                            "    employee_id INTEGER NOT NULL," +
                            "    adjustment_type TEXT NOT NULL," +
                            "    amount REAL NOT NULL," +
                            "    reason TEXT," +
                            "    added_by INTEGER NOT NULL," +
                            "    approved_by INTEGER," +
                            "    date_added TEXT NOT NULL," +
                            "    date_approved TEXT," +
                            "    status TEXT DEFAULT 'pending'," +
                            "    notes TEXT," +
                            "    FOREIGN KEY(payroll_id) REFERENCES payroll(id) ON DELETE CASCADE," +
                            "    FOREIGN KEY(employee_id) REFERENCES employees(id) ON DELETE CASCADE," +
                            "    FOREIGN KEY(added_by) REFERENCES users(id)," +
                            "    FOREIGN KEY(approved_by) REFERENCES users(id)" +
                            ")"
            );
            System.out.println("   ✓ Adjustments table created\n");

            // 3. Salary Holds Table
            System.out.println("Creating salary holds table...");
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS salary_holds (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    employee_id INTEGER NOT NULL," +
                            "    reason TEXT NOT NULL," +
                            "    hold_date TEXT NOT NULL," +
                            "    release_date TEXT," +
                            "    held_by INTEGER NOT NULL," +
                            "    released_by INTEGER," +
                            "    status TEXT DEFAULT 'active'," +
                            "    notes TEXT," +
                            "    created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                            "    FOREIGN KEY(employee_id) REFERENCES employees(id) ON DELETE CASCADE," +
                            "    FOREIGN KEY(held_by) REFERENCES users(id)," +
                            "    FOREIGN KEY(released_by) REFERENCES users(id)" +
                            ")"
            );
            System.out.println("   ✓ Salary holds table created\n");

            // 4. Update Payroll Table
            System.out.println("4️⃣  Adding new columns to payroll table...");
            addColumnSafely(stmt, "payroll", "sss_deduction", "REAL DEFAULT 0");
            addColumnSafely(stmt, "payroll", "pagibig_deduction", "REAL DEFAULT 0");
            addColumnSafely(stmt, "payroll", "philhealth_deduction", "REAL DEFAULT 0");
            addColumnSafely(stmt, "payroll", "is_held", "INTEGER DEFAULT 0");
            addColumnSafely(stmt, "payroll", "total_adjustments", "REAL DEFAULT 0");
            System.out.println("   ✓ Payroll columns added\n");

            // 5. Create Indexes
            System.out.println("Creating indexes for performance...");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_adjustments_employee ON payroll_adjustments(employee_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_adjustments_payroll ON payroll_adjustments(payroll_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_adjustments_status ON payroll_adjustments(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_holds_employee ON salary_holds(employee_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_holds_status ON salary_holds(status)");
            System.out.println("   ✓ Indexes created\n");

            // 6. Government Deductions Configuration
            System.out.println(" Creating government deductions configuration...");
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS government_deductions (" +
                            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    deduction_type TEXT NOT NULL," +
                            "    salary_range_min REAL," +
                            "    salary_range_max REAL," +
                            "    employee_share REAL NOT NULL," +
                            "    employer_share REAL," +
                            "    is_active INTEGER DEFAULT 1," +
                            "    effective_date TEXT," +
                            "    notes TEXT" +
                            ")"
            );
            System.out.println("   ✓ Government deductions table created\n");

            // 7. Insert Default Rates
            System.out.println("Inserting default deduction rates...");
            insertDefaultRates(stmt);
            System.out.println("   ✓ Default rates inserted\n");

            stmt.close();
            conn.close();
            System.out.println("   MIGRATION COMPLETED SUCCESSFULLY!");
            System.out.println(" Summary:");
            System.out.println("   • Emergency contact fields added to employees");
            System.out.println("   • Profile picture support added");
            System.out.println("   • Payroll adjustments with audit trail");
            System.out.println("   • Salary hold feature");
            System.out.println("   • Government deduction calculations");
            System.out.println("   • Performance indexes created");
            System.out.println("\nSystem is ready for new features!");

        } catch (SQLException e) {
            System.err.println("MIGRATION FAILED!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println(String.format("   ⚠ %s.%s already exists (skipped)", table, column));
            } else {
                System.err.println(String.format("   ✗ Failed to add %s.%s: %s", table, column, e.getMessage()));
            }
        }
    }

    /**
     * Insert default government deduction rates
     */
    private static void insertDefaultRates(Statement stmt) throws SQLException {
        // Check if rates already exist
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM government_deductions");
        if (rs.next() && rs.getInt(1) > 0) {
            System.out.println("   ⚠ Deduction rates already exist (skipped)");
            return;
        }

        // Insert SSS rates (simplified brackets)
        stmt.execute(
                "INSERT INTO government_deductions (deduction_type, salary_range_min, salary_range_max, employee_share, is_active) VALUES " +
                        "('SSS', 0, 4250, 180, 1)," +
                        "('SSS', 4250, 8750, 360, 1)," +
                        "('SSS', 8750, 13250, 585, 1)," +
                        "('SSS', 13250, 17750, 810, 1)," +
                        "('SSS', 17750, 22250, 990, 1)," +
                        "('SSS', 22250, 999999, 1125, 1)"
        );

        // Insert PhilHealth rates (2% of salary, max 1800)
        stmt.execute(
                "INSERT INTO government_deductions (deduction_type, salary_range_min, salary_range_max, employee_share, is_active, notes) VALUES " +
                        "('PHILHEALTH', 0, 10000, 200, 1, 'Minimum contribution')," +
                        "('PHILHEALTH', 10000, 999999, 0, 1, '2% of salary, max 1800')"
        );

        // Insert Pag-IBIG rates (1-2% of salary, max 100)
        stmt.execute(
                "INSERT INTO government_deductions (deduction_type, salary_range_min, salary_range_max, employee_share, is_active, notes) VALUES " +
                        "('PAGIBIG', 0, 1500, 0, 1, '1% of salary')," +
                        "('PAGIBIG', 1500, 999999, 0, 1, '2% of salary, max 100')"
        );

        System.out.println("   ✓ SSS rates: 6 brackets");
        System.out.println("   ✓ PhilHealth rates: 2% calculation");
        System.out.println("   ✓ Pag-IBIG rates: 1-2% calculation");
    }
}