package dao;
import database.DatabaseConnection;
import models.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public void recordTimeIn(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (employeeId, date, timeIn, timeOut,  status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attendance.getEmployeeId());
            stmt.setString(2, attendance.getDate());
            stmt.setString(3, attendance.getTimeIn());
            stmt.setString(4, attendance.getTimeOut());
            stmt.setString(5, attendance.getStatus());
            stmt.executeUpdate();

            System.out.println("Time in recorder for employee ID" + attendance.getEmployeeId() + "- Status: " + attendance.getStatus());
        }
    }


}