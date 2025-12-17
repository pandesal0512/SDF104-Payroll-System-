package dao;

import database.DatabaseConnection;
import models.SalaryHold;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Salary Hold feature
 */
public class SalaryHoldDAO {

    /**
     * Create a new salary hold
     */
    public void holdSalary(SalaryHold hold) throws SQLException {
        String sql = "INSERT INTO salary_holds " +
                "(employee_id, reason, hold_date, held_by, notes, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, 'active', datetime('now'))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, hold.getEmployeeId());
            stmt.setString(2, hold.getReason());
            stmt.setString(3, hold.getHoldDate());
            stmt.setInt(4, hold.getHeldBy());
            stmt.setString(5, hold.getNotes());

            stmt.executeUpdate();

            // Get generated ID
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                hold.setId(keys.getInt(1));
            }

            System.out.println("✓ Salary held for employee #" + hold.getEmployeeId());
        }
    }

    /**
     * Release a salary hold
     */
    public void releaseSalary(int holdId, int releasedBy, String releaseDate) throws SQLException {
        String sql = "UPDATE salary_holds SET status = 'released', " +
                "released_by = ?, release_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, releasedBy);
            stmt.setString(2, releaseDate);
            stmt.setInt(3, holdId);
            stmt.executeUpdate();

            System.out.println("✓ Salary hold #" + holdId + " released");
        }
    }

    /**
     * Check if employee's salary is currently held
     */
    public boolean isSalaryHeld(int employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM salary_holds " +
                "WHERE employee_id = ? AND status = 'active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Get active hold for employee
     */
    public SalaryHold getActiveHold(int employeeId) throws SQLException {
        String sql = "SELECT * FROM salary_holds " +
                "WHERE employee_id = ? AND status = 'active' " +
                "ORDER BY hold_date DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractSalaryHold(rs);
            }
        }
        return null;
    }

    /**
     * Get all holds for employee (active and released)
     */
    public List<SalaryHold> getHoldsByEmployee(int employeeId) throws SQLException {
        List<SalaryHold> holds = new ArrayList<>();
        String sql = "SELECT * FROM salary_holds WHERE employee_id = ? " +
                "ORDER BY hold_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                holds.add(extractSalaryHold(rs));
            }
        }
        return holds;
    }

    /**
     * Get all active holds
     */
    public List<SalaryHold> getAllActiveHolds() throws SQLException {
        List<SalaryHold> holds = new ArrayList<>();
        String sql = "SELECT * FROM salary_holds WHERE status = 'active' " +
                "ORDER BY hold_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                holds.add(extractSalaryHold(rs));
            }
        }
        return holds;
    }

    /**
     * Update hold notes
     */
    public void updateHoldNotes(int holdId, String notes) throws SQLException {
        String sql = "UPDATE salary_holds SET notes = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notes);
            stmt.setInt(2, holdId);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a hold (only if active and within 24 hours of creation)
     */
    public boolean deleteHold(int holdId) throws SQLException {
        String sql = "DELETE FROM salary_holds " +
                "WHERE id = ? AND status = 'active' " +
                "AND datetime(created_at) > datetime('now', '-1 day')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, holdId);
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                System.out.println("✓ Hold deleted");
                return true;
            } else {
                System.out.println("⚠ Cannot delete: Hold is too old or already released");
                return false;
            }
        }
    }

    /**
     * Get hold by ID
     */
    public SalaryHold getHoldById(int id) throws SQLException {
        String sql = "SELECT * FROM salary_holds WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractSalaryHold(rs);
            }
        }
        return null;
    }

    /**
     * Get hold count for employee
     */
    public int getHoldCount(int employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM salary_holds WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get hold history summary
     */
    public HoldSummary getHoldSummary(int employeeId) throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) as total_holds, " +
                "SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as active_holds, " +
                "SUM(CASE WHEN status = 'released' THEN 1 ELSE 0 END) as released_holds " +
                "FROM salary_holds WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new HoldSummary(
                        rs.getInt("total_holds"),
                        rs.getInt("active_holds"),
                        rs.getInt("released_holds")
                );
            }
        }
        return new HoldSummary(0, 0, 0);
    }

    /**
     * Extract SalaryHold from ResultSet
     */
    private SalaryHold extractSalaryHold(ResultSet rs) throws SQLException {
        return new SalaryHold(
                rs.getInt("id"),
                rs.getInt("employee_id"),
                rs.getString("reason"),
                rs.getString("hold_date"),
                rs.getString("release_date"),
                rs.getInt("held_by"),
                rs.getObject("released_by") != null ? rs.getInt("released_by") : null,
                rs.getString("status"),
                rs.getString("notes"),
                rs.getString("created_at")
        );
    }

    /**
     * Inner class for hold summary
     */
    public static class HoldSummary {
        public final int totalHolds;
        public final int activeHolds;
        public final int releasedHolds;

        public HoldSummary(int total, int active, int released) {
            this.totalHolds = total;
            this.activeHolds = active;
            this.releasedHolds = released;
        }

        @Override
        public String toString() {
            return String.format(
                    "Hold History: %d total | %d active | %d released",
                    totalHolds, activeHolds, releasedHolds
            );
        }
    }
}