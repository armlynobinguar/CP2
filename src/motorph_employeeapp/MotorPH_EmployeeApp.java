
package MotorPH_EmployeeApp;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * MotorPH_EmployeeApp
 * This is the main controller of the application. 
 * It manages the initial execution flow: Login -> GUI Initialization.
 */
public class MotorPH_EmployeeApp {

    public static void main(String[] args) {
        if (showLoginDialog()) {
            java.awt.EventQueue.invokeLater(() -> {
                
                MotorPH_GUI.initialize();
            });
        } else {
            System.out.println("System closed.");
            System.exit(0);
        }
    }

    // Handles the user authentication process via a dialog box.
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
                // --- POP-UP FOR WRONG LOGIN ---
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
