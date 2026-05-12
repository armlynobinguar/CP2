
package MotorPH_EmployeeApp;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class MotorPH_EmployeeApp {

    public static void main(String[] args) {
        if (showLoginDialog()) {
            java.awt.EventQueue.invokeLater(() -> {
                // Call the static method instead of creating an object
                MotorPH_GUI.initialize();
            });
        } else {
            System.out.println("System closed.");
            System.exit(0);
        }
    }

    private static boolean showLoginDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "MotorPH Login", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            // Check credentials
            if (pass.equals("12345") || pass.equals("password123") && (user.equals("employee") || user.equals("payroll_staff"))) {
                return true;
            } else {
                // --- NEW POP-UP FOR WRONG LOGIN ---
                JOptionPane.showMessageDialog(null,
                        "Invalid Username or Password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false; // User clicked Cancel
    }
}
