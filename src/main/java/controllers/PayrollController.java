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
    @FXML private ListView<String> searchResultsList;  // NEW - Auto search results

    @FXML private TableView<PayrollDisplay> payrollTable;
    @FXML private TableColumn<PayrollDisplay, String> qrCodeColumn;
    @FXML private TableColumn<PayrollDisplay, String> nameColumn;
    @FXML private TableColumn<PayrollDisplay, Double> hoursColumn;
    @FXML private TableColumn<PayrollDisplay, Double> hourlyRateColumn;
    @FXML private TableColumn<PayrollDisplay, Double> baseSalaryColumn;
    @FXML private TableColumn<PayrollDisplay, Double> govDeductionsColumn;  // NEW
    @FXML private TableColumn<PayrollDisplay, Double> adjustmentColumn;
    @FXML private TableColumn<PayrollDisplay, Double> netPayColumn;
    @FXML private TableColumn<PayrollDisplay, String> statusColumn;  // NEW - Hold status
    @FXML private TableColumn<PayrollDisplay, Void> actionsColumn;

    private PayrollDAO payrollDAO = new PayrollDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private PositionDAO positionDAO = new PositionDAO();
    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    private PayrollAdjustmentDAO adjustmentDAO = new PayrollAdjustmentDAO();
    private SalaryHoldDAO salaryHoldDAO = new SalaryHoldDAO();

    private ObservableList<PayrollDisplay> payrollList = FXCollections.observableArrayList();
    private int selectedMonth;
    private int selectedYear;
    private int currentUserId = 1;  // TODO: Get from session

    @FXML
    public void initialize() {
        setupMonthCombo();
        setupYearCombo();
        setupDepartmentFilter();
        setupTableColumns();
        setupAutoSearch();  // NEW

        selectedMonth = DateTimeHelper.getCurrentMonth();
        selectedYear = DateTimeHelper.getCurrentYear();

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

    /**
     * NEW - Setup auto-search functionality
     */
    private void setupAutoSearch() {
        if (payrollSearchField != null) {
            payrollSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty()) {
                    searchEmployeesForPayroll(newVal);
                } else {
                    if (searchResultsList != null) {
                        searchResultsList.getItems().clear();
                    }
                    // Show all employees in table
                    loadExistingPayroll();
                }
            });
        }

        if (searchResultsList != null) {
            searchResultsList.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 1) {
                    String selected = searchResultsList.getSelectionModel().getSelectedItem();
                    if (selected != null && !selected.trim().isEmpty()) {
                        handleSelectEmployeeFromSearch();
                    }
                }
            });
        }
    }

    /**
     * NEW - Search employees for payroll
     */
    private void searchEmployeesForPayroll(String searchTerm) {
        try {
            List<Employee> employees = employeeDAO.searchEmployeesByName(searchTerm);
            ObservableList<String> results = FXCollections.observableArrayList();

            for (Employee emp : employees) {
                results.add(emp.getName() + " (" + emp.getQrCode() + ")");
            }

            if (searchResultsList != null) {
                searchResultsList.setItems(results);
            }

            // Also filter table
            filterPayrollTable(searchTerm);

        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    /**
     * NEW - Filter payroll table by search term
     */
    private void filterPayrollTable(String searchTerm) {
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

    /**
     * NEW - Handle employee selection from search
     */
    private void handleSelectEmployeeFromSearch() {
        String selected = searchResultsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.trim().isEmpty()) {
            return;
        }

        try {
            int start = selected.lastIndexOf("(") + 1;
            int end = selected.lastIndexOf(")");

            if (start > 0 && end > start) {
                String qrCode = selected.substring(start, end).trim();

                // Find employee in current payroll list
                for (PayrollDisplay pd : payrollList) {
                    if (pd.getQrCode().equals(qrCode)) {
                        payrollTable.getSelectionModel().select(pd);
                        payrollTable.scrollTo(pd);
                        break;
                    }
                }

                payrollSearchField.clear();
                searchResultsList.getItems().clear();
            }
        } catch (Exception e) {
            showError("Failed to select employee: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        qrCodeColumn.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        hourlyRateColumn.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        govDeductionsColumn.setCellValueFactory(new PropertyValueFactory<>("govDeductions"));
        adjustmentColumn.setCellValueFactory(new PropertyValueFactory<>("adjustment"));
        netPayColumn.setCellValueFactory(new PropertyValueFactory<>("netPay"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("holdStatus"));

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

        // NEW - Gov Deductions column
        govDeductionsColumn.setCellFactory(col -> new TableCell<PayrollDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("");
                } else {
                    setText(String.format("-₱%,.2f", item));
                    setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                }
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

        // NEW - Status column (Hold indicator)
        statusColumn.setCellFactory(col -> new TableCell<PayrollDisplay, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("");
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
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

    private void loadExistingPayroll() {
        try {
            payrollList.clear();
            List<Payroll> existingPayroll = payrollDAO.getPayrollByPeriod(selectedMonth, selectedYear);

            for (Payroll payroll : existingPayroll) {
                Employee emp = employeeDAO.getEmployeeById(payroll.getEmployeeId());
                if (emp == null) continue;

                Position pos = positionDAO.getPositionById(emp.getPositionId());
                if (pos == null) continue;

                YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
                String startDate = yearMonth.atDay(1).toString();
                String endDate = yearMonth.atEndOfMonth().toString();
                double totalHours = attendanceDAO.getTotalHoursWorked(emp.getId(), startDate, endDate);

                // Calculate government deductions
                GovernmentDeductionCalculator.GovernmentDeductions govDed =
                        GovernmentDeductionCalculator.calculateAll(payroll.getBaseSalary());

                // Get adjustments
                double adjustment = adjustmentDAO.calculateTotalAdjustments(
                        emp.getId(), startDate, endDate);

                // Check if salary is held
                boolean isHeld = salaryHoldDAO.isSalaryHeld(emp.getId());
                String holdStatus = isHeld ? "🔒 HELD" : "";

                payrollList.add(new PayrollDisplay(
                        emp.getId(), emp.getQrCode(), emp.getName(),
                        totalHours, pos.getHourlyRate(), payroll.getBaseSalary(),
                        govDed.total, adjustment, payroll.getFinalSalary(),
                        holdStatus, payroll.getNotes()
                ));
            }

            System.out.println("✓ Loaded " + payrollList.size() + " payroll records");

        } catch (SQLException e) {
            System.out.println("No existing payroll for " + selectedMonth + "/" + selectedYear);
        }
    }

    @FXML
    private void handleCalculatePayroll() {
        selectedMonth = DateTimeHelper.getMonthNumber(monthCombo.getValue());
        selectedYear = yearCombo.getValue();

        loadExistingPayroll();

        if (!payrollList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Existing Payroll Found");
            alert.setHeaderText(payrollList.size() + " employee(s) already processed");
            alert.setContentText("Payroll loaded from database.\n\n" +
                    "Options:\n" +
                    "• Click 'Recalculate' to override with latest data\n" +
                    "• Use 'Calculate Individual' for specific employees\n" +
                    "• Use 'Add Adjustment' for bonuses/deductions");

            ButtonType recalcBtn = new ButtonType("🔄 Recalculate All", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Keep Existing", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(recalcBtn, cancelBtn);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() == cancelBtn) {
                return;
            }
        }

        calculateAllPayroll();
    }

    /**
     * NEW - Calculate individual employee payroll
     */
    @FXML
    private void handleCalculateIndividual() {
        PayrollDisplay selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee first!");
            return;
        }

        if (!DialogHelper.showConfirmation("Calculate Payroll",
                "Recalculate payroll for " + selected.getEmployeeName() + "?\n\n" +
                        "This will update their payroll with:\n" +
                        "• Latest attendance hours\n" +
                        "• Current government deductions\n" +
                        "• All approved adjustments")) {
            return;
        }

        try {
            calculateIndividualPayroll(selected.getEmployeeId());
            showInfo("✓ Payroll calculated for " + selected.getEmployeeName() + "!");
            loadExistingPayroll();
        } catch (SQLException e) {
            showError("Calculation failed: " + e.getMessage());
        }
    }

    private void calculateIndividualPayroll(int employeeId) throws SQLException {
        Employee emp = employeeDAO.getEmployeeById(employeeId);
        if (emp == null || !"active".equals(emp.getStatus())) return;

        Position pos = positionDAO.getPositionById(emp.getPositionId());
        if (pos == null) return;

        YearMonth payrollPeriod = YearMonth.of(selectedYear, selectedMonth);
        LocalDate payrollStart = payrollPeriod.atDay(1);
        LocalDate payrollEnd = payrollPeriod.atEndOfMonth();

        LocalDate hireDate = LocalDate.parse(emp.getHireDate());
        if (hireDate.isAfter(payrollEnd)) {
            showWarning(emp.getName() + " was hired after this payroll period");
            return;
        }

        String startDate = payrollStart.toString();
        String endDate = payrollEnd.toString();

        double totalHours = attendanceDAO.getTotalHoursWorked(emp.getId(), startDate, endDate);
        double hourlyRate = pos.getHourlyRate();
        double baseSalary = totalHours * hourlyRate;

        // Calculate government deductions
        GovernmentDeductionCalculator.GovernmentDeductions govDed =
                GovernmentDeductionCalculator.calculateAll(baseSalary);

        // Get approved adjustments
        double adjustment = adjustmentDAO.calculateTotalAdjustments(emp.getId(), startDate, endDate);

        // Calculate final salary
        double finalSalary = baseSalary - govDed.total + adjustment;

        // Check if exists
        Payroll existing = payrollDAO.getPayrollByEmployeeAndPeriod(emp.getId(), selectedMonth, selectedYear);

        if (existing != null) {
            existing.setBaseSalary(baseSalary);
            existing.setTotalDeductions(govDed.total);
            existing.setFinalSalary(finalSalary);
            payrollDAO.updatePayroll(existing);
        } else {
            Payroll payroll = new Payroll(
                    emp.getId(), selectedMonth, selectedYear,
                    baseSalary, govDed.total, finalSalary,
                    0, 0, DateTimeHelper.getCurrentDate(), ""
            );
            payrollDAO.addPayroll(payroll);
        }
    }

    private void calculateAllPayroll() {
        try {
            payrollList.clear();
            List<Employee> employees = departmentFilterCombo.getValue() != null ?
                    employeeDAO.getEmployeesByDepartment(departmentFilterCombo.getValue().getId()) :
                    employeeDAO.getActiveEmployees();

            YearMonth payrollPeriod = YearMonth.of(selectedYear, selectedMonth);
            LocalDate payrollStart = payrollPeriod.atDay(1);
            LocalDate payrollEnd = payrollPeriod.atEndOfMonth();

            int processed = 0;
            for (Employee emp : employees) {
                if (!"active".equals(emp.getStatus())) continue;

                LocalDate hireDate = LocalDate.parse(emp.getHireDate());
                if (hireDate.isAfter(payrollEnd)) continue;

                Position pos = positionDAO.getPositionById(emp.getPositionId());
                if (pos == null) continue;

                String startDate = payrollStart.toString();
                String endDate = payrollEnd.toString();

                double totalHours = attendanceDAO.getTotalHoursWorked(emp.getId(), startDate, endDate);
                double hourlyRate = pos.getHourlyRate();
                double baseSalary = totalHours * hourlyRate;

                // Calculate government deductions
                GovernmentDeductionCalculator.GovernmentDeductions govDed =
                        GovernmentDeductionCalculator.calculateAll(baseSalary);

                // Get adjustments
                double adjustment = adjustmentDAO.calculateTotalAdjustments(emp.getId(), startDate, endDate);

                double netPay = baseSalary - govDed.total + adjustment;

                // Check hold status
                boolean isHeld = salaryHoldDAO.isSalaryHeld(emp.getId());
                String holdStatus = isHeld ? "🔒 HELD" : "";

                payrollList.add(new PayrollDisplay(
                        emp.getId(), emp.getQrCode(), emp.getName(),
                        totalHours, hourlyRate, baseSalary, govDed.total,
                        adjustment, netPay, holdStatus, ""
                ));
                processed++;
            }

            if (processed == 0) {
                showWarning("No employees found for the selected period!");
            } else {
                showInfo("Payroll calculated for " + processed + " employees!\n\n" +
                        "Includes:\n" +
                        "• Hours worked × Hourly rate\n" +
                        "• Government deductions (SSS, PhilHealth, Pag-IBIG)\n" +
                        "• Approved adjustments\n\n" +
                        "⚠️ Click 'Process All' to save to database.");
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
                "This will save all records to the database with government deductions.")) {
            return;
        }

        try {
            String today = DateTimeHelper.getCurrentDate();
            int processed = 0;
            int updated = 0;

            for (PayrollDisplay pd : payrollList) {
                Payroll existing = payrollDAO.getPayrollByEmployeeAndPeriod(
                        pd.getEmployeeId(), selectedMonth, selectedYear
                );

                if (existing != null) {
                    existing.setBaseSalary(pd.getBaseSalary());
                    existing.setTotalDeductions(pd.getGovDeductions());
                    existing.setFinalSalary(pd.getNetPay());
                    existing.setNotes(pd.getNotes());
                    payrollDAO.updatePayroll(existing);
                    updated++;
                } else {
                    Payroll payroll = new Payroll(
                            pd.getEmployeeId(), selectedMonth, selectedYear,
                            pd.getBaseSalary(), pd.getGovDeductions(), pd.getNetPay(),
                            0, 0, today, pd.getNotes()
                    );
                    payrollDAO.addPayroll(payroll);
                    processed++;
                }
            }

            showInfo("✓ Payroll Processing Complete!\n\n" +
                    "New: " + processed + " | Updated: " + updated + "\n" +
                    "Data saved with government deductions!");

            loadExistingPayroll();

        } catch (SQLException e) {
            showError("Processing failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleManualAdjustment() {
        PayrollDisplay selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee first!");
            return;
        }

        showAdjustmentDialog(selected);
    }

    /**
     * NEW - Show adjustment dialog with full audit trail
     */
    private void showAdjustmentDialog(PayrollDisplay selected) {
        Dialog<PayrollAdjustment> dialog = new Dialog<>();
        dialog.setTitle("Add Payroll Adjustment");
        dialog.setHeaderText("Adjust salary for: " + selected.getEmployeeName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton bonusRadio = new RadioButton("💰 Bonus");
        RadioButton overtimeRadio = new RadioButton("⏰ Overtime");
        RadioButton allowanceRadio = new RadioButton("🎁 Allowance");
        RadioButton deductionRadio = new RadioButton("📉 Deduction");

        bonusRadio.setToggleGroup(typeGroup);
        overtimeRadio.setToggleGroup(typeGroup);
        allowanceRadio.setToggleGroup(typeGroup);
        deductionRadio.setToggleGroup(typeGroup);
        bonusRadio.setSelected(true);

        VBox typeBox = new VBox(10, bonusRadio, overtimeRadio, allowanceRadio, deductionRadio);

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason for adjustment (required)");
        reasonArea.setPrefRowCount(3);

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Reason:"), 0, 2);
        grid.add(reasonArea, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String reason = reasonArea.getText().trim();
                    if (reason.isEmpty()) {
                        showError("Please provide a reason for this adjustment");
                        return null;
                    }

                    double value = Double.parseDouble(amountField.getText());
                    String type;
                    double adjustmentAmount;

                    if (bonusRadio.isSelected()) {
                        type = "bonus";
                        adjustmentAmount = value;
                    } else if (overtimeRadio.isSelected()) {
                        type = "overtime";
                        adjustmentAmount = value;
                    } else if (allowanceRadio.isSelected()) {
                        type = "allowance";
                        adjustmentAmount = value;
                    } else {
                        type = "deduction";
                        adjustmentAmount = -value;
                    }

                    PayrollAdjustment adjustment = new PayrollAdjustment(
                            selected.getEmployeeId(),
                            type,
                            adjustmentAmount,
                            reason,
                            currentUserId,
                            DateTimeHelper.getCurrentDate()
                    );

                    return adjustment;

                } catch (NumberFormatException e) {
                    showError("Please enter a valid amount");
                    return null;
                }
            }
            return null;
        });

        Optional<PayrollAdjustment> result = dialog.showAndWait();
        result.ifPresent(adj -> {
            try {
                adjustmentDAO.addAdjustment(adj);

                // Auto-approve if admin (you can add approval workflow later)
                adjustmentDAO.approveAdjustment(adj.getId(), currentUserId, DateTimeHelper.getCurrentDate());

                showInfo("✓ Adjustment Added!\n\n" +
                        "Type: " + adj.getAdjustmentType() + "\n" +
                        "Amount: " + adj.getFormattedAmount() + "\n" +
                        "Reason: " + adj.getReason() + "\n\n" +
                        "Status: Approved\n" +
                        "Recalculating payroll...");

                // Recalculate this employee's payroll
                calculateIndividualPayroll(selected.getEmployeeId());
                loadExistingPayroll();

            } catch (SQLException e) {
                showError("Failed to add adjustment: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleViewAdjustments() {
        PayrollDisplay selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee!");
            return;
        }

        try {
            Employee emp = employeeDAO.getEmployeeById(selected.getEmployeeId());
            List<PayrollAdjustment> adjustments =
                    adjustmentDAO.getAdjustmentsByEmployee(emp.getId());

            if (adjustments.isEmpty()) {
                showInfo("No adjustments found for " + emp.getName());
                return;
            }

            StringBuilder history = new StringBuilder();
            history.append("═══════════════════════════════════════════\n")
                    .append("   ADJUSTMENT HISTORY\n")
                    .append("   ").append(emp.getName()).append("\n")
                    .append("═══════════════════════════════════════════\n\n");

            double runningTotal = 0;

            for (PayrollAdjustment adj : adjustments) {
                runningTotal += adj.getAmount();

                history.append(adj.getTypeEmoji())
                        .append(" ")
                        .append(adj.getAdjustmentType().toUpperCase())
                        .append("\n")
                        .append("   Amount: ").append(adj.getFormattedAmount()).append("\n")
                        .append("   Running Total: ₱")
                        .append(String.format("%,.2f", runningTotal)).append("\n")
                        .append("   Reason: ").append(adj.getReason()).append("\n")
                        .append("   Approved On: ").append(adj.getDateApproved()).append("\n")
                        .append("───────────────────────────────────────────\n");
            }

            history.append(String.format("TOTAL ADJUSTMENTS: ₱%,.2f\n", runningTotal))
                    .append("═══════════════════════════════════════════\n");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Adjustment History");
            alert.setHeaderText(emp.getName() + " - Full Breakdown");

            TextArea textArea = new TextArea(history.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-font-family: 'Courier New';");

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefWidth(600);
            alert.showAndWait();

        } catch (SQLException e) {
            showError("Failed to load adjustments: " + e.getMessage());
        }
    }

    /**
 * NEW - Hold/Release salary
 */
        @FXML
        private void handleSalaryHold() {
            PayrollDisplay selected = payrollTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Please select an employee!");
                return;
            }

            try {
                Employee emp = employeeDAO.getEmployeeById(selected.getEmployeeId());
                boolean isHeld = salaryHoldDAO.isSalaryHeld(emp.getId());

                if (isHeld) {
                    // Release salary
                    if (DialogHelper.showConfirmation("Release Salary",
                            "Release salary hold for " + emp.getName() + "?")) {

                        SalaryHold hold = salaryHoldDAO.getActiveHold(emp.getId());
                        if (hold != null) {
                            salaryHoldDAO.releaseSalary(hold.getId(), currentUserId, DateTimeHelper.getCurrentDate());
                            showInfo("✓ Salary released for " + emp.getName());
                            loadExistingPayroll();
                        }
                    }
                } else {
                    // Hold salary
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Hold Salary");
                    dialog.setHeaderText("Hold salary for: " + emp.getName());

                    TextArea reasonArea = new TextArea();
                    reasonArea.setPromptText("Enter reason for holding salary (required)");
                    reasonArea.setPrefRowCount(4);

                    dialog.getDialogPane().setContent(reasonArea);
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                    dialog.setResultConverter(button -> {
                        if (button == ButtonType.OK) {
                            String reason = reasonArea.getText().trim();
                            if (reason.isEmpty()) {
                                showError("Please provide a reason");
                                return null;
                            }
                            return reason;
                        }
                        return null;
                    });

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(reason -> {
                        try {
                            SalaryHold hold = new SalaryHold(
                                    emp.getId(),
                                    reason,
                                    DateTimeHelper.getCurrentDate(),
                                    currentUserId,
                                    ""
                            );
                            salaryHoldDAO.holdSalary(hold);
                            showInfo("✓ Salary held for " + emp.getName() + "\n\nReason: " + reason);
                            loadExistingPayroll();
                        } catch (SQLException e) {
                            showError("Failed to hold salary: " + e.getMessage());
                        }
                    });
                }

            } catch (SQLException e) {
                showError("Failed to process salary hold: " + e.getMessage());
            }
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

                // Get government deductions breakdown
                GovernmentDeductionCalculator.GovernmentDeductions govDed =
                        GovernmentDeductionCalculator.calculateAll(selected.getBaseSalary());

                // Get adjustment history for this period
                YearMonth yearMonth = YearMonth.of(selectedYear, selectedMonth);
                String startDate = yearMonth.atDay(1).toString();
                String endDate = yearMonth.atEndOfMonth().toString();

                List<PayrollAdjustment> adjustments = adjustmentDAO.getApprovedAdjustments(
                        emp.getId(), startDate, endDate);

                String payslip = generateDetailedPayslip(emp, pos, dept, selected, govDed, adjustments);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Payslip - " + emp.getName());
                alert.setHeaderText(null);

                TextArea textArea = new TextArea(payslip);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefRowCount(30);
                textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

                alert.getDialogPane().setContent(textArea);
                alert.getDialogPane().setPrefWidth(700);

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

        private String generateDetailedPayslip(Employee emp, Position pos, Department dept,
                PayrollDisplay pd,
                GovernmentDeductionCalculator.GovernmentDeductions govDed,
                List<PayrollAdjustment> adjustments) {
            StringBuilder sb = new StringBuilder();

            sb.append("═══════════════════════════════════════════════\n");
            sb.append("           HR PAYROLL SYSTEM\n");
            sb.append("              EMPLOYEE PAYSLIP\n");
            sb.append(String.format("               %s %d\n", monthCombo.getValue(), selectedYear));
            sb.append("═══════════════════════════════════════════════\n\n");

            sb.append("EMPLOYEE INFORMATION\n");
            sb.append("─────────────────────────────────────────────\n");
            sb.append(String.format("Name:             %s\n", emp.getName()));
            sb.append(String.format("QR Code:          %s\n", emp.getQrCode()));
            sb.append(String.format("Position:         %s\n", pos.getTitle()));
            sb.append(String.format("Department:       %s\n\n", dept.getName()));

            sb.append("COMPENSATION BREAKDOWN\n");
            sb.append("─────────────────────────────────────────────\n");
            sb.append(String.format("Hourly Rate:      ₱%.2f per hour\n", pd.getHourlyRate()));
            sb.append(String.format("Hours Worked:     %.2f hours\n", pd.getHoursWorked()));
            sb.append(String.format("Gross Pay:        ₱%,.2f\n", pd.getBaseSalary()));
            sb.append(String.format("                  (%.2f hrs × ₱%.2f/hr)\n\n",
                    pd.getHoursWorked(), pd.getHourlyRate()));

            sb.append("GOVERNMENT DEDUCTIONS\n");
            sb.append("─────────────────────────────────────────────\n");
            sb.append(String.format("SSS:              -₱%,.2f\n", govDed.sss));
            sb.append(String.format("PhilHealth:       -₱%,.2f\n", govDed.philHealth));
            sb.append(String.format("Pag-IBIG:         -₱%,.2f\n", govDed.pagIbig));
            sb.append(String.format("Total Deductions: -₱%,.2f\n\n", govDed.total));

            sb.append("ADJUSTMENTS\n");
            sb.append("─────────────────────────────────────────────\n");

            if (adjustments.isEmpty()) {
                sb.append("None\n\n");
            } else {
                for (PayrollAdjustment adj : adjustments) {
                    sb.append(String.format("%s %-12s  %s\n",
                            adj.getTypeEmoji(),
                            adj.getAdjustmentType() + ":",
                            adj.getFormattedAmount()));
                    sb.append(String.format("  Reason: %s\n", adj.getReason()));
                    sb.append(String.format("  Date: %s\n", adj.getDateApproved()));
                }
                sb.append(String.format("\nTotal Adjustments: ₱%,.2f\n\n", pd.getAdjustment()));
            }

            // Check if held
            try {
                if (salaryHoldDAO.isSalaryHeld(emp.getId())) {
                    SalaryHold hold = salaryHoldDAO.getActiveHold(emp.getId());
                    sb.append("🔒 SALARY HOLD NOTICE\n");
                    sb.append("─────────────────────────────────────────────\n");
                    sb.append(String.format("Status:           HELD\n"));
                    sb.append(String.format("Since:            %s\n", hold.getHoldDate()));
                    sb.append(String.format("Reason:           %s\n\n", hold.getReason()));
                }
            } catch (SQLException e) {
                // Ignore
            }

            sb.append("═══════════════════════════════════════════════\n");
            sb.append(String.format("NET PAY:          ₱%,.2f\n", pd.getNetPay()));
            sb.append("═══════════════════════════════════════════════\n\n");

            sb.append(String.format("Processed: %s\n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))));
            sb.append("This is a computer-generated payslip.\n");

            return sb.toString();
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
                    showInfo("Payslip saved!\n\n" + file.getAbsolutePath());
                } catch (Exception e) {
                    showError("Failed to save payslip: " + e.getMessage());
                }
            }
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
                    writer.println("═══════════════════════════════════════════════════════════════════════");
                    writer.println("                    PAYROLL SUMMARY REPORT");
                    writer.println("                  " + monthCombo.getValue() + " " + selectedYear);
                    writer.println("═══════════════════════════════════════════════════════════════════════\n");

                    writer.println(String.format("%-15s %-25s %10s %12s %12s %12s %15s",
                            "QR Code", "Name", "Hours", "Gross", "Gov Ded", "Adjust", "Net Pay"));
                    writer.println("───────────────────────────────────────────────────────────────────────");

                    double totalGross = 0;
                    double totalGov = 0;
                    double totalAdj = 0;
                    double totalNet = 0;

                    for (PayrollDisplay pd : payrollList) {
                        writer.println(String.format("%-15s %-25s %10.2f ₱%11.2f ₱%11.2f ₱%11.2f ₱%14.2f %s",
                                pd.getQrCode(),
                                pd.getEmployeeName().length() > 25 ? pd.getEmployeeName().substring(0, 22) + "..." : pd.getEmployeeName(),
                                pd.getHoursWorked(),
                                pd.getBaseSalary(),
                                pd.getGovDeductions(),
                                pd.getAdjustment(),
                                pd.getNetPay(),
                                pd.getHoldStatus()
                        ));

                        totalGross += pd.getBaseSalary();
                        totalGov += pd.getGovDeductions();
                        totalAdj += pd.getAdjustment();
                        totalNet += pd.getNetPay();
                    }

                    writer.println("───────────────────────────────────────────────────────────────────────");
                    writer.println(String.format("%-15s %-25s %10s ₱%11.2f ₱%11.2f ₱%11.2f ₱%14.2f",
                            "", "TOTAL", "", totalGross, totalGov, totalAdj, totalNet));
                    writer.println("═══════════════════════════════════════════════════════════════════════");
                    writer.println("\nGenerated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
                    writer.println("Total Employees: " + payrollList.size());

                    showInfo("✓ Export Successful!\n\n" +
                            "Payroll summary saved to:\n" + file.getAbsolutePath());

                } catch (Exception e) {
                    showError("Export failed: " + e.getMessage());
                }
            }
        }

        @FXML
        private void handleSearch() {
            String searchTerm = payrollSearchField.getText().trim();
            if (searchTerm.isEmpty()) {
                loadExistingPayroll();
                return;
            }
            filterPayrollTable(searchTerm);
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

// ==================== INNER CLASS ====================

        public static class PayrollDisplay {
            private final int employeeId;
            private final String qrCode;
            private final String employeeName;
            private final double hoursWorked;
            private final double hourlyRate;
            private final double baseSalary;
            private final double govDeductions;
            private double adjustment;
            private double netPay;
            private String holdStatus;
            private String notes;

            public PayrollDisplay(int employeeId, String qrCode, String employeeName,
                                  double hoursWorked, double hourlyRate, double baseSalary,
                                  double govDeductions, double adjustment, double netPay,
                                  String holdStatus, String notes) {
                this.employeeId = employeeId;
                this.qrCode = qrCode;
                this.employeeName = employeeName;
                this.hoursWorked = hoursWorked;
                this.hourlyRate = hourlyRate;
                this.baseSalary = baseSalary;
                this.govDeductions = govDeductions;
                this.adjustment = adjustment;
                this.netPay = netPay;
                this.holdStatus = holdStatus;
                this.notes = notes;
            }

            // Getters
            public int getEmployeeId() { return employeeId; }
            public String getQrCode() { return qrCode; }
            public String getEmployeeName() { return employeeName; }
            public double getHoursWorked() { return hoursWorked; }
            public double getHourlyRate() { return hourlyRate; }
            public double getBaseSalary() { return baseSalary; }
            public double getGovDeductions() { return govDeductions; }
            public double getAdjustment() { return adjustment; }
            public double getNetPay() { return netPay; }
            public String getHoldStatus() { return holdStatus; }
            public String getNotes() { return notes; }

            // Setters
            public void setAdjustment(double adjustment) { this.adjustment = adjustment; }
            public void setNetPay(double netPay) { this.netPay = netPay; }
            public void setHoldStatus(String holdStatus) { this.holdStatus = holdStatus; }
            public void setNotes(String notes) { this.notes = notes; }
        }
    }
