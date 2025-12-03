package models;

public class Payroll {
    // Fields
    private int id;
    private int employeeId;
    private int month;
    private int year;
    private double baseSalary;
    private double totalDeductions;
    private double finalSalary;
    private int lateCount;
    private int absentCount;
    private String dateProcessed;
    private String notes; // For manual adjustments (e.g., "Sick leave - 3 days")

    // Constructor 1: For creating NEW payroll records (no ID yet)
    public Payroll(int employeeId, int month, int year, double baseSalary,
                   double totalDeductions, double finalSalary, int lateCount,
                   int absentCount, String dateProcessed, String notes) {
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.baseSalary = baseSalary;
        this.totalDeductions = totalDeductions;
        this.finalSalary = finalSalary;
        this.lateCount = lateCount;
        this.absentCount = absentCount;
        this.dateProcessed = dateProcessed;
        this.notes = notes;
    }

    // Constructor 2: For loading EXISTING payroll from database (has ID)
    public Payroll(int id, int employeeId, int month, int year, double baseSalary,
                   double totalDeductions, double finalSalary, int lateCount,
                   int absentCount, String dateProcessed, String notes) {
        this.id = id;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.baseSalary = baseSalary;
        this.totalDeductions = totalDeductions;
        this.finalSalary = finalSalary;
        this.lateCount = lateCount;
        this.absentCount = absentCount;
        this.dateProcessed = dateProcessed;
        this.notes = notes;
    }

    // Constructor 3: Alternative constructor for compatibility (no notes)
    public Payroll(int id, int employeeId, int month, int year, double baseSalary,
                   double totalDeductions, double finalSalary, int lateCount,
                   int absentCount, String dateProcessed) {
        this(id, employeeId, month, year, baseSalary, totalDeductions, finalSalary,
                lateCount, absentCount, dateProcessed, "");
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public double getFinalSalary() {
        return finalSalary;
    }

    public int getLateCount() {
        return lateCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public String getDateProcessed() {
        return dateProcessed;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public void setTotalDeductions(double totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public void setFinalSalary(double finalSalary) {
        this.finalSalary = finalSalary;
    }

    public void setLateCount(int lateCount) {
        this.lateCount = lateCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public void setDateProcessed(String dateProcessed) {
        this.dateProcessed = dateProcessed;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}