package models;

public class Attendance {
    // Fields
    private int id;
    private int employeeId;
    private String date;
    private String timeIn;
    private String timeOut;
    private String status;

    // Constructor 1: For creating NEW attendance records (no ID yet)
    public Attendance(int employeeId, String date, String timeIn, String timeOut, String status) {
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
    }

    // Constructor 2: For loading EXISTING attendance from database (has ID)
    public Attendance(int id, int employeeId, String date, String timeIn, String timeOut, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getDate() {
        return date;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTimeIn(String timeIn) {
        this.timeIn = timeIn;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}