package com.example.utang.models;
import com.example.utang.R;

public enum DebtStatus {
    PENDING("Pending"),
    OVERDUE("Overdue"),
    PAID("Paid"),
    PARTIAL("Partial");

    private final String displayName;

    DebtStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorResource() {
        switch (this) {
            case OVERDUE:
                return R.color.status_overdue;
            case PENDING:
                return R.color.status_pending;
            case PAID:
                return R.color.status_paid;
            case PARTIAL:
                return R.color.status_pending; // Same as pending
            default:
                return R.color.text_secondary;
        }
    }

    public int getBackgroundResource() {
        switch (this) {
            case OVERDUE:
                return R.drawable.status_overdue_bg;
            case PENDING:
                return R.drawable.status_pending_bg;
            case PAID:
                return R.drawable.status_paid_bg;
            case PARTIAL:
                return R.drawable.status_pending_bg;
            default:
                return R.drawable.button_outline;
        }
    }
}