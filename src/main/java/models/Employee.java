package models;

public class Employee {
    // Fields
    private int id;
    private String name;
    private int age;
    private int positionId;
    private int departmentId;
    private String hireDate;
    private String contactInfo;
    private String qrCode;
    private String status;

    // Constructor 1: For creating NEW employees (no ID yet)
    public Employee(String name, int age, int positionId, int departmentId,
                    String hireDate, String contactInfo, String qrCode) {
        this.name = name;
        this.age = age;
        this.positionId = positionId;
        this.departmentId = departmentId;
        this.hireDate = hireDate;
        this.contactInfo = contactInfo;
        this.qrCode = qrCode;
        this.status = "active";  // Default status
    }

    // Constructor 2: For loading EXISTING employees from database (has ID)
    public Employee(int id, String name, int age, int positionId, int departmentId,
                    String hireDate, String contactInfo, String qrCode, String status) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.positionId = positionId;
        this.departmentId = departmentId;
        this.hireDate = hireDate;
        this.contactInfo = contactInfo;
        this.qrCode = qrCode;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getPositionId() {
        return positionId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getHireDate() {
        return hireDate;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}