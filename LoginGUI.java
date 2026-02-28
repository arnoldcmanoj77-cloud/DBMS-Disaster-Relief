import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;

    public LoginGUI() {

        setTitle("Login - Disaster Relief System");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("DISASTER RELIEF LOGIN", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        panel.add(title);
        panel.add(usernameField);
        panel.add(passwordField);
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(e -> login());
    }

    public void login() {

        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.equals("admin") && password.equals("admin123")) {

            JOptionPane.showMessageDialog(this, "Login Successful");
            new DisasterReliefGUI().setVisible(true);
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Invalid Credentials");
        }
    }

    public static void main(String[] args) {
        new LoginGUI().setVisible(true);
    }
}