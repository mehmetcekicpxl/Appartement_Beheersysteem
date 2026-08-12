package com.example.apartmanyonetim.models;

// Modelklasse voor een appartement
public class Apartment {
    private int id;
    private int doorNumber; // Deurnummer
    private int floor;      // Verdieping
    private double currentBalance; // Huidig saldo (positief = tegoed, negatief = schuld)
    private String tenantName; // Naam van de huurder
    private double rentAmount; // Huurbedrag
    private int residentCount; // Aantal bewoners (Nieuw v4)
    private String address;    // Adres (Nieuw v4)

    // Lege constructor
    public Apartment() {
    }

    // Constructor met parameters
    public Apartment(int id, int doorNumber, int floor, double currentBalance, String tenantName, double rentAmount) {
        this(id, doorNumber, floor, currentBalance, tenantName, rentAmount, 0, "");
    }

    // Uitgebreide constructor
    public Apartment(int id, int doorNumber, int floor, double currentBalance, String tenantName, double rentAmount, int residentCount, String address) {
        this.id = id;
        this.doorNumber = doorNumber;
        this.floor = floor;
        this.currentBalance = currentBalance;
        this.tenantName = tenantName;
        this.rentAmount = rentAmount;
        this.residentCount = residentCount;
        this.address = address;
    }

    // Getters en Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDoorNumber() {
        return doorNumber;
    }

    public void setDoorNumber(int doorNumber) {
        this.doorNumber = doorNumber;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public int getResidentCount() {
        return residentCount;
    }

    public void setResidentCount(int residentCount) {
        this.residentCount = residentCount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
