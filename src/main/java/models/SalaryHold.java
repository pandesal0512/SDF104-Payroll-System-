package models;

/**
 * Model for salary hold feature
 */
public class SalaryHold {
    private int id;
    private int employeeId;
    private String reason;
    private String holdDate;
    private String releaseDate;
    private int heldBy; // User ID who held salary
    private Integer releasedBy; // User ID who released (null = still held)
    private String status; // 'active', 'released'
    private String notes;
    private String createdAt;

    // Constructor for NEW holds (no ID, no release yet)
    public SalaryHold(int employeeId, String reason, String holdDate,
                      int heldBy, String notes) {
        this.employeeId = employeeId;
        this.reason = reason;
        this.holdDate = holdDate;
        this.heldBy = heldBy;
        this.notes = notes;
        this.status = "active";
    }

    // Constructor for EXISTING holds from database
    public SalaryHold(int id, int employeeId, String reason, String holdDate,
                      String releaseDate, int heldBy, Integer releasedBy,
                      String status, String notes, String createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.reason = reason;
        this.holdDate = holdDate;
        this.releaseDate = releaseDate;
        this.heldBy = heldBy;
        this.releasedBy = releasedBy;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public String getReason() { return reason; }
    public String getHoldDate() { return holdDate; }
    public String getReleaseDate() { return releaseDate; }
    public int getHeldBy() { return heldBy; }
    public Integer getReleasedBy() { return releasedBy; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setReason(String reason) { this.reason = reason; }
    public void setHoldDate(String holdDate) { this.holdDate = holdDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setHeldBy(int heldBy) { this.heldBy = heldBy; }
    public void setReleasedBy(Integer releasedBy) { this.releasedBy = releasedBy; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean isReleased() {
        return "released".equals(status);
    }

    /**
     * Release this hold
     */
    public void release(int releasedByUserId, String releaseDate) {
        this.releasedBy = releasedByUserId;
        this.releaseDate = releaseDate;
        this.status = "released";
    }

    /**
     * Get status emoji
     */
    public String getStatusEmoji() {
        return isActive() ? "🔒" : "🔓";
    }

    /**
     * Get hold duration in days (if still active)
     */
    public long getHoldDurationDays() {
        try {
            java.time.LocalDate hold = java.time.LocalDate.parse(holdDate);
            java.time.LocalDate end = releaseDate != null ?
                    java.time.LocalDate.parse(releaseDate) :
                    java.time.LocalDate.now();
            return java.time.temporal.ChronoUnit.DAYS.between(hold, end);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        if (isActive()) {
            return String.format("🔒 ACTIVE HOLD - %s (since %s)", reason, holdDate);
        } else {
            return String.format(" Released - %s (held: %s to %s)",
                    reason, holdDate, releaseDate);
        }
    }
}