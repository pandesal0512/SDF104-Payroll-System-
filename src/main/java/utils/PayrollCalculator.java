package utils;

/**
 * Utility class for payroll calculations
 */
public class PayrollCalculator {

    // Default deduction rates (can be customized)
    private static final double DEFAULT_LATE_DEDUCTION = 50.0;
    private static final double DEFAULT_ABSENT_DEDUCTION = 200.0;

    // Tax brackets (Philippine BIR rates as example)
    private static final double TAX_BRACKET_1 = 20833;  // Up to 250,000/year
    private static final double TAX_BRACKET_2 = 33333;  // 250,001 to 400,000/year
    private static final double TAX_BRACKET_3 = 66667;  // 400,001 to 800,000/year

    // SSS, PhilHealth, Pag-IBIG contribution rates (example rates)
    private static final double SSS_RATE = 0.045;      // 4.5% employee share
    private static final double PHILHEALTH_RATE = 0.02; // 2% employee share
    private static final double PAGIBIG_RATE = 0.02;   // 2% employee share

    /**
     * Calculate total deductions for late and absent
     */
    public static double calculateAttendanceDeductions(int lateCount, int absentCount) {
        return calculateAttendanceDeductions(lateCount, absentCount,
                DEFAULT_LATE_DEDUCTION,
                DEFAULT_ABSENT_DEDUCTION);
    }

    /**
     * Calculate total deductions with custom rates
     */
    public static double calculateAttendanceDeductions(int lateCount, int absentCount,
                                                       double lateDeduction, double absentDeduction) {
        double lateTotal = lateCount * lateDeduction;
        double absentTotal = absentCount * absentDeduction;
        return lateTotal + absentTotal;
    }

    /**
     * Calculate SSS contribution
     */
    public static double calculateSSS(double monthlySalary) {
        // Simplified calculation - actual SSS has brackets
        if (monthlySalary <= 0) return 0;

        double contribution = monthlySalary * SSS_RATE;

        // Cap at maximum contribution (example: 1125.00)
        double maxContribution = 1125.0;
        return Math.min(contribution, maxContribution);
    }

    /**
     * Calculate PhilHealth contribution
     */
    public static double calculatePhilHealth(double monthlySalary) {
        if (monthlySalary <= 0) return 0;

        double contribution = monthlySalary * PHILHEALTH_RATE;

        // Cap at maximum contribution (example: 1800.00)
        double maxContribution = 1800.0;
        return Math.min(contribution, maxContribution);
    }

    /**
     * Calculate Pag-IBIG contribution
     */
    public static double calculatePagIBIG(double monthlySalary) {
        if (monthlySalary <= 0) return 0;

        double contribution = monthlySalary * PAGIBIG_RATE;

        // Cap at maximum contribution (example: 200.00)
        double maxContribution = 200.0;
        return Math.min(contribution, maxContribution);
    }

    /**
     * Calculate withholding tax (simplified)
     */
    public static double calculateWithholdingTax(double monthlySalary) {
        if (monthlySalary <= TAX_BRACKET_1) {
            // 0% tax
            return 0;
        } else if (monthlySalary <= TAX_BRACKET_2) {
            // 20% of excess over 20,833
            return (monthlySalary - TAX_BRACKET_1) * 0.20;
        } else if (monthlySalary <= TAX_BRACKET_3) {
            // 2,500 + 25% of excess over 33,333
            return 2500 + ((monthlySalary - TAX_BRACKET_2) * 0.25);
        } else {
            // 10,833.33 + 30% of excess over 66,667
            return 10833.33 + ((monthlySalary - TAX_BRACKET_3) * 0.30);
        }
    }

    /**
     * Calculate total government deductions (SSS + PhilHealth + Pag-IBIG + Tax)
     */
    public static double calculateGovernmentDeductions(double monthlySalary) {
        double sss = calculateSSS(monthlySalary);
        double philHealth = calculatePhilHealth(monthlySalary);
        double pagIbig = calculatePagIBIG(monthlySalary);
        double tax = calculateWithholdingTax(monthlySalary);

        return sss + philHealth + pagIbig + tax;
    }

    /**
     * Calculate total deductions (attendance + government)
     */
    public static double calculateTotalDeductions(double baseSalary, int lateCount, int absentCount,
                                                  double lateDeduction, double absentDeduction,
                                                  boolean includeGovernmentDeductions) {
        double attendanceDeductions = calculateAttendanceDeductions(lateCount, absentCount,
                lateDeduction, absentDeduction);

        if (includeGovernmentDeductions) {
            double governmentDeductions = calculateGovernmentDeductions(baseSalary);
            return attendanceDeductions + governmentDeductions;
        }

        return attendanceDeductions;
    }

    /**
     * Calculate net pay
     */
    public static double calculateNetPay(double baseSalary, double totalDeductions) {
        return Math.max(0, baseSalary - totalDeductions);
    }

    /**
     * Calculate net pay with all parameters
     */
    public static double calculateNetPay(double baseSalary, int lateCount, int absentCount,
                                         double lateDeduction, double absentDeduction,
                                         boolean includeGovernmentDeductions) {
        double totalDeductions = calculateTotalDeductions(baseSalary, lateCount, absentCount,
                lateDeduction, absentDeduction,
                includeGovernmentDeductions);
        return calculateNetPay(baseSalary, totalDeductions);
    }

    /**
     * Calculate daily rate from monthly salary
     */
    public static double calculateDailyRate(double monthlySalary, int workingDaysPerMonth) {
        if (workingDaysPerMonth <= 0) {
            workingDaysPerMonth = 22; // Default: 22 working days per month
        }
        return monthlySalary / workingDaysPerMonth;
    }

    /**
     * Calculate hourly rate from monthly salary
     */
    public static double calculateHourlyRate(double monthlySalary, int workingDaysPerMonth,
                                             int hoursPerDay) {
        if (workingDaysPerMonth <= 0) workingDaysPerMonth = 22;
        if (hoursPerDay <= 0) hoursPerDay = 8;

        double dailyRate = calculateDailyRate(monthlySalary, workingDaysPerMonth);
        return dailyRate / hoursPerDay;
    }

    /**
     * Calculate overtime pay
     */
    public static double calculateOvertimePay(double monthlySalary, double overtimeHours) {
        double hourlyRate = calculateHourlyRate(monthlySalary, 22, 8);
        double overtimeRate = hourlyRate * 1.25; // 125% for regular overtime
        return overtimeHours * overtimeRate;
    }

    /**
     * Format amount to Philippine Peso format
     */
    public static String formatToPeso(double amount) {
        return String.format("₱%,.2f", amount);
    }

    /**
     * Round to 2 decimal places
     */
    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Payroll breakdown data class
     */
    public static class PayrollBreakdown {
        public final double baseSalary;
        public final double lateDeductions;
        public final double absentDeductions;
        public final double sss;
        public final double philHealth;
        public final double pagIbig;
        public final double withholdingTax;
        public final double totalDeductions;
        public final double netPay;

        public PayrollBreakdown(double baseSalary, int lateCount, int absentCount,
                                double lateDeductionRate, double absentDeductionRate) {
            this.baseSalary = baseSalary;
            this.lateDeductions = lateCount * lateDeductionRate;
            this.absentDeductions = absentCount * absentDeductionRate;
            this.sss = calculateSSS(baseSalary);
            this.philHealth = calculatePhilHealth(baseSalary);
            this.pagIbig = calculatePagIBIG(baseSalary);
            this.withholdingTax = calculateWithholdingTax(baseSalary);
            this.totalDeductions = lateDeductions + absentDeductions + sss +
                    philHealth + pagIbig + withholdingTax;
            this.netPay = baseSalary - totalDeductions;
        }

        @Override
        public String toString() {
            return String.format(
                    "Payroll Breakdown:\n" +
                            "Base Salary: %s\n" +
                            "Late Deductions: %s\n" +
                            "Absent Deductions: %s\n" +
                            "SSS: %s\n" +
                            "PhilHealth: %s\n" +
                            "Pag-IBIG: %s\n" +
                            "Withholding Tax: %s\n" +
                            "Total Deductions: %s\n" +
                            "Net Pay: %s",
                    formatToPeso(baseSalary),
                    formatToPeso(lateDeductions),
                    formatToPeso(absentDeductions),
                    formatToPeso(sss),
                    formatToPeso(philHealth),
                    formatToPeso(pagIbig),
                    formatToPeso(withholdingTax),
                    formatToPeso(totalDeductions),
                    formatToPeso(netPay)
            );
        }
    }
}