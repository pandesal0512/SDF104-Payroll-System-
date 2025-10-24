package utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    public static boolean isLate(String timeIn, String cutoffTime) {
        LocalTime arrival = LocalTime.parse(timeIn, TIME_FORMATTER);
        LocalTime cutoff = LocalTime.parse(cutoffTime, TIME_FORMATTER);
        return arrival.isAfter(cutoff);
    }

    public static String determineStatus(String timeIn) {
        String cutoffTime = "09:00:00"; // 9 AM cutoff
        return isLate(timeIn, cutoffTime) ? "Late" : "On-time";
    }

    public static int calculateWorkDays(int month, int year) {
        // Simple calculation - you can make this more sophisticated
        return 22; // Average work days per month
    }
}