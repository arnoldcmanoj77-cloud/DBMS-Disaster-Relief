import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DisasterReliefGUI extends JFrame {

    public DisasterReliefGUI() {

        setTitle("Disaster Relief Management System");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel header = new JLabel("DISASTER RELIEF MANAGEMENT SYSTEM", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));
        panel.add(header, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(7,1,15,15));
        menu.setBorder(BorderFactory.createEmptyBorder(30,120,30,120));

        JButton eventBtn = new JButton("DISASTER EVENTS");
        JButton regionBtn = new JButton("AFFECTED REGIONS");
        JButton campBtn = new JButton("RELIEF CAMPS");
        JButton volunteerBtn = new JButton("VOLUNTEERS");
        JButton resourceBtn = new JButton("RESOURCES");
        JButton distributionBtn = new JButton("DISTRIBUTION");
        JButton exitBtn = new JButton("EXIT");

        menu.add(eventBtn);
        menu.add(regionBtn);
        menu.add(campBtn);
        menu.add(volunteerBtn);
        menu.add(resourceBtn);
        menu.add(distributionBtn);
        menu.add(exitBtn);

        panel.add(menu, BorderLayout.CENTER);
        add(panel);

        // ✅ FIXED TABLE NAMES
        eventBtn.addActionListener(e -> openModule("disaster_event"));
        regionBtn.addActionListener(e -> openModule("affected_region")); // FIXED
        campBtn.addActionListener(e -> openModule("relief_camp"));
        volunteerBtn.addActionListener(e -> openModule("volunteer"));
        resourceBtn.addActionListener(e -> openModule("resource"));
        distributionBtn.addActionListener(e -> handleDistribution());
        exitBtn.addActionListener(e -> System.exit(0));
    }

    // DATABASE CONNECTION
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

    // GENERIC MODULE WINDOW
    public void openModule(String tableName) {

        JFrame frame = new JFrame(tableName.toUpperCase() + " MANAGEMENT");
        frame.setSize(800,450);
        frame.setLocationRelativeTo(null);

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);

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
        JButton addBtn = new JButton("ADD");
        JButton deleteBtn = new JButton("DELETE");
        JButton refreshBtn = new JButton("REFRESH");

        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        // ADD RECORD
        addBtn.addActionListener(e -> {
            try {
                Connection con = getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SHOW COLUMNS FROM " + tableName);

                StringBuilder cols = new StringBuilder();
                StringBuilder vals = new StringBuilder();

                while (rs.next()) {
                    String col = rs.getString("Field");
                    String extra = rs.getString("Extra");

                    if (!extra.contains("auto_increment")) {
                        String value = JOptionPane.showInputDialog("Enter " + col + ":");
                        cols.append(col).append(",");
                        vals.append("'").append(value).append("',");
                    }
                }

                String query = "INSERT INTO " + tableName +
                        " (" + cols.substring(0, cols.length()-1) + ")" +
                        " VALUES (" + vals.substring(0, vals.length()-1) + ")";

                st.executeUpdate(query);
                JOptionPane.showMessageDialog(frame, "Added Successfully");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Insert Error: " + ex.getMessage());
            }
        });

        // DELETE RECORD (SMART ID DETECTION)
        deleteBtn.addActionListener(e -> {
            try {
                String id = JOptionPane.showInputDialog("Enter ID to Delete:");

                Connection con = getConnection();
                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("SHOW COLUMNS FROM " + tableName);
                String idColumn = "";

                while (rs.next()) {
                    if (rs.getString("Key").equals("PRI")) {
                        idColumn = rs.getString("Field");
                        break;
                    }
                }

                st.executeUpdate("DELETE FROM " + tableName +
                        " WHERE " + idColumn + "=" + id);

                JOptionPane.showMessageDialog(frame, "Deleted Successfully");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Delete Error: " + ex.getMessage());
            }
        });

        refreshBtn.addActionListener(e -> {
            frame.dispose();
            openModule(tableName);
        });

        frame.setVisible(true);
    }

    // FIXED DISTRIBUTION FUNCTION
    public void handleDistribution() {
        try {
            int resourceId = Integer.parseInt(
                    JOptionPane.showInputDialog("Enter Resource ID:"));
            int campId = Integer.parseInt(
                    JOptionPane.showInputDialog("Enter Camp ID:"));
            int qty = Integer.parseInt(
                    JOptionPane.showInputDialog("Enter Quantity:"));

            Connection con = getConnection();

            // Reduce stock
            PreparedStatement pst = con.prepareStatement(
                    "UPDATE resource SET total_quantity = total_quantity - ? " +
                            "WHERE resource_id=? AND total_quantity>=?");
            pst.setInt(1, qty);
            pst.setInt(2, resourceId);
            pst.setInt(3, qty);

            int rows = pst.executeUpdate();

            if (rows > 0) {

                // Insert into correct table
                PreparedStatement dist = con.prepareStatement(
                        "INSERT INTO resource_distribution(resource_id,camp_id,quantity_distributed,distribution_date) VALUES(?,?,?,CURDATE())");
                dist.setInt(1, resourceId);
                dist.setInt(2, campId);
                dist.setInt(3, qty);
                dist.executeUpdate();

                JOptionPane.showMessageDialog(this, "Distributed Successfully");

            } else {
                JOptionPane.showMessageDialog(this, "Not Enough Stock");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Distribution Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DisasterReliefGUI().setVisible(true);
    }
}