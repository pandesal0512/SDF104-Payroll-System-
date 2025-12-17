package controllers;

import dao.EmployeeDAO;
import dao.DepartmentDAO;
import dao.PositionDAO;
import dao.ShiftDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Employee;
import models.Department;
import models.Position;
import models.Shift;
import utils.QRCodeGenerator;
import utils.ImageHelper;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Employee Form - WITH SHIFT SELECTION
 */
public class EmployeeFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private TextField contactField;

    @FXML private TextField emergencyContactNameField;
    @FXML private TextField emergencyContactPhoneField;

    @FXML private ComboBox<Department> departmentCombo;
    @FXML private ComboBox<Position> positionCombo;
    @FXML private ComboBox<Shift> shiftCombo;  // NEW - Shift selection
    @FXML private DatePicker hireDatePicker;
    @FXML private ComboBox<String> statusCombo;

    @FXML private ImageView profilePictureView;
    @FXML private Label photoStatusLabel;
    @FXML private Button uploadPhotoButton;
    @FXML private Button removePhotoButton;

    @FXML private ImageView qrImageView;
    @FXML private Label qrCodeLabel;

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private PositionDAO positionDAO = new PositionDAO();
    private ShiftDAO shiftDAO = new ShiftDAO();  // NEW

    private Employee currentEmployee;
    private String generatedQRCode;
    private String currentProfilePicturePath;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        loadDepartments();
        loadShifts();  // NEW - Load available shifts
        setupStatusCombo();
        setupDepartmentListener();
        hireDatePicker.setValue(LocalDate.now());

        initializeProfilePicture();
    }

    private void initializeProfilePicture() {
        Image defaultImage = ImageHelper.getDefaultProfileImage();
        profilePictureView.setImage(defaultImage);
        ImageHelper.makeCircular(profilePictureView);
        photoStatusLabel.setText("No photo selected");
        removePhotoButton.setDisable(true);
    }

    /**
     * NEW - Load available shifts
     */
    private void loadShifts() {
        try {
            List<Shift> shifts = shiftDAO.getActiveShifts();
            shiftCombo.setItems(FXCollections.observableArrayList(shifts));

            shiftCombo.setCellFactory(param -> new ListCell<Shift>() {
                @Override
                protected void updateItem(Shift item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getShiftEmoji() + " " + item.getName() +
                                " (" + item.getShiftTimeRange() + ")");
                    }
                }
            });

            shiftCombo.setButtonCell(new ListCell<Shift>() {
                @Override
                protected void updateItem(Shift item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Shift (Optional)");
                    } else {
                        setText(item.getShiftEmoji() + " " + item.getName() +
                                " (" + item.getShiftTimeRange() + ")");
                    }
                }
            });

            // Set prompt text
            shiftCombo.setPromptText("Select Shift (Optional)");

        } catch (SQLException e) {
            showError("Failed to load shifts: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadPhoto() {
        Stage stage = (Stage) uploadPhotoButton.getScene().getWindow();
        String qrForFilename = generatedQRCode != null ? generatedQRCode : "TEMP_" + System.currentTimeMillis();

        String picturePath = ImageHelper.selectAndSaveProfilePicture(stage, qrForFilename);

        if (picturePath != null) {
            currentProfilePicturePath = picturePath;
            Image image = ImageHelper.loadProfilePicture(picturePath);
            profilePictureView.setImage(image);
            ImageHelper.makeCircular(profilePictureView);

            photoStatusLabel.setText("✓ Photo selected");
            photoStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
            removePhotoButton.setDisable(false);

            System.out.println("✓ Profile picture selected: " + picturePath);
        }
    }

    @FXML
    private void handleRemovePhoto() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Photo");
        confirm.setHeaderText("Remove Profile Picture?");
        confirm.setContentText("This will remove the current profile picture.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                currentProfilePicturePath = null;
                initializeProfilePicture();
                showInfo("Profile picture removed");
            }
        });
    }

    private void loadDepartments() {
        try {
            List<Department> departments = departmentDAO.getAllDepartments();
            departmentCombo.setItems(FXCollections.observableArrayList(departments));

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

            emergencyContactNameField.setText(employee.getEmergencyContactName());
            emergencyContactPhoneField.setText(employee.getEmergencyContactPhone());

            if (employee.hasProfilePicture()) {
                currentProfilePicturePath = employee.getProfilePicturePath();
                Image image = ImageHelper.loadProfilePicture(currentProfilePicturePath);
                profilePictureView.setImage(image);
                ImageHelper.makeCircular(profilePictureView);
                photoStatusLabel.setText("✓ Photo loaded");
                photoStatusLabel.setStyle("-fx-text-fill: #4CAF50;");
                removePhotoButton.setDisable(false);
            }

            if (employee.getHireDate() != null && !employee.getHireDate().isEmpty()) {
                hireDatePicker.setValue(LocalDate.parse(employee.getHireDate()));
            }

            Department dept = departmentDAO.getDepartmentById(employee.getDepartmentId());
            if (dept != null) {
                departmentCombo.setValue(dept);
            }

            Position pos = positionDAO.getPositionById(employee.getPositionId());
            if (pos != null) {
                positionCombo.setValue(pos);
            }

            // NEW - Load employee's shift
            if (employee.hasShift()) {
                Shift shift = shiftDAO.getShiftById(employee.getShiftId());
                if (shift != null) {
                    shiftCombo.setValue(shift);
                }
            }

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

            String emergencyName = emergencyContactNameField.getText().trim();
            String emergencyPhone = emergencyContactPhoneField.getText().trim();

            // NEW - Get selected shift (can be null)
            Integer shiftId = null;
            if (shiftCombo.getValue() != null) {
                shiftId = shiftCombo.getValue().getId();
            }

            if (currentEmployee == null) {
                // ADD MODE
                if (generatedQRCode == null) {
                    int nextId = getNextEmployeeId();
                    generatedQRCode = QRCodeGenerator.generateQRCodeText(nextId, name.split(" ")[0]);
                }

                Employee newEmployee = new Employee(
                        name, age, positionId, departmentId,
                        hireDate, contact, generatedQRCode,
                        emergencyName, emergencyPhone, currentProfilePicturePath,
                        shiftId  // NEW - Pass shift ID
                );
                newEmployee.setStatus(status);
                employeeDAO.addEmployee(newEmployee);

                String shiftInfo = shiftId != null ?
                        "\nShift: " + shiftCombo.getValue().getName() :
                        "\nShift: Not assigned";
                showInfo("✓ Employee added successfully!" + shiftInfo);

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
                currentEmployee.setEmergencyContactName(emergencyName);
                currentEmployee.setEmergencyContactPhone(emergencyPhone);
                currentEmployee.setProfilePicturePath(currentProfilePicturePath);
                currentEmployee.setShiftId(shiftId);  // NEW - Update shift

                employeeDAO.updateEmployee(currentEmployee);

                String shiftInfo = shiftId != null ?
                        "\nShift: " + shiftCombo.getValue().getName() :
                        "\nShift: Not assigned";
                showInfo("✓ Employee updated successfully!" + shiftInfo);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

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

            showInfo("✓ QR Code regenerated: " + generatedQRCode);
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

        showInfo("QR Code: " + generatedQRCode + "\n\nCopy this code for attendance scanning.");
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
            int age = Integer.parseInt(ageField.getText().trim());
            if (age < 18 || age > 70) {
                showWarning("Age must be between 18 and 70");
                return false;
            }
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