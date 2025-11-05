package dao;

import database.DatabaseConnection;
import models.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    /**
     * Add a new employee to the database
     */
    public void addEmployee(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (name, age, position_id, department_id, hire_date, contact_info, qr_code, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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
            stmt.executeUpdate();

            System.out.println("Employee added: " + employee.getName() + " (QR: " + employee.getQrCode() + ")");
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
                return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getInt("position_id"),
                        rs.getInt("department_id"),
                        rs.getString("hire_date"),
                        rs.getString("contact_info"),
                        rs.getString("qr_code"),
                        rs.getString("status")
                );
            }
        }
        return null;  // Not found
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
                return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getInt("position_id"),
                        rs.getInt("department_id"),
                        rs.getString("hire_date"),
                        rs.getString("contact_info"),
                        rs.getString("qr_code"),
                        rs.getString("status")
                );
            }
        }
        return null;  // Not found
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
                Employee emp = new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getInt("position_id"),
                        rs.getInt("department_id"),
                        rs.getString("hire_date"),
                        rs.getString("contact_info"),
                        rs.getString("qr_code"),
                        rs.getString("status")
                );
                employees.add(emp);
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
                Employee emp = new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getInt("position_id"),
                        rs.getInt("department_id"),
                        rs.getString("hire_date"),
                        rs.getString("contact_info"),
                        rs.getString("qr_code"),
                        rs.getString("status")
                );
                employees.add(emp);
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
                Employee emp = new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getInt("position_id"),
                        rs.getInt("department_id"),
                        rs.getString("hire_date"),
                        rs.getString("contact_info"),
                        rs.getString("qr_code"),
                        rs.getString("status")
                );
                employees.add(emp);
            }
        }
        return employees;
    }

    /**
     * Update an existing employee
     */
    public void updateEmployee(Employee employee) throws SQLException {
        String sql = "UPDATE employees SET name = ?, age = ?, position_id = ?, department_id = ?, " +
                "hire_date = ?, contact_info = ?, qr_code = ?, status = ? WHERE id = ?";

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
            stmt.setInt(9, employee.getId());
            stmt.executeUpdate();

            System.out.println("Employee updated: " + employee.getName());
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

            System.out.println("Employee deleted (ID: " + id + ")");
        }
    }

    /**
     * Search employees by name (partial match)
     */
    public List<Employee> searchEmployeesByName(String searchTerm) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee emp = new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getInt("position_id"),
                        rs.getInt("department_id"),
                        rs.getString("hire_date"),
                        rs.getString("contact_info"),
                        rs.getString("qr_code"),
                        rs.getString("status")
                );
                employees.add(emp);
            }
        }
        return employees;
    }
}