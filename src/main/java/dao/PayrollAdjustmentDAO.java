package dao;

import database.DatabaseConnection;
import models.PayrollAdjustment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Payroll Adjustments with full audit trail
 */
public class PayrollAdjustmentDAO {

    /**
     * Add a new adjustment (status = 'pending')
     */
    public void addAdjustment(PayrollAdjustment adjustment) throws SQLException {
        String sql = "INSERT INTO payroll_adjustments " +
                "(employee_id, adjustment_type, amount, reason, added_by, date_added, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, adjustment.getEmployeeId());
            stmt.setString(2, adjustment.getAdjustmentType());
            stmt.setDouble(3, adjustment.getAmount());
            stmt.setString(4, adjustment.getReason());
            stmt.setInt(5, adjustment.getAddedBy());
            stmt.setString(6, adjustment.getDateAdded());
            stmt.setString(7, adjustment.getStatus());

            stmt.executeUpdate();

            // Get generated ID
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                adjustment.setId(keys.getInt(1));
            }

            System.out.println("✓ Adjustment added: " + adjustment.getFormattedAmount() +
                    " - " + adjustment.getReason());
        }
    }

    /**
     * Approve an adjustment (admin only)
     */
    public void approveAdjustment(int adjustmentId, int approvedBy, String dateApproved)
            throws SQLException {
        String sql = "UPDATE payroll_adjustments SET status = 'approved', " +
                "approved_by = ?, date_approved = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, approvedBy);
            stmt.setString(2, dateApproved);
            stmt.setInt(3, adjustmentId);
            stmt.executeUpdate();

            System.out.println("✓ Adjustment #" + adjustmentId + " approved");
        }
    }

    /**
     * Reject an adjustment
     */
    public void rejectAdjustment(int adjustmentId, String notes) throws SQLException {
        String sql = "UPDATE payroll_adjustments SET status = 'rejected', notes = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notes);
            stmt.setInt(2, adjustmentId);
            stmt.executeUpdate();

            System.out.println("✓ Adjustment #" + adjustmentId + " rejected");
        }
    }

    /**
     * Get all adjustments for an employee
     */
    public List<PayrollAdjustment> getAdjustmentsByEmployee(int employeeId) throws SQLException {
        List<PayrollAdjustment> adjustments = new ArrayList<>();
        String sql = "SELECT * FROM payroll_adjustments WHERE employee_id = ? " +
                "ORDER BY date_added DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                adjustments.add(extractAdjustment(rs));
            }
        }
        return adjustments;
    }

    /**
     * Get adjustments for a specific payroll period
     */
    public List<PayrollAdjustment> getAdjustmentsByPayroll(int payrollId) throws SQLException {
        List<PayrollAdjustment> adjustments = new ArrayList<>();
        String sql = "SELECT * FROM payroll_adjustments WHERE payroll_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payrollId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                adjustments.add(extractAdjustment(rs));
            }
        }
        return adjustments;
    }

    /**
     * Get pending adjustments (for approval)
     */
    public List<PayrollAdjustment> getPendingAdjustments() throws SQLException {
        List<PayrollAdjustment> adjustments = new ArrayList<>();
        String sql = "SELECT * FROM payroll_adjustments WHERE status = 'pending' " +
                "ORDER BY date_added DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                adjustments.add(extractAdjustment(rs));
            }
        }
        return adjustments;
    }

    /**
     * Get approved adjustments for employee in date range
     */
    public List<PayrollAdjustment> getApprovedAdjustments(int employeeId,
                                                          String startDate, String endDate) throws SQLException {
        List<PayrollAdjustment> adjustments = new ArrayList<>();
        String sql = "SELECT * FROM payroll_adjustments " +
                "WHERE employee_id = ? AND status = 'approved' " +
                "AND date_approved BETWEEN ? AND ? " +
                "ORDER BY date_approved DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                adjustments.add(extractAdjustment(rs));
            }
        }
        return adjustments;
    }

    /**
     * Calculate total adjustments for employee in period
     */
    public double calculateTotalAdjustments(int employeeId, String startDate, String endDate)
            throws SQLException {
        String sql = "SELECT SUM(amount) FROM payroll_adjustments " +
                "WHERE employee_id = ? AND status = 'approved' " +
                "AND date_approved BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Link adjustment to payroll
     */
    public void linkToPayroll(int adjustmentId, int payrollId) throws SQLException {
        String sql = "UPDATE payroll_adjustments SET payroll_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payrollId);
            stmt.setInt(2, adjustmentId);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete adjustment (admin only, only if pending)
     */
    public void deleteAdjustment(int adjustmentId) throws SQLException {
        String sql = "DELETE FROM payroll_adjustments WHERE id = ? AND status = 'pending'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adjustmentId);
            int deleted = stmt.executeUpdate();

            if (deleted > 0) {
                System.out.println("✓ Adjustment deleted");
            } else {
                System.out.println("⚠ Cannot delete: Adjustment not found or already approved");
            }
        }
    }

    /**
     * Get adjustment by ID
     */
    public PayrollAdjustment getAdjustmentById(int id) throws SQLException {
        String sql = "SELECT * FROM payroll_adjustments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractAdjustment(rs);
            }
        }
        return null;
    }

    /**
     * Extract PayrollAdjustment from ResultSet
     */
    private PayrollAdjustment extractAdjustment(ResultSet rs) throws SQLException {
        return new PayrollAdjustment(
                rs.getInt("id"),
                rs.getObject("payroll_id") != null ? rs.getInt("payroll_id") : null,
                rs.getInt("employee_id"),
                rs.getString("adjustment_type"),
                rs.getDouble("amount"),
                rs.getString("reason"),
                rs.getInt("added_by"),
                rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null,
                rs.getString("date_added"),
                rs.getString("date_approved"),
                rs.getString("status"),
                rs.getString("notes")
        );
    }

    /**
     * Get adjustment summary for reporting
     */
    public AdjustmentSummary getAdjustmentSummary(int employeeId, String startDate, String endDate)
            throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) as total_count, " +
                "SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END) as total_bonuses, " +
                "SUM(CASE WHEN amount < 0 THEN ABS(amount) ELSE 0 END) as total_deductions, " +
                "SUM(amount) as net_adjustment " +
                "FROM payroll_adjustments " +
                "WHERE employee_id = ? AND status = 'approved' " +
                "AND date_approved BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new AdjustmentSummary(
                        rs.getInt("total_count"),
                        rs.getDouble("total_bonuses"),
                        rs.getDouble("total_deductions"),
                        rs.getDouble("net_adjustment")
                );
            }
        }
        return new AdjustmentSummary(0, 0.0, 0.0, 0.0);
    }

    /**
     * Inner class for adjustment summary
     */
    public static class AdjustmentSummary {
        public final int count;
        public final double totalBonuses;
        public final double totalDeductions;
        public final double netAdjustment;

        public AdjustmentSummary(int count, double totalBonuses,
                                 double totalDeductions, double netAdjustment) {
            this.count = count;
            this.totalBonuses = totalBonuses;
            this.totalDeductions = totalDeductions;
            this.netAdjustment = netAdjustment;
        }

        @Override
        public String toString() {
            return String.format(
                    "Adjustments: %d total | Bonuses: +₱%,.2f | Deductions: -₱%,.2f | Net: ₱%,.2f",
                    count, totalBonuses, totalDeductions, netAdjustment
            );
        }
    }
}