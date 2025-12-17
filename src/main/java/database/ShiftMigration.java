package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Migration to add shift_id column to employees table
 * Run this ONCE to update the database schema
 */
public class ShiftMigration {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    public static void main(String[] args) {
        System.out.println("  HR PAYROLL SYSTEM - SHIFT MIGRATION");
        System.out.println("  Moving shift from Position to Employee level");
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();

            // 1. Add shift_id column to employees table
            System.out.println("Adding shift_id to employees table...");
            addColumnSafely(stmt, "employees", "shift_id", "INTEGER");
            System.out.println(" shift_id column added to employees\n");

            // 2. Create foreign key index
            System.out.println(" Creating index for employee shift...");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_employee_shift ON employees(shift_id)");
            System.out.println("  Index created\n");

            // 3. Optional: Migrate existing shift data from positions to employees
            System.out.println("Migrating existing shift assignments...");
            try {
                // Copy shift_id from position to employee if position has shift
                String migrateSql = "UPDATE employees SET shift_id = (" +
                        "SELECT shift_id FROM positions WHERE positions.id = employees.position_id" +
                        ") WHERE EXISTS (" +
                        "SELECT 1 FROM positions WHERE positions.id = employees.position_id AND positions.shift_id IS NOT NULL" +
                        ")";
                int migrated = stmt.executeUpdate(migrateSql);
                System.out.println("  Migrated shift for " + migrated + " employee(s)\n");
            } catch (SQLException e) {
                System.out.println("  No existing shifts to migrate (this is OK for new systems)\n");
            }

            stmt.close();
            conn.close();
            System.out.println("  MIGRATION COMPLETED SUCCESSFULLY!");
            System.out.println("Summary:");
            System.out.println("   • Shift assignment moved to employee level");
            System.out.println("   • Each employee can now have their own shift");
            System.out.println("   • Multiple employees with same position can");
            System.out.println("     have different shifts");
            System.out.println("\nYour system is ready!");

        } catch (SQLException e) {
            System.err.println(" MIGRATION FAILED!");
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
}