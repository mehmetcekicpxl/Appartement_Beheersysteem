package com.example.apartmanyonetim.models;

// Modelklasse voor een huurder
public class Tenant {
    private int id;
    private int apartmentId; // Verwijzing naar het appartement
    private String fullName; // Volledige naam
    private int familySize;  // Aantal gezinsleden
    private String contractDate; // Contractdatum
    private int contractDurationMonths; // Contractduur in maanden

    public Tenant() {
    }

    public Tenant(int id, int apartmentId, String fullName, int familySize, String contractDate, int contractDurationMonths) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.fullName = fullName;
        this.familySize = familySize;
        this.contractDate = contractDate;
        this.contractDurationMonths = contractDurationMonths;
    }

    // Getters en Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getFamilySize() {
        return familySize;
    }

    public void setFamilySize(int familySize) {
        this.familySize = familySize;
    }

    public String getContractDate() {
        return contractDate;
    }

    public void setContractDate(String contractDate) {
        this.contractDate = contractDate;
    }

    public int getContractDurationMonths() {
        return contractDurationMonths;
    }

    public void setContractDurationMonths(int contractDurationMonths) {
        this.contractDurationMonths = contractDurationMonths;
    }
}
