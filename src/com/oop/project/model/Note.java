package com.oop.project.model;

public class Note {
    private int id;
    private int apartmentId;
    private String content;

    public Note(int id, int apartmentId, String content) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.content = content;
    }

    public int getId() { return id; }
    public int getApartmentId() { return apartmentId; }
    public String getContent() { return content; }
}