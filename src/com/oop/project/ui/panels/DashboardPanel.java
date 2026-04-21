package com.oop.project.ui.panels;

import com.oop.project.service.StatisticsService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardPanel extends JPanel {

    // ── App color palette (matches sidebar) ──────────────────────────────────
    private static final Color BG            = new Color(236, 240, 244);   // light grey page bg
    private static final Color SIDEBAR_DARK  = new Color(44,  62,  80);    // #2C3E50
    private static final Color ACCENT_BLUE   = new Color(52, 152, 219);    // #3498DB
    private static final Color ACCENT_GREEN  = new Color(39, 174, 96);     // #27AE60
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);    // #E67E22
    private static final Color ACCENT_PURPLE = new Color(142, 68, 173);    // #8E44AD
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_PRIMARY  = new Color(44,  62,  80);
    private static final Color TEXT_MUTED    = new Color(127, 140, 141);

    private final StatisticsService statService;

    private JLabel lblTotalProperties, lblOccupancyRate, lblActiveContracts,
                    lblTotalRevenue, lblAvgPrice, lblFavorites;
    private JPanel activityContainer;
    private ColoredBarChart chartPanel;

    public DashboardPanel() {
        this.statService = new StatisticsService();
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        initComponents();
        refreshData();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BUILD UI
    // ─────────────────────────────────────────────────────────────────────────
    private void initComponents() {

        /* ── Header ── */
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        /* ── Body (scrollable) ── */
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // KPI row: 2 rows x 3 columns (FR-5.4)
        JPanel kpiRow = new JPanel(new GridLayout(2, 3, 16, 12));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        lblTotalProperties = new JLabel("–");
        lblOccupancyRate   = new JLabel("–");
        lblActiveContracts = new JLabel("–");
        lblTotalRevenue    = new JLabel("–");
        lblAvgPrice        = new JLabel("–");
        lblFavorites       = new JLabel("–");

        kpiRow.add(buildKpiCard("Total Units",     lblTotalProperties, ACCENT_BLUE));
        kpiRow.add(buildKpiCard("Occupancy Rate",  lblOccupancyRate,   ACCENT_GREEN));
        kpiRow.add(buildKpiCard("Active Leases",   lblActiveContracts, ACCENT_ORANGE));
        kpiRow.add(buildKpiCard("Monthly Revenue", lblTotalRevenue,    ACCENT_PURPLE));
        kpiRow.add(buildKpiCard("Avg Rent Price",  lblAvgPrice,        new Color(231, 76, 60)));
        kpiRow.add(buildKpiCard("Total Favorites", lblFavorites,       new Color(26, 188, 156)));

        body.add(kpiRow);
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        // -- Bottom section: Chart + Activity Log --
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomRow.setOpaque(false);

        chartPanel = new ColoredBarChart();
        bottomRow.add(buildSection("Listings by Location", chartPanel));

        activityContainer = new JPanel();
        activityContainer.setLayout(new BoxLayout(activityContainer, BoxLayout.Y_AXIS));
        activityContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(activityContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        bottomRow.add(buildSection("Recent Activity", scroll));

        body.add(bottomRow);

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time snapshot of your property portfolio");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);

        // Refresh button styled to match app accent
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(ACCENT_BLUE);
        btnRefresh.setBorder(new EmptyBorder(9, 20, 9, 20));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnRefresh.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnRefresh.setBackground(ACCENT_BLUE);
            }
        });
        btnRefresh.addActionListener(e -> refreshData());

        // Wrap button in a panel with vertical centering
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnRefresh);

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /** A colored accent KPI card matching the app theme */
    private JPanel buildKpiCard(String title, JLabel valLabel, Color accentColor) {
        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Colored top strip
        JPanel strip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                // Round only top corners
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 16, 16, 16);
                g2.dispose();
            }
        };
        strip.setPreferredSize(new Dimension(0, 6));
        strip.setOpaque(false);

        // Text area
        JPanel body = new JPanel(new GridLayout(3, 1, 0, 4));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLabel.setForeground(TEXT_PRIMARY);

        // Small colour dot as visual cue
        JLabel dot = new JLabel("● LIVE");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 10));
        dot.setForeground(accentColor);

        body.add(lblTitle);
        body.add(valLabel);
        body.add(dot);

        card.add(strip, BorderLayout.NORTH);
        card.add(body,  BorderLayout.CENTER);
        return card;
    }

    /** White rounded section wrapper with a title */
    private RoundedPanel buildSection(String title, Component content) {
        RoundedPanel rp = new RoundedPanel(16, CARD_BG);
        rp.setLayout(new BorderLayout(0, 12));
        rp.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT_PRIMARY);

        // Thin accent underline
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 225, 230));

        titleBar.add(lbl, BorderLayout.WEST);

        JPanel titled = new JPanel(new BorderLayout(0, 8));
        titled.setOpaque(false);
        titled.add(titleBar, BorderLayout.NORTH);
        titled.add(sep,      BorderLayout.CENTER);

        rp.add(titled,  BorderLayout.NORTH);
        rp.add(content, BorderLayout.CENTER);
        return rp;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DATA REFRESH
    // ─────────────────────────────────────────────────────────────────────────
    public void refreshData() {
        try {
            int total    = statService.getTotalApartments();
            double occ   = statService.getOccupancyRate();
            int active   = statService.getActiveContractsCount();
            double rev   = statService.getEstimatedMonthlyRevenue();

            lblTotalProperties.setText(String.valueOf(total));
            lblOccupancyRate  .setText(String.format("%.1f%%", occ));
            lblActiveContracts.setText(String.valueOf(active));
            lblTotalRevenue   .setText(String.format("$%,.0f", rev));
            lblAvgPrice       .setText(String.format("$%,.0f", statService.getAveragePrice()));
            lblFavorites      .setText(String.valueOf(statService.getFavoritesCount()));

            chartPanel.setData(statService.getCountByLocation());

            // Activity log
            activityContainer.removeAll();
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            addActivity("Data Synced",        "Database refreshed successfully",   now, ACCENT_GREEN);
            addActivity("Portfolio Overview",  total + " units tracked in system",  now, ACCENT_BLUE);
            addActivity("Active Contracts",    active + " leases currently running", now, ACCENT_ORANGE);
            addActivity("Revenue Snapshot",    String.format("$%,.0f / month", rev), now, ACCENT_PURPLE);
            addActivity("Session Active",      "Authenticated user logged in",       now, SIDEBAR_DARK);
            activityContainer.revalidate();
            activityContainer.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addActivity(String title, String detail, String time, Color badge) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setBorder(new EmptyBorder(8, 4, 8, 4));

        // Color dot accent
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badge);
                int d = 8;
                g2.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(20, 20));
        dot.setOpaque(false);

        // Text block
        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_PRIMARY);
        JLabel lblDetail = new JLabel(detail);
        lblDetail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDetail.setForeground(TEXT_MUTED);
        text.add(lblTitle);
        text.add(lblDetail);

        // Timestamp
        JLabel lblTime = new JLabel(time);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(TEXT_MUTED);

        row.add(dot, BorderLayout.WEST);
        row.add(text,      BorderLayout.CENTER);
        row.add(lblTime,   BorderLayout.EAST);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(236, 240, 241));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);

        activityContainer.add(wrapper);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INNER: Rounded panel
    // ─────────────────────────────────────────────────────────────────────────
    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Subtle drop shadow
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fill(new RoundRectangle2D.Double(2, 3, getWidth()-3, getHeight()-3, radius, radius));
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INNER: Horizontal Bar Chart (clean & spacious)
    // ─────────────────────────────────────────────────────────────────────────
    class ColoredBarChart extends JPanel {
        private Map<String, Long> data = new LinkedHashMap<>();

        private final Color[] COLORS = {
            ACCENT_BLUE, ACCENT_GREEN, ACCENT_ORANGE, ACCENT_PURPLE,
            new Color(231, 76, 60), new Color(26, 188, 156),
            new Color(52, 73, 94),  new Color(241, 196, 15)
        };

        ColoredBarChart() { setOpaque(false); }

        void setData(Map<String, Long> d) {
            this.data = (d == null) ? new LinkedHashMap<>() : d;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setColor(TEXT_MUTED);
                g.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                g.drawString("No data available", 20, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Sort descending, cap at 8 entries
            java.util.List<Map.Entry<String, Long>> entries = data.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .collect(java.util.stream.Collectors.toList());

            int n      = entries.size();
            int padL   = 145;  // left column for location labels
            int padR   = 48;   // right margin for value labels
            int padT   = 6;
            int padB   = 6;

            int totalH = getHeight() - padT - padB;
            int rowH   = n > 0 ? totalH / n : 0;
            int barH   = (int) (rowH * 0.46);
            int barGap = (rowH - barH) / 2;
            int chartW = getWidth() - padL - padR;
            long max   = entries.get(0).getValue();

            for (int i = 0; i < n; i++) {
                Map.Entry<String, Long> e = entries.get(i);
                String label = e.getKey();
                long   val   = e.getValue();
                Color  bar   = COLORS[i % COLORS.length];

                int y  = padT + i * rowH + barGap;
                int bw = max > 0 ? (int) ((double) val / max * chartW) : 0;

                // Background track (pill shape)
                g2.setColor(new Color(236, 240, 241));
                g2.fillRoundRect(padL, y, chartW, barH, barH, barH);

                // Filled gradient bar
                if (bw > barH) {
                    GradientPaint gp = new GradientPaint(
                        padL,      y,        bar.brighter(),
                        padL + bw, y + barH, bar
                    );
                    g2.setPaint(gp);
                    g2.fillRoundRect(padL, y, bw, barH, barH, barH);
                }

                // Location name — right-aligned inside padL column
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                String lbl = label;
                while (fm.stringWidth(lbl) > padL - 12 && lbl.length() > 4) {
                    lbl = lbl.substring(0, lbl.length() - 1);
                }
                if (!lbl.equals(label)) lbl = lbl.trim() + "…";
                int textY = y + (barH + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(lbl, padL - 10 - fm.stringWidth(lbl), textY);

                // Value label after bar end
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(bar.darker());
                g2.drawString(String.valueOf(val), padL + bw + 8, textY);
            }

            g2.dispose();
        }
    }
}