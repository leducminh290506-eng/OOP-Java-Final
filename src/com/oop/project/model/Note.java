package com.oop.project.model;

/**
 * Note - Ghi chú nội bộ cho từng căn hộ (FR-3.2, FR-3.4).
 * Mapping với bảng "notes" trong Database:
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
