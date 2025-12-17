package database;

import dao.ShiftDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class ConsolidatedMigration {

    private static final String DATABASE_URL = "jdbc:sqlite:payroll.db";

    public static void main(String[] args) {
        System.out.println("  Setting up complete shift system");

        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();


            // 1.1: Add 'hours_worked' to attendance
            System.out.println("1.1 Adding 'hours_worked' to attendance...");
            try {
                stmt.execute("ALTER TABLE attendance ADD COLUMN hours_worked REAL DEFAULT 0.0");
                System.out.println("Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("Already exists");
                } else {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            // 1.2: Add 'notes' to payroll
            System.out.println("1.2 Adding 'notes' to payroll...");
            try {
                stmt.execute("ALTER TABLE payroll ADD COLUMN notes TEXT");
                System.out.println("Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("Already exists");
                } else {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            // 1.3: Add 'hourly_rate' to positions
            System.out.println("1.3 Adding 'hourly_rate' to positions...");
            try {
                stmt.execute("ALTER TABLE positions ADD COLUMN hourly_rate REAL DEFAULT 0.0");
                System.out.println("Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("Already exists");
                } else {
                    System.out.println(" Error: " + e.getMessage());
                }
            }

            // 1.4: Calculate hourly rates
            System.out.println("1.4 Calculating hourly rates...");
            int updated = stmt.executeUpdate(
                    "UPDATE positions SET hourly_rate = base_salary / 160 " +
                            "WHERE hourly_rate = 0.0 OR hourly_rate IS NULL"
            );
            System.out.println("     Updated " + updated + " position(s)");

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
            System.out.println("    Created");

            // 2.2: Add shift_id to positions
            System.out.println("2.2 Adding shift_id to positions...");
            try {
                stmt.execute("ALTER TABLE positions ADD COLUMN shift_id INTEGER");
                System.out.println("Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("    Already exists");
                } else {
                    System.out.println("    Error: " + e.getMessage());
                }
            }

            // 2.3: Add shift_id to attendance
            System.out.println("2.3 Adding shift_id to attendance...");
            try {
                stmt.execute("ALTER TABLE attendance ADD COLUMN shift_id INTEGER");
                System.out.println("     Added");
            } catch (SQLException e) {
                if (e.getMessage().contains("duplicate column")) {
                    System.out.println("    Already exists");
                } else {
                    System.out.println("     Error: " + e.getMessage());
                }
            }

            // 2.4: Create indexes
            System.out.println("2.4 Creating indexes...");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shifts_active ON shifts(is_active)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_positions_shift ON positions(shift_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_shift ON attendance(shift_id)");
            System.out.println("Indexes created");

            stmt.close();
            conn.close();

            // Use ShiftDAO to create default shifts
            ShiftDAO shiftDAO = new ShiftDAO();
            shiftDAO.initializeDefaultShifts();

        } catch (SQLException e) {
            System.out.println("   MIGRATION FAILED!");
            System.err.println("\nError details:");
            e.printStackTrace();
            System.out.println("\nPlease check the error above and try again.");
        }
    }
}