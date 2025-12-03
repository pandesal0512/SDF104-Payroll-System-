
package models;

public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role; // "admin" or "hr_staff"
    private String status; // "active" or "inactive"

    // Constructor for new users (no ID)
    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = "active";
    }

    // Constructor for existing users (with ID)
    public User(int id, String username, String password, String fullName, String role, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
}