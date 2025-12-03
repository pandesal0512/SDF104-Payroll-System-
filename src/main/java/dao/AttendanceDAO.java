package dao;

import database.DatabaseConnection;
import models.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    /**
     * Add new attendance record
     */
    public void addAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (employee_id, date, time_in, time_out, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attendance.getEmployeeId());
            stmt.setString(2, attendance.getDate());
            stmt.setString(3, attendance.getTimeIn());
            stmt.setString(4, attendance.getTimeOut());
            stmt.setString(5, attendance.getStatus());
            stmt.executeUpdate();

            System.out.println("Attendance recorded for employee ID: " + attendance.getEmployeeId());
        }
    }

    /**
     * Get attendance by employee and date
     */
    public Attendance getAttendanceByEmployeeAndDate(int employeeId, String date) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, date);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Attendance(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("date"),
                        rs.getString("time_in"),
                        rs.getString("time_out"),
                        rs.getString("status")
                );
            }
        }
        return null;
    }

    /**
     * Get all attendance records for a specific date
     */
    public List<Attendance> getAttendanceByDate(String date) throws SQLException {
        List<Attendance> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE date = ? ORDER BY time_in ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance att = new Attendance(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("date"),
                        rs.getString("time_in"),
                        rs.getString("time_out"),
                        rs.getString("status")
                );
                records.add(att);
            }
        }
        return records;
    }

    /**
     * Get attendance records for an employee in a date range
     */
    public List<Attendance> getAttendanceByEmployeeAndDateRange(int employeeId, String startDate, String endDate) throws SQLException {
        List<Attendance> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE employee_id = ? AND date BETWEEN ? AND ? ORDER BY date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance att = new Attendance(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("date"),
                        rs.getString("time_in"),
                        rs.getString("time_out"),
                        rs.getString("status")
                );
                records.add(att);
            }
        }
        return records;
    }

    /**
     * Update attendance (for time out)
     */
    public void updateAttendance(Attendance attendance) throws SQLException {
        String sql = "UPDATE attendance SET time_out = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, attendance.getTimeOut());
            stmt.setString(2, attendance.getStatus());
            stmt.setInt(3, attendance.getId());
            stmt.executeUpdate();

            System.out.println("Attendance updated (ID: " + attendance.getId() + ")");
        }
    }

    /**
     * Count late occurrences for employee in a month
     */
    public int countLateByEmployeeAndMonth(int employeeId, int year, int month) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? " +
                "AND strftime('%Y', date) = ? AND strftime('%m', date) = ? AND status = 'late'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, String.valueOf(year));
            stmt.setString(3, String.format("%02d", month));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Count absent days for employee in a month
     */
    public int countAbsentByEmployeeAndMonth(int employeeId, int year, int month) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? " +
                "AND strftime('%Y', date) = ? AND strftime('%m', date) = ? AND status = 'absent'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, String.valueOf(year));
            stmt.setString(3, String.format("%02d", month));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get all attendance records
     */
    public List<Attendance> getAllAttendance() throws SQLException {
        List<Attendance> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance ORDER BY date DESC, time_in DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Attendance att = new Attendance(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("date"),
                        rs.getString("time_in"),
                        rs.getString("time_out"),
                        rs.getString("status")
                );
                records.add(att);
            }
        }
        return records;
    }

    /**
     * Delete attendance record
     */
    public void deleteAttendance(int id) throws SQLException {
        String sql = "DELETE FROM attendance WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Attendance deleted (ID: " + id + ")");
        }
    }
}