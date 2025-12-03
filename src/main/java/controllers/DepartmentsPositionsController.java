package controllers;

import dao.DepartmentDAO;
import dao.PositionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Department;
import models.Position;
import utils.DialogHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DepartmentsPositionsController {

    // Departments Table
    @FXML private TableView<Department> departmentsTable;
    @FXML private TableColumn<Department, Integer> deptIdColumn;
    @FXML private TableColumn<Department, String> deptNameColumn;
    @FXML private TableColumn<Department, String> deptDescColumn;
    @FXML private TableColumn<Department, String> deptEmpCountColumn;

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

    private ObservableList<Department> departmentList = FXCollections.observableArrayList();
    private ObservableList<PositionDisplay> positionList = FXCollections.observableArrayList();

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

        departmentsTable.setItems(departmentList);
    }

    private void setupPositionsTable() {
        posIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        posTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        posDeptColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        posSalaryColumn.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        posDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Format salary column
        posSalaryColumn.setCellFactory(column -> new TableCell<PositionDisplay, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₱%,.2f", item));
                }
            }
        });

        positionsTable.setItems(positionList);
    }

    private void setupFilterCombo() {
        filterDepartmentCombo.setItems(departmentList);
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
            List<Department> departments = departmentDAO.getAllDepartments();
            departmentList.addAll(departments);
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
                        pos.getBaseSalary(),
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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Department");
        dialog.setHeaderText("Create New Department");
        dialog.setContentText("Department Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    // Check if department already exists
                    if (departmentDAO.departmentExists(name.trim())) {
                        DialogHelper.showWarning("A department with this name already exists!");
                        return;
                    }

                    Department dept = new Department(name.trim(), "");
                    departmentDAO.addDepartment(dept);
                    DialogHelper.showSuccess("Department '" + name + "' added successfully!");
                    loadDepartments();
                    loadPositions(); // Refresh in case positions need updated dept names
                } catch (SQLException e) {
                    DialogHelper.showError("Failed to add department: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleEditDepartment() {
        Department selected = departmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("Please select a department to edit");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Edit Department");
        dialog.setHeaderText("Update Department: " + selected.getName());
        dialog.setContentText("Department Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    selected.setName(name.trim());
                    departmentDAO.updateDepartment(selected);
                    DialogHelper.showSuccess("Department updated successfully!");
                    loadDepartments();
                    loadPositions();
                } catch (SQLException e) {
                    DialogHelper.showError("Failed to update department: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleDeleteDepartment() {
        Department selected = departmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showWarning("Please select a department to delete");
            return;
        }

        try {
            // Check if department has employees
            int empCount = departmentDAO.getEmployeeCountByDepartment(selected.getId());
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
        if (departmentList.isEmpty()) {
            DialogHelper.showWarning("Please create at least one department first!");
            return;
        }

        // Show position form dialog
        Optional<Position> result = positionFormDialog.showAddDialog();

        result.ifPresent(position -> {
            if (positionFormDialog.savePosition(position)) {
                loadPositions();
                loadDepartments(); // Refresh departments table
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
                            pos.getBaseSalary(),
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

    // ==================== DISPLAY CLASS ====================

    public static class PositionDisplay {
        private final int id;
        private final String title;
        private final String departmentName;
        private final double baseSalary;
        private final String description;
        private final int departmentId;

        public PositionDisplay(int id, String title, String departmentName,
                               double baseSalary, String description, int departmentId) {
            this.id = id;
            this.title = title;
            this.departmentName = departmentName;
            this.baseSalary = baseSalary;
            this.description = description;
            this.departmentId = departmentId;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDepartmentName() { return departmentName; }
        public double getBaseSalary() { return baseSalary; }
        public String getDescription() { return description; }
        public int getDepartmentId() { return departmentId; }
    }
}