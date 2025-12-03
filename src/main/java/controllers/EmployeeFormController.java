
package controllers;

import dao.EmployeeDAO;
import dao.DepartmentDAO;
import dao.PositionDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Employee;
import models.Department;
import models.Position;
import utils.QRCodeGenerator;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EmployeeFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private TextField contactField;
    @FXML private ComboBox<Department> departmentCombo;
    @FXML private ComboBox<Position> positionCombo;
    @FXML private DatePicker hireDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ImageView qrImageView;
    @FXML private Label qrCodeLabel;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private PositionDAO positionDAO = new PositionDAO();

    private Employee currentEmployee; // For edit mode
    private String generatedQRCode;
    private Runnable onSaveCallback; // Callback to refresh parent view

    @FXML
    public void initialize() {
        loadDepartments();
        setupStatusCombo();
        setupDepartmentListener();
        hireDatePicker.setValue(LocalDate.now());
    }

    private void loadDepartments() {
        try {
            List<Department> departments = departmentDAO.getAllDepartments();
            departmentCombo.setItems(FXCollections.observableArrayList(departments));

            // Custom display for combo box
            departmentCombo.setCellFactory(param -> new ListCell<Department>() {
                @Override
                protected void updateItem(Department item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });

            departmentCombo.setButtonCell(new ListCell<Department>() {
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

    private void setupDepartmentListener() {
        departmentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPositionsByDepartment(newVal.getId());
            }
        });
    }

    private void loadPositionsByDepartment(int departmentId) {
        try {
            List<Position> positions = positionDAO.getPositionsByDepartment(departmentId);
            positionCombo.setItems(FXCollections.observableArrayList(positions));

            positionCombo.setCellFactory(param -> new ListCell<Position>() {
                @Override
                protected void updateItem(Position item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getTitle());
                }
            });

            positionCombo.setButtonCell(new ListCell<Position>() {
                @Override
                protected void updateItem(Position item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getTitle());
                }
            });

        } catch (SQLException e) {
            showError("Failed to load positions: " + e.getMessage());
        }
    }

    private void setupStatusCombo() {
        statusCombo.setItems(FXCollections.observableArrayList("active", "inactive"));
        statusCombo.setValue("active");
    }

    /**
     * Set employee for edit mode
     */
    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;
        formTitleLabel.setText("✏️ EDIT EMPLOYEE");
        populateFields(employee);
    }

    private void populateFields(Employee employee) {
        try {
            nameField.setText(employee.getName());
            ageField.setText(String.valueOf(employee.getAge()));
            contactField.setText(employee.getContactInfo());
            statusCombo.setValue(employee.getStatus());

            // Set hire date
            if (employee.getHireDate() != null && !employee.getHireDate().isEmpty()) {
                hireDatePicker.setValue(LocalDate.parse(employee.getHireDate()));
            }

            // Load department
            Department dept = departmentDAO.getDepartmentById(employee.getDepartmentId());
            if (dept != null) {
                departmentCombo.setValue(dept);
            }

            // Load position
            Position pos = positionDAO.getPositionById(employee.getPositionId());
            if (pos != null) {
                positionCombo.setValue(pos);
            }

            // Show existing QR code
            generatedQRCode = employee.getQrCode();
            qrCodeLabel.setText(generatedQRCode);
            generateQRImage(generatedQRCode);

        } catch (SQLException e) {
            showError("Failed to load employee data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String contact = contactField.getText().trim();
            int departmentId = departmentCombo.getValue().getId();
            int positionId = positionCombo.getValue().getId();
            String hireDate = hireDatePicker.getValue().toString();
            String status = statusCombo.getValue();

            if (currentEmployee == null) {
                // ADD MODE - Generate new QR code
                if (generatedQRCode == null) {
                    // Auto-generate QR code based on name
                    int nextId = getNextEmployeeId();
                    generatedQRCode = QRCodeGenerator.generateQRCodeText(nextId, name.split(" ")[0]);
                }

                Employee newEmployee = new Employee(name, age, positionId, departmentId,
                        hireDate, contact, generatedQRCode);
                newEmployee.setStatus(status);
                employeeDAO.addEmployee(newEmployee);

                showInfo("Employee added successfully!");

            } else {
                // EDIT MODE
                currentEmployee.setName(name);
                currentEmployee.setAge(age);
                currentEmployee.setContactInfo(contact);
                currentEmployee.setDepartmentId(departmentId);
                currentEmployee.setPositionId(positionId);
                currentEmployee.setHireDate(hireDate);
                currentEmployee.setStatus(status);
                currentEmployee.setQrCode(generatedQRCode);

                employeeDAO.updateEmployee(currentEmployee);

                showInfo("Employee updated successfully!");
            }

            // Trigger callback to refresh parent view
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            // Close form
            closeForm();

        } catch (SQLException e) {
            showError("Failed to save employee: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showError("Please enter a valid age");
        }
    }

    @FXML
    private void handleRegenerateQR() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showWarning("Please enter employee name first");
            return;
        }

        try {
            int id = (currentEmployee != null) ? currentEmployee.getId() : getNextEmployeeId();
            generatedQRCode = QRCodeGenerator.generateQRCodeText(id, name.split(" ")[0]);
            qrCodeLabel.setText(generatedQRCode);
            generateQRImage(generatedQRCode);

            showInfo("QR Code regenerated: " + generatedQRCode);
        } catch (Exception e) {
            showError("Failed to generate QR code: " + e.getMessage());
        }
    }

    @FXML
    private void handleDownloadQR() {
        if (generatedQRCode == null) {
            showWarning("Generate QR code first");
            return;
        }

        showInfo("QR Code download feature coming soon!");
        // TODO: Implement file chooser and save QR image
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void generateQRImage(String qrText) {
        try {
            byte[] qrImageBytes = QRCodeGenerator.generateQRImageBytes(qrText, 200, 200);
            Image qrImage = new Image(new ByteArrayInputStream(qrImageBytes));
            qrImageView.setImage(qrImage);
        } catch (Exception e) {
            System.err.println("Failed to generate QR image: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showWarning("Please enter employee name");
            return false;
        }
        if (ageField.getText().trim().isEmpty()) {
            showWarning("Please enter employee age");
            return false;
        }
        if (departmentCombo.getValue() == null) {
            showWarning("Please select a department");
            return false;
        }
        if (positionCombo.getValue() == null) {
            showWarning("Please select a position");
            return false;
        }
        if (hireDatePicker.getValue() == null) {
            showWarning("Please select hire date");
            return false;
        }

        try {
            Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Please enter a valid age");
            return false;
        }

        return true;
    }

    private int getNextEmployeeId() throws SQLException {
        List<Employee> employees = employeeDAO.getAllEmployees();
        return employees.size() + 1;
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
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
}