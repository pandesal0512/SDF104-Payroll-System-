package utils;

import database.DatabaseConnection;
import java.sql.*;

/**
 * Calculator for Philippine government deductions
 * Employee-side deductions only:
 *  - SSS
 *  - PhilHealth (employee share)
 *  - Pag-IBIG
 *
 * IMPORTANT REAL-WORLD RULES:
 *  - No salary = no deductions
 *  - Total deductions must NEVER exceed gross salary
 */
public class GovernmentDeductionCalculator {

    /**
     * Calculate SSS contribution (employee share)
     * Based on salary brackets stored in DB
     */
    public static double calculateSSS(double monthlySalary) {

        if (monthlySalary <= 0) return 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT employee_share FROM government_deductions " +
                             "WHERE deduction_type = 'SSS' AND is_active = 1 " +
                             "AND ? >= salary_range_min AND ? < salary_range_max " +
                             "ORDER BY salary_range_min DESC LIMIT 1")) {

            stmt.setDouble(1, monthlySalary);
            stmt.setDouble(2, monthlySalary);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("employee_share");
            }

            // Fallback: highest bracket
            try (PreparedStatement maxStmt = conn.prepareStatement(
                    "SELECT employee_share FROM government_deductions " +
                            "WHERE deduction_type = 'SSS' AND is_active = 1 " +
                            "ORDER BY salary_range_max DESC LIMIT 1")) {

                ResultSet maxRs = maxStmt.executeQuery();
                if (maxRs.next()) {
                    return maxRs.getDouble("employee_share");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error calculating SSS: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Calculate PhilHealth employee share
     * Employee share = 2% of salary (employer pays other 2%)
     */
    public static double calculatePhilHealth(double monthlySalary) {

        if (monthlySalary <= 0) return 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT employee_share FROM government_deductions " +
                             "WHERE deduction_type = 'PHILHEALTH' AND is_active = 1 " +
                             "AND ? >= salary_range_min AND ? < salary_range_max " +
                             "LIMIT 1")) {

            stmt.setDouble(1, monthlySalary);
            stmt.setDouble(2, monthlySalary);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double fixed = rs.getDouble("employee_share");
                if (fixed > 0) {
                    return fixed;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error calculating PhilHealth: " + e.getMessage());
        }

        // Default rule: 2% capped
        double contribution = monthlySalary * 0.02;
        return Math.min(contribution, 1800.0);
    }

    /**
     * Calculate Pag-IBIG employee contribution
     *  - 1% if salary ≤ ₱1,500
     *  - 2% if salary > ₱1,500
     *  - Maximum ₱100
     */
    public static double calculatePagIBIG(double monthlySalary) {


        if (monthlySalary <= 0) return 0.0;

        double contribution;
        if (monthlySalary <= 1500) {
            contribution = monthlySalary * 0.01;
        } else {
            contribution = monthlySalary * 0.02;
        }

        return Math.min(contribution, 100.0);
    }

    /**
     * Calculate all government deductions safely
     * Ensures deductions never exceed gross salary
     */
    public static GovernmentDeductions calculateAll(double monthlySalary) {


        if (monthlySalary <= 0) {
            return new GovernmentDeductions(0, 0, 0);
        }

        double sss = calculateSSS(monthlySalary);
        double philHealth = calculatePhilHealth(monthlySalary);
        double pagIbig = calculatePagIBIG(monthlySalary);

        double total = sss + philHealth + pagIbig;


        if (total > monthlySalary) {
            double factor = monthlySalary / total;
            sss *= factor;
            philHealth *= factor;
            pagIbig *= factor;
        }

        return new GovernmentDeductions(sss, philHealth, pagIbig);
    }

    /**
     * Data holder for deduction breakdown
     */
    public static class GovernmentDeductions {
        public final double sss;
        public final double philHealth;
        public final double pagIbig;
        public final double total;

        public GovernmentDeductions(double sss, double philHealth, double pagIbig) {
            this.sss = round(sss);
            this.philHealth = round(philHealth);
            this.pagIbig = round(pagIbig);
            this.total = round(this.sss + this.philHealth + this.pagIbig);
        }

        private static double round(double value) {
            return Math.round(value * 100.0) / 100.0;
        }

        public String getBreakdown() {
            return String.format(
                    "Government Deductions:\n" +
                            "  SSS:        ₱%,.2f\n" +
                            "  PhilHealth: ₱%,.2f\n" +
                            "  Pag-IBIG:   ₱%,.2f\n" +
                            "  ─────────────────\n" +
                            "  TOTAL:      ₱%,.2f",
                    sss, philHealth, pagIbig, total
            );
        }
    }

    /**
     * Test runner
     */
    public static void main(String[] args) {

        double[] testSalaries = {0, 5000, 10000, 15000, 20000};

        for (double salary : testSalaries) {
            System.out.println("\nSalary: ₱" + String.format("%,.2f", salary));
            GovernmentDeductions d = calculateAll(salary);
            System.out.println(d.getBreakdown());
            System.out.println("Net Pay: ₱" +
                    String.format("%,.2f", salary - d.total));
        }
    }
}
