package dao;

import database.DatabaseConnection;
import models.Payroll;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {

    /**
     * Add new payroll record
     */
    public void addPayroll(Payroll payroll) throws SQLException {
        String sql = "INSERT INTO payroll (employee_id, month, year, base_salary, total_deductions, " +
                "final_salary, late_count, absent_count, date_processed, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payroll.getEmployeeId());
            stmt.setInt(2, payroll.getMonth());
            stmt.setInt(3, payroll.getYear());
            stmt.setDouble(4, payroll.getBaseSalary());
            stmt.setDouble(5, payroll.getTotalDeductions());
            stmt.setDouble(6, payroll.getFinalSalary());
            stmt.setInt(7, payroll.getLateCount());
            stmt.setInt(8, payroll.getAbsentCount());
            stmt.setString(9, payroll.getDateProcessed());
            stmt.setString(10, payroll.getNotes());
            stmt.executeUpdate();

            System.out.println("Payroll added for employee ID: " + payroll.getEmployeeId());
        }
    }

    /**
     * Get payroll by employee, month, and year
     */
    public Payroll getPayrollByEmployeeAndPeriod(int employeeId, int month, int year) throws SQLException {
        String sql = "SELECT * FROM payroll WHERE employee_id = ? AND month = ? AND year = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Payroll(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("total_deductions"),
                        rs.getDouble("final_salary"),
                        rs.getInt("late_count"),
                        rs.getInt("absent_count"),
                        rs.getString("date_processed"),
                        rs.getString("notes")
                );
            }
        }
        return null;
    }

    /**
     * Get all payroll records for a specific month/year
     */
    public List<Payroll> getPayrollByPeriod(int month, int year) throws SQLException {
        List<Payroll> payrolls = new ArrayList<>();
        String sql = "SELECT * FROM payroll WHERE month = ? AND year = ? ORDER BY employee_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Payroll payroll = new Payroll(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("total_deductions"),
                        rs.getDouble("final_salary"),
                        rs.getInt("late_count"),
                        rs.getInt("absent_count"),
                        rs.getString("date_processed"),
                        rs.getString("notes")
                );
                payrolls.add(payroll);
            }
        }
        return payrolls;
    }

    /**
     * Get all payroll records for an employee
     */
    public List<Payroll> getPayrollByEmployee(int employeeId) throws SQLException {
        List<Payroll> payrolls = new ArrayList<>();
        String sql = "SELECT * FROM payroll WHERE employee_id = ? ORDER BY year DESC, month DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Payroll payroll = new Payroll(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("total_deductions"),
                        rs.getDouble("final_salary"),
                        rs.getInt("late_count"),
                        rs.getInt("absent_count"),
                        rs.getString("date_processed"),
                        rs.getString("notes")
                );
                payrolls.add(payroll);
            }
        }
        return payrolls;
    }

    /**
     * Update payroll record (for manual adjustments)
     */
    public void updatePayroll(Payroll payroll) throws SQLException {
        String sql = "UPDATE payroll SET base_salary = ?, total_deductions = ?, final_salary = ?, " +
                "late_count = ?, absent_count = ?, notes = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, payroll.getBaseSalary());
            stmt.setDouble(2, payroll.getTotalDeductions());
            stmt.setDouble(3, payroll.getFinalSalary());
            stmt.setInt(4, payroll.getLateCount());
            stmt.setInt(5, payroll.getAbsentCount());
            stmt.setString(6, payroll.getNotes());
            stmt.setInt(7, payroll.getId());
            stmt.executeUpdate();

            System.out.println("Payroll updated (ID: " + payroll.getId() + ")");
        }
    }

    /**
     * Delete payroll record
     */
    public void deletePayroll(int id) throws SQLException {
        String sql = "DELETE FROM payroll WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Payroll deleted (ID: " + id + ")");
        }
    }

    /**
     * Get all payroll records
     */
    public List<Payroll> getAllPayroll() throws SQLException {
        List<Payroll> payrolls = new ArrayList<>();
        String sql = "SELECT * FROM payroll ORDER BY year DESC, month DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Payroll payroll = new Payroll(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("total_deductions"),
                        rs.getDouble("final_salary"),
                        rs.getInt("late_count"),
                        rs.getInt("absent_count"),
                        rs.getString("date_processed"),
                        rs.getString("notes")
                );
                payrolls.add(payroll);
            }
        }
        return payrolls;
    }

    /**
     * Check if payroll already processed for employee in period
     */
    public boolean isPayrollProcessed(int employeeId, int month, int year) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payroll WHERE employee_id = ? AND month = ? AND year = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Get payroll summary for a period
     */
    public PayrollSummary getPayrollSummary(int month, int year) throws SQLException {
        String sql = "SELECT COUNT(*) as emp_count, SUM(base_salary) as total_base, " +
                "SUM(total_deductions) as total_deduct, SUM(final_salary) as total_net " +
                "FROM payroll WHERE month = ? AND year = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, month);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new PayrollSummary(
                        rs.getInt("emp_count"),
                        rs.getDouble("total_base"),
                        rs.getDouble("total_deduct"),
                        rs.getDouble("total_net")
                );
            }
        }
        return new PayrollSummary(0, 0.0, 0.0, 0.0);
    }

    /**
     * Inner class for payroll summary
     */
    public static class PayrollSummary {
        public final int employeeCount;
        public final double totalBaseSalary;
        public final double totalDeductions;
        public final double totalNetPay;

        public PayrollSummary(int employeeCount, double totalBaseSalary,
                              double totalDeductions, double totalNetPay) {
            this.employeeCount = employeeCount;
            this.totalBaseSalary = totalBaseSalary;
            this.totalDeductions = totalDeductions;
            this.totalNetPay = totalNetPay;
        }
    }
}