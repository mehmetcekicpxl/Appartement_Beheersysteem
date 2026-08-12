package com.example.apartmanyonetim.models;

public class FileItem {
    private int id;
    private String uri;
    private String name;
    private String type;

    public FileItem(int id, String uri, String name, String type) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.type = type;
    }

    public int getId() { return id; }
    public String getUri() { return uri; }
    public String getName() { return name; }
    public String getType() { return type; }
}
