package models;

/**
 * Model for payroll adjustments with full audit trail
 */
public class PayrollAdjustment {
    private int id;
    private Integer payrollId; // Can be null for pending adjustments
    private int employeeId;
    private String adjustmentType; // 'bonus', 'deduction', 'overtime', 'allowance'
    private double amount;
    private String reason;
    private int addedBy; // User ID who created adjustment
    private Integer approvedBy; // User ID who approved (null = pending)
    private String dateAdded;
    private String dateApproved;
    private String status; // 'pending', 'approved', 'rejected'
    private String notes;

    // Constructor for NEW adjustments (no ID, no approval yet)
    public PayrollAdjustment(int employeeId, String adjustmentType, double amount,
                             String reason, int addedBy, String dateAdded) {
        this.employeeId = employeeId;
        this.adjustmentType = adjustmentType;
        this.amount = amount;
        this.reason = reason;
        this.addedBy = addedBy;
        this.dateAdded = dateAdded;
        this.status = "pending";
    }

    // Constructor for EXISTING adjustments from database
    public PayrollAdjustment(int id, Integer payrollId, int employeeId, String adjustmentType,
                             double amount, String reason, int addedBy, Integer approvedBy,
                             String dateAdded, String dateApproved, String status, String notes) {
        this.id = id;
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.adjustmentType = adjustmentType;
        this.amount = amount;
        this.reason = reason;
        this.addedBy = addedBy;
        this.approvedBy = approvedBy;
        this.dateAdded = dateAdded;
        this.dateApproved = dateApproved;
        this.status = status;
        this.notes = notes;
    }

    // Getters
    public int getId() { return id; }
    public Integer getPayrollId() { return payrollId; }
    public int getEmployeeId() { return employeeId; }
    public String getAdjustmentType() { return adjustmentType; }
    public double getAmount() { return amount; }
    public String getReason() { return reason; }
    public int getAddedBy() { return addedBy; }
    public Integer getApprovedBy() { return approvedBy; }
    public String getDateAdded() { return dateAdded; }
    public String getDateApproved() { return dateApproved; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setPayrollId(Integer payrollId) { this.payrollId = payrollId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setReason(String reason) { this.reason = reason; }
    public void setAddedBy(int addedBy) { this.addedBy = addedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }
    public void setDateApproved(String dateApproved) { this.dateApproved = dateApproved; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }

    // Helper methods
    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }

    public boolean isBonus() {
        return amount > 0;
    }

    public boolean isDeduction() {
        return amount < 0;
    }

    /**
     * Get formatted amount with sign
     */
    public String getFormattedAmount() {
        if (amount >= 0) {
            return String.format("+₱%,.2f", amount);
        } else {
            return String.format("-₱%,.2f", Math.abs(amount));
        }
    }

    /**
     * Get type emoji
     */
    public String getTypeEmoji() {
        switch (adjustmentType.toLowerCase()) {
            case "bonus": return "💰";
            case "deduction": return "📉";
            case "overtime": return "⏰";
            case "allowance": return "🎁";
            case "leave": return "🏖️";
            default: return "📝";
        }
    }

    /**
     * Get status emoji
     */
    public String getStatusEmoji() {
        switch (status.toLowerCase()) {
            case "approved": return "✅";
            case "pending": return "⏳";
            case "rejected": return "❌";
            default: return "❓";
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s: %s - %s (%s)",
                getTypeEmoji(),
                adjustmentType,
                getFormattedAmount(),
                reason,
                getStatusEmoji() + " " + status);
    }
}