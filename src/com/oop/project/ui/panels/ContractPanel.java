package com.oop.project.ui.panels;

import com.oop.project.model.Apartment;
import com.oop.project.model.LeaseContract;
import com.oop.project.model.User;
import com.oop.project.service.ApartmentService;
import com.oop.project.repository.ContractRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ContractPanel extends JPanel {
    private final User currentUser;
    private final ApartmentService apartmentService;
    private final ContractRepository contractRepo;

    private JTable table;
    private DefaultTableModel tableModel;

    public ContractPanel(User currentUser, ApartmentService apartmentService) {
        this.currentUser = currentUser;
        this.apartmentService = apartmentService;
        this.contractRepo = new ContractRepository();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        initComponents();
        loadDataToTable();
    }

    private void initComponents() {
        // ── Action bar ───────────────────────────────────────────────────────
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionPanel.setOpaque(false);

        JButton btnAdd    = styledBtn("Create New Contract", new Color(46, 204, 113));
        JButton btnDetail = styledBtn("View Detail",         new Color(52, 152, 219));
        JButton btnDelete = styledBtn("Delete",              new Color(231, 76, 60));

        btnAdd   .addActionListener(e -> showAddContractForm());
        btnDetail.addActionListener(e -> showDetail());
        btnDelete.addActionListener(e -> deleteSelected());

        actionPanel.add(btnAdd);
        actionPanel.add(btnDetail);
        actionPanel.add(btnDelete);

        // ── Table ────────────────────────────────────────────────────────────
        String[] columns = {"Contract ID", "Apartment ID", "Customer Name", "Start Date", "End Date", "Monthly Rent"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(table);

        add(actionPanel, BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
    }

    // ── Load data ─────────────────────────────────────────────────────────────
    private void loadDataToTable() {
        tableModel.setRowCount(0);
        try {
            for (LeaseContract c : contractRepo.findAll()) {
                tableModel.addRow(new Object[]{
                    c.getContractId(), c.getApartmentId(), c.getCustomerName(),
                    c.getStartDate(), c.getEndDate(), "$" + c.getMonthlyRent()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading contracts: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── View Detail ───────────────────────────────────────────────────────────
    private void showDetail() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a contract first."); return; }
        int modelRow = table.convertRowIndexToModel(row);

        String contractId    = tableModel.getValueAt(modelRow, 0).toString();
        String apartmentId   = tableModel.getValueAt(modelRow, 1).toString();
        String customerName  = tableModel.getValueAt(modelRow, 2).toString();
        String startDate     = tableModel.getValueAt(modelRow, 3).toString();
        String endDate       = tableModel.getValueAt(modelRow, 4).toString();
        String monthlyRent   = tableModel.getValueAt(modelRow, 5).toString();

        try {
            LeaseContract contract = contractRepo.findById(Integer.parseInt(contractId));
            String notesMsg = (contract != null && contract.getNotes() != null && !contract.getNotes().isEmpty()) 
                                ? contract.getNotes() : "No notes";
            
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.add(bold("Contract ID:"));    panel.add(new JLabel(contractId));
            panel.add(bold("Apartment ID:"));   panel.add(new JLabel(apartmentId));
            panel.add(bold("Customer Name:"));  panel.add(new JLabel(customerName));
            panel.add(bold("Start Date:"));     panel.add(new JLabel(startDate));
            panel.add(bold("End Date:"));       panel.add(new JLabel(endDate));
            panel.add(bold("Monthly Rent:"));   panel.add(new JLabel(monthlyRent));
            panel.add(bold("Notes:"));          panel.add(new JLabel(notesMsg));

            JOptionPane.showMessageDialog(this, panel, "Contract Detail #" + contractId, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a contract to delete."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        int contractId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete contract #" + contractId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                contractRepo.delete(contractId);
                loadDataToTable();
                JOptionPane.showMessageDialog(this, "Contract deleted.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Create form ───────────────────────────────────────────────────────────
    private void showAddContractForm() {
        JComboBox<String> cbApartments = new JComboBox<>();
        List<Apartment> availableApts = apartmentService.findAll();
        if (availableApts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No apartments available.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (Apartment apt : availableApts) {
            cbApartments.addItem(apt.getId() + " - " + apt.getListingCode() + " (" + apt.getAddress() + ")");
        }

        JTextField txtCustomerName = new JTextField();
        JTextField txtStartDate    = new JTextField(LocalDate.now().toString());
        JTextField txtEndDate      = new JTextField(LocalDate.now().plusYears(1).toString());
        JTextField txtRent         = new JTextField();
        JTextField txtNotes        = new JTextField();

        // Auto filter price logic
        cbApartments.addActionListener(e -> {
            int idx = cbApartments.getSelectedIndex();
            if (idx >= 0 && idx < availableApts.size()) {
                txtRent.setText(String.valueOf(availableApts.get(idx).getPrice()));
            }
        });
        if (!availableApts.isEmpty()) {
            txtRent.setText(String.valueOf(availableApts.get(0).getPrice()));
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Select Apartment:"));         panel.add(cbApartments);
        panel.add(new JLabel("Customer Name:"));            panel.add(txtCustomerName);
        panel.add(new JLabel("Start Date (YYYY-MM-DD):")); panel.add(txtStartDate);
        panel.add(new JLabel("End Date (YYYY-MM-DD):"));   panel.add(txtEndDate);
        panel.add(new JLabel("Monthly Rent ($):"));         panel.add(txtRent);
        panel.add(new JLabel("Notes:"));                    panel.add(txtNotes);

        if (JOptionPane.showConfirmDialog(this, panel, "Create Lease Contract",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                String selected = (String) cbApartments.getSelectedItem();
                int aptId = Integer.parseInt(selected.split(" - ")[0]);
                String customerName = txtCustomerName.getText().trim();
                if (customerName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Customer name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LocalDate startDate = LocalDate.parse(txtStartDate.getText().trim());
                LocalDate endDate   = LocalDate.parse(txtEndDate.getText().trim());
                double rent = Double.parseDouble(txtRent.getText().trim());
                String notes = txtNotes.getText().trim();

                contractRepo.save(new LeaseContract(0, aptId, customerName, startDate, endDate, rent, notes));
                loadDataToTable();
                JOptionPane.showMessageDialog(this, "Contract created successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private JLabel bold(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }
}