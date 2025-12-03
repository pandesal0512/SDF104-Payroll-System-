package controllers;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.*;
import utils.DateTimeHelper;

import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private Label totalEmployeesLabel;
    @FXML private Label activeEmployeesLabel;
    @FXML private Label totalDepartmentsLabel;
    @FXML private Label presentTodayLabel;
    @FXML private Label lateTodayLabel;
    @FXML private Label absentTodayLabel;

    @FXML private TableView<ActivityDisplay> recentActivityTable;
    @FXML private TableColumn<ActivityDisplay, String> timeColumn;
    @FXML private TableColumn<ActivityDisplay, String> employeeColumn;
    @FXML private TableColumn<ActivityDisplay, String> actionColumn;
    @FXML private TableColumn<ActivityDisplay, String> statusColumn;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    private ObservableList<ActivityDisplay> activityList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadStatistics();
        loadRecentActivity();
    }

    private void setupTableColumns() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        employeeColumn.setCellValueFactory(new PropertyValueFactory<>("employee"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        recentActivityTable.setItems(activityList);
    }

    private void loadStatistics() {
        try {
            // Total Employees
            List<Employee> allEmployees = employeeDAO.getAllEmployees();
            totalEmployeesLabel.setText(String.valueOf(allEmployees.size()));

            // Active Employees
            List<Employee> activeEmployees = employeeDAO.getActiveEmployees();
            activeEmployeesLabel.setText(String.valueOf(activeEmployees.size()));

            // Total Departments
            List<Department> departments = departmentDAO.getAllDepartments();
            totalDepartmentsLabel.setText(String.valueOf(departments.size()));

            // Today's Attendance
            String today = DateTimeHelper.getCurrentDate();
            List<Attendance> todayRecords = attendanceDAO.getAttendanceByDate(today);

            int presentCount = 0;
            int lateCount = 0;

            for (Attendance att : todayRecords) {
                if ("on-time".equals(att.getStatus())) {
                    presentCount++;
                } else if ("late".equals(att.getStatus())) {
                    lateCount++;
                }
            }

            presentTodayLabel.setText(String.valueOf(presentCount));
            lateTodayLabel.setText(String.valueOf(lateCount));

            int absentCount = activeEmployees.size() - todayRecords.size();
            absentTodayLabel.setText(String.valueOf(Math.max(0, absentCount)));

        } catch (SQLException e) {
            System.err.println("Failed to load statistics: " + e.getMessage());
        }
    }

    private void loadRecentActivity() {
        try {
            activityList.clear();
            String today = DateTimeHelper.getCurrentDate();
            List<Attendance> records = attendanceDAO.getAttendanceByDate(today);

            for (Attendance att : records) {
                Employee emp = employeeDAO.getEmployeeById(att.getEmployeeId());
                if (emp != null) {
                    String time = DateTimeHelper.formatTimeForDisplay(att.getTimeIn());
                    String action = "Time In";
                    String status = "on-time".equals(att.getStatus()) ? "On Time" : "Late";

                    activityList.add(new ActivityDisplay(time, emp.getName(), action, status));

                    if (att.getTimeOut() != null && !att.getTimeOut().isEmpty()) {
                        time = DateTimeHelper.formatTimeForDisplay(att.getTimeOut());
                        activityList.add(new ActivityDisplay(time, emp.getName(), "Time Out", "Complete"));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to load recent activity: " + e.getMessage());
        }
    }

    public static class ActivityDisplay {
        private final String time;
        private final String employee;
        private final String action;
        private final String status;

        public ActivityDisplay(String time, String employee, String action, String status) {
            this.time = time;
            this.employee = employee;
            this.action = action;
            this.status = status;
        }

        public String getTime() { return time; }
        public String getEmployee() { return employee; }
        public String getAction() { return action; }
        public String getStatus() { return status; }
    }
}