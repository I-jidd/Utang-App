package com.example.utang.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Debt {
    private String id;
    private String customerName;
    private double amount;
    private double paidAmount;
    private String description;
    private Date dueDate;
    private Date dateAdded;
    private DebtStatus status;
    private List<Item> items;

    public Debt() {
        this.items = new ArrayList<>();
        this.dateAdded = new Date();
        this.paidAmount = 0.0;
    }

    public Debt(String id, String customerName, double amount, String description, Date dueDate, DebtStatus status) {
        this();
        this.id = id;
        this.customerName = customerName;
        this.amount = amount;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;

        // Auto-determine status if not set
        if (this.status == DebtStatus.PENDING && dueDate.before(new Date())) {
            this.status = DebtStatus.OVERDUE;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
        updateStatus();
    }

    public double getRemainingAmount() {
        return amount - paidAmount;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
        updateStatus();
    }

    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }

    public DebtStatus getStatus() { return status; }
    public void setStatus(DebtStatus status) { this.status = status; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public void addItem(Item item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public void removeItem(Item item) {
        if (this.items != null) {
            this.items.remove(item);
        }
    }

    public boolean isOverdue() {
        return dueDate.before(new Date()) && status != DebtStatus.PAID;
    }

    public boolean isPaid() {
        return status == DebtStatus.PAID || paidAmount >= amount;
    }

    public boolean isPartiallyPaid() {
        return paidAmount > 0 && paidAmount < amount;
    }

    private void updateStatus() {
        if (paidAmount >= amount) {
            status = DebtStatus.PAID;
        } else if (paidAmount > 0) {
            status = DebtStatus.PARTIAL;
        } else if (dueDate.before(new Date())) {
            status = DebtStatus.OVERDUE;
        } else {
            status = DebtStatus.PENDING;
        }
    }

    public String getItemsPreview() {
        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder preview = new StringBuilder();
        int displayCount = Math.min(3, items.size());

        for (int i = 0; i < displayCount; i++) {
            Item item = items.get(i);
            if (i > 0) preview.append(", ");
            preview.append(item.getName());
            if (item.getQuantity() > 1) {
                preview.append(" (").append(item.getQuantity()).append(item.getUnit()).append(")");
            }
        }

        if (items.size() > 3) {
            preview.append(" +").append(items.size() - 3).append(" more");
        }

        return preview.toString();
    }

    public double getTotalItemsValue() {
        if (items == null) return 0.0;

        double total = 0.0;
        for (Item item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
}