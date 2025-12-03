package controllers;

import dao.DepartmentDAO;
import dao.PositionDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import models.Department;
import models.Position;
import utils.DialogHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Helper class for Position Form Dialog
 */
public class PositionFormDialog {

    private final PositionDAO positionDAO = new PositionDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    /**
     * Show dialog to add a new position
     */
    public Optional<Position> showAddDialog() {
        return showDialog(null);
    }

    /**
     * Show dialog to edit existing position
     */
    public Optional<Position> showEditDialog(Position position) {
        return showDialog(position);
    }

    /**
     * Main dialog method for add/edit
     */
    private Optional<Position> showDialog(Position existingPosition) {
        boolean isEditMode = existingPosition != null;

        Dialog<Position> dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? "Edit Position" : "Add New Position");
        dialog.setHeaderText(isEditMode ? "Update position details" : "Enter new position details");

        ButtonType saveButtonType = new ButtonType(isEditMode ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createFormGrid();

        TextField titleField = new TextField();
        titleField.setPromptText("e.g., Software Developer");

        ComboBox<Department> departmentCombo = new ComboBox<>();
        departmentCombo.setPromptText("Select Department");

        TextField salaryField = new TextField();
        salaryField.setPromptText("e.g., 50000.00");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Job description (optional)");
        descriptionArea.setPrefRowCount(3);

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
            DialogHelper.showError("Failed to load departments: " + e.getMessage());
            return Optional.empty();
        }

        if (isEditMode) {
            titleField.setText(existingPosition.getTitle());
            salaryField.setText(String.valueOf(existingPosition.getBaseSalary()));
            descriptionArea.setText(existingPosition.getDescription());

            try {
                Department dept = departmentDAO.getDepartmentById(existingPosition.getDepartmentId());
                departmentCombo.setValue(dept);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        grid.add(new Label("Position Title:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Department:"), 0, 1);
        grid.add(departmentCombo, 1, 1);

        grid.add(new Label("Base Salary (₱):"), 0, 2);
        grid.add(salaryField, 1, 2);

        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        titleField.textProperty().addListener((obs, old, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty() ||
                    departmentCombo.getValue() == null ||
                    salaryField.getText().trim().isEmpty());
        });

        departmentCombo.valueProperty().addListener((obs, old, newVal) -> {
            saveButton.setDisable(titleField.getText().trim().isEmpty() ||
                    newVal == null ||
                    salaryField.getText().trim().isEmpty());
        });

        salaryField.textProperty().addListener((obs, old, newVal) -> {
            saveButton.setDisable(titleField.getText().trim().isEmpty() ||
                    departmentCombo.getValue() == null ||
                    newVal.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String title = titleField.getText().trim();
                    Department dept = departmentCombo.getValue();
                    double salary = Double.parseDouble(salaryField.getText().trim());
                    String description = descriptionArea.getText().trim();

                    if (title.isEmpty() || dept == null || salary <= 0) {
                        DialogHelper.showError("Please fill all required fields with valid data");
                        return null;
                    }

                    Position position;
                    if (isEditMode) {
                        position = existingPosition;
                        position.setTitle(title);
                        position.setDepartmentId(dept.getId());
                        position.setBaseSalary(salary);
                        position.setDescription(description);
                    } else {
                        position = new Position(title, dept.getId(), salary, description);
                    }

                    return position;

                } catch (NumberFormatException e) {
                    DialogHelper.showError("Please enter a valid salary amount");
                    return null;
                }
            }
            return null;
        });

        javafx.application.Platform.runLater(() -> titleField.requestFocus());

        return dialog.showAndWait();
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    public boolean savePosition(Position position) {
        try {
            if (position.getId() == 0) {
                positionDAO.addPosition(position);
                DialogHelper.showSuccess("Position added successfully!");
            } else {
                positionDAO.updatePosition(position);
                DialogHelper.showSuccess("Position updated successfully!");
            }
            return true;
        } catch (SQLException e) {
            DialogHelper.showError("Failed to save position: " + e.getMessage());
            return false;
        }
    }
}