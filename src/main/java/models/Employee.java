package models;

/**
 * Employee model - Updated with shift assignment at employee level
 */
public class Employee {
    private int id;
    private String name;
    private int age;
    private int positionId;
    private int departmentId;
    private String hireDate;
    private String contactInfo;
    private String qrCode;
    private String status;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String profilePicturePath;
    private Integer shiftId;  // NEW - Employee-specific shift

    // Constructor for creating NEW employees (no ID yet)
    public Employee(String name, int age, int positionId, int departmentId,
                    String hireDate, String contactInfo, String qrCode,
                    String emergencyContactName, String emergencyContactPhone,
                    String profilePicturePath, Integer shiftId) {
        this.name = name;
        this.age = age;
        this.positionId = positionId;
        this.departmentId = departmentId;
        this.hireDate = hireDate;
        this.contactInfo = contactInfo;
        this.qrCode = qrCode;
        this.status = "active";
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.profilePicturePath = profilePicturePath;
        this.shiftId = shiftId;
    }

    // Constructor for loading EXISTING employees from database
    public Employee(int id, String name, int age, int positionId, int departmentId,
                    String hireDate, String contactInfo, String qrCode, String status,
                    String emergencyContactName, String emergencyContactPhone,
                    String profilePicturePath, Integer shiftId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.positionId = positionId;
        this.departmentId = departmentId;
        this.hireDate = hireDate;
        this.contactInfo = contactInfo;
        this.qrCode = qrCode;
        this.status = status;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.profilePicturePath = profilePicturePath;
        this.shiftId = shiftId;
    }

    // BACKWARD COMPATIBILITY Constructor (for old data without shift)
    public Employee(int id, String name, int age, int positionId, int departmentId,
                    String hireDate, String contactInfo, String qrCode, String status,
                    String emergencyContactName, String emergencyContactPhone,
                    String profilePicturePath) {
        this(id, name, age, positionId, departmentId, hireDate, contactInfo, qrCode, status,
                emergencyContactName, emergencyContactPhone, profilePicturePath, null);
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public int getPositionId() { return positionId; }
    public int getDepartmentId() { return departmentId; }
    public String getHireDate() { return hireDate; }
    public String getContactInfo() { return contactInfo; }
    public String getQrCode() { return qrCode; }
    public String getStatus() { return status; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public String getProfilePicturePath() { return profilePicturePath; }
    public Integer getShiftId() { return shiftId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setPositionId(int positionId) { this.positionId = positionId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public void setStatus(String status) { this.status = status; }
    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }
    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
    public void setShiftId(Integer shiftId) { this.shiftId = shiftId; }

    // Helper methods
    public boolean hasEmergencyContact() {
        return emergencyContactName != null && !emergencyContactName.trim().isEmpty();
    }

    public boolean hasProfilePicture() {
        return profilePicturePath != null && !profilePicturePath.trim().isEmpty();
    }

    public boolean hasShift() {
        return shiftId != null && shiftId > 0;
    }

    public String getEmergencyContactInfo() {
        if (hasEmergencyContact()) {
            return emergencyContactName + " - " + emergencyContactPhone;
        }
        return "No emergency contact";
    }
}