package models;

public class Position {
    private int id;
    private String title;
    private int departmentId;
    private double baseSalary;      // Kept for backward compatibility
    private double hourlyRate;      // NEW: Hourly rate (₱/hour)
    private String description;

    // Constructor 1: For creating NEW positions (no ID yet)
    public Position(String title, int departmentId, double hourlyRate, String description) {
        this.title = title;
        this.departmentId = departmentId;
        this.hourlyRate = hourlyRate;
        this.baseSalary = hourlyRate * 160; // Approximate monthly (160 hours)
        this.description = description;
    }

    // Constructor 2: For loading EXISTING positions from database (has ID)
    public Position(int id, String title, int departmentId, double baseSalary, double hourlyRate, String description) {
        this.id = id;
        this.title = title;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
        this.hourlyRate = hourlyRate;
        this.description = description;
    }

    // Constructor 3: Backward compatibility (calculate hourly from base salary)
    public Position(int id, String title, int departmentId, double baseSalary, String description) {
        this.id = id;
        this.title = title;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
        this.hourlyRate = baseSalary / 160; // Calculate from base salary
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getDepartmentId() { return departmentId; }
    public double getBaseSalary() { return baseSalary; }
    public double getHourlyRate() { return hourlyRate; }
    public String getDescription() { return description; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
        this.hourlyRate = baseSalary / 160; // Auto-calculate hourly
    }
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
        this.baseSalary = hourlyRate * 160; // Auto-calculate base
    }
    public void setDescription(String description) { this.description = description; }
}