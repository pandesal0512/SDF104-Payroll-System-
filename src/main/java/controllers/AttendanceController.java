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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Employee;
import models.Attendance;
import models.Department;
import models.Position;
import database.DatabaseConnection;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import dao.ShiftDAO;
import models.Shift;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
    private final ShiftDAO shiftDAO = new ShiftDAO();

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
        setupListClickHandler();
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
        // Auto-search as user types
        searchNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                searchEmployeesByName(newVal);
            } else {
                searchResultsList.getItems().clear();
            }
        });
    }

    /**
     * Setup single-click handler for search results list
     */
    private void setupListClickHandler() {
        searchResultsList.setOnMouseClicked(event -> {
            // Single click to select
            if (event.getClickCount() >= 1) {
                String selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null && !selected.trim().isEmpty()) {
                    handleSelectFromList();
                }
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
                qrCodeField.clear(); // Clear after successful search
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
                results.add(emp.getName() + " (" + emp.getQrCode() + ")");
            }

            searchResultsList.setItems(results);

            // If only one result, auto-select it
            if (results.size() == 1) {
                searchResultsList.getSelectionModel().select(0);
            }
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleSelectFromList() {
        String selected = searchResultsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.trim().isEmpty()) {
            return;
        }

        try {
            // Extract QR code from format: "Name (QR_CODE)"
            int start = selected.lastIndexOf("(") + 1;
            int end = selected.lastIndexOf(")");

            if (start > 0 && end > start) {
                String qrCode = selected.substring(start, end).trim();

                Employee employee = employeeDAO.getEmployeeByQRCode(qrCode);
                if (employee != null) {
                    displayEmployee(employee);
                    searchNameField.clear();
                    searchResultsList.getItems().clear();
                }
            }
        } catch (Exception e) {
            showError("Failed to load employee: " + e.getMessage());
            e.printStackTrace();
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

            // Check today's attendance
            Attendance todayRecord = attendanceDAO.getAttendanceByEmployeeAndDate(
                    employee.getId(),
                    LocalDate.now().toString()
            );

            if (todayRecord != null) {
                // Already has attendance today
                if (todayRecord.getTimeOut() == null || todayRecord.getTimeOut().isEmpty()) {
                    attendanceStatusLabel.setText("ALREADY TIMED IN");
                    attendanceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    timeInButton.setDisable(true);
                    timeOutButton.setDisable(false);
                } else {
                    attendanceStatusLabel.setText("ATTENDANCE COMPLETE");
                    attendanceStatusLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                    timeInButton.setDisable(true);
                    timeOutButton.setDisable(true);
                }
            } else {
                // No attendance yet - check if late based on shift
                LocalTime now = LocalTime.now();

                // Get employee's shift from position
                Shift employeeShift = null;
                if (pos != null && pos.hasShift()) {
                    employeeShift = shiftDAO.getShiftById(pos.getShiftId());
                }

                if (employeeShift != null) {
                    // Use shift-specific late detection
                    if (employeeShift.isLate(now)) {
                        attendanceStatusLabel.setText("LATE - " + employeeShift.getName());
                        attendanceStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                    } else {
                        attendanceStatusLabel.setText("ON TIME - " + employeeShift.getName());
                        attendanceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    }
                } else {
                    // No shift assigned - try to auto-detect shift
                    employeeShift = shiftDAO.findShiftForTimeIn(now);

                    if (employeeShift != null) {
                        if (employeeShift.isLate(now)) {
                            attendanceStatusLabel.setText("LATE - " + employeeShift.getName() + " (auto-detected)");
                            attendanceStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        } else {
                            attendanceStatusLabel.setText("ON TIME - " + employeeShift.getName() + " (auto-detected)");
                            attendanceStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                        }
                    } else {
                        // Cannot determine shift
                        attendanceStatusLabel.setText("⚠️ NO SHIFT ASSIGNED");
                        attendanceStatusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    }
                }

                timeInButton.setDisable(false);
                timeOutButton.setDisable(true);
            }

        } catch (SQLException e) {
            showError("Failed to load employee details: " + e.getMessage());
        }
    }

    // 4. REPLACE handleTimeIn() method with shift-aware version
    @FXML
    private void handleTimeIn() {
        if (selectedEmployee == null) {
            showWarning("No employee selected");
            return;
        }

        try {
            String today = LocalDate.now().toString();
            LocalTime now = LocalTime.now();
            String timeIn = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Get employee's position and shift
            Position pos = positionDAO.getPositionById(selectedEmployee.getPositionId());
            Shift employeeShift = null;

            if (pos != null && pos.hasShift()) {
                employeeShift = shiftDAO.getShiftById(pos.getShiftId());
            }

            // If no shift assigned, try auto-detect
            if (employeeShift == null) {
                employeeShift = shiftDAO.findShiftForTimeIn(now);
            }

            // Determine if late
            String status;
            String shiftInfo = "";

            if (employeeShift != null) {
                status = employeeShift.isLate(now) ? "late" : "on-time";
                shiftInfo = " (" + employeeShift.getName() + ")";
            } else {
                // Fallback to old 8:30 AM cutoff if no shift found
                LocalTime cutoff = LocalTime.of(8, 30);
                status = now.isBefore(cutoff) || now.equals(cutoff) ? "on-time" : "late";
                shiftInfo = " (Default schedule)";
            }

            // Create attendance record
            Attendance attendance = new Attendance(
                    selectedEmployee.getId(),
                    today,
                    timeIn,
                    null,
                    status
            );

            attendanceDAO.addAttendance(attendance);

            // Update shift_id in attendance if shift detected
            if (employeeShift != null) {
                // Store shift info in attendance record for reporting
                Connection conn = DatabaseConnection.getConnection();
                String updateSql = "UPDATE attendance SET shift_id = ? WHERE id = (SELECT MAX(id) FROM attendance WHERE employee_id = ?)";
                PreparedStatement stmt = conn.prepareStatement(updateSql);
                stmt.setInt(1, employeeShift.getId());
                stmt.setInt(2, selectedEmployee.getId());
                stmt.executeUpdate();
                stmt.close();
                conn.close();
            }

            showInfo("✓ Time In Recorded!\n\n" +
                    selectedEmployee.getName() + "\n" +
                    now.format(DateTimeFormatter.ofPattern("hh:mm a")) + "\n" +
                    "Status: " + (status.equals("on-time") ? "ON TIME" : "LATE") +
                    shiftInfo);

            loadTodayAttendance();
            displayEmployee(selectedEmployee);

        } catch (SQLException e) {
            showError("Failed to record time in: " + e.getMessage());
        }
    }

    // 5. ADD helper method to show shift information
    private void showShiftInformation() {
        try {
            List<Shift> shifts = shiftDAO.getActiveShifts();
            StringBuilder info = new StringBuilder();

            info.append("═══════════════════════════════════════════\n");
            info.append("     HOSPITAL SHIFT SCHEDULE\n");
            info.append("═══════════════════════════════════════════\n\n");

            for (Shift shift : shifts) {
                info.append(shift.getShiftEmoji()).append(" ").append(shift.getName()).append("\n");
                info.append("   Time: ").append(shift.getShiftTimeRange()).append("\n");
                info.append("   Late: Any time after ").append(shift.getStartTime().format(
                        DateTimeFormatter.ofPattern("hh:mm a"))).append("\n");
                info.append("   Hours: ").append(String.format("%.1f", shift.getShiftHours())).append("\n\n");
            }

            info.append("═══════════════════════════════════════════\n");
            info.append("⚠️  NO GRACE PERIOD - Exact start time required\n");
            info.append("═══════════════════════════════════════════\n");
            info.append("Current Time: ").append(LocalTime.now().format(
                    DateTimeFormatter.ofPattern("hh:mm a"))).append("\n");

            // Find current shift
            Shift currentShift = shiftDAO.findShiftForTimeIn(LocalTime.now());
            if (currentShift != null) {
                info.append("Active Shift: ").append(currentShift.getName()).append("\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Shift Information");
            alert.setHeaderText("Hospital 24/7 Operations");

            TextArea textArea = new TextArea(info.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(15);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();

        } catch (SQLException e) {
            showError("Failed to load shift information: " + e.getMessage());
        }
    }


    @FXML
    private void handleShowShiftInfo() {
        showShiftInformation();
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

                // Calculate hours worked
                double hoursWorked = calculateHours(attendance.getTimeIn(), timeOut);

                attendanceDAO.updateAttendance(attendance);

                showInfo("Time Out Recorded!\n\n" +
                        selectedEmployee.getName() + "\n" +
                        LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + "\n" +
                        String.format("Hours worked: %.2f", hoursWorked));

                loadTodayAttendance();
                displayEmployee(selectedEmployee);
            }

        } catch (SQLException e) {
            showError("Failed to record time out: " + e.getMessage());
        }
    }

    private double calculateHours(String timeIn, String timeOut) {
        try {
            LocalTime in = LocalTime.parse(timeIn);
            LocalTime out = LocalTime.parse(timeOut);
            long minutes = ChronoUnit.MINUTES.between(in, out);
            return minutes / 60.0;
        } catch (Exception e) {
            return 0.0;
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
                    String action = "In";

                    if (att.getTimeOut() != null && !att.getTimeOut().isEmpty()) {
                        time = att.getTimeOut();
                        action = "Out";
                    }

                    String status = formatStatus(att.getStatus());

                    attendanceList.add(new AttendanceDisplay(
                            time.substring(0, 5),
                            emp.getQrCode(),
                            emp.getName(),
                            action,
                            status
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
        try {
            String today = LocalDate.now().toString();
            List<Attendance> records = attendanceDAO.getAttendanceByDate(today);

            int totalPresent = 0;
            int totalLate = 0;
            int totalOnTime = 0;
            double totalHours = 0;

            for (Attendance att : records) {
                totalPresent++;
                if ("late".equals(att.getStatus())) {
                    totalLate++;
                } else if ("on-time".equals(att.getStatus())) {
                    totalOnTime++;
                }

                if (att.getTimeOut() != null && !att.getTimeOut().isEmpty()) {
                    totalHours += calculateHours(att.getTimeIn(), att.getTimeOut());
                }
            }

            int totalEmployees = employeeDAO.getActiveEmployees().size();
            int absent = totalEmployees - totalPresent;

            String report = String.format(
                    "═══════════════════════════════════════════\n" +
                            "     ATTENDANCE REPORT - %s\n" +
                            "═══════════════════════════════════════════\n\n" +
                            "SUMMARY\n" +
                            "───────────────────────────────────────────\n" +
                            "Total Employees:      %d\n" +
                            "Present:              %d\n" +
                            "  - On Time:          %d\n" +
                            "  - Late:             %d\n" +
                            "Absent:               %d\n" +
                            "Total Hours Worked:   %.2f hours\n\n" +
                            "ATTENDANCE RATE\n" +
                            "───────────────────────────────────────────\n" +
                            "Present Rate:         %.1f%%\n" +
                            "On-Time Rate:         %.1f%%\n" +
                            "Late Rate:            %.1f%%\n\n" +
                            "═══════════════════════════════════════════\n",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    totalEmployees,
                    totalPresent,
                    totalOnTime,
                    totalLate,
                    absent,
                    totalHours,
                    (totalPresent * 100.0 / totalEmployees),
                    (totalOnTime * 100.0 / totalEmployees),
                    (totalLate * 100.0 / totalEmployees)
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Attendance Report");
            alert.setHeaderText(null);

            TextArea textArea = new TextArea(report);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(20);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefWidth(550);
            alert.showAndWait();

        } catch (SQLException e) {
            showError("Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Export to TEXT format (not Excel) - similar to payslip receipts
     */
    @FXML
    private void handleExport() {
        if (attendanceList.isEmpty()) {
            showWarning("No attendance records to export!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Today's Attendance");
        fileChooser.setInitialFileName("attendance_" + LocalDate.now().toString() + ".txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        Stage stage = (Stage) attendanceLogTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Header
                writer.println("═══════════════════════════════════════════════════════════");
                writer.println("                 DAILY ATTENDANCE LOG");
                writer.println("                " + LocalDate.now().format(
                        DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
                writer.println("═══════════════════════════════════════════════════════════\n");

                // Table header
                writer.println(String.format("%-8s %-15s %-25s %-8s %-12s",
                        "Time", "QR Code", "Employee Name", "Action", "Status"));
                writer.println("───────────────────────────────────────────────────────────");

                // Records
                for (AttendanceDisplay att : attendanceList) {
                    writer.println(String.format("%-8s %-15s %-25s %-8s %-12s",
                            att.getTime(),
                            att.getQrCode(),
                            att.getEmployeeName().length() > 25 ?
                                    att.getEmployeeName().substring(0, 22) + "..." :
                                    att.getEmployeeName(),
                            att.getAction(),
                            att.getStatus()
                    ));
                }

                writer.println("───────────────────────────────────────────────────────────");
                writer.println("\nTotal Records: " + attendanceList.size());
                writer.println("Exported: " + LocalTime.now().format(
                        DateTimeFormatter.ofPattern("hh:mm a")));
                writer.println("\n═══════════════════════════════════════════════════════════");

                showInfo(" Export Successful!\n\n" +
                        attendanceList.size() + " records exported to:\n" +
                        file.getAbsolutePath() + "\n\n" +
                        "You can now print this file.");

            } catch (Exception e) {
                showError("Export failed: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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