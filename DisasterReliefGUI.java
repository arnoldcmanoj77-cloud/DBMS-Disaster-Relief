import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
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

        JLabel header = new JLabel("DISASTER RELIEF MANAGEMENT SYSTEM", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setOpaque(true);
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(183, 28, 28));
        header.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        JLabel welcome = new JLabel(
                "<html><center>Welcome to Disaster Relief Management System<br><br>Select a module from the left panel</center></html>",
                JLabel.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        centerPanel.add(welcome, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        JPanel sidebar = new JPanel(new GridLayout(7, 1, 10, 10));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setPreferredSize(new Dimension(250, 0));

        JButton eventBtn = createButton("Disaster Events");
        JButton regionBtn = createButton("Affected Regions");
        JButton campBtn = createButton("Relief Camps");
        JButton volunteerBtn = createButton("Volunteers");
        JButton resourceBtn = createButton("Resources");
        JButton distributionBtn = createButton("Distribution");
        JButton exitBtn = createButton("Exit");

        sidebar.add(eventBtn);
        sidebar.add(regionBtn);
        sidebar.add(campBtn);
        sidebar.add(volunteerBtn);
        sidebar.add(resourceBtn);
        sidebar.add(distributionBtn);
        sidebar.add(exitBtn);

        add(sidebar, BorderLayout.WEST);

        statusBar = new JLabel(" Ready");
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(230, 230, 230));
        add(statusBar, BorderLayout.SOUTH);

        eventBtn.addActionListener(e -> openModule("disaster_event"));
        regionBtn.addActionListener(e -> openModule("affected_region"));
        campBtn.addActionListener(e -> openModule("relief_camp"));
        volunteerBtn.addActionListener(e -> openModule("volunteer"));
        resourceBtn.addActionListener(e -> openModule("resource"));
        distributionBtn.addActionListener(e -> handleDistribution());
        exitBtn.addActionListener(e -> System.exit(0));
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(198, 40, 40));
        btn.setForeground(Color.WHITE);
        return btn;
    }

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

    public void openModule(String tableName) {

        JFrame frame = new JFrame(tableName.toUpperCase() + " MANAGEMENT");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);

        loadTableData(model, tableName);

        JButton addBtn = new JButton("Add");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        JPanel panel = new JPanel();
        panel.add(addBtn);
        panel.add(deleteBtn);
        panel.add(refreshBtn);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        // ADD
        addBtn.addActionListener(e -> {
            try {
                Connection con = getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SHOW COLUMNS FROM " + tableName);

                StringBuilder columns = new StringBuilder();
                StringBuilder placeholders = new StringBuilder();

                while (rs.next()) {
                    if (!rs.getString("Extra").contains("auto_increment")) {
                        columns.append(rs.getString("Field")).append(",");
                        placeholders.append("?,");
                    }
                }

                String colStr = columns.substring(0, columns.length() - 1);
                String placeStr = placeholders.substring(0, placeholders.length() - 1);

                String query = "INSERT INTO " + tableName +
                        " (" + colStr + ") VALUES (" + placeStr + ")";

                PreparedStatement pst = con.prepareStatement(query);

                String[] cols = colStr.split(",");
                for (int i = 0; i < cols.length; i++) {
                    String value = JOptionPane.showInputDialog(frame, "Enter " + cols[i] + ":");
                    pst.setString(i + 1, value);
                }

                pst.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Added Successfully");

                loadTableData(model, tableName);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Insert Error:\n" + ex.getMessage());
            }
        });

        // DELETE
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Select a row first!");
                return;
            }

            try {
                Connection con = getConnection();
                String idColumn = table.getColumnName(0);
                Object idValue = table.getValueAt(selectedRow, 0);

                String query = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setObject(1, idValue);

                pst.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Deleted Successfully");

                loadTableData(model, tableName);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Delete Error:\n" + ex.getMessage());
            }
        });

        refreshBtn.addActionListener(e -> loadTableData(model, tableName));

        frame.setVisible(true);
        statusBar.setText(" Opened: " + tableName);
    }

    private void loadTableData(DefaultTableModel model, String tableName) {
        try {
            model.setRowCount(0);
            model.setColumnCount(0);

            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + tableName);

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            for (int i = 1; i <= colCount; i++)
                model.addColumn(meta.getColumnName(i));

            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 0; i < colCount; i++)
                    row[i] = rs.getObject(i + 1);
                model.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    // ✅ FULL DISTRIBUTION LOGIC RESTORED
    public void handleDistribution() {
        try {
            int resourceId = Integer.parseInt(
                    JOptionPane.showInputDialog(this, "Enter Resource ID:"));
            int campId = Integer.parseInt(
                    JOptionPane.showInputDialog(this, "Enter Camp ID:"));
            int qty = Integer.parseInt(
                    JOptionPane.showInputDialog(this, "Enter Quantity:"));

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
            JOptionPane.showMessageDialog(this, "Distribution Error:\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DisasterReliefGUI().setVisible(true);
    }
}