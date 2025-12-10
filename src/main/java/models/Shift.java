package models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model for employee work shifts in 24/7 hospital operation
 */
public class Shift {
    private int id;
    private String name;                    // "Day Shift", "Evening Shift", "Night Shift"
    private LocalTime startTime;            // e.g., 05:00:00
    private LocalTime endTime;              // e.g., 13:30:00
    private String description;
    private boolean isActive;

    // Constructor for new shifts (no ID)
    public Shift(String name, LocalTime startTime, LocalTime endTime, String description) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.isActive = true;
    }

    // Constructor for existing shifts (with ID)
    public Shift(int id, String name, LocalTime startTime, LocalTime endTime,
                 String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean active) { isActive = active; }

    /**
     * Check if a time-in is late for this shift
     * Late = any time after the shift start time
     */
    public boolean isLate(LocalTime timeIn) {
        // Handle night shift crossing midnight
        if (endTime.isBefore(startTime)) {
            // Night shift (e.g., 22:00 - 06:30)
            if (timeIn.isAfter(LocalTime.MIDNIGHT) && timeIn.isBefore(endTime)) {
                // Time-in is in the morning portion (00:00 - 06:30)
                // Check if it's before the end time
                return false; // On time if they're within the shift
            } else if (timeIn.isBefore(startTime) && timeIn.isAfter(endTime)) {
                // Time-in is between end and start (e.g., 07:00 - 21:59)
                // This is outside the shift entirely
                return true; // Mark as late
            } else {
                // Time-in is in the evening portion (22:00 - 23:59)
                return timeIn.isAfter(startTime);
            }
        } else {
            // Regular shift (start < end)
            return timeIn.isAfter(startTime);
        }
    }

    /**
     * Calculate total shift hours
     */
    public double getShiftHours() {
        if (endTime.isBefore(startTime)) {
            // Night shift crosses midnight
            long minutesToMidnight = java.time.Duration.between(startTime, LocalTime.MAX).toMinutes() + 1;
            long minutesFromMidnight = java.time.Duration.between(LocalTime.MIDNIGHT, endTime).toMinutes();
            return (minutesToMidnight + minutesFromMidnight) / 60.0;
        } else {
            // Regular shift
            return java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        }
    }

    /**
     * Format time for display
     */
    public String getFormattedStartTime() {
        return startTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getFormattedEndTime() {
        return endTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getShiftTimeRange() {
        return getFormattedStartTime() + " - " + getFormattedEndTime();
    }

    /**
     * Get shift type emoji
     */
    public String getShiftEmoji() {
        int hour = startTime.getHour();
        if (hour >= 5 && hour < 13) {
            return "☀️"; // Day shift
        } else if (hour >= 13 && hour < 22) {
            return "🌆"; // Evening shift
        } else {
            return "🌙"; // Night shift
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s)",
                getShiftEmoji(), name, getShiftTimeRange());
    }

    /**
     * Create default hospital shifts (NO GRACE PERIOD)
     */
    public static Shift[] getDefaultShifts() {
        return new Shift[] {
                new Shift(
                        "Day Shift",
                        LocalTime.of(5, 0),      // 05:00 AM
                        LocalTime.of(13, 30),    // 01:30 PM
                        "Early morning shift - Primary day operations"
                ),
                new Shift(
                        "Evening Shift",
                        LocalTime.of(13, 30),    // 01:30 PM
                        LocalTime.of(22, 0),     // 10:00 PM
                        "Afternoon/evening shift - Swing shift coverage"
                ),
                new Shift(
                        "Night Shift",
                        LocalTime.of(22, 0),     // 10:00 PM
                        LocalTime.of(6, 30),     // 06:30 AM (next day)
                        "Overnight shift - Graveyard shift, crosses midnight"
                )
        };
    }
}