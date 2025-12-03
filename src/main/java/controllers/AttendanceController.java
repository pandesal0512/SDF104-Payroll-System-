package controllers;

import dao.EmployeeDAO;
import dao.AttendanceDAO;
import dao.DepartmentDAO;
import dao.PositionDAO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import models.Employee;
import models.Attendance;
import models.Department;
import models.Position;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendanceController {

    @FXML private TextField qrCodeField;
    @FXML private TextField searchNameField;
    @FXML private ListView<String> searchResultsList;

    @FXML private Label empNameLabel;
    @FXML private Label empQrLabel;
    @FXML private Label empPositionLabel;
    @FXML private Label empDepartmentLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label attendanceStatusLabel;
    @FXML private Label dateLabel;

    @FXML private Button timeInButton;
    @FXML private Button timeOutButton;

    @FXML private TableView<AttendanceDisplay> attendanceLogTable;
    @FXML private TableColumn<AttendanceDisplay, String> timeColumn;
    @FXML private TableColumn<AttendanceDisplay, String> qrCodeColumn;
    @FXML private TableColumn<AttendanceDisplay, String> employeeNameColumn;
    @FXML private TableColumn<AttendanceDisplay, String> actionColumn;
    @FXML private TableColumn<AttendanceDisplay, String> statusColumn;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private PositionDAO positionDAO = new PositionDAO();

    private Employee selectedEmployee;
    private ObservableList<AttendanceDisplay> attendanceList = FXCollections.observableArrayList();
    private Timeline clockTimeline;

    @FXML
    public void initialize() {
        setupTableColumns();
        startClock();
        loadTodayAttendance();
        updateDateLabel();
        setupSearchListener();
    }

    private void setupTableColumns() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        qrCodeColumn.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        attendanceLogTable.setItems(attendanceList);
    }

    private void startClock() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime now = LocalTime.now();
            currentTimeLabel.setText(now.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    private void updateDateLabel() {
        LocalDate today = LocalDate.now();
        dateLabel.setText("Today's Attendance Log - " +
                today.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
    }

    private void setupSearchListener() {
        searchNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                searchEmployeesByName(newVal);
            } else {
                searchResultsList.getItems().clear();
            }
        });
    }

    @FXML
    private void handleFindByQR() {
        String qrCode = qrCodeField.getText().trim();
        if (qrCode.isEmpty()) {
            showWarning("Please enter a QR code");
            return;
        }

        try {
            Employee employee = employeeDAO.getEmployeeByQRCode(qrCode);
            if (employee != null) {
                displayEmployee(employee);
            } else {
                showWarning("Employee not found with QR code: " + qrCode);
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchByName() {
        String searchTerm = searchNameField.getText().trim();
        if (searchTerm.isEmpty()) {
            searchResultsList.getItems().clear();
            return;
        }
        searchEmployeesByName(searchTerm);
    }

    private void searchEmployeesByName(String searchTerm) {
        try {
            List<Employee> employees = employeeDAO.searchEmployeesByName(searchTerm);
            ObservableList<String> results = FXCollections.observableArrayList();

            for (Employee emp : employees) {
                results.add("• " + emp.getName() + " (" + emp.getQrCode() + ")");
            }

            searchResultsList.setItems(results);
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleSelectFromList() {
        String selected = searchResultsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Extract QR code from format: "• Name (QR-CODE)"
        int start = selected.indexOf("(") + 1;
        int end = selected.indexOf(")");
        String qrCode = selected.substring(start, end);

        try {
            Employee employee = employeeDAO.getEmployeeByQRCode(qrCode);
            if (employee != null) {
                displayEmployee(employee);
            }
        } catch (SQLException e) {
            showError("Failed to load employee: " + e.getMessage());
        }
    }

    private void displayEmployee(Employee employee) {
        this.selectedEmployee = employee;

        try {
            Department dept = departmentDAO.getDepartmentById(employee.getDepartmentId());
            Position pos = positionDAO.getPositionById(employee.getPositionId());

            empNameLabel.setText(employee.getName());
            empQrLabel.setText(employee.getQrCode());
            empPositionLabel.setText(pos != null ? pos.getTitle() : "Unknown");
            empDepartmentLabel.setText(dept != null ? dept.getName() : "Unknown");

            // Check if already timed in today
            Attendance todayRecord = attendanceDAO.getAttendanceByEmployeeAndDate(
                    employee.getId(),
                    LocalDate.now().toString()
            );

            if (todayRecord != null) {
                if (todayRecord.getTimeOut() == null || todayRecord.getTimeOut().isEmpty()) {
                    // Already timed in, can time out
                    attendanceStatusLabel.setText("ALREADY TIMED IN");
                    attendanceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    timeInButton.setDisable(true);
                    timeOutButton.setDisable(false);
                } else {
                    // Already completed attendance
                    attendanceStatusLabel.setText("ATTENDANCE COMPLETE");
                    attendanceStatusLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                    timeInButton.setDisable(true);
                    timeOutButton.setDisable(true);
                }
            } else {
                // Can time in
                LocalTime now = LocalTime.now();
                LocalTime cutoff = LocalTime.of(8, 30); // 8:30 AM

                if (now.isBefore(cutoff) || now.equals(cutoff)) {
                    attendanceStatusLabel.setText("ON TIME");
                    attendanceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                } else {
                    attendanceStatusLabel.setText("LATE");
                    attendanceStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                }

                timeInButton.setDisable(false);
                timeOutButton.setDisable(true);
            }

        } catch (SQLException e) {
            showError("Failed to load employee details: " + e.getMessage());
        }
    }

    @FXML
    private void handleTimeIn() {
        if (selectedEmployee == null) {
            showWarning("No employee selected");
            return;
        }

        try {
            String today = LocalDate.now().toString();
            String timeIn = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Determine status
            LocalTime now = LocalTime.now();
            LocalTime cutoff = LocalTime.of(8, 30);
            String status = now.isBefore(cutoff) || now.equals(cutoff) ? "on-time" : "late";

            Attendance attendance = new Attendance(
                    selectedEmployee.getId(),
                    today,
                    timeIn,
                    null,
                    status
            );

            attendanceDAO.addAttendance(attendance);

            showInfo("Time In recorded for " + selectedEmployee.getName() + " at " +
                    LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));

            loadTodayAttendance();
            displayEmployee(selectedEmployee); // Refresh display

        } catch (SQLException e) {
            showError("Failed to record time in: " + e.getMessage());
        }
    }

    @FXML
    private void handleTimeOut() {
        if (selectedEmployee == null) {
            showWarning("No employee selected");
            return;
        }

        try {
            String today = LocalDate.now().toString();
            Attendance attendance = attendanceDAO.getAttendanceByEmployeeAndDate(
                    selectedEmployee.getId(),
                    today
            );

            if (attendance != null && (attendance.getTimeOut() == null || attendance.getTimeOut().isEmpty())) {
                String timeOut = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                attendance.setTimeOut(timeOut);
                attendanceDAO.updateAttendance(attendance);

                showInfo("Time Out recorded for " + selectedEmployee.getName() + " at " +
                        LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));

                loadTodayAttendance();
                displayEmployee(selectedEmployee);
            }

        } catch (SQLException e) {
            showError("Failed to record time out: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        selectedEmployee = null;
        qrCodeField.clear();
        searchNameField.clear();
        searchResultsList.getItems().clear();

        empNameLabel.setText("---");
        empQrLabel.setText("---");
        empPositionLabel.setText("---");
        empDepartmentLabel.setText("---");
        attendanceStatusLabel.setText("---");
        attendanceStatusLabel.setStyle("");

        timeInButton.setDisable(true);
        timeOutButton.setDisable(true);
    }

    private void loadTodayAttendance() {
        try {
            attendanceList.clear();
            String today = LocalDate.now().toString();
            List<Attendance> records = attendanceDAO.getAttendanceByDate(today);

            for (Attendance att : records) {
                Employee emp = employeeDAO.getEmployeeById(att.getEmployeeId());
                if (emp != null) {
                    String time = att.getTimeIn();
                    if (att.getTimeOut() != null && !att.getTimeOut().isEmpty()) {
                        time = att.getTimeOut();
                    }

                    String action = (att.getTimeOut() != null && !att.getTimeOut().isEmpty()) ? "Out" : "In";
                    String status = att.getStatus();

                    attendanceList.add(new AttendanceDisplay(
                            time.substring(0, 5), // HH:mm
                            emp.getQrCode(),
                            emp.getName(),
                            action,
                            formatStatus(status)
                    ));
                }
            }
        } catch (SQLException e) {
            showError("Failed to load attendance log: " + e.getMessage());
        }
    }

    private String formatStatus(String status) {
        switch (status.toLowerCase()) {
            case "on-time": return "On Time";
            case "late": return "Late";
            case "absent": return "Absent";
            default: return status;
        }
    }

    @FXML
    private void handleViewReport() {
        showInfo("Detailed report feature coming soon!");
    }

    @FXML
    private void handleExport() {
        showInfo("Export feature coming soon!");
    }

    // Alert methods
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Display class for TableView
     */
    public static class AttendanceDisplay {
        private final String time;
        private final String qrCode;
        private final String employeeName;
        private final String action;
        private final String status;

        public AttendanceDisplay(String time, String qrCode, String employeeName,
                                 String action, String status) {
            this.time = time;
            this.qrCode = qrCode;
            this.employeeName = employeeName;
            this.action = action;
            this.status = status;
        }

        public String getTime() { return time; }
        public String getQrCode() { return qrCode; }
        public String getEmployeeName() { return employeeName; }
        public String getAction() { return action; }
        public String getStatus() { return status; }
    }
}