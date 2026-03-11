package com.oop.project.ui.panels;

import com.oop.project.model.Note;
import com.oop.project.repository.NoteRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * NotesDialog - Quản lý ghi chú (FR-3.4): xem/sửa/xóa.
 */
public class NotesDialog extends JDialog {
    private final NoteRepository noteRepository;
    private final int apartmentId;
    private final int currentUserId;

    private final JTable table;
    private final DefaultTableModel model;

    public NotesDialog(Window owner, NoteRepository noteRepository, int apartmentId, int currentUserId) {
        super(owner, "Ghi chú căn hộ ID = " + apartmentId, ModalityType.APPLICATION_MODAL);
        this.noteRepository = noteRepository;
        this.apartmentId = apartmentId;
        this.currentUserId = currentUserId;

        setSize(700, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        model = new DefaultTableModel(new Object[]{"Note ID", "User ID", "Nội dung"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnClose = new JButton("Close");

        btnAdd.addActionListener(e -> addNote());
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadNotes());
        btnClose.addActionListener(e -> dispose());

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);
        actions.add(btnRefresh);
        actions.add(btnClose);
        add(actions, BorderLayout.SOUTH);

        loadNotes();
    }

    private void loadNotes() {
        model.setRowCount(0);
        List<Note> notes = noteRepository.findByApartmentId(apartmentId);
        for (Note n : notes) {
            model.addRow(new Object[]{n.getId(), n.getUserId(), n.getNoteText()});
        }
    }

    private Integer getSelectedNoteId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        return (v instanceof Integer) ? (Integer) v : Integer.parseInt(String.valueOf(v));
    }

    private void addNote() {
        JTextArea area = new JTextArea(8, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        int result = JOptionPane.showConfirmDialog(
                this, new JScrollPane(area), "Thêm ghi chú", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String text = area.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung ghi chú không được để trống!");
            return;
        }

        try {
            noteRepository.save(new Note(0, apartmentId, currentUserId, text));
            loadNotes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm ghi chú: " + ex.getMessage());
        }
    }

    private void editSelected() {
        Integer noteId = getSelectedNoteId();
        if (noteId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ghi chú để sửa!");
            return;
        }

        int row = table.getSelectedRow();
        String currentText = String.valueOf(model.getValueAt(row, 2));

        JTextArea area = new JTextArea(8, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(currentText);
        area.setCaretPosition(area.getText().length());

        int result = JOptionPane.showConfirmDialog(
                this, new JScrollPane(area), "Sửa ghi chú (ID = " + noteId + ")", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String updated = area.getText().trim();
        if (updated.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung ghi chú không được để trống!");
            return;
        }

        try {
            // apartmentId/userId giữ nguyên; chỉ update text
            noteRepository.update(new Note(noteId, apartmentId, currentUserId, updated));
            loadNotes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi sửa ghi chú: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        Integer noteId = getSelectedNoteId();
        if (noteId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ghi chú để xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Xóa ghi chú ID = " + noteId + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            noteRepository.delete(noteId);
            loadNotes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa ghi chú: " + ex.getMessage());
        }
    }
}

