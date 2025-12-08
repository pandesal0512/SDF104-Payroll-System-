package controllers;

import dao.DepartmentDAO;
import dao.PositionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.Department;
import models.Position;
import utils.DialogHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DepartmentsPositionsController {

    // Departments Table
    @FXML private TableView<DepartmentDisplay> departmentsTable;
    @FXML private TableColumn<DepartmentDisplay, Integer> deptIdColumn;
    @FXML private TableColumn<DepartmentDisplay, String> deptNameColumn;
    @FXML private TableColumn<DepartmentDisplay, String> deptDescColumn;
    @FXML private TableColumn<DepartmentDisplay, Integer> deptEmpCountColumn;

    // Positions Table
    @FXML private TableView<PositionDisplay> positionsTable;
    @FXML private TableColumn<PositionDisplay, Integer> posIdColumn;
    @FXML private TableColumn<PositionDisplay, String> posTitleColumn;
    @FXML private TableColumn<PositionDisplay, String> posDeptColumn;
    @FXML private TableColumn<PositionDisplay, Double> posSalaryColumn;
    @FXML private TableColumn<PositionDisplay, String> posDescColumn;
    @FXML private ComboBox<Department> filterDepartmentCombo;

    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private PositionDAO positionDAO = new PositionDAO();
    private PositionFormDialog positionFormDialog = new PositionFormDialog();

    private ObservableList<DepartmentDisplay> departmentList = FXCollections.observableArrayList();
    private ObservableList<PositionDisplay> positionList = FXCollections.observableArrayList();
    private ObservableList<Department> departmentFilterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupDepartmentsTable();
        setupPositionsTable();
        loadDepartments();
        loadPositions();
        setupFilterCombo();
    }

    private void setupDepartmentsTable() {
        deptIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        deptNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        deptDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        deptEmpCountColumn.setCellValueFactory(new PropertyValueFactory<>("employeeCount"));

        departmentsTable.setItems(departmentList);
    }

    private void setupPositionsTable() {
        posIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        posTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        posDeptColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        posSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
        posDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Format hourly rate column
        posSalaryColumn.setCellFactory(column -> new TableCell<PositionDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₱%.2f/hr", item));
                }
            }
        });

        positionsTable.setItems(positionList);
    }

    private void setupFilterCombo() {
        filterDepartmentCombo.setItems(departmentFilterList);
        filterDepartmentCombo.setCellFactory(param -> new ListCell<Department>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        filterDepartmentCombo.setButtonCell(new ListCell<Department>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    private void loadDepartments() {
        try {
            departmentList.clear();
            departmentFilterList.clear();

            List<Department> departments = departmentDAO.getAllDepartments();

            for (Department dept : departments) {
                // Get employee count for this department
                int empCount = departmentDAO.getEmployeeCountByDepartment(dept.getId());

                departmentList.add(new DepartmentDisplay(
                        dept.getId(),
                        dept.getName(),
                        dept.getDescription(),
                        empCount
                ));

                departmentFilterList.add(dept);
            }
        } catch (SQLException e) {
            DialogHelper.showError("Failed to load departments: " + e.getMessage());
        }
    }

    private void loadPositions() {
        try {
            positionList.clear();
            List<Position> positions = positionDAO.getAllPositions();

            for (Position pos : positions) {
                Department dept = departmentDAO.getDepartmentById(pos.getDepartmentId());
                String deptName = (dept != null) ? dept.getName() : "Unknown";

                positionList.add(new PositionDisplay(
                        pos.getId(),
                        pos.getTitle(),
                        deptName,
                        pos.getHourlyRate(),
                        pos.getDescription(),
                        pos.getDepartmentId()
                ));
            }
        } catch (SQLException e) {
            DialogHelper.showError("Failed to load positions: " + e.getMessage());
        }
    }

    // ==================== DEPARTMENT ACTIONS ====================

    @FXML
    private void handleAddDepartment() {
        Dialog<DepartmentInput> dialog = new Dialog<>();
        dialog.setTitle("Add Department");
        dialog.setHeaderText("Create New Department");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Human Resources, IT, Sales");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Brief description of the department");
        descArea.setPrefRowCount(3);

        grid.add(new Label("Department Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Focus on name field
        nameField.requestFocus();

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String name = nameField.getText().trim();
                String desc = descArea.getText().trim();

                if (name.isEmpty()) {
                    DialogHelper.showWarning("Department name cannot be empty!");
                    return null;
                }

                return new DepartmentInput(name, desc);
            }
            return null;
        });

        Optional<DepartmentInput> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                // Check if department already exists
                if (departmentDAO.departmentExists(input.name)) {
                    DialogHelper.showWarning("A department with this name already exists!");
                    return;
                }

                Department dept = new Department(input.name, input.description);
                departmentDAO.addDepartment(dept);
                DialogHelper.showSuccess("Department '" + input.name + "' added successfully!");
                loadDepartments();
                loadPositions();
            } catch (SQLException e) {
                DialogHelper.showError("Failed to add department: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditDepartment() {
        DepartmentDisplay selected = departmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("Please select a department to edit");
            return;
        }

        Dialog<DepartmentInput> dialog = new Dialog<>();
        dialog.setTitle("Edit Department");
        dialog.setHeaderText("Update Department: " + selected.getName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextArea descArea = new TextArea(selected.getDescription());
        descArea.setPrefRowCount(3);

        grid.add(new Label("Department Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String name = nameField.getText().trim();
                String desc = descArea.getText().trim();

                if (name.isEmpty()) {
                    DialogHelper.showWarning("Department name cannot be empty!");
                    return null;
                }

                return new DepartmentInput(name, desc);
            }
            return null;
        });

        Optional<DepartmentInput> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                Department dept = departmentDAO.getDepartmentById(selected.getId());
                dept.setName(input.name);
                dept.setDescription(input.description);
                departmentDAO.updateDepartment(dept);
                DialogHelper.showSuccess("Department updated successfully!");
                loadDepartments();
                loadPositions();
            } catch (SQLException e) {
                DialogHelper.showError("Failed to update department: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteDepartment() {
        DepartmentDisplay selected = departmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("Please select a department to delete");
            return;
        }

        try {
            // Check if department has employees
            int empCount = selected.getEmployeeCount();
            if (empCount > 0) {
                DialogHelper.showWarning(
                        "Cannot delete department '" + selected.getName() + "'\n\n" +
                                "This department has " + empCount + " employee(s). " +
                                "Please reassign or remove employees first."
                );
                return;
            }

            // Check if department has positions
            List<Position> positions = positionDAO.getPositionsByDepartment(selected.getId());
            if (!positions.isEmpty()) {
                boolean confirm = DialogHelper.showConfirmation(
                        "Delete Department",
                        "Department '" + selected.getName() + "' has " + positions.size() + " position(s).\n\n" +
                                "Deleting this department will also delete all its positions.\n\n" +
                                "Do you want to continue?"
                );

                if (!confirm) return;

                // Delete all positions in this department first
                for (Position pos : positions) {
                    positionDAO.deletePosition(pos.getId());
                }
            }

            // Confirm deletion
            boolean confirm = DialogHelper.showDeleteConfirmation("department '" + selected.getName() + "'");
            if (confirm) {
                departmentDAO.deleteDepartment(selected.getId());
                DialogHelper.showSuccess("Department deleted successfully!");
                loadDepartments();
                loadPositions();
            }

        } catch (SQLException e) {
            DialogHelper.showError("Failed to delete department: " + e.getMessage());
        }
    }

    // ==================== POSITION ACTIONS ====================

    @FXML
    private void handleAddPosition() {
        // Check if any departments exist
        if (departmentFilterList.isEmpty()) {
            DialogHelper.showWarning("Please create at least one department first!");
            return;
        }

        // Show position form dialog
        Optional<Position> result = positionFormDialog.showAddDialog();

        result.ifPresent(position -> {
            if (positionFormDialog.savePosition(position)) {
                loadPositions();
                loadDepartments();
            }
        });
    }

    @FXML
    private void handleEditPosition() {
        PositionDisplay selected = positionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("Please select a position to edit");
            return;
        }

        try {
            // Load full position object
            Position position = positionDAO.getPositionById(selected.getId());
            if (position == null) {
                DialogHelper.showError("Position not found!");
                return;
            }

            // Show edit dialog
            Optional<Position> result = positionFormDialog.showEditDialog(position);

            result.ifPresent(updatedPosition -> {
                if (positionFormDialog.savePosition(updatedPosition)) {
                    loadPositions();
                    loadDepartments();
                }
            });

        } catch (SQLException e) {
            DialogHelper.showError("Failed to load position: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeletePosition() {
        PositionDisplay selected = positionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("Please select a position to delete");
            return;
        }

        boolean confirm = DialogHelper.showDeleteConfirmation("position '" + selected.getTitle() + "'");

        if (confirm) {
            try {
                positionDAO.deletePosition(selected.getId());
                DialogHelper.showSuccess("Position deleted successfully!");
                loadPositions();
                loadDepartments();
            } catch (SQLException e) {
                DialogHelper.showError("Failed to delete position: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFilterByDepartment() {
        Department selected = filterDepartmentCombo.getValue();
        if (selected != null) {
            try {
                positionList.clear();
                List<Position> positions = positionDAO.getPositionsByDepartment(selected.getId());

                for (Position pos : positions) {
                    positionList.add(new PositionDisplay(
                            pos.getId(),
                            pos.getTitle(),
                            selected.getName(),
                            pos.getHourlyRate(),
                            pos.getDescription(),
                            pos.getDepartmentId()
                    ));
                }
            } catch (SQLException e) {
                DialogHelper.showError("Filter failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearFilter() {
        filterDepartmentCombo.setValue(null);
        loadPositions();
    }

    // ==================== DISPLAY CLASSES ====================

    public static class DepartmentDisplay {
        private final int id;
        private final String name;
        private final String description;
        private final int employeeCount;

        public DepartmentDisplay(int id, String name, String description, int employeeCount) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.employeeCount = employeeCount;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getEmployeeCount() { return employeeCount; }
    }

    public static class PositionDisplay {
        private final int id;
        private final String title;
        private final String departmentName;
        private final double hourlyRate;
        private final String description;
        private final int departmentId;

        public PositionDisplay(int id, String title, String departmentName,
                               double hourlyRate, String description, int departmentId) {
            this.id = id;
            this.title = title;
            this.departmentName = departmentName;
            this.hourlyRate = hourlyRate;
            this.description = description;
            this.departmentId = departmentId;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDepartmentName() { return departmentName; }
        public double getHourlyRate() { return hourlyRate; }
        public String getDescription() { return description; }
        public int getDepartmentId() { return departmentId; }
    }

    private static class DepartmentInput {
        final String name;
        final String description;

        DepartmentInput(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}