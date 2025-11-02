package models;

public class Position {
    // Fields
    private int id;
    private String title;
    private int departmentId;
    private double baseSalary;
    private String description;

    // Constructor 1: For creating NEW positions (no ID yet)
    public Position(String title, int departmentId, double baseSalary, String description) {
        this.title = title;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
        this.description = description;
    }

    // Constructor 2: For loading EXISTING positions from database (has ID)
    public Position(int id, String title, int departmentId, double baseSalary, String description) {
        this.id = id;
        this.title = title;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
        this.description = description;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}