package dao;

import database.DatabaseConnection;
import models.Position;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PositionDAO {

    /**
     * Check if shift_id column exists in positions table
     */
    private boolean hasShiftColumn(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT shift_id FROM positions LIMIT 1");
            rs.close();
            stmt.close();
            return true;
        } catch (SQLException e) {
            // Column doesn't exist
            return false;
        }
    }

    /**
     * Add a new position to the database (WITH SHIFT if column exists)
     */
    public void addPosition(Position position) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {

            boolean hasShift = hasShiftColumn(conn);
            String sql;

            if (hasShift) {
                sql = "INSERT INTO positions (title, department_id, base_salary, hourly_rate, description, shift_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                sql = "INSERT INTO positions (title, department_id, base_salary, hourly_rate, description) " +
                        "VALUES (?, ?, ?, ?, ?)";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, position.getTitle());
                stmt.setInt(2, position.getDepartmentId());
                stmt.setDouble(3, position.getBaseSalary());
                stmt.setDouble(4, position.getHourlyRate());
                stmt.setString(5, position.getDescription());

                if (hasShift) {
                    stmt.setInt(6, position.getShiftId());
                }

                stmt.executeUpdate();
                System.out.println("Position added: " + position.getTitle() +
                        " (Hourly Rate: ₱" + position.getHourlyRate() + ")");
            }
        }
    }

    /**
     * Get a position by ID (SAFE - handles missing shift_id column)
     */
    public Position getPositionById(int id) throws SQLException {
        String sql = "SELECT * FROM positions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int shiftId = 0;

                // Safely check if shift_id exists
                try {
                    shiftId = rs.getInt("shift_id");
                    if (rs.wasNull()) {
                        shiftId = 0;
                    }
                } catch (SQLException e) {
                    // Column doesn't exist - use default
                    shiftId = 0;
                }

                // Check if hourly_rate column exists
                double hourlyRate = 0.0;
                try {
                    hourlyRate = rs.getDouble("hourly_rate");
                    if (rs.wasNull()) {
                        // Calculate from base_salary
                        hourlyRate = rs.getDouble("base_salary") / 160.0;
                    }
                } catch (SQLException e) {
                    // hourly_rate column doesn't exist - calculate from base
                    hourlyRate = rs.getDouble("base_salary") / 160.0;
                }

                return new Position(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("department_id"),
                        rs.getDouble("base_salary"),
                        hourlyRate,
                        rs.getString("description"),
                        shiftId
                );
            }
        }
        return null;
    }

    /**
     * Get all positions (SAFE - handles missing columns)
     */
    public List<Position> getAllPositions() throws SQLException {
        List<Position> positions = new ArrayList<>();
        String sql = "SELECT * FROM positions";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int shiftId = 0;

                // Safely check if shift_id exists
                try {
                    shiftId = rs.getInt("shift_id");
                    if (rs.wasNull()) {
                        shiftId = 0;
                    }
                } catch (SQLException e) {
                    shiftId = 0;
                }

                // Check if hourly_rate column exists
                double hourlyRate = 0.0;
                try {
                    hourlyRate = rs.getDouble("hourly_rate");
                    if (rs.wasNull()) {
                        hourlyRate = rs.getDouble("base_salary") / 160.0;
                    }
                } catch (SQLException e) {
                    hourlyRate = rs.getDouble("base_salary") / 160.0;
                }

                Position position = new Position(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("department_id"),
                        rs.getDouble("base_salary"),
                        hourlyRate,
                        rs.getString("description"),
                        shiftId
                );
                positions.add(position);
            }
        }
        return positions;
    }

    /**
     * Get all positions in a specific department (SAFE)
     */
    public List<Position> getPositionsByDepartment(int departmentId) throws SQLException {
        List<Position> positions = new ArrayList<>();
        String sql = "SELECT * FROM positions WHERE department_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int shiftId = 0;

                try {
                    shiftId = rs.getInt("shift_id");
                    if (rs.wasNull()) {
                        shiftId = 0;
                    }
                } catch (SQLException e) {
                    shiftId = 0;
                }

                double hourlyRate = 0.0;
                try {
                    hourlyRate = rs.getDouble("hourly_rate");
                    if (rs.wasNull()) {
                        hourlyRate = rs.getDouble("base_salary") / 160.0;
                    }
                } catch (SQLException e) {
                    hourlyRate = rs.getDouble("base_salary") / 160.0;
                }

                Position position = new Position(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("department_id"),
                        rs.getDouble("base_salary"),
                        hourlyRate,
                        rs.getString("description"),
                        shiftId
                );
                positions.add(position);
            }
        }
        return positions;
    }

    /**
     * Update an existing position (SAFE - checks if shift_id exists)
     */
    public void updatePosition(Position position) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {

            boolean hasShift = hasShiftColumn(conn);
            String sql;

            if (hasShift) {
                sql = "UPDATE positions SET title = ?, department_id = ?, base_salary = ?, " +
                        "hourly_rate = ?, description = ?, shift_id = ? WHERE id = ?";
            } else {
                sql = "UPDATE positions SET title = ?, department_id = ?, base_salary = ?, " +
                        "hourly_rate = ?, description = ? WHERE id = ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, position.getTitle());
                stmt.setInt(2, position.getDepartmentId());
                stmt.setDouble(3, position.getBaseSalary());
                stmt.setDouble(4, position.getHourlyRate());
                stmt.setString(5, position.getDescription());

                if (hasShift) {
                    stmt.setInt(6, position.getShiftId());
                    stmt.setInt(7, position.getId());
                } else {
                    stmt.setInt(6, position.getId());
                }

                stmt.executeUpdate();
                System.out.println("Position updated: " + position.getTitle());
            }
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