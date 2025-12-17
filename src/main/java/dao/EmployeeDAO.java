package dao;

import database.DatabaseConnection;
import models.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeDAO - Updated with employee-level shift assignment
 */
public class EmployeeDAO {

    /**
     * Add a new employee (with shift)
     */
    public void addEmployee(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees " +
                "(name, age, position_id, department_id, hire_date, contact_info, qr_code, status, " +
                "emergency_contact_name, emergency_contact_phone, profile_picture_path, shift_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getName());
            stmt.setInt(2, employee.getAge());
            stmt.setInt(3, employee.getPositionId());
            stmt.setInt(4, employee.getDepartmentId());
            stmt.setString(5, employee.getHireDate());
            stmt.setString(6, employee.getContactInfo());
            stmt.setString(7, employee.getQrCode());
            stmt.setString(8, employee.getStatus());
            stmt.setString(9, employee.getEmergencyContactName());
            stmt.setString(10, employee.getEmergencyContactPhone());
            stmt.setString(11, employee.getProfilePicturePath());

            // Handle shift_id (can be null)
            if (employee.getShiftId() != null) {
                stmt.setInt(12, employee.getShiftId());
            } else {
                stmt.setNull(12, Types.INTEGER);
            }

            stmt.executeUpdate();

            System.out.println("✓ Employee added: " + employee.getName() +
                    " (QR: " + employee.getQrCode() +
                    ", Shift: " + (employee.hasShift() ? "Assigned" : "Not assigned") + ")");
        }
    }

    /**
     * Get an employee by ID
     */
    public Employee getEmployeeById(int id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractEmployee(rs);
            }
        }
        return null;
    }

    /**
     * Get an employee by QR code
     */
    public Employee getEmployeeByQRCode(String qrCode) throws SQLException {
        String sql = "SELECT * FROM employees WHERE qr_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, qrCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractEmployee(rs);
            }
        }
        return null;
    }

    /**
     * Get all employees
     */
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employees.add(extractEmployee(rs));
            }
        }
        return employees;
    }

    /**
     * Get all employees in a specific department
     */
    public List<Employee> getEmployeesByDepartment(int departmentId) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE department_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(extractEmployee(rs));
            }
        }
        return employees;
    }

    /**
     * Get all active employees
     */
    public List<Employee> getActiveEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE status = 'active'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employees.add(extractEmployee(rs));
            }
        }
        return employees;
    }

    /**
     * Update an existing employee (with shift)
     */
    public void updateEmployee(Employee employee) throws SQLException {
        String sql = "UPDATE employees SET " +
                "name = ?, age = ?, position_id = ?, department_id = ?, " +
                "hire_date = ?, contact_info = ?, qr_code = ?, status = ?, " +
                "emergency_contact_name = ?, emergency_contact_phone = ?, " +
                "profile_picture_path = ?, shift_id = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getName());
            stmt.setInt(2, employee.getAge());
            stmt.setInt(3, employee.getPositionId());
            stmt.setInt(4, employee.getDepartmentId());
            stmt.setString(5, employee.getHireDate());
            stmt.setString(6, employee.getContactInfo());
            stmt.setString(7, employee.getQrCode());
            stmt.setString(8, employee.getStatus());
            stmt.setString(9, employee.getEmergencyContactName());
            stmt.setString(10, employee.getEmergencyContactPhone());
            stmt.setString(11, employee.getProfilePicturePath());

            // Handle shift_id (can be null)
            if (employee.getShiftId() != null) {
                stmt.setInt(12, employee.getShiftId());
            } else {
                stmt.setNull(12, Types.INTEGER);
            }

            stmt.setInt(13, employee.getId());

            stmt.executeUpdate();

            System.out.println("✓ Employee updated: " + employee.getName());
        }
    }

    /**
     * Delete an employee by ID
     */
    public void deleteEmployee(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("✓ Employee deleted (ID: " + id + ")");
        }
    }

    /**
     * Search employees by name (partial match)
     */
    public List<Employee> searchEmployeesByName(String searchTerm) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE name LIKE ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(extractEmployee(rs));
            }
        }
        return employees;
    }

    /**
     * Update employee shift
     */
    public void updateEmployeeShift(int employeeId, Integer shiftId) throws SQLException {
        String sql = "UPDATE employees SET shift_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (shiftId != null) {
                stmt.setInt(1, shiftId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, employeeId);
            stmt.executeUpdate();

            System.out.println("✓ Shift updated for employee #" + employeeId);
        }
    }

    /**
     * Get employees by shift
     */
    public List<Employee> getEmployeesByShift(int shiftId) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE shift_id = ? AND status = 'active'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, shiftId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(extractEmployee(rs));
            }
        }
        return employees;
    }

    /**
     * Extract Employee from ResultSet (handles all fields safely)
     */
    private Employee extractEmployee(ResultSet rs) throws SQLException {
        String emergencyName = null;
        String emergencyPhone = null;
        String picturePath = null;
        Integer shiftId = null;

        try {
            emergencyName = rs.getString("emergency_contact_name");
            emergencyPhone = rs.getString("emergency_contact_phone");
            picturePath = rs.getString("profile_picture_path");

            // Handle shift_id (can be null)
            int tempShiftId = rs.getInt("shift_id");
            if (!rs.wasNull()) {
                shiftId = tempShiftId;
            }
        } catch (SQLException e) {
            // Columns don't exist yet - backward compatibility
            System.out.println("⚠ Using old employee schema (run migration)");
        }

        return new Employee(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getInt("position_id"),
                rs.getInt("department_id"),
                rs.getString("hire_date"),
                rs.getString("contact_info"),
                rs.getString("qr_code"),
                rs.getString("status"),
                emergencyName,
                emergencyPhone,
                picturePath,
                shiftId
        );
    }
}