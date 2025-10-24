package utils;

public class PayrollCalculator {

    private static final double LATE_PENALTY_PERCENT = 0.05; // 5%
    private static final int WORK_DAYS_PER_MONTH = 22;

    public static double calculateDailySalary(double baseSalary) {
        return baseSalary / WORK_DAYS_PER_MONTH;
    }

    public static double calculateLateDeduction(double baseSalary, int lateCount) {
        double dailySalary = calculateDailySalary(baseSalary);
        return dailySalary * LATE_PENALTY_PERCENT * lateCount;
    }

    public static double calculateAbsenceDeduction(double baseSalary, int absentCount) {
        double dailySalary = calculateDailySalary(baseSalary);
        return dailySalary * absentCount;
    }

    public static double calculateTotalDeductions(double baseSalary, int lateCount, int absentCount) {
        double lateDeduction = calculateLateDeduction(baseSalary, lateCount);
        double absenceDeduction = calculateAbsenceDeduction(baseSalary, absentCount);
        return lateDeduction + absenceDeduction;
    }

    public static double calculateFinalSalary(double baseSalary, int lateCount, int absentCount) {
        double totalDeductions = calculateTotalDeductions(baseSalary, lateCount, absentCount);
        return baseSalary - totalDeductions;
    }
}