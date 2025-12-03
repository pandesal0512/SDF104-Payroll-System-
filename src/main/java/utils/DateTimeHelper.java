package utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations
 */
public class DateTimeHelper {

    // Date formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    public static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Time formatters
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    public static final DateTimeFormatter TIME_12HR_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");

    /**
     * Get current date as string (yyyy-MM-dd)
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * Get current time as string (HH:mm:ss)
     */
    public static String getCurrentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    /**
     * Get current date in display format (e.g., "December 02, 2024")
     */
    public static String getCurrentDateDisplay() {
        return LocalDate.now().format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Get current time in 12-hour format (e.g., "08:30 AM")
     */
    public static String getCurrentTimeDisplay() {
        return LocalTime.now().format(DISPLAY_TIME_FORMATTER);
    }

    /**
     * Format date string for display
     */
    public static String formatDateForDisplay(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.format(DISPLAY_DATE_FORMATTER);
        } catch (Exception e) {
            return dateStr;
        }
    }

    /**
     * Format time string for display (24hr to 12hr)
     */
    public static String formatTimeForDisplay(String timeStr) {
        try {
            LocalTime time = LocalTime.parse(timeStr, TIME_FORMATTER);
            return time.format(DISPLAY_TIME_FORMATTER);
        } catch (Exception e) {
            return timeStr;
        }
    }

    /**
     * Check if time is late (after 8:30 AM)
     */
    public static boolean isLate(String timeInStr) {
        try {
            LocalTime timeIn = LocalTime.parse(timeInStr, TIME_FORMATTER);
            LocalTime cutoff = LocalTime.of(8, 30); // 8:30 AM
            return timeIn.isAfter(cutoff);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if time is late with custom cutoff
     */
    public static boolean isLate(String timeInStr, int cutoffHour, int cutoffMinute) {
        try {
            LocalTime timeIn = LocalTime.parse(timeInStr, TIME_FORMATTER);
            LocalTime cutoff = LocalTime.of(cutoffHour, cutoffMinute);
            return timeIn.isAfter(cutoff);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculate hours worked
     */
    public static double calculateHoursWorked(String timeInStr, String timeOutStr) {
        try {
            LocalTime timeIn = LocalTime.parse(timeInStr, TIME_FORMATTER);
            LocalTime timeOut = LocalTime.parse(timeOutStr, TIME_FORMATTER);

            long minutes = ChronoUnit.MINUTES.between(timeIn, timeOut);
            return minutes / 60.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Get month name from number (1-12)
     */
    public static String getMonthName(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "Invalid Month";
    }

    /**
     * Get month number from name
     */
    public static int getMonthNumber(String monthName) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i + 1;
            }
        }
        return 1; // Default to January
    }

    /**
     * Get current month number
     */
    public static int getCurrentMonth() {
        return LocalDate.now().getMonthValue();
    }

    /**
     * Get current year
     */
    public static int getCurrentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * Get days in month
     */
    public static int getDaysInMonth(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        return date.lengthOfMonth();
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.equals(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if date is in current month
     */
    public static boolean isCurrentMonth(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalDate now = LocalDate.now();
            return date.getYear() == now.getYear() &&
                    date.getMonthValue() == now.getMonthValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get date range for a month
     */
    public static String[] getMonthDateRange(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        return new String[] {
                firstDay.format(DATE_FORMATTER),
                lastDay.format(DATE_FORMATTER)
        };
    }

    /**
     * Parse date string safely
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse time string safely
     */
    public static LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}