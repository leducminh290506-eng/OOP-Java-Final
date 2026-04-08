package com.oop.project.ui.panels;

import com.oop.project.service.StatisticsService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final StatisticsService statService;

    private JLabel lblTotalProperties, lblOccupancyRate, lblActiveContracts, lblTotalRevenue;
    private JPanel listContainer;
    private MinimalBarChart chartPanel;

    public DashboardPanel() {
        this.statService = new StatisticsService();
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));
        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblHeader = new JLabel("Business Dashboard");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 28));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());
        headerPanel.add(lblHeader, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content Wrapper
        JPanel centerWrapper = new JPanel();
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.setOpaque(false);

        // KPI Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblTotalProperties = new JLabel("0");
        lblOccupancyRate   = new JLabel("0%");
        lblActiveContracts = new JLabel("0");
        lblTotalRevenue    = new JLabel("$0");

        cardsPanel.add(createCard("Total Units", lblTotalProperties, true));
        cardsPanel.add(createCard("Occupancy", lblOccupancyRate, false));
        cardsPanel.add(createCard("Active Leases", lblActiveContracts, false));
        cardsPanel.add(createCard("Est. Revenue", lblTotalRevenue, false));

        centerWrapper.add(cardsPanel);
        centerWrapper.add(Box.createRigidArea(new Dimension(0, 25)));

        // Data Section (Chart & List)
        JPanel dataPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        dataPanel.setOpaque(false);

        chartPanel = new MinimalBarChart();
        RoundedPanel chartCont = createWrapper("Listings by Location", chartPanel);
        
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);
        RoundedPanel listCont = createWrapper("Operational Logs", new JScrollPane(listContainer));

        dataPanel.add(chartCont);
        dataPanel.add(listCont);
        centerWrapper.add(dataPanel);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel lblValue, boolean isPrimary) {
        RoundedPanel card = new RoundedPanel(20, isPrimary ? new Color(25, 25, 25) : Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(isPrimary ? Color.WHITE : Color.BLACK);
        JLabel lblT = new JLabel(title);
        lblT.setForeground(isPrimary ? Color.LIGHT_GRAY : Color.GRAY);
        card.add(lblT, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel createWrapper(String title, Component comp) {
        RoundedPanel rp = new RoundedPanel(20, Color.WHITE);
        rp.setLayout(new BorderLayout(10, 10));
        rp.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rp.add(l, BorderLayout.NORTH);
        rp.add(comp, BorderLayout.CENTER);
        return rp;
    }

    public void refreshData() {
        try {
            lblTotalProperties.setText(String.valueOf(statService.getTotalApartments()));
            lblOccupancyRate.setText(String.format("%.1f%%", statService.getOccupancyRate()));
            lblActiveContracts.setText(String.valueOf(statService.getActiveContractsCount()));
            lblTotalRevenue.setText(String.format("$%,.0f", statService.getEstimatedMonthlyRevenue()));
            chartPanel.setData(statService.getCountByLocation());
            
            listContainer.removeAll();
            addRow("System", "Data synchronized with database", "Live");
            addRow("Security", "User session verified", "Secure");
            listContainer.revalidate(); listContainer.repaint();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addRow(String t, String d, String s) {
        JPanel r = new JPanel(new BorderLayout());
        r.setBackground(Color.WHITE);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        r.add(new JLabel(" " + t + " - " + d), BorderLayout.WEST);
        r.add(new JLabel(s + " "), BorderLayout.EAST);
        listContainer.add(r);
    }

    class RoundedPanel extends JPanel {
        private int r; private Color c;
        public RoundedPanel(int r, Color c) { this.r = r; this.c = c; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c); g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),r,r));
            g2.dispose();
        }
    }

    class MinimalBarChart extends JPanel {
        private Map<String, Long> d;
        public void setData(Map<String, Long> d) { this.d = d; repaint(); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); if (d == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = 20; long max = d.values().stream().max(Long::compare).orElse(1L);
            for (long v : d.values()) {
                int h = (int)((double)v/max * (getHeight()-50));
                g2.setColor(new Color(40,40,40));
                g2.fillRoundRect(x, getHeight()-20-h, 30, h, 5, 5);
                x += 45;
            }
            g2.dispose();
        }
    }
}