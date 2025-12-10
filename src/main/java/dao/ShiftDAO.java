package dao;

import database.DatabaseConnection;
import models.Shift;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Shift management
 */
public class ShiftDAO {

    /**
     * Add a new shift
     */
    public void addShift(Shift shift) throws SQLException {
        String sql = "INSERT INTO shifts (name, start_time, end_time, description, is_active) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, shift.getName());
            stmt.setString(2, shift.getStartTime().toString());
            stmt.setString(3, shift.getEndTime().toString());
            stmt.setString(4, shift.getDescription());
            stmt.setBoolean(5, shift.isActive());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                shift.setId(keys.getInt(1));
            }

            System.out.println("✓ Shift added: " + shift.getName() + " (" + shift.getShiftTimeRange() + ")");
        }
    }

    /**
     * Get shift by ID
     */
    public Shift getShiftById(int id) throws SQLException {
        String sql = "SELECT * FROM shifts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Shift(
                        rs.getInt("id"),
                        rs.getString("name"),
                        LocalTime.parse(rs.getString("start_time")),
                        LocalTime.parse(rs.getString("end_time")),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                );
            }
        }
        return null;
    }

    /**
     * Get all shifts
     */
    public List<Shift> getAllShifts() throws SQLException {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM shifts ORDER BY start_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Shift shift = new Shift(
                        rs.getInt("id"),
                        rs.getString("name"),
                        LocalTime.parse(rs.getString("start_time")),
                        LocalTime.parse(rs.getString("end_time")),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                );
                shifts.add(shift);
            }
        }
        return shifts;
    }

    /**
     * Get all active shifts
     */
    public List<Shift> getActiveShifts() throws SQLException {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM shifts WHERE is_active = 1 ORDER BY start_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Shift shift = new Shift(
                        rs.getInt("id"),
                        rs.getString("name"),
                        LocalTime.parse(rs.getString("start_time")),
                        LocalTime.parse(rs.getString("end_time")),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                );
                shifts.add(shift);
            }
        }
        return shifts;
    }

    /**
     * Update shift
     */
    public void updateShift(Shift shift) throws SQLException {
        String sql = "UPDATE shifts SET name = ?, start_time = ?, end_time = ?, " +
                "description = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shift.getName());
            stmt.setString(2, shift.getStartTime().toString());
            stmt.setString(3, shift.getEndTime().toString());
            stmt.setString(4, shift.getDescription());
            stmt.setBoolean(5, shift.isActive());
            stmt.setInt(6, shift.getId());

            stmt.executeUpdate();
            System.out.println("✓ Shift updated: " + shift.getName());
        }
    }

    /**
     * Delete shift
     */
    public void deleteShift(int id) throws SQLException {
        String sql = "DELETE FROM shifts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✓ Shift deleted (ID: " + id + ")");
        }
    }

    /**
     * Find appropriate shift for a given time-in
     * Returns the shift that the time falls within
     */
    public Shift findShiftForTimeIn(LocalTime timeIn) throws SQLException {
        List<Shift> shifts = getActiveShifts();

        for (Shift shift : shifts) {
            LocalTime start = shift.getStartTime();
            LocalTime end = shift.getEndTime();

            if (end.isBefore(start)) {
                // Night shift crosses midnight
                if (timeIn.isAfter(start) || timeIn.isBefore(end)) {
                    return shift;
                }
            } else {
                // Regular shift
                if (timeIn.isAfter(start.minusHours(1)) && timeIn.isBefore(end)) {
                    return shift;
                }
            }
        }

        return null; // No matching shift found
    }

    /**
     * Check if shift name already exists
     */
    public boolean shiftNameExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM shifts WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Initialize default hospital shifts if none exist
     */
    public void initializeDefaultShifts() throws SQLException {
        List<Shift> existing = getAllShifts();

        if (existing.isEmpty()) {
            System.out.println("No shifts found. Creating default hospital shifts...");

            Shift[] defaultShifts = Shift.getDefaultShifts();
            for (Shift shift : defaultShifts) {
                addShift(shift);
            }

            System.out.println("✓ Default shifts created successfully");
        }
    }
}