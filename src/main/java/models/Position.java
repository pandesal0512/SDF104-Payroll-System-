package models;

/**
 * Position model with shift assignment for 24/7 hospital operations
 */
public class Position {
    private int id;
    private String title;
    private int departmentId;
    private double baseSalary;      // Kept for backward compatibility
    private double hourlyRate;      // Hourly rate (₱/hour)
    private String description;
    private int shiftId;            // Assigned shift (Day/Evening/Night)

    // Constructor 1: For creating NEW positions (no ID yet)
    public Position(String title, int departmentId, double hourlyRate,
                    String description, int shiftId) {
        this.title = title;
        this.departmentId = departmentId;
        this.hourlyRate = hourlyRate;
        this.baseSalary = hourlyRate * 160; // Approximate monthly (160 hours)
        this.description = description;
        this.shiftId = shiftId;
    }

    // Constructor 2: For loading EXISTING positions from database (has ID)
    public Position(int id, String title, int departmentId, double baseSalary,
                    double hourlyRate, String description, int shiftId) {
        this.id = id;
        this.title = title;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
        this.hourlyRate = hourlyRate;
        this.description = description;
        this.shiftId = shiftId;
    }

    // Constructor 3: Backward compatibility (calculate hourly from base salary, no shift)
    public Position(int id, String title, int departmentId, double baseSalary, String description) {
        this.id = id;
        this.title = title;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
        this.hourlyRate = baseSalary / 160;
        this.description = description;
        this.shiftId = 0; // No shift assigned (old data)
    }

    // Constructor 4: For PositionDAO compatibility (with hourly_rate column but no shift yet)
    public Position(int id, String title, int departmentId, double baseSalary,
                    double hourlyRate, String description) {
        this.id = id;
        this.title = title;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
        this.hourlyRate = hourlyRate;
        this.description = description;
        this.shiftId = 0; // No shift assigned yet
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getDepartmentId() { return departmentId; }
    public double getBaseSalary() { return baseSalary; }
    public double getHourlyRate() { return hourlyRate; }
    public String getDescription() { return description; }
    public int getShiftId() { return shiftId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
        this.hourlyRate = baseSalary / 160;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
        this.baseSalary = hourlyRate * 160;
    }

    public void setDescription(String description) { this.description = description; }
    public void setShiftId(int shiftId) { this.shiftId = shiftId; }

    /**
     * Check if position has shift assigned
     * Returns true if shiftId > 0
     */
    public boolean hasShift() {
        return shiftId > 0;
    }
}