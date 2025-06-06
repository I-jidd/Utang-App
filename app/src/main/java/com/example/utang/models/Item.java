package com.example.utang.models;

public class Item {
    private String name;
    private double quantity;
    private double price;
    private String unit;

    public Item() {
        this.unit = "pcs";
    }

    public Item(String name, double quantity, double price, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.unit = unit != null ? unit : "pcs";
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getTotalPrice() {
        return quantity * price;
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
                quantity > 0 && price > 0;
    }

    @Override
    public String toString() {
        return name + " (" + quantity + unit + ") - ₱" + String.format("%.2f", price);
    }
}