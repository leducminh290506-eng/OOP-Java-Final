package com.oop.project.repository;

import com.oop.project.exception.DatabaseException;
import com.oop.project.model.Note;
import com.oop.project.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections; // Import thêm cái này
import java.util.List;
import java.util.Optional;

public class NoteRepository implements IRepository<Note, Integer> {
    
    @Override
    public void save(Note entity) {
        String sql = "INSERT INTO notes (apartment_id, content) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entity.getApartmentId());
            stmt.setString(2, entity.getContent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error saving note", e);
        }
    }

    public List<Note> findByApartmentId(int aptId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, aptId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(new Note(rs.getInt("id"), rs.getInt("apartment_id"), rs.getString("content")));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding notes", e);
        }
        return notes;
    }

    @Override
    public void update(Note entity) {}
    @Override
    public void delete(Integer id) {}
    @Override
    public Optional<Note> findById(Integer id) { return Optional.empty(); }
    
    @Override
    public List<Note> findAll() { 
        // Thay List.of() bằng Collections.emptyList() để chạy trên Java 8
        return Collections.emptyList(); 
    }
}