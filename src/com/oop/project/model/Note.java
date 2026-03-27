package com.oop.project.model;

/**
 * note model to represent user notes on apartments
 * fields:
 *  - note_id
 *  - apartment_id
 *  - user_id
 *  - note_text
 */
public class Note {
    private int id;
    private int apartmentId;
    private int userId;
    private String noteText;

    public Note(int id, int apartmentId, int userId, String noteText) {
        this.id = id;
        this.apartmentId = apartmentId;
        this.userId = userId;
        this.noteText = noteText;
    }

    public int getId() {
        return id;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public int getUserId() {
        return userId;
    }

    public String getNoteText() {
        return noteText;
    }
}
