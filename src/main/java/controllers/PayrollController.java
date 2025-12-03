package controllers;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.*;
import utils.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PayrollController {

    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<Department> departmentFilterCombo;
    @FXML private TextField lateDeductionField;
    @FXML private TextField absentDeductionField;
    @FXML private TextField payrollSearchField;

    @FXML private Label summaryTitleLabel;
    @FXML private Label totalEmployeesLabel;
    @FXML private Label totalPayrollLabel;
    @FXML private Label totalDeductionsLabel;
    @FXML private Label netPayLabel;

    @FXML private TableView<PayrollDisplay> payrollTable;
    @FXML private TableColumn<PayrollDisplay, String> qrCodeColumn;
    @FXML private TableColumn<PayrollDisplay, String> nameColumn;
    @FXML private TableColumn<PayrollDisplay, Double> baseSalaryColumn;
    @FXML private TableColumn<PayrollDisplay, Integer> lateCountColumn;
    @FXML private TableColumn<PayrollDisplay, Integer> absentCountColumn;
    @FXML private TableColumn<PayrollDisplay, Double> deductionsColumn;
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
        setupDeductionFields();
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
        } catch (SQLException e) {
            DialogHelper.showError("Failed to load departments: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        qrCodeColumn.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        baseSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        lateCountColumn.setCellValueFactory(new PropertyValueFactory<>("lateCount"));
        absentCountColumn.setCellValueFactory(new PropertyValueFactory<>("absentCount"));
        deductionsColumn.setCellValueFactory(new PropertyValueFactory<>("totalDeductions"));
        netPayColumn.setCellValueFactory(new PropertyValueFactory<>("netPay"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        payrollTable.setItems(payrollList);
    }

    private void setupDeductionFields() {
        lateDeductionField.setText("50.00");
        absentDeductionField.setText("200.00");
    }

    @FXML
    private void handleCalculatePayroll() {
        selectedMonth = DateTimeHelper.getMonthNumber(monthCombo.getValue());
        selectedYear = yearCombo.getValue();

        try {
            double lateDeduction = Double.parseDouble(lateDeductionField.getText());
            double absentDeduction = Double.parseDouble(absentDeductionField.getText());

            payrollList.clear();
            List<Employee> employees = departmentFilterCombo.getValue() != null ?
                    employeeDAO.getEmployeesByDepartment(departmentFilterCombo.getValue().getId()) :
                    employeeDAO.getAllEmployees();

            double totalBase = 0;
            double totalDeduct = 0;
            int empCount = 0;

            for (Employee emp : employees) {
                if (!"active".equals(emp.getStatus())) continue;

                Position pos = positionDAO.getPositionById(emp.getPositionId());
                if (pos == null) continue;

                int lateCount = attendanceDAO.countLateByEmployeeAndMonth(emp.getId(), selectedYear, selectedMonth);
                int absentCount = attendanceDAO.countAbsentByEmployeeAndMonth(emp.getId(), selectedYear, selectedMonth);

                double baseSalary = pos.getBaseSalary();
                double deductions = PayrollCalculator.calculateAttendanceDeductions(
                        lateCount, absentCount, lateDeduction, absentDeduction);
                double netPay = PayrollCalculator.calculateNetPay(baseSalary, deductions);

                payrollList.add(new PayrollDisplay(
                        emp.getId(), emp.getQrCode(), emp.getName(),
                        baseSalary, lateCount, absentCount,
                        deductions, netPay, ""
                ));

                totalBase += baseSalary;
                totalDeduct += deductions;
                empCount++;
            }

            totalEmployeesLabel.setText(String.valueOf(empCount));
            totalPayrollLabel.setText(PayrollCalculator.formatToPeso(totalBase));
            totalDeductionsLabel.setText(PayrollCalculator.formatToPeso(totalDeduct));
            netPayLabel.setText(PayrollCalculator.formatToPeso(totalBase - totalDeduct));

            summaryTitleLabel.setText("Payroll Summary for " + monthCombo.getValue() + " " + selectedYear);

            DialogHelper.showSuccess("Payroll calculated for " + empCount + " employees!");

        } catch (SQLException | NumberFormatException e) {
            DialogHelper.showError("Calculation failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleProcessAll() {
        if (payrollList.isEmpty()) {
            DialogHelper.showWarning("Please calculate payroll first!");
            return;
        }

        if (!DialogHelper.showConfirmation("Process payroll for all employees?\nThis will save to database.")) {
            return;
        }

        try {
            double lateDeduction = Double.parseDouble(lateDeductionField.getText());
            double absentDeduction = Double.parseDouble(absentDeductionField.getText());
            String today = DateTimeHelper.getCurrentDate();

            int processed = 0;
            for (PayrollDisplay pd : payrollList) {
                if (payrollDAO.isPayrollProcessed(pd.getEmployeeId(), selectedMonth, selectedYear)) {
                    continue;
                }

                Payroll payroll = new Payroll(
                        pd.getEmployeeId(), selectedMonth, selectedYear,
                        pd.getBaseSalary(), pd.getTotalDeductions(), pd.getNetPay(),
                        pd.getLateCount(), pd.getAbsentCount(), today, pd.getNotes()
                );

                payrollDAO.addPayroll(payroll);
                processed++;
            }

            DialogHelper.showSuccess("Processed payroll for " + processed + " employees!");

        } catch (SQLException | NumberFormatException e) {
            DialogHelper.showError("Processing failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleManualAdjustment() {
        PayrollDisplay selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("Please select an employee!");
            return;
        }

        Dialog<PayrollAdjustment> dialog = new Dialog<>();
        dialog.setTitle("Manual Payroll Adjustment");
        dialog.setHeaderText("Adjust payroll for: " + selected.getEmployeeName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField deductionField = new TextField(String.valueOf(selected.getTotalDeductions()));
        TextArea notesArea = new TextArea(selected.getNotes());
        notesArea.setPromptText("Reason for adjustment (e.g., Sick leave - 3 days)");
        notesArea.setPrefRowCount(3);

        grid.add(new Label("Total Deductions:"), 0, 0);
        grid.add(deductionField, 1, 0);
        grid.add(new Label("Notes:"), 0, 1);
        grid.add(notesArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    double newDeduction = Double.parseDouble(deductionField.getText());
                    return new PayrollAdjustment(newDeduction, notesArea.getText());
                } catch (NumberFormatException e) {
                    DialogHelper.showError("Invalid deduction amount");
                    return null;
                }
            }
            return null;
        });

        Optional<PayrollAdjustment> result = dialog.showAndWait();
        result.ifPresent(adj -> {
            selected.setTotalDeductions(adj.deduction);
            selected.setNetPay(selected.getBaseSalary() - adj.deduction);
            selected.setNotes(adj.notes);
            payrollTable.refresh();
            DialogHelper.showSuccess("Adjustment applied!");
        });
    }

    @FXML
    private void handleViewPayslip() {
        DialogHelper.showInfo("Payslip view feature coming soon!");
    }

    @FXML
    private void handleSendPayslips() {
        DialogHelper.showInfo("Email feature coming soon!");
    }

    @FXML
    private void handleExportPayroll() {
        DialogHelper.showInfo("Export feature coming soon!");
    }

    @FXML
    private void handleSearch() {
        String searchTerm = payrollSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            handleCalculatePayroll();
            return;
        }

        ObservableList<PayrollDisplay> filtered = FXCollections.observableArrayList();
        for (PayrollDisplay pd : payrollList) {
            if (pd.getEmployeeName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    pd.getQrCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                filtered.add(pd);
            }
        }
        payrollTable.setItems(filtered);
    }

    // Inner classes
    public static class PayrollDisplay {
        private final int employeeId;
        private final String qrCode;
        private final String employeeName;
        private final double baseSalary;
        private final int lateCount;
        private final int absentCount;
        private double totalDeductions;
        private double netPay;
        private String notes;

        public PayrollDisplay(int employeeId, String qrCode, String employeeName,
                              double baseSalary, int lateCount, int absentCount,
                              double totalDeductions, double netPay, String notes) {
            this.employeeId = employeeId;
            this.qrCode = qrCode;
            this.employeeName = employeeName;
            this.baseSalary = baseSalary;
            this.lateCount = lateCount;
            this.absentCount = absentCount;
            this.totalDeductions = totalDeductions;
            this.netPay = netPay;
            this.notes = notes;
        }

        public int getEmployeeId() { return employeeId; }
        public String getQrCode() { return qrCode; }
        public String getEmployeeName() { return employeeName; }
        public double getBaseSalary() { return baseSalary; }
        public int getLateCount() { return lateCount; }
        public int getAbsentCount() { return absentCount; }
        public double getTotalDeductions() { return totalDeductions; }
        public double getNetPay() { return netPay; }
        public String getNotes() { return notes; }

        public void setTotalDeductions(double totalDeductions) {
            this.totalDeductions = totalDeductions;
        }
        public void setNetPay(double netPay) {
            this.netPay = netPay;
        }
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    private static class PayrollAdjustment {
        final double deduction;
        final String notes;

        PayrollAdjustment(double deduction, String notes) {
            this.deduction = deduction;
            this.notes = notes;
        }
    }
}