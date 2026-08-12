package com.example.apartmanyonetim.models;

// Modelklasse voor transacties (inkomsten en uitgaven)
public class Transaction {
    private int id;
    private Integer apartmentId; // Kan null zijn voor algemene uitgaven
    private String type; // "INCOME" of "EXPENSE"
    private String category; // Categorie
    private double amount; // Bedrag
    private String date; // Datum
    private boolean isPaid; // Betaald status
    private String description; // Omschrijving

    public Transaction() {
    }

    public Transaction(int id, Integer apartmentId, String type, String category, double amount, String date, boolean isPaid, String description) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.isPaid = isPaid;
        this.description = description;
    }

    // Getters en Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Integer apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
