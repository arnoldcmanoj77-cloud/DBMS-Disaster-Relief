import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DisasterReliefGUI extends JFrame {

    JTextField nameField, totalField;
    JButton addButton, viewButton;
    JTable table;
    DefaultTableModel model;

    // Database Connection Method
    public Connection getConnection() {
       try {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://localhost:3306/disaster_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "root123";  
        Connection con = DriverManager.getConnection(url, user, password);

        System.out.println("DATABASE CONNECTED SUCCESSFULLY");
        return con;

    } catch (Exception e) {
        System.out.println("DATABASE CONNECTION ERROR:");
        e.printStackTrace();   // VERY IMPORTANT
        return null;
        }
    }

    // Constructor (GUI Design)
    public DisasterReliefGUI() {

        setTitle("Disaster Relief Resource Management");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Add Resource"));

        panel.add(new JLabel("Resource Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Total Quantity:"));
        totalField = new JTextField();
        panel.add(totalField);

        addButton = new JButton("Add Resource");
        viewButton = new JButton("View Resources");

        panel.add(addButton);
        panel.add(viewButton);

        add(panel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Total", "Available"});
        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Add Button Action
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addResource();
            }
        });

        // View Button Action
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewResources();
            }
        });
    }

    // Insert Data
    public void addResource() {
        try {
            Connection con = getConnection();
            String sql = "INSERT INTO Resource (resource_name, total_quantity, available_quantity) VALUES (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, nameField.getText());
            pst.setInt(2, Integer.parseInt(totalField.getText()));
            pst.setInt(3, Integer.parseInt(totalField.getText()));

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Resource Added Successfully!");

            nameField.setText("");
            totalField.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // View Data
    public void viewResources() {
        try {
            Connection con = getConnection();
            String sql = "SELECT * FROM Resource";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("resource_id"),
                        rs.getString("resource_name"),
                        rs.getInt("total_quantity"),
                        rs.getInt("available_quantity")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DisasterReliefGUI().setVisible(true);
    }
}