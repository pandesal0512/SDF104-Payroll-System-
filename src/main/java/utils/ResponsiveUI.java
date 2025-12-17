package utils;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Enhanced Responsive UI - Adapts to monitor size changes automatically
 */
public class ResponsiveUI {

    // Base size for scaling (1920x1080 - Full HD)
    private static final double BASE_WIDTH = 1920.0;
    private static final double BASE_HEIGHT = 1080.0;

    // Font size scaling
    private static final double SMALL_SCREEN_FONT = 12.0;
    private static final double MEDIUM_SCREEN_FONT = 14.0;
    private static final double LARGE_SCREEN_FONT = 16.0;

    /**
     *  Make stage responsive with automatic monitor change detection
     */
    public static void makeResponsive(Stage stage) {
        if (stage == null || stage.getScene() == null) {
            return;
        }

        // Apply initial responsive sizing
        applyResponsiveSizing(stage);

        // Listen for window position changes (monitor switch)
        stage.xProperty().addListener((obs, oldVal, newVal) -> {
            if (hasMonitorChanged(stage)) {
                System.out.println("🖥️ Monitor changed detected - adjusting UI");
                applyResponsiveSizing(stage);
            }
        });

        stage.yProperty().addListener((obs, oldVal, newVal) -> {
            if (hasMonitorChanged(stage)) {
                System.out.println("🖥️ Monitor changed detected - adjusting UI");
                applyResponsiveSizing(stage);
            }
        });

        // Listen for window size changes
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.getScene() != null) {
                applyFontScaling(stage.getScene(), getCurrentScreenBounds(stage));
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (stage.getScene() != null) {
                applyFontScaling(stage.getScene(), getCurrentScreenBounds(stage));
            }
        });

        System.out.println("✓ Responsive UI listeners attached");
    }

    /**
     *  Detect if window moved to different monitor
     */
    private static boolean hasMonitorChanged(Stage stage) {
        try {
            Screen currentScreen = getCurrentScreen(stage);
            Rectangle2D currentBounds = currentScreen.getVisualBounds();

            // Store previous screen bounds (simplified check)
            double stageX = stage.getX();
            double stageY = stage.getY();

            // Check if stage is outside current screen bounds
            return stageX < currentBounds.getMinX() - 100 ||
                    stageX > currentBounds.getMaxX() + 100 ||
                    stageY < currentBounds.getMinY() - 100 ||
                    stageY > currentBounds.getMaxY() + 100;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *  Get the screen where the stage is currently displayed
     */
    private static Screen getCurrentScreen(Stage stage) {
        // Get stage center point
        double stageCenterX = stage.getX() + (stage.getWidth() / 2);
        double stageCenterY = stage.getY() + (stage.getHeight() / 2);

        // Find which screen contains this point
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            if (bounds.contains(stageCenterX, stageCenterY)) {
                return screen;
            }
        }

        // Fallback to primary screen
        return Screen.getPrimary();
    }

    /**
     * ✨ NEW: Get current screen bounds for stage
     */
    private static Rectangle2D getCurrentScreenBounds(Stage stage) {
        return getCurrentScreen(stage).getVisualBounds();
    }

    /**
     * Apply responsive sizing based on current screen
     */
    private static void applyResponsiveSizing(Stage stage) {
        Rectangle2D screenBounds = getCurrentScreenBounds(stage);

        // If maximized, don't adjust size
        if (stage.isMaximized()) {
            applyFontScaling(stage.getScene(), screenBounds);
            return;
        }

        // For non-maximized windows, scale to 90% of screen
        double width = screenBounds.getWidth() * 0.9;
        double height = screenBounds.getHeight() * 0.9;

        stage.setWidth(width);
        stage.setHeight(height);

        // Center on current screen
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - width) / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - height) / 2);

        // Apply font scaling
        if (stage.getScene() != null) {
            applyFontScaling(stage.getScene(), screenBounds);
        }
    }

    /**
     * Apply font scaling based on screen resolution
     */
    public static void applyFontScaling(Scene scene, Rectangle2D screenBounds) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        double screenWidth = screenBounds.getWidth();
        double fontSize;

        if (screenWidth < 1024) {
            fontSize = SMALL_SCREEN_FONT;
        } else if (screenWidth <= 1920) {
            fontSize = MEDIUM_SCREEN_FONT;
        } else {
            fontSize = LARGE_SCREEN_FONT;
        }

        // Apply to root - preserving existing styles
        String existingStyle = scene.getRoot().getStyle();

        // Remove old font-size if exists
        existingStyle = existingStyle.replaceAll("-fx-font-size:\\s*[^;]+;", "");

        // Add new font-size
        String newStyle = existingStyle + String.format("-fx-font-size: %.1fpx;", fontSize);
        scene.getRoot().setStyle(newStyle);

        System.out.println(String.format(" Font scaled to %.1fpx for %dx%.0f screen",
                fontSize, (int)screenWidth, screenBounds.getHeight()));
    }

    /**
     * Get scale factor based on screen vs base resolution
     */
    public static double getScaleFactor() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double widthRatio = screenBounds.getWidth() / BASE_WIDTH;
        double heightRatio = screenBounds.getHeight() / BASE_HEIGHT;
        return Math.min(widthRatio, heightRatio);
    }

    /**
     * Scale a value based on current screen size
     */
    public static double scale(double value) {
        return value * getScaleFactor();
    }

    /**
     * Get recommended font size for current screen
     */
    public static double getRecommendedFontSize() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();

        if (screenWidth < 1024) {
            return SMALL_SCREEN_FONT;
        } else if (screenWidth <= 1920) {
            return MEDIUM_SCREEN_FONT;
        } else {
            return LARGE_SCREEN_FONT;
        }
    }

    /**
     * Get screen size category
     */
    public static String getScreenCategory() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();

        if (screenWidth < 1024) {
            return "SMALL";
        } else if (screenWidth <= 1920) {
            return "MEDIUM";
        } else {
            return "LARGE";
        }
    }

    /**
     * Print screen information (for debugging)
     */
    public static void printScreenInfo() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double scaleFactor = getScaleFactor();
        System.out.println("  SCREEN INFORMATION");
        System.out.println("Resolution:    " + (int)screenBounds.getWidth() + " × " +
                (int)screenBounds.getHeight());
        System.out.println("Category:      " + getScreenCategory());
        System.out.println("Scale Factor:  " + String.format("%.2f", scaleFactor));
        System.out.println("Font Size:     " + getRecommendedFontSize() + "px");
        System.out.println("Monitors:      " + Screen.getScreens().size());

    }

    /**
     * Apply responsive style to a Scene
     */
    public static void applyResponsiveStyle(Scene scene) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        applyFontScaling(scene, screenBounds);

        // Load CSS if available
        try {
            String css = ResponsiveUI.class.getResource("/css/application.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
                System.out.println("Responsive CSS loaded");
            }
        } catch (Exception e) {
            System.out.println(" CSS file not found - using default styles");
        }
    }

    /**
     * Initialize stage with responsive settings
     */
    public static void initializeStage(Stage stage, String title, double prefWidth, double prefHeight) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Calculate size (90% of screen or preferred size, whichever is smaller)
        double width = Math.min(prefWidth, screenBounds.getWidth() * 0.9);
        double height = Math.min(prefHeight, screenBounds.getHeight() * 0.9);

        stage.setTitle(title);
        stage.setWidth(width);
        stage.setHeight(height);

        // Center on screen
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - width) / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - height) / 2);

        // Set minimum size
        stage.setMinWidth(Math.min(800, screenBounds.getWidth() * 0.5));
        stage.setMinHeight(Math.min(600, screenBounds.getHeight() * 0.5));
    }
}