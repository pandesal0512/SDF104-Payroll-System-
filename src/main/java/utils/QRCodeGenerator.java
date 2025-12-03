package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * QR Code Generator utility - SIMPLIFIED VERSION
 * Generates QR code TEXT without requiring ZXing library
 * Creates a simple visual placeholder instead of actual QR image
 */
public class QRCodeGenerator {

    /**
     * Generate unique QR code text for employee
     * Format: EMP-YEAR-ID-FIRSTNAME
     * Example: EMP-2024-001-JUAN
     */
    public static String generateQRCodeText(int employeeId, String firstName) {
        int year = java.time.Year.now().getValue();
        String formattedId = String.format("%03d", employeeId); // 001, 002, etc.
        String sanitizedName = firstName.toUpperCase().replaceAll("[^A-Z]", "");

        // Limit name to 10 characters
        if (sanitizedName.length() > 10) {
            sanitizedName = sanitizedName.substring(0, 10);
        }

        return String.format("EMP-%d-%s-%s", year, formattedId, sanitizedName);
    }

    /**
     * Alternative: Simple numeric code
     * Format: E + 3-digit ID
     * Example: E001, E002, E003
     */
    public static String generateSimpleCode(int employeeId) {
        return String.format("E%03d", employeeId);
    }

    /**
     * Generate QR code image as byte array (PLACEHOLDER VERSION)
     * Creates a simple text-based image instead of actual QR code
     */
    public static byte[] generateQRImageBytes(String qrText, int width, int height) {
        try {
            // Create a simple image with the QR text
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // White background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Black border
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(2, 2, width-4, height-4);

            // Draw QR code placeholder pattern (simple grid)
            g2d.setColor(Color.BLACK);
            int cellSize = 10;
            for (int i = 0; i < width/cellSize; i++) {
                for (int j = 0; j < height/cellSize; j++) {
                    if ((i + j) % 2 == 0) {
                        g2d.fillRect(i * cellSize, j * cellSize, cellSize-1, cellSize-1);
                    }
                }
            }

            // Draw text in center
            g2d.setColor(Color.WHITE);
            g2d.fillRect(width/4, height/2 - 20, width/2, 40);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(qrText);
            int x = (width - textWidth) / 2;
            int y = (height / 2) + 5;
            g2d.drawString(qrText, x, y);

            g2d.dispose();

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            System.err.println("Failed to generate QR placeholder: " + e.getMessage());
            return null;
        }
    }

    /**
     * Save QR code to file (PLACEHOLDER VERSION)
     */
    public static void saveQRCodeToFile(String qrText, String filePath, int width, int height) {
        try {
            byte[] imageBytes = generateQRImageBytes(qrText, width, height);
            if (imageBytes != null) {
                java.nio.file.Files.write(
                        java.nio.file.Paths.get(filePath),
                        imageBytes
                );
                System.out.println("QR Code placeholder saved to: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to save QR code: " + e.getMessage());
        }
    }

    /**
     * Generate printable employee badge data
     */
    public static String generateBadgeText(String employeeName, String qrCode,
                                           String position, String department) {
        return String.format(
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "   COMPANY NAME\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "  %s\n" +
                        "  %s\n" +
                        "  %s Department\n\n" +
                        "  QR Code: %s\n" +
                        "  (Type this for attendance)\n\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━\n",
                employeeName, position, department, qrCode
        );
    }
}