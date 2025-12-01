package utils;

public class QRCodeGenerator {

    /**
     * Generate simple employee code
     * Format: E + 3-digit number
     * Example: E001, E002, E003
     */
    public static String generateSimpleCode(int employeeId) {
        return String.format("E%03d", employeeId);
    }

    /**
     * Generate detailed QR code text
     * Format: EMP-YEAR-ID-FIRSTNAME
     * Example: EMP-2024-001-JUAN
     */
    public static String generateQRCodeText(int employeeId, String firstName) {
        int year = java.time.Year.now().getValue();
        String formattedId = String.format("%03d", employeeId);
        String sanitizedName = firstName.toUpperCase().replaceAll("[^A-Z]", "");

        if (sanitizedName.length() > 10) {
            sanitizedName = sanitizedName.substring(0, 10);
        }

        return String.format("EMP-%d-%s-%s", year, formattedId, sanitizedName);
    }

    /**
     * Validate QR code format
     */
    public static boolean isValidQRCode(String qrCode) {
        if (qrCode == null || qrCode.trim().isEmpty()) {
            return false;
        }

        // Check for simple format (E001) or detailed format (EMP-2024-001-NAME)
        return qrCode.matches("E\\d{3}") || qrCode.matches("EMP-\\d{4}-\\d{3}-[A-Z]+");
    }
}