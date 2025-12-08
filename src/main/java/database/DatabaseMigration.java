package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Migration Script - Run this to fix all database issues
 */
public class DatabaseMigration {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  DATABASE MIGRATION - FIXING ALL ISSUES");
        System.out.println("==============================================\n");

        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();

            // Fix 1: Add 'hours_worked' column to attendance table
            System.out.println("1. Checking 'hours_worked' column in attendance table...");
            try {
                String addHoursColumn = "ALTER TABLE attendance ADD COLUMN hours_worked REAL DEFAULT 0.0";
                stmt.execute(addHoursColumn);
                System.out.println("   ✓ Added 'hours_worked' column to attendance table");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("   ✓ 'hours_worked' column already exists");
                } else {
                    System.out.println("   ✗ Error: " + e.getMessage());
                }
            }

            // Fix 2: Add 'notes' column to payroll table
            System.out.println("\n2. Checking 'notes' column in payroll table...");
            try {
                String addNotesColumn = "ALTER TABLE payroll ADD COLUMN notes TEXT";
                stmt.execute(addNotesColumn);
                System.out.println("   ✓ Added 'notes' column to payroll table");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("   ✓ 'notes' column already exists");
                } else {
                    System.out.println("   ✗ Error: " + e.getMessage());
                }
            }

            // Fix 3: Add 'hourly_rate' column to positions table
            System.out.println("\n3. Checking 'hourly_rate' column in positions table...");
            try {
                String addHourlyRateColumn = "ALTER TABLE positions ADD COLUMN hourly_rate REAL DEFAULT 0.0";
                stmt.execute(addHourlyRateColumn);
                System.out.println("   ✓ Added 'hourly_rate' column to positions table");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("   ✓ 'hourly_rate' column already exists");
                } else {
                    System.out.println("   ✗ Error: " + e.getMessage());
                }
            }

            // Fix 4: Update existing positions with hourly rates
            System.out.println("\n4. Updating hourly rates for existing positions...");
            try {
                String updateHourlyRates = "UPDATE positions SET hourly_rate = base_salary / 160 WHERE hourly_rate = 0.0 OR hourly_rate IS NULL";
                int updated = stmt.executeUpdate(updateHourlyRates);
                System.out.println("   ✓ Updated " + updated + " position(s) with calculated hourly rates");
            } catch (SQLException e) {
                System.out.println("   ✗ Error: " + e.getMessage());
            }

            // Fix 5: Update attendance records to calculate hours worked
            System.out.println("\n5. Calculating hours worked for existing attendance records...");
            try {
                String updateHours =
                        "UPDATE attendance " +
                                "SET hours_worked = " +
                                "CASE " +
                                "  WHEN time_out IS NOT NULL AND time_out != '' THEN " +
                                "    (CAST(substr(time_out, 1, 2) AS INTEGER) * 60 + CAST(substr(time_out, 4, 2) AS INTEGER) - " +
                                "     CAST(substr(time_in, 1, 2) AS INTEGER) * 60 - CAST(substr(time_in, 4, 2) AS INTEGER)) / 60.0 " +
                                "  ELSE 0.0 " +
                                "END " +
                                "WHERE (hours_worked = 0.0 OR hours_worked IS NULL) " +
                                "AND time_in IS NOT NULL AND time_in != ''";
                int updated = stmt.executeUpdate(updateHours);
                System.out.println("   ✓ Calculated hours for " + updated + " attendance record(s)");
            } catch (SQLException e) {
                System.out.println("   ✗ Error: " + e.getMessage());
            }

            stmt.close();
            conn.close();

            System.out.println("\n==============================================");
            System.out.println("  MIGRATION COMPLETED SUCCESSFULLY! ✓");
            System.out.println("==============================================");
            System.out.println("\nYou can now run your application.");

        } catch (SQLException e) {
            System.out.println("\n==============================================");
            System.out.println("  MIGRATION FAILED! ✗");
            System.out.println("==============================================");
            e.printStackTrace();
        }
    }
}