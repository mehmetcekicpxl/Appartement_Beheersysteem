package com.example.apartmanyonetim.models;

public class Note {
    private int id;
    private int apartmentId;
    private String content;
    private String date;

    public Note(int id, int apartmentId, String content, String date) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.content = content;
        this.date = date;
    }

    public int getId() { return id; }
    public int getApartmentId() { return apartmentId; }
    public String getContent() { return content; }
    public String getDate() { return date; }
}
