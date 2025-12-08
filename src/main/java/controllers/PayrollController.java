package controllers;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.*;
import utils.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PayrollController {

    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Department> departmentFilterCombo;
    @FXML private TextField payrollSearchField;

    @FXML private TableView<PayrollDisplay> payrollTable;
    @FXML private TableColumn<PayrollDisplay, String> qrCodeColumn;
    @FXML private TableColumn<PayrollDisplay, String> nameColumn;
    @FXML private TableColumn<PayrollDisplay, Double> hoursColumn;
    @FXML private TableColumn<PayrollDisplay, Double> hourlyRateColumn;
    @FXML private TableColumn<PayrollDisplay, Double> baseSalaryColumn;
    @FXML private TableColumn<PayrollDisplay, Double> adjustmentColumn;
    @FXML private TableColumn<PayrollDisplay, Double> netPayColumn;
    @FXML private TableColumn<PayrollDisplay, String> notesColumn;
    @FXML private TableColumn<PayrollDisplay, Void> actionsColumn;

    private PayrollDAO payrollDAO = new PayrollDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private PositionDAO positionDAO = new PositionDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();

    private ObservableList<PayrollDisplay> payrollList = FXCollections.observableArrayList();
    private int selectedMonth;
    private int selectedYear;

    @FXML
    public void initialize() {
        setupMonthCombo();
        setupYearCombo();
        setupDepartmentFilter();
        setupTableColumns();

        // Auto-load current month's payroll
        selectedMonth = DateTimeHelper.getCurrentMonth();
        selectedYear = DateTimeHelper.getCurrentYear();

        // Always load from database first
        loadExistingPayroll();
    }

    private void setupMonthCombo() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthCombo.setItems(FXCollections.observableArrayList(months));
        monthCombo.setValue(DateTimeHelper.getMonthName(DateTimeHelper.getCurrentMonth()));
    }

    private void setupYearCombo() {
        int currentYear = DateTimeHelper.getCurrentYear();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int i = currentYear - 2; i <= currentYear + 1; i++) {
            years.add(i);
        }
        yearCombo.setItems(years);
        yearCombo.setValue(currentYear);
    }

    private void setupDepartmentFilter() {
        try {
            List<Department> departments = departmentDAO.getAllDepartments();
            departmentFilterCombo.setItems(FXCollections.observableArrayList(departments));

            departmentFilterCombo.setCellFactory(param -> new ListCell<Department>() {
                @Override
                protected void updateItem(Department item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });

            departmentFilterCombo.setButtonCell(new ListCell<Department>() {
                @Override
                protected void updateItem(Department item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
        } catch (SQLException e) {
            showError("Failed to load departments: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        qrCodeColumn.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        hourlyRateColumn.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        adjustmentColumn.setCellValueFactory(new PropertyValueFactory<>("adjustment"));
        netPayColumn.setCellValueFactory(new PropertyValueFactory<>("netPay"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Format columns
        hoursColumn.setCellFactory(col -> new TableCell<PayrollDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%.2f hrs", item));
            }
        });

        hourlyRateColumn.setCellFactory(col -> new TableCell<PayrollDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("₱%.2f/hr", item));
            }
        });

        baseSalaryColumn.setCellFactory(col -> new TableCell<PayrollDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("₱%,.2f", item));
            }
        });

        adjustmentColumn.setCellFactory(col -> new TableCell<PayrollDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("");
                } else if (item > 0) {
                    setText(String.format("+₱%,.2f", item));
                    setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                } else {
                    setText(String.format("-₱%,.2f", Math.abs(item)));
                    setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                }
            }
        });

        netPayColumn.setCellFactory(col -> new TableCell<PayrollDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("₱%,.2f", item));
                if (!empty && item != null) {
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3; -fx-font-size: 14px;");
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<PayrollDisplay, Void>() {
            private final Button viewButton = new Button("📊 View");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px;");
                viewButton.setOnAction(event -> {
                    PayrollDisplay data = getTableView().getItems().get(getIndex());
                    payrollTable.getSelectionModel().select(data);
                    handleViewPayslip();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        payrollTable.setItems(payrollList);
    }

    /**
     * Load existing payroll from database (keeps data persistent)
     */
    private void loadExistingPayroll() {
        try {
            payrollList.clear();
            List<Payroll> existingPayroll = payrollDAO.getPayrollByPeriod(selectedMonth, selectedYear);

            for (Payroll payroll : existingPayroll) {
                Employee emp = employeeDAO.getEmployeeById(payroll.getEmployeeId());
                if (emp == null) continue;

                Position pos = positionDAO.getPositionById(emp.getPositionId());
                if (pos == null) continue;

                // Calculate hours for display
                YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
                String startDate = yearMonth.atDay(1).toString();
                String endDate = yearMonth.atEndOfMonth().toString();
                double totalHours = attendanceDAO.getTotalHoursWorked(emp.getId(), startDate, endDate);

                // Adjustment = final_salary - base_salary
                double adjustment = payroll.getFinalSalary() - payroll.getBaseSalary();

                payrollList.add(new PayrollDisplay(
                        emp.getId(), emp.getQrCode(), emp.getName(),
                        totalHours, pos.getHourlyRate(), payroll.getBaseSalary(),
                        adjustment, payroll.getFinalSalary(), payroll.getNotes()
                ));
            }

            System.out.println("✓ Loaded " + payrollList.size() + " payroll records from database");

        } catch (SQLException e) {
            // No existing payroll, that's fine
            System.out.println("No existing payroll for " + selectedMonth + "/" + selectedYear);
        }
    }

    @FXML
    private void handleCalculatePayroll() {
        selectedMonth = DateTimeHelper.getMonthNumber(monthCombo.getValue());
        selectedYear = yearCombo.getValue();

        // First, try to load existing payroll
        loadExistingPayroll();

        if (!payrollList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Existing Payroll Found");
            alert.setHeaderText(payrollList.size() + " employee(s) already processed for " +
                    monthCombo.getValue() + " " + selectedYear);
            alert.setContentText("Payroll has been loaded from database.\n\n" +
                    "To update with latest attendance:\n" +
                    "• Click 'Recalculate Anyway' to override, OR\n" +
                    "• Use 'Add Adjustment' for individual changes");

            ButtonType recalculateButton = new ButtonType("🔄 Recalculate Anyway", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Keep Existing", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(recalculateButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() == cancelButton) {
                return;
            }
            // User chose to recalculate - continue below
        }

        // Calculate new payroll (or recalculate)
        try {
            payrollList.clear();
            List<Employee> employees = departmentFilterCombo.getValue() != null ?
                    employeeDAO.getEmployeesByDepartment(departmentFilterCombo.getValue().getId()) :
                    employeeDAO.getActiveEmployees();

            // FIX: Create YearMonth for the selected period
            YearMonth payrollPeriod = YearMonth.of(selectedYear, selectedMonth);
            LocalDate payrollStart = payrollPeriod.atDay(1);
            LocalDate payrollEnd = payrollPeriod.atEndOfMonth();

            int processed = 0;
            for (Employee emp : employees) {
                if (!"active".equals(emp.getStatus())) continue;

                LocalDate hireDate = LocalDate.parse(emp.getHireDate());

                // FIX: Employee should be included if hired on or before the LAST day of the payroll month
                if (hireDate.isAfter(payrollEnd)) {
                    System.out.println("Skipping " + emp.getName() + " - hired after payroll period");
                    continue;
                }

                Position pos = positionDAO.getPositionById(emp.getPositionId());
                if (pos == null) continue;

                String startDate = payrollStart.toString();
                String endDate = payrollEnd.toString();

                double totalHours = attendanceDAO.getTotalHoursWorked(emp.getId(), startDate, endDate);
                double hourlyRate = pos.getHourlyRate();
                double baseSalary = totalHours * hourlyRate;
                double adjustment = 0.0;
                double netPay = baseSalary + adjustment;

                payrollList.add(new PayrollDisplay(
                        emp.getId(), emp.getQrCode(), emp.getName(),
                        totalHours, hourlyRate, baseSalary, adjustment, netPay, ""
                ));
                processed++;
            }

            if (processed == 0) {
                showWarning("No employees found for the selected period!");
            } else {
                showInfo("✅ Payroll calculated for " + processed + " employees!\n\n" +
                        "Based on: Hours Worked × Hourly Rate\n\n" +
                        "⚠️ Click 'Process All' to save to database.\n" +
                        "Data will NOT persist until you click 'Process All'!");
            }

        } catch (SQLException e) {
            showError("Calculation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProcessAll() {
        if (payrollList.isEmpty()) {
            showWarning("Please calculate payroll first!");
            return;
        }

        if (!DialogHelper.showConfirmation("Process payroll for " + payrollList.size() + " employees?\n\n" +
                "This will save all records to the database.")) {
            return;
        }

        try {
            String today = DateTimeHelper.getCurrentDate();
            int processed = 0;
            int updated = 0;
            int skipped = 0;

            for (PayrollDisplay pd : payrollList) {
                // Check if payroll already exists
                Payroll existing = payrollDAO.getPayrollByEmployeeAndPeriod(
                        pd.getEmployeeId(), selectedMonth, selectedYear
                );

                if (existing != null) {
                    // UPDATE existing payroll
                    existing.setBaseSalary(pd.getBaseSalary());
                    existing.setTotalDeductions(0.0);
                    existing.setFinalSalary(pd.getNetPay());
                    existing.setNotes(pd.getNotes());

                    payrollDAO.updatePayroll(existing);
                    updated++;
                } else {
                    // INSERT new payroll
                    Payroll payroll = new Payroll(
                            pd.getEmployeeId(), selectedMonth, selectedYear,
                            pd.getBaseSalary(), 0.0, pd.getNetPay(),
                            0, 0, today, pd.getNotes()
                    );

                    payrollDAO.addPayroll(payroll);
                    processed++;
                }
            }

            String message = "✅ Payroll Processing Complete!\n\n";
            if (processed > 0) {
                message += "New records: " + processed + " employees\n";
            }
            if (updated > 0) {
                message += "Updated: " + updated + " employees\n";
            }
            if (skipped > 0) {
                message += "Skipped: " + skipped + " (errors)\n";
            }
            message += "\n✓ Payroll data saved and will persist!";

            showInfo(message);

            // Reload from database to confirm save
            loadExistingPayroll();

        } catch (SQLException e) {
            showError("Processing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManualAdjustment() {
        PayrollDisplay selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee first!");
            return;
        }

        Dialog<Adjustment> dialog = new Dialog<>();
        dialog.setTitle("Add Adjustment");
        dialog.setHeaderText("Adjust salary for: " + selected.getEmployeeName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Adjustment type
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton bonusRadio = new RadioButton("💰 Bonus");
        RadioButton leaveRadio = new RadioButton("🏖️ Leave with Pay");
        RadioButton overtimeRadio = new RadioButton("⏰ Overtime Hours");
        RadioButton deductionRadio = new RadioButton("📉 Deduction");

        bonusRadio.setToggleGroup(typeGroup);
        leaveRadio.setToggleGroup(typeGroup);
        overtimeRadio.setToggleGroup(typeGroup);
        deductionRadio.setToggleGroup(typeGroup);
        bonusRadio.setSelected(true);

        VBox typeBox = new VBox(10, bonusRadio, leaveRadio, overtimeRadio, deductionRadio);

        // Amount/Hours input
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount or hours");

        // Notes
        TextArea notesArea = new TextArea(selected.getNotes());
        notesArea.setPromptText("E.g., 13th month pay, Holiday bonus, Medical leave, etc.");
        notesArea.setPrefRowCount(3);

        grid.add(new Label("Adjustment Type:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Amount/Hours:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Notes:"), 0, 2);
        grid.add(notesArea, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    double value = Double.parseDouble(amountField.getText());
                    String type;
                    double adjustmentAmount;

                    if (bonusRadio.isSelected()) {
                        type = "Bonus";
                        adjustmentAmount = value;
                    } else if (leaveRadio.isSelected()) {
                        type = "Leave with Pay";
                        adjustmentAmount = value * selected.getHourlyRate();
                    } else if (overtimeRadio.isSelected()) {
                        type = "Overtime";
                        adjustmentAmount = value * selected.getHourlyRate() * 1.5;
                    } else {
                        type = "Deduction";
                        adjustmentAmount = -value;
                    }

                    return new Adjustment(type, adjustmentAmount, notesArea.getText());
                } catch (NumberFormatException e) {
                    showError("Please enter a valid number");
                    return null;
                }
            }
            return null;
        });

        Optional<Adjustment> result = dialog.showAndWait();
        result.ifPresent(adj -> {
            double newAdjustment = selected.getAdjustment() + adj.amount;
            double newNetPay = selected.getBaseSalary() + newAdjustment;

            selected.setAdjustment(newAdjustment);
            selected.setNetPay(newNetPay);
            selected.setNotes(adj.notes);

            payrollTable.refresh();

            showInfo("✅ Adjustment Applied!\n\n" +
                    "Type: " + adj.type + "\n" +
                    "Amount: ₱" + String.format("%,.2f", adj.amount) + "\n" +
                    "New Net Pay: ₱" + String.format("%,.2f", newNetPay) + "\n\n" +
                    "⚠️ Remember to click 'Process All' to save!");
        });
    }

    @FXML
    private void handleViewPayslip() {
        PayrollDisplay selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee!");
            return;
        }

        try {
            Employee emp = employeeDAO.getEmployeeById(selected.getEmployeeId());
            Position pos = positionDAO.getPositionById(emp.getPositionId());
            Department dept = departmentDAO.getDepartmentById(emp.getDepartmentId());

            String payslip = generatePayslip(emp, pos, dept, selected);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payslip - " + emp.getName());
            alert.setHeaderText(null);

            TextArea textArea = new TextArea(payslip);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(25);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefWidth(650);

            // Add Print button
            ButtonType printButton = new ButtonType("🖨️ Print", ButtonBar.ButtonData.LEFT);
            alert.getButtonTypes().add(0, printButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == printButton) {
                handlePrintPayslip(payslip, emp.getName());
            }

        } catch (SQLException e) {
            showError("Failed to generate payslip: " + e.getMessage());
        }
    }

    private void handlePrintPayslip(String payslip, String employeeName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payslip");
        fileChooser.setInitialFileName("payslip_" + employeeName.replace(" ", "_") + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        Stage stage = (Stage) payrollTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.print(payslip);
                showInfo("✅ Payslip saved!\n\n" + file.getAbsolutePath() + "\n\nYou can now print this file.");
            } catch (Exception e) {
                showError("Failed to save payslip: " + e.getMessage());
            }
        }
    }

    private String generatePayslip(Employee emp, Position pos, Department dept, PayrollDisplay pd) {
        StringBuilder adjustmentDetails = new StringBuilder();
        if (pd.getAdjustment() != 0) {
            adjustmentDetails.append(String.format("Amount:           ₱%,.2f\n", pd.getAdjustment()));
            if (!pd.getNotes().isEmpty()) {
                adjustmentDetails.append(String.format("Details:          %s\n", pd.getNotes()));
            }
        } else {
            adjustmentDetails.append("None\n");
        }

        return String.format(
                "═══════════════════════════════════════════════\n" +
                        "           HR PAYROLL SYSTEM\n" +
                        "              EMPLOYEE PAYSLIP\n" +
                        "               %s %d\n" +
                        "═══════════════════════════════════════════════\n\n" +
                        "EMPLOYEE INFORMATION\n" +
                        "─────────────────────────────────────────────\n" +
                        "Name:             %s\n" +
                        "QR Code:          %s\n" +
                        "Position:         %s\n" +
                        "Department:       %s\n\n" +
                        "COMPENSATION BREAKDOWN\n" +
                        "─────────────────────────────────────────────\n" +
                        "Hourly Rate:      ₱%.2f per hour\n" +
                        "Hours Worked:     %.2f hours\n" +
                        "Gross Pay:        ₱%,.2f\n" +
                        "                  (%.2f hrs × ₱%.2f/hr)\n\n" +
                        "ADJUSTMENTS\n" +
                        "─────────────────────────────────────────────\n" +
                        "%s\n" +
                        "═══════════════════════════════════════════════\n" +
                        "NET PAY:          ₱%,.2f\n" +
                        "═══════════════════════════════════════════════\n\n" +
                        "Processed: %s\n" +
                        "This is a computer-generated payslip.\n",
                monthCombo.getValue(), selectedYear,
                emp.getName(), emp.getQrCode(),
                pos.getTitle(), dept.getName(),
                pd.getHourlyRate(), pd.getHoursWorked(), pd.getBaseSalary(),
                pd.getHoursWorked(), pd.getHourlyRate(),
                adjustmentDetails.toString(),
                pd.getNetPay(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
        );
    }

    @FXML
    private void handleSendPayslips() {
        if (payrollList.isEmpty()) {
            showWarning("No payroll data available!");
            return;
        }

        showInfo("📧 Email Payslips\n\n" +
                "This feature requires email configuration.\n\n" +
                "Current options:\n" +
                "• View individual payslips and print\n" +
                "• Export all payslips to text files\n\n" +
                "Contact your system administrator to set up email functionality.");
    }

    @FXML
    private void handleExportPayroll() {
        if (payrollList.isEmpty()) {
            showWarning("No payroll data to export!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payroll Summary");
        fileChooser.setInitialFileName("payroll_" + monthCombo.getValue() + "_" + selectedYear + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        Stage stage = (Stage) payrollTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Header
                writer.println("═══════════════════════════════════════════════════════════════");
                writer.println("                    PAYROLL SUMMARY REPORT");
                writer.println("                  " + monthCombo.getValue() + " " + selectedYear);
                writer.println("═══════════════════════════════════════════════════════════════\n");

                // Table header
                writer.println(String.format("%-15s %-25s %10s %12s %12s %15s",
                        "QR Code", "Name", "Hours", "Hourly Rate", "Base Salary", "Net Pay"));
                writer.println("───────────────────────────────────────────────────────────────");

                double totalHours = 0;
                double totalBase = 0;
                double totalNet = 0;

                for (PayrollDisplay pd : payrollList) {
                    writer.println(String.format("%-15s %-25s %10.2f ₱%11.2f ₱%11.2f ₱%14.2f",
                            pd.getQrCode(),
                            pd.getEmployeeName().length() > 25 ? pd.getEmployeeName().substring(0, 22) + "..." : pd.getEmployeeName(),
                            pd.getHoursWorked(),
                            pd.getHourlyRate(),
                            pd.getBaseSalary(),
                            pd.getNetPay()
                    ));

                    totalHours += pd.getHoursWorked();
                    totalBase += pd.getBaseSalary();
                    totalNet += pd.getNetPay();
                }

                writer.println("───────────────────────────────────────────────────────────────");
                writer.println(String.format("%-15s %-25s %10.2f %12s ₱%11.2f ₱%14.2f",
                        "", "TOTAL", totalHours, "", totalBase, totalNet));
                writer.println("═══════════════════════════════════════════════════════════════");
                writer.println("\nGenerated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
                writer.println("Total Employees: " + payrollList.size());

                showInfo("✅ Export Successful!\n\n" +
                        "Payroll summary saved to:\n" +
                        file.getAbsolutePath() + "\n\n" +
                        "You can now print this file.");

            } catch (Exception e) {
                showError("Export failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleSearch() {
        String searchTerm = payrollSearchField.getText().trim();

        if (searchTerm.isEmpty()) {
            // Reload from database
            loadExistingPayroll();
            return;
        }

        ObservableList<PayrollDisplay> filtered = FXCollections.observableArrayList();
        String lowerSearch = searchTerm.toLowerCase();

        for (PayrollDisplay pd : payrollList) {
            if (pd.getEmployeeName().toLowerCase().contains(lowerSearch) ||
                    pd.getQrCode().toLowerCase().contains(lowerSearch)) {
                filtered.add(pd);
            }
        }

        payrollTable.setItems(filtered);
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
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== INNER CLASSES ====================

    public static class PayrollDisplay {
        private final int employeeId;
        private final String qrCode;
        private final String employeeName;
        private final double hoursWorked;
        private final double hourlyRate;
        private final double baseSalary;
        private double adjustment;
        private double netPay;
        private String notes;

        public PayrollDisplay(int employeeId, String qrCode, String employeeName,
                              double hoursWorked, double hourlyRate, double baseSalary,
                              double adjustment, double netPay, String notes) {
            this.employeeId = employeeId;
            this.qrCode = qrCode;
            this.employeeName = employeeName;
            this.hoursWorked = hoursWorked;
            this.hourlyRate = hourlyRate;
            this.baseSalary = baseSalary;
            this.adjustment = adjustment;
            this.netPay = netPay;
            this.notes = notes;
        }

        // Getters
        public int getEmployeeId() { return employeeId; }
        public String getQrCode() { return qrCode; }
        public String getEmployeeName() { return employeeName; }
        public double getHoursWorked() { return hoursWorked; }
        public double getHourlyRate() { return hourlyRate; }
        public double getBaseSalary() { return baseSalary; }
        public double getAdjustment() { return adjustment; }
        public double getNetPay() { return netPay; }
        public String getNotes() { return notes; }

        // Setters
        public void setAdjustment(double adjustment) { this.adjustment = adjustment; }
        public void setNetPay(double netPay) { this.netPay = netPay; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    private static class Adjustment {
        final String type;
        final double amount;
        final String notes;

        Adjustment(String type, double amount, String notes) {
            this.type = type;
            this.amount = amount;
            this.notes = notes;
        }
    }
}