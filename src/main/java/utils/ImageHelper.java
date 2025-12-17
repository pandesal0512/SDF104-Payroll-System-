package utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility class for handling employee profile pictures
 */
public class ImageHelper {

    // Directory to store profile pictures
    private static final String PROFILE_PICTURES_DIR = "profile_pictures";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    // Default profile picture (placeholder)
    private static final String DEFAULT_PROFILE = "/images/default_profile.png";

    static {
        // Create profile pictures directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(PROFILE_PICTURES_DIR));
            System.out.println("Profile pictures directory ready: " + PROFILE_PICTURES_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create profile pictures directory: " + e.getMessage());
        }
    }

    /**
     * Show file chooser and copy selected image to profile pictures folder
     * @return relative path to saved image, or null if cancelled/failed
     */
    public static String selectAndSaveProfilePicture(Window ownerWindow, String employeeQrCode) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(ownerWindow);

        if (selectedFile == null) {
            return null; // User cancelled
        }

        // Validate file
        String validationError = validateImageFile(selectedFile);
        if (validationError != null) {
            DialogHelper.showError("Invalid Image", validationError);
            return null;
        }

        // Copy file to profile pictures directory with unique name
        try {
            String extension = getFileExtension(selectedFile.getName());
            String fileName = employeeQrCode + "_" + System.currentTimeMillis() + "." + extension;
            Path targetPath = Paths.get(PROFILE_PICTURES_DIR, fileName);

            Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✓ Profile picture saved: " + targetPath);
            return targetPath.toString();

        } catch (IOException e) {
            DialogHelper.showError("Save Failed", "Could not save profile picture: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load profile picture from path
     * @return Image object, or default placeholder if not found
     */
    public static Image loadProfilePicture(String picturePath) {
        if (picturePath == null || picturePath.trim().isEmpty()) {
            return getDefaultProfileImage();
        }

        try {
            File imageFile = new File(picturePath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString());
            } else {
                System.out.println("Profile picture not found: " + picturePath);
                return getDefaultProfileImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading profile picture: " + e.getMessage());
            return getDefaultProfileImage();
        }
    }

    /**
     * Get default profile placeholder image
     */
    public static Image getDefaultProfileImage() {
        try {
            // Try to load default image from resources
            var stream = ImageHelper.class.getResourceAsStream(DEFAULT_PROFILE);
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception e) {
            System.out.println("Default profile image not found in resources");
        }

        // Return generated placeholder
        return createPlaceholderImage();
    }

    /**
     * Create a simple placeholder image
     */
    private static Image createPlaceholderImage() {
        BufferedImage bufferedImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = bufferedImage.createGraphics();

        // Gray background
        g2d.setColor(java.awt.Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, 150, 150);

        // Draw person icon
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillOval(50, 30, 50, 50); // Head
        g2d.fillOval(40, 85, 70, 50); // Body

        g2d.dispose();

        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Make ImageView circular (for profile pictures)
     */
    public static void makeCircular(ImageView imageView) {
        double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2;
        Circle clip = new Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2, radius);
        imageView.setClip(clip);
    }

    /**
     * Delete profile picture file
     */
    public static boolean deleteProfilePicture(String picturePath) {
        if (picturePath == null || picturePath.trim().isEmpty()) {
            return false;
        }

        try {
            File imageFile = new File(picturePath);
            if (imageFile.exists() && imageFile.delete()) {
                System.out.println("✓ Profile picture deleted: " + picturePath);
                return true;
            }
        } catch (Exception e) {
            System.err.println("✗ Error deleting profile picture: " + e.getMessage());
        }
        return false;
    }

    /**
     * Validate image file
     */
    private static String validateImageFile(File file) {
        // Check if file exists
        if (!file.exists()) {
            return "File does not exist";
        }

        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            return "File size exceeds 5MB limit";
        }

        // Check file extension
        String extension = getFileExtension(file.getName()).toLowerCase();
        boolean validExtension = false;
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            return "Invalid file format. Allowed: JPG, PNG, GIF";
        }

        // Try to read as image
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return "File is not a valid image";
            }

            // Check dimensions (optional)
            if (img.getWidth() > 4000 || img.getHeight() > 4000) {
                return "Image dimensions too large (max 4000x4000)";
            }

        } catch (IOException e) {
            return "Cannot read image file: " + e.getMessage();
        }

        return null; // Valid
    }

    /**
     * Get file extension
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Resize image to fit profile picture requirements
     */
    public static Image resizeImage(Image original, double maxWidth, double maxHeight) {
        double width = original.getWidth();
        double height = original.getHeight();

        double ratio = Math.min(maxWidth / width, maxHeight / height);

        if (ratio < 1.0) {
            return new Image(
                    original.getUrl(),
                    width * ratio,
                    height * ratio,
                    true, // preserve ratio
                    true  // smooth
            );
        }

        return original;
    }

    /**
     * Load and prepare profile picture for ImageView
     */
    public static void loadProfilePictureIntoView(ImageView imageView, String picturePath) {
        Image image = loadProfilePicture(picturePath);

        // Resize to fit
        if (imageView.getFitWidth() > 0 && imageView.getFitHeight() > 0) {
            image = resizeImage(image, imageView.getFitWidth(), imageView.getFitHeight());
        }

        imageView.setImage(image);

        // Make circular
        makeCircular(imageView);
    }

    /**
     * Get formatted file size
     */
    public static String getFileSizeString(File file) {
        long bytes = file.length();

        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}