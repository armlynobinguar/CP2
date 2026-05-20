
package MotorPH_EmployeeApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * MotorPH_EmployeeApp
 * Procedural main controller handling system authentication.
 */
public class MotorPH_EmployeeApp {

    // SYSTEM-WIDE UNIFIED THEME CONSTANTS
    static final Font APP_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font APP_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    
    static final Color PALETTE_WHITE = Color.WHITE;
    static final Color PALETTE_LIGHT_BLUE = new Color(235, 243, 255); // #EBF3FF Soft Light Blue
    static final Color ACCENT_BLUE = new Color(24, 119, 242);       // #1877F2 Modern Vivid Blue
    static final Color HOVER_BLUE = new Color(12, 103, 222);
    static final Color TEXT_DARK_NAVY = new Color(28, 57, 112);     // #1C3970 Deep text color
    static final Color BORDER_BLUE = new Color(180, 205, 240);

    // GLOBAL AUTHENTICATION CREDENTIAL CONSTANTS
    static final String AUTH_VAL_1 = "employee";
    static final String AUTH_VAL_2 = "payroll_staff";
    static final String AUTH_VAL_3 = "12345";
    static final String AUTH_VAL_4 = "password123";

    static boolean loginSuccessful = false;
    
    /**
     * The main method acting as our procedural program entry point.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        showCustomLoginDialog();

        if (loginSuccessful) {
            java.awt.EventQueue.invokeLater(() -> {
                MotorPH_GUI.initialize();
            });
        } else {
            System.exit(0);
        }
    }

    /**
     * Creates and configures all GUI components to construct the Login Screen.
     */
    private static void showCustomLoginDialog() {
        JDialog loginDialog = new JDialog((Frame) null, "MotorPH Payroll System - Login", true);
        loginDialog.setSize(420, 480);
        loginDialog.setLayout(new BorderLayout());
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setResizable(false);
        
        // Base Window Panel - Uniform Light Blue
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        rootPanel.setBackground(PALETTE_LIGHT_BLUE);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // System Branding Header Box
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        headerPanel.setBackground(PALETTE_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_BLUE, 1),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        
        JLabel lblTitle = new JLabel("MotorPH", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_DARK_NAVY);
        
        JLabel lblSubtitle = new JLabel("Employee App", SwingConstants.CENTER);
        lblSubtitle.setFont(APP_FONT_PLAIN);
        lblSubtitle.setForeground(TEXT_DARK_NAVY);
        
        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);

        // Core Form Inputs Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PALETTE_WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, BORDER_BLUE),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(APP_FONT_BOLD);
        lblUser.setForeground(TEXT_DARK_NAVY);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField usernameField = new JTextField();
        styleInputField(usernameField);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(APP_FONT_BOLD);
        lblPass.setForeground(TEXT_DARK_NAVY);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(APP_FONT_PLAIN);
        passwordField.setBackground(PALETTE_LIGHT_BLUE);
        passwordField.setForeground(TEXT_DARK_NAVY);
        passwordField.setCaretColor(TEXT_DARK_NAVY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_BLUE, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnLogin = new JButton("Login");
        styleAccentButton(btnLogin);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble input layout structure
        formPanel.add(lblUser);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(25));
        formPanel.add(btnLogin);

        // Authentication Action Hook
        btnLogin.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());

            // Check if both fields match any item in our accepted credential options list
            if ((pass.equals(AUTH_VAL_3) || pass.equals(AUTH_VAL_4)) && 
                (user.equals(AUTH_VAL_1) || user.equals(AUTH_VAL_2))) {
                loginSuccessful = true;
                loginDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(loginDialog,
                        "Invalid Username or Password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(formPanel, BorderLayout.CENTER);
        
        loginDialog.add(rootPanel);
        loginDialog.setVisible(true);
    }
    
    /**
     * Procedural helper method to customize textfield components uniform styling properties.
     */
    private static void styleInputField(JTextField field) {
        field.setFont(APP_FONT_PLAIN);
        field.setBackground(PALETTE_LIGHT_BLUE); // Fields match the light blue theme
        field.setForeground(TEXT_DARK_NAVY);
        field.setCaretColor(TEXT_DARK_NAVY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_BLUE, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
    }

    /**
     * Procedural helper method to style JButtons and assign focus, highlight and cursor overrides.
     */
    private static void styleAccentButton(JButton button) {
        button.setFont(APP_FONT_BOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBackground(ACCENT_BLUE);
        button.setForeground(PALETTE_WHITE);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(HOVER_BLUE); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(ACCENT_BLUE); }
        });
    }
}