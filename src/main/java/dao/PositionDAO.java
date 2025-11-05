package dao;

import database.DatabaseConnection;
import models.Position;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PositionDAO {

    /**
     * Add a new position to the database
     */
    public void addPosition(Position position) throws SQLException {
        String sql = "INSERT INTO positions (title, department_id, base_salary, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, position.getTitle());
            stmt.setInt(2, position.getDepartmentId());
            stmt.setDouble(3, position.getBaseSalary());
            stmt.setString(4, position.getDescription());
            stmt.executeUpdate();

            System.out.println("Position added: " + position.getTitle() + " (Salary: ₱" + position.getBaseSalary() + ")");
        }
    }

    /**
     * Get a position by ID
     */
    public Position getPositionById(int id) throws SQLException {
        String sql = "SELECT * FROM positions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Position(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("department_id"),
                        rs.getDouble("base_salary"),
                        rs.getString("description")
                );
            }
        }
        return null;  // Not found
    }

    /**
     * Get all positions
     */
    public List<Position> getAllPositions() throws SQLException {
        List<Position> positions = new ArrayList<>();
        String sql = "SELECT * FROM positions";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Position position = new Position(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("department_id"),
                        rs.getDouble("base_salary"),
                        rs.getString("description")
                );
                positions.add(position);
            }
        }
        return positions;
    }

    /**
     * Get all positions in a specific department
     */
    public List<Position> getPositionsByDepartment(int departmentId) throws SQLException {
        List<Position> positions = new ArrayList<>();
        String sql = "SELECT * FROM positions WHERE department_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Position position = new Position(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("department_id"),
                        rs.getDouble("base_salary"),
                        rs.getString("description")
                );
                positions.add(position);
            }
        }
        return positions;
    }

    /**
     * Update an existing position
     */
    public void updatePosition(Position position) throws SQLException {
        String sql = "UPDATE positions SET title = ?, department_id = ?, base_salary = ?, description = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, position.getTitle());
            stmt.setInt(2, position.getDepartmentId());
            stmt.setDouble(3, position.getBaseSalary());
            stmt.setString(4, position.getDescription());
            stmt.setInt(5, position.getId());
            stmt.executeUpdate();

            System.out.println("Position updated: " + position.getTitle());
        }
    }

    /**
     * Delete a position by ID
     */
    public void deletePosition(int id) throws SQLException {
        String sql = "DELETE FROM positions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Position deleted (ID: " + id + ")");
        }
    }
}
