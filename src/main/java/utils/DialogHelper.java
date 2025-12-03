package utils;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Utility class for showing dialogs and alerts
 */
public class DialogHelper {

    /**
     * Show error alert
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error alert with default title
     */
    public static void showError(String message) {
        showError("Error", message);
    }

    /**
     * Show warning alert
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning alert with default title
     */
    public static void showWarning(String message) {
        showWarning("Warning", message);
    }

    /**
     * Show information alert
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show information alert with default title
     */
    public static void showInfo(String message) {
        showInfo("Information", message);
    }

    /**
     * Show success alert
     */
    public static void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show confirmation dialog with default title
     */
    public static boolean showConfirmation(String message) {
        return showConfirmation("Confirm Action", message);
    }

    /**
     * Show delete confirmation
     */
    public static boolean showDeleteConfirmation(String itemName) {
        return showConfirmation(
                "Confirm Delete",
                "Are you sure you want to delete " + itemName + "?\n\nThis action cannot be undone."
        );
    }

    /**
     * Show text input dialog
     */
    public static Optional<String> showTextInput(String title, String header, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);

        return dialog.showAndWait();
    }

    /**
     * Show text input dialog with default value
     */
    public static Optional<String> showTextInput(String title, String header,
                                                 String prompt, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);

        return dialog.showAndWait();
    }

    /**
     * Show choice dialog
     */
    @SafeVarargs
    public static <T> Optional<T> showChoice(String title, String header,
                                             String prompt, T defaultChoice, T... choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);

        return dialog.showAndWait();
    }

    /**
     * Show multi-line text input dialog
     */
    public static Optional<String> showTextArea(String title, String header, String prompt) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        TextArea textArea = new TextArea();
        textArea.setPromptText(prompt);
        textArea.setPrefRowCount(5);
        textArea.setPrefColumnCount(40);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return textArea.getText();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Show custom dialog with OK/Cancel buttons
     */
    public static Optional<ButtonType> showCustomDialog(String title, String header,
                                                        javafx.scene.Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        return dialog.showAndWait();
    }

    /**
     * Show exception dialog
     */
    public static void showException(String title, String message, Exception exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());

        // Create expandable exception details
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Exception stacktrace:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Show loading indicator (non-blocking)
     */
    public static Alert showLoading(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Please Wait");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().clear(); // Remove buttons
        alert.show();
        return alert;
    }

    /**
     * Close loading indicator
     */
    public static void closeLoading(Alert loadingAlert) {
        if (loadingAlert != null) {
            loadingAlert.close();
        }
    }

    /**
     * Show Yes/No dialog
     */
    public static boolean showYesNo(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    /**
     * Show alert with custom buttons
     */
    public static Optional<ButtonType> showAlert(String title, String message,
                                                 AlertType type, ButtonType... buttons) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (buttons.length > 0) {
            alert.getButtonTypes().setAll(buttons);
        }

        return alert.showAndWait();
    }
}