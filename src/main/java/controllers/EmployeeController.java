package controllers;

import dao.EmployeeDAO;
import dao.DepartmentDAO;
import dao.PositionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Modality;
import models.Employee;
import models.Department;
import models.Position;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmployeeController {

    @FXML private TableView<EmployeeDisplay> employeeTable;
    @FXML private TableColumn<EmployeeDisplay, Integer> idColumn;
    @FXML private TableColumn<EmployeeDisplay, String> qrColumn;
    @FXML private TableColumn<EmployeeDisplay, String> nameColumn;
    @FXML private TableColumn<EmployeeDisplay, String> positionColumn;
    @FXML private TableColumn<EmployeeDisplay, String> deptColumn;
    @FXML private TableColumn<EmployeeDisplay, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private Button employeeButton;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private PositionDAO positionDAO = new PositionDAO();

    private ObservableList<EmployeeDisplay> employeeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadEmployees();
        setupSearch();
        setupTableDoubleClick();
        makeQRColumnCopyable();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        qrColumn.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("positionTitle"));
        deptColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        employeeTable.setItems(employeeList);
    }

    private void loadEmployees() {
        try {
            employeeList.clear();
            List<Employee> employees = employeeDAO.getAllEmployees();

            for (Employee emp : employees) {
                // Get department and position names
                Department dept = departmentDAO.getDepartmentById(emp.getDepartmentId());
                Position pos = positionDAO.getPositionById(emp.getPositionId());

                String deptName = (dept != null) ? dept.getName() : "Unknown";
                String posTitle = (pos != null) ? pos.getTitle() : "Unknown";

                employeeList.add(new EmployeeDisplay(
                        emp.getId(),
                        emp.getQrCode(),
                        emp.getName(),
                        posTitle,
                        deptName,
                        emp.getStatus()
                ));
            }
        } catch (SQLException e) {
            showError("Failed to load employees: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                loadEmployees();
            } else {
                searchEmployees(newValue);
            }
        });
    }

    private void searchEmployees(String searchTerm) {
        try {
            employeeList.clear();
            List<Employee> employees = employeeDAO.searchEmployeesByName(searchTerm);

            for (Employee emp : employees) {
                Department dept = departmentDAO.getDepartmentById(emp.getDepartmentId());
                Position pos = positionDAO.getPositionById(emp.getPositionId());

                String deptName = (dept != null) ? dept.getName() : "Unknown";
                String posTitle = (pos != null) ? pos.getTitle() : "Unknown";

                employeeList.add(new EmployeeDisplay(
                        emp.getId(),
                        emp.getQrCode(),
                        emp.getName(),
                        posTitle,
                        deptName,
                        emp.getStatus()
                ));
            }
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddEmployee() {
        openEmployeeForm(null);
    }

    @FXML
    private void handleEditEmployee() {
        EmployeeDisplay selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee to edit");
            return;
        }

        try {
            Employee employee = employeeDAO.getEmployeeById(selected.getId());
            openEmployeeForm(employee);
        } catch (SQLException e) {
            showError("Failed to load employee: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        EmployeeDisplay selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Employee");
        confirm.setContentText("Are you sure you want to delete " + selected.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                employeeDAO.deleteEmployee(selected.getId());
                showInfo("Employee deleted successfully");
                loadEmployees();
            } catch (SQLException e) {
                showError("Failed to delete employee: " + e.getMessage());
            }
        }
    }

    /**
     * Open employee form for add/edit
     */
    private void openEmployeeForm(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/employee-form.fxml"));
            Parent root = loader.load();

            EmployeeFormController controller = loader.getController();

            if (employee != null) {
                controller.setEmployee(employee); // Edit mode
            }

            // Set callback to refresh table after save
            controller.setOnSaveCallback(() -> loadEmployees());

            Stage stage = new Stage();
            stage.setTitle(employee == null ? "Add New Employee" : "Edit Employee");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            showError("Failed to open employee form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExport() {
        showInfo("Export feature coming soon!");
    }

    /**
     * Copy selected employee's QR code to clipboard
     */
    @FXML
    private void handleCopyQRCode() {
        EmployeeDisplay selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an employee first!");
            return;
        }

        // Copy QR code to clipboard
        final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(selected.getQrCode());
        clipboard.setContent(content);

        showInfo("QR Code copied!\n\n" + selected.getQrCode() + "\n\nYou can now paste it in the attendance system.");
    }

    /**
     * Setup double-click to copy QR code
     */
    private void setupTableDoubleClick() {
        employeeTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                EmployeeDisplay selected = employeeTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleCopyQRCode();
                }
            }
        });
    }

    /**
     * Make QR column cells copyable with right-click
     */
    private void makeQRColumnCopyable() {
        qrColumn.setCellFactory(column -> {
            TableCell<EmployeeDisplay, String> cell = new TableCell<EmployeeDisplay, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setStyle(empty ? "" : "-fx-cursor: hand;");
                }
            };

            // Right-click context menu
            ContextMenu contextMenu = new ContextMenu();
            MenuItem copyItem = new MenuItem("Copy QR Code");
            copyItem.setOnAction(e -> {
                String qrCode = cell.getItem();
                if (qrCode != null && !qrCode.isEmpty()) {
                    final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(qrCode);
                    clipboard.setContent(content);
                    showInfo("QR Code copied!\n\n" + qrCode);
                }
            });
            contextMenu.getItems().add(copyItem);

            cell.setContextMenu(contextMenu);

            // Single click on QR cell also copies
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !cell.isEmpty()) {
                    String qrCode = cell.getItem();
                    if (qrCode != null && !qrCode.isEmpty()) {
                        final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                        final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                        content.putString(qrCode);
                        clipboard.setContent(content);

                        // Visual feedback
                        cell.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
                        pause.setOnFinished(e -> cell.setStyle(""));
                        pause.play();
                    }
                }
            });

            return cell;
        });
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
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Display class for TableView (combines employee with dept/position names)
     */
    public static class EmployeeDisplay {
        private final int id;
        private final String qrCode;
        private final String name;
        private final String positionTitle;
        private final String departmentName;
        private final String status;

        public EmployeeDisplay(int id, String qrCode, String name, String positionTitle,
                               String departmentName, String status) {
            this.id = id;
            this.qrCode = qrCode;
            this.name = name;
            this.positionTitle = positionTitle;
            this.departmentName = departmentName;
            this.status = status;
        }

        public int getId() { return id; }
        public String getQrCode() { return qrCode; }
        public String getName() { return name; }
        public String getPositionTitle() { return positionTitle; }
        public String getDepartmentName() { return departmentName; }
        public String getStatus() { return status; }
    }
}