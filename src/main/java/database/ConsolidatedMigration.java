package database;

import dao.ShiftDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class ConsolidatedMigration {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("  CONSOLIDATED DATABASE MIGRATION");
        System.out.println("  Setting up complete shift system");
        System.out.println("═══════════════════════════════════════════════════════\n");

        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();

            // PART 1: Fix existing columns
            System.out.println("PART 1: Updating existing tables");
            System.out.println("─────────────────────────────────────────────────────");

            // 1.1: Add 'hours_worked' to attendance
            System.out.println("1.1 Adding 'hours_worked' to attendance...");
            try {
                stmt.execute("ALTER TABLE attendance ADD COLUMN hours_worked REAL DEFAULT 0.0");
                System.out.println("    ✓ Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("    ✓ Already exists");
                } else {
                    System.out.println("    ✗ Error: " + e.getMessage());
                }
            }

            // 1.2: Add 'notes' to payroll
            System.out.println("1.2 Adding 'notes' to payroll...");
            try {
                stmt.execute("ALTER TABLE payroll ADD COLUMN notes TEXT");
                System.out.println("    ✓ Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("    ✓ Already exists");
                } else {
                    System.out.println("    ✗ Error: " + e.getMessage());
                }
            }

            // 1.3: Add 'hourly_rate' to positions
            System.out.println("1.3 Adding 'hourly_rate' to positions...");
            try {
                stmt.execute("ALTER TABLE positions ADD COLUMN hourly_rate REAL DEFAULT 0.0");
                System.out.println("    ✓ Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("    ✓ Already exists");
                } else {
                    System.out.println("    ✗ Error: " + e.getMessage());
                }
            }

            // 1.4: Calculate hourly rates
            System.out.println("1.4 Calculating hourly rates...");
            int updated = stmt.executeUpdate(
                    "UPDATE positions SET hourly_rate = base_salary / 160 " +
                            "WHERE hourly_rate = 0.0 OR hourly_rate IS NULL"
            );
            System.out.println("    ✓ Updated " + updated + " position(s)");

            // PART 2: Create shifts table
            System.out.println("\nPART 2: Creating shift system");
            System.out.println("─────────────────────────────────────────────────────");

            // 2.1: Create shifts table
            System.out.println("2.1 Creating shifts table...");
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
            System.out.println("    ✓ Created");

            // 2.2: Add shift_id to positions
            System.out.println("2.2 Adding shift_id to positions...");
            try {
                stmt.execute("ALTER TABLE positions ADD COLUMN shift_id INTEGER");
                System.out.println("    ✓ Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("    ✓ Already exists");
                } else {
                    System.out.println("    ✗ Error: " + e.getMessage());
                }
            }

            // 2.3: Add shift_id to attendance
            System.out.println("2.3 Adding shift_id to attendance...");
            try {
                stmt.execute("ALTER TABLE attendance ADD COLUMN shift_id INTEGER");
                System.out.println("    ✓ Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("    ✓ Already exists");
                } else {
                    System.out.println("    ✗ Error: " + e.getMessage());
                }
            }

            // 2.4: Create indexes
            System.out.println("2.4 Creating indexes...");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shifts_active ON shifts(is_active)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_positions_shift ON positions(shift_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_shift ON attendance(shift_id)");
            System.out.println("    ✓ Indexes created");

            // PART 3: Create default shifts
            System.out.println("\nPART 3: Creating default shifts");
            System.out.println("─────────────────────────────────────────────────────");

            stmt.close();
            conn.close();

            // Use ShiftDAO to create default shifts
            ShiftDAO shiftDAO = new ShiftDAO();
            shiftDAO.initializeDefaultShifts();

            // SUMMARY
            System.out.println("\n═══════════════════════════════════════════════════════");
            System.out.println("  ✓ MIGRATION COMPLETED SUCCESSFULLY!");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("\n✅ Your database now has:");
            System.out.println("   • hours_worked column in attendance");
            System.out.println("   • hourly_rate column in positions");
            System.out.println("   • shift_id column in positions");
            System.out.println("   • shift_id column in attendance");
            System.out.println("   • shifts table with 3 default shifts");
            System.out.println("\n📋 Default Shifts:");
            System.out.println("   ☀️  Day Shift:     05:00 AM - 01:30 PM");
            System.out.println("   🌆  Evening Shift: 01:30 PM - 10:00 PM");
            System.out.println("   🌙  Night Shift:   10:00 PM - 06:30 AM");
            System.out.println("\n🎯 Next Steps:");
            System.out.println("   1. Restart your application");
            System.out.println("   2. Go to Departments & Positions");
            System.out.println("   3. Create/Edit positions and assign shifts");
            System.out.println("   4. Employees will now use shift-based late detection");

        } catch (SQLException e) {
            System.out.println("\n═══════════════════════════════════════════════════════");
            System.out.println("  ✗ MIGRATION FAILED!");
            System.out.println("═══════════════════════════════════════════════════════");
            System.err.println("\nError details:");
            e.printStackTrace();
            System.out.println("\n⚠️  Please check the error above and try again.");
        }
    }
}