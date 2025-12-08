package models;

/**
 * Display class for TableView (combines employee with dept/position names)
 */
public class EmployeeDisplay {
    private final int id;
    private final String qrCode;
    private final String name;
    private final String positionTitle;
    private final String departmentName;
    private final String status;
    private final double hourlyRate;

    // Constructor WITH hourlyRate (the correct one)
    public EmployeeDisplay(int id, String qrCode, String name, String positionTitle,
                           String departmentName, String status, double hourlyRate) {
        this.id = id;
        this.qrCode = qrCode;
        this.name = name;
        this.positionTitle = positionTitle;
        this.departmentName = departmentName;
        this.status = status;
        this.hourlyRate = hourlyRate;
    }

    // Getters
    public int getId() { return id; }
    public String getQrCode() { return qrCode; }
    public String getName() { return name; }
    public String getPositionTitle() { return positionTitle; }
    public String getDepartmentName() { return departmentName; }
    public String getStatus() { return status; }
    public double getHourlyRate() { return hourlyRate; }
}