import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class DisasterReliefGUI extends JFrame {

    private JLabel statusBar;

    public DisasterReliefGUI() {

        setTitle("Disaster Relief Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== HEADER =====
        JLabel header = new JLabel("DISASTER RELIEF MANAGEMENT SYSTEM", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setOpaque(true);
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(183, 28, 28));
        header.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(header, BorderLayout.NORTH);

        // ===== SIDEBAR MENU =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(8, 1, 10, 10));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setPreferredSize(new Dimension(250, 0));

        JButton eventBtn = createMenuButton("Disaster Events");
        JButton regionBtn = createMenuButton("Affected Regions");
        JButton campBtn = createMenuButton("Relief Camps");
        JButton volunteerBtn = createMenuButton("Volunteers");
        JButton resourceBtn = createMenuButton("Resources");
        JButton distributionBtn = createMenuButton("Distribution");
        JButton exitBtn = createMenuButton("Exit");

        sidebar.add(eventBtn);
        sidebar.add(regionBtn);
        sidebar.add(campBtn);
        sidebar.add(volunteerBtn);
        sidebar.add(resourceBtn);
        sidebar.add(distributionBtn);
        sidebar.add(exitBtn);

        add(sidebar, BorderLayout.WEST);

        // ===== MAIN WELCOME PANEL =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        JLabel welcome = new JLabel(
                "<html><center>Welcome to Disaster Relief Management System<br><br>Select a module from the left panel</center></html>",
                JLabel.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        centerPanel.add(welcome, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ===== STATUS BAR =====
        statusBar = new JLabel(" Ready");
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(230, 230, 230));
        add(statusBar, BorderLayout.SOUTH);

        // ===== BUTTON ACTIONS =====
        eventBtn.addActionListener(e -> openModule("disaster_event"));
        regionBtn.addActionListener(e -> openModule("affected_region"));
        campBtn.addActionListener(e -> openModule("relief_camp"));
        volunteerBtn.addActionListener(e -> openModule("volunteer"));
        resourceBtn.addActionListener(e -> openModule("resource"));
        distributionBtn.addActionListener(e -> handleDistribution());
        exitBtn.addActionListener(e -> System.exit(0));
    }

    // ===== STYLED BUTTON =====
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(198, 40, 40));
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ===== DATABASE CONNECTION =====
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/disaster_db",
                    "root",
                    "root123"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return null;
        }
    }

    // ===== MODULE WINDOW =====
    public void openModule(String tableName) {

        JFrame frame = new JFrame(tableName.toUpperCase() + " MANAGEMENT");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++)
                model.addColumn(meta.getColumnName(i));

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++)
                    row[i] = rs.getObject(i + 1);
                model.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error loading data: " + e.getMessage());
        }

        JPanel btnPanel = new JPanel();

        JButton addBtn = new JButton("Add");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        styleActionButton(addBtn, new Color(46, 125, 50));
        styleActionButton(deleteBtn, new Color(211, 47, 47));
        styleActionButton(refreshBtn, new Color(2, 136, 209));

        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            frame.dispose();
            openModule(tableName);
        });

        frame.setVisible(true);
        statusBar.setText(" Opened module: " + tableName);
    }

    private void styleActionButton(JButton btn, Color color) {
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
    }

    // ===== DISTRIBUTION FUNCTION (unchanged logic) =====
    public void handleDistribution() {
        try {
            int resourceId = Integer.parseInt(
                    JOptionPane.showInputDialog("Enter Resource ID:"));
            int campId = Integer.parseInt(
                    JOptionPane.showInputDialog("Enter Camp ID:"));
            int qty = Integer.parseInt(
                    JOptionPane.showInputDialog("Enter Quantity:"));

            Connection con = getConnection();

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE resource SET total_quantity = total_quantity - ? " +
                            "WHERE resource_id=? AND total_quantity>=?");
            pst.setInt(1, qty);
            pst.setInt(2, resourceId);
            pst.setInt(3, qty);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                PreparedStatement dist = con.prepareStatement(
                        "INSERT INTO resource_distribution(resource_id,camp_id,quantity_distributed,distribution_date) VALUES(?,?,?,CURDATE())");
                dist.setInt(1, resourceId);
                dist.setInt(2, campId);
                dist.setInt(3, qty);
                dist.executeUpdate();

                JOptionPane.showMessageDialog(this, "Distributed Successfully");
                statusBar.setText(" Distribution Completed");
            } else {
                JOptionPane.showMessageDialog(this, "Not Enough Stock");
                statusBar.setText(" Distribution Failed");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Distribution Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DisasterReliefGUI().setVisible(true);
    }
}