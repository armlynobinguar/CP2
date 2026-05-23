
package motorph_employeeapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * MotorPH_GUI
 * BroCode Java Swing Tutorial Reference Concepts.
 */
public class MotorPH_GUI {
    // Shared structural layout window handles
    static JFrame frame;
    static JComboBox<String> monthCombo;

    // Core User Input Fields (From Lesson: TextFields)
    static JTextField txtEmployeeNo;
    static JTextField txtEmployeeName;
    static JTextField txtMonth;
    static JTextField txtYear;
    static JTextArea txtResultArea;

    // Reference Lookup Field Window Variables
    static JTextField txtLookupInput;
    static JTextArea txtLookupDisplay;

    // Fixed Font Styles sourced directly from BroCode lesson parameters
    static final Font APP_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font APP_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font RECEIPT_FONT = new Font("Consolas", Font.PLAIN, 13);

    // SYSTEM-WIDE UNIFIED THEME CONSTANTS (From Lesson: Labels, Buttons)
    static final Font LOGIN_APP_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font LOGIN_APP_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);

    static final Color PALETTE_WHITE = Color.WHITE;
    static final Color PALETTE_LIGHT_BLUE = new Color(235, 243, 255);
    static final Color ACCENT_BLUE = new Color(24, 119, 242);
    static final Color HOVER_BLUE = new Color(12, 103, 222);
    static final Color TEXT_DARK_NAVY = new Color(28, 57, 112);
    static final Color BORDER_BLUE = new Color(180, 205, 240);
    static final Color BORDER_DEFAULT = new Color(163, 196, 243);
    static final Color BORDER_ERROR = Color.RED;

    // UI Global Handles accessible across the procedure hooks
    static JDialog loginDialog;
    static JTextField usernameField;
    static JPasswordField passwordField;
    static JButton btnLogin;

    /**
     * Constructs the login window with absolute boundaries. (From Lesson: Panels,
     * TextFields, Buttons)
     */
    public static void showCustomLoginDialog() {
        loginDialog = new JDialog();
        loginDialog.setTitle("MotorPH Payroll System - Login");
        loginDialog.setSize(420, 540);
        loginDialog.setModal(true); // Replaces Frame parent wrapper injection cleanly
        loginDialog.setLayout(null); // Setting layout manager to null for explicit setBounds tracking
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setResizable(false);

        // Base Root Panel Container (From Lesson: Panels)
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(null);
        rootPanel.setBackground(PALETTE_LIGHT_BLUE);
        rootPanel.setBounds(0, 0, 420, 480);

        // System Branding Header Box (Using clear absolute grid positions instead of
        // layout wrappers)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBackground(PALETTE_WHITE);
        headerPanel.setBounds(20, 20, 365, 80);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE, 1),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)));

        JLabel lblTitle = new JLabel("MotorPH", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_DARK_NAVY);
        lblTitle.setBounds(10, 12, 345, 30);

        JLabel lblSubtitle = new JLabel("Employee App", SwingConstants.CENTER);
        lblSubtitle.setFont(LOGIN_APP_FONT_PLAIN);
        lblSubtitle.setForeground(TEXT_DARK_NAVY);
        lblSubtitle.setBounds(10, 42, 345, 20);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);

        // Core Form Inputs Panel Sheet
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null); // Explicit layout alignment matching absolute canvas
        formPanel.setBackground(PALETTE_WHITE);
        formPanel.setBounds(20, 100, 365, 315);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, BORDER_BLUE),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        // Username Rows (From Lesson: Labels, TextFields)
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(LOGIN_APP_FONT_BOLD);
        lblUser.setForeground(TEXT_DARK_NAVY);
        lblUser.setBounds(30, 25, 305, 20);

        usernameField = new JTextField();
        usernameField.setBounds(30, 50, 305, 38);
        styleInputField(usernameField);

        // Password Rows (From Lesson: TextFields)
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(LOGIN_APP_FONT_BOLD);
        lblPass.setForeground(TEXT_DARK_NAVY);
        lblPass.setBounds(30, 105, 305, 20);

        passwordField = new JPasswordField();
        passwordField.setFont(LOGIN_APP_FONT_PLAIN);
        passwordField.setBackground(PALETTE_LIGHT_BLUE);
        passwordField.setForeground(TEXT_DARK_NAVY);
        passwordField.setCaretColor(TEXT_DARK_NAVY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        passwordField.setBounds(30, 130, 305, 38);

        // Login Action Trigger Button (From Lesson: Buttons)
        btnLogin = new JButton("Login");
        btnLogin.setBounds(30, 200, 305, 40);
        styleAccentButton(btnLogin);

        // --- STICKING TO STANDARD ACTIONLISTENER INTERFACE ---
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnLogin) {
                    String user = usernameField.getText().trim();
                    String pass = new String(passwordField.getPassword());

                    resetLoginFieldBorders();

                    List<String> loginErrors = new ArrayList<>();

                    if (user.isEmpty()) {
                        setLoginFieldError(usernameField);
                        loginErrors.add("Username is required.");
                    }
                    if (pass.isEmpty()) {
                        setLoginFieldError(passwordField);
                        loginErrors.add("Password is required.");
                    }

                    if (!loginErrors.isEmpty()) {
                        showBulletErrorDialog(loginDialog, loginErrors, "Input Error",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Verification logic parsing text parameters
                    if ((pass.equals(MotorPH_EmployeeApp.AUTH_VAL_3) || pass.equals(MotorPH_EmployeeApp.AUTH_VAL_4))
                            && (user.equals(MotorPH_EmployeeApp.AUTH_VAL_1)
                                    || user.equals(MotorPH_EmployeeApp.AUTH_VAL_2))) {
                        MotorPH_EmployeeApp.loginSuccessful = true;
                        loginDialog.dispose();
                    } else {
                        setLoginFieldError(usernameField);
                        setLoginFieldError(passwordField);
                        List<String> authErrors = new ArrayList<>();
                        authErrors.add("Invalid Username or Password.");
                        authErrors.add("Please check your credentials and try again.");
                        showBulletErrorDialog(loginDialog, authErrors, "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Allow Enter key to trigger login from either field
        KeyListener enterKeyListener = new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnLogin.doClick();
                }
            }
            @Override public void keyReleased(KeyEvent e) {}
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // Assemble all elements back onto absolute panel structures
        formPanel.add(lblUser);
        formPanel.add(usernameField);
        formPanel.add(lblPass);
        formPanel.add(passwordField);
        formPanel.add(btnLogin);

        rootPanel.add(headerPanel);
        rootPanel.add(formPanel);

        loginDialog.add(rootPanel);
        loginDialog.setVisible(true);
    }

    /**
     * Procedural helper setting style parameters on input text components.
     */
    private static void styleInputField(JTextField field) {
        field.setFont(LOGIN_APP_FONT_PLAIN);
        field.setBackground(PALETTE_LIGHT_BLUE);
        field.setForeground(TEXT_DARK_NAVY);
        field.setCaretColor(TEXT_DARK_NAVY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    /**
     * Helper mapping style bounds and attaching full interface MouseListeners.
     */
    private static void styleAccentButton(JButton button) {
        button.setFont(LOGIN_APP_FONT_BOLD);
        button.setFocusable(false);
        button.setOpaque(true); // ← tells macOS to actually paint the background
        button.setContentAreaFilled(true); // forces the button area to be filled
        button.setBorderPainted(false); // ← removes the native macOS button chrome
        button.setBackground(ACCENT_BLUE);
        button.setForeground(PALETTE_WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- STICKING TO FULL MOUSELISTENER IMPLEMENTATION (From Lesson:
        // MouseListener) ---
        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT_BLUE);
            }

            // Unused explicit lifecycle methods mandated by the core Interface
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });
    }

    /**
     * Initializes the frame base window container.
     * Lesson: Frames
     */
    public static void initialize() {
        frame = new JFrame();
        frame.setTitle("MotorPH Management System");
        frame.setSize(550, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); // Matching BroCode default window properties
        frame.setLocationRelativeTo(null);

        showMainMenu();
    }

    /**
     * Renders a static Main Navigation Menu using explicit bounds.
     * Lesson: Frames, Labels, Panels, Buttons
     */
    static void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.setLayout(null); // Setting layout to null to use setBounds explicitly
        frame.setSize(520, 600);
        frame.getContentPane().setBackground(new Color(212, 228, 252)); // Hex conversion color match

        // Title Header Label (Lesson: Labels)
        JLabel lblMenuTitle = new JLabel("MAIN MENU", SwingConstants.CENTER);
        lblMenuTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMenuTitle.setForeground(new Color(11, 29, 58));
        lblMenuTitle.setBounds(50, 30, 400, 30);
        frame.add(lblMenuTitle);

        // Container Panel (Lesson: Panels)
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.white);
        menuPanel.setLayout(null); // Using null layout within the panel container
        menuPanel.setBounds(50, 80, 400, 380);
        menuPanel.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        // Navigation Menu Interactive Buttons (Lesson: Buttons)
        JButton btnPayroll = new JButton("1. MPHCRO1: Pay Coverage");
        btnPayroll.setBounds(40, 50, 320, 60);
        guiStyleAccentButton(btnPayroll);
        btnPayroll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setupPayrollUI();
            }
        });

        JButton btnInfo = new JButton("2. Employee Information");
        btnInfo.setBounds(40, 160, 320, 60);
        guiStyleAccentButton(btnInfo);
        btnInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEmployeeLookupUI();
            }
        });

        JButton btnLogout = new JButton("3. Logout");
        btnLogout.setBounds(40, 270, 320, 60);
        styleStandardButton(btnLogout);
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuPanel.add(btnPayroll);
        menuPanel.add(btnInfo);
        menuPanel.add(btnLogout);

        frame.add(menuPanel);
        updateDisplay();
    }

    /**
     * Constructs the processing workbench for payroll execution loops.
     * Lesson: Panels, TextFields, Buttons, Labels
     */
    static void setupPayrollUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(550, 750);
        frame.getContentPane().setBackground(new Color(212, 228, 252));

        // Form Control container panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(new Color(212, 228, 252));
        formPanel.setBounds(0, 0, 550, 290);

        // Instantiating standard text row elements (Lesson: Labels)
        JLabel lblEmpNo = createStyledLabel("Employee Number (ex. 10001):");
        lblEmpNo.setBounds(30, 20, 230, 30);
        txtEmployeeNo = createStyledTextField(true);
        txtEmployeeNo.setBounds(260, 20, 240, 30);

        txtEmployeeNo.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Invoked when a key is typed. Uses KeyChar. (Required by KeyListener
                // interface)
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    validateEmployeeNumberField(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                updateEmployeeNameFromId(false);
            }
        });

        JLabel lblEmpName = createStyledLabel("Employee Name:");
        lblEmpName.setBounds(30, 65, 230, 30);
        txtEmployeeName = createStyledTextField(false); // Locked representation state
        txtEmployeeName.setBounds(260, 65, 240, 30);

        JLabel lblMonth = createStyledLabel("Pay Coverage Month:");
        lblMonth.setBounds(30, 110, 230, 30);
        String[] months = {" ", "06 - June",
                        "07 - July", "08 - August", "09 - September",
                        "10 - October", "11 - November", "12 - December"};
        monthCombo = new JComboBox<>(months);
        monthCombo.setBounds(255, 110, 243, 30);
        monthCombo.setFont(APP_FONT_PLAIN);
        monthCombo.setBackground(Color.white);
        monthCombo.setForeground(new Color(11, 29, 58));
        monthCombo.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
        ((JLabel) monthCombo.getRenderer()).setBorder(
            BorderFactory.createEmptyBorder(0, 2, 0, 0));

        JLabel lblYear = createStyledLabel("Pay Coverage Year (2024 only):");
        lblYear.setBounds(30, 155, 230, 30);
        txtYear = createStyledTextField(true);
        txtYear.setBounds(260, 155, 240, 30);

        // Core Trigger Processing Action Switches (Lesson: Buttons)
        JButton btnProcess = new JButton("Process Payroll");
        btnProcess.setBounds(30, 215, 220, 45);
        guiStyleAccentButton(btnProcess);
        btnProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runPayrollCalculation();
            }
        });

        JButton btnBack = new JButton("Back to Menu");
        btnBack.setBounds(280, 215, 220, 45);
        styleStandardButton(btnBack);
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        formPanel.add(lblEmpNo);
        formPanel.add(txtEmployeeNo);
        formPanel.add(lblEmpName);
        formPanel.add(txtEmployeeName);
        formPanel.add(lblMonth);
        formPanel.add(monthCombo);
        formPanel.add(lblYear);
        formPanel.add(txtYear);
        formPanel.add(btnProcess);
        formPanel.add(btnBack);

        // Text display output panel container
        txtResultArea = new JTextArea();
        txtResultArea.setBackground(Color.white);
        txtResultArea.setForeground(new Color(11, 29, 58));
        txtResultArea.setFont(RECEIPT_FONT);
        txtResultArea.setEditable(false);

        // JScrollPane allows text wrapping views inside layout sheets
        JScrollPane scrollPane = new JScrollPane(txtResultArea);
        scrollPane.setBounds(30, 290, 475, 380);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        frame.add(formPanel);
        frame.add(scrollPane);

        updateDisplay();
    }

    /**
     * Displays a clean info lookup interface aligned with BroCode's layout style.
     * Lesson: OpenNewWindow panel manipulation models
     */
    static void showEmployeeLookupUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(480, 520);
        frame.getContentPane().setBackground(new Color(212, 228, 252));

        JPanel lookupPanel = new JPanel();
        lookupPanel.setLayout(null);
        lookupPanel.setBackground(Color.white);
        lookupPanel.setBounds(40, 40, 400, 400);
        lookupPanel.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        JLabel lblPrompt = createStyledLabel("Enter Employee ID:");
        lblPrompt.setBounds(30, 25, 340, 25);

        txtLookupInput = createStyledTextField(true);
        txtLookupInput.setBounds(30, 55, 340, 35);
        txtLookupInput.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runEmployeeLookupAction();
                }
            }
            @Override public void keyReleased(KeyEvent e) {}
        });

        JButton btnSearch = new JButton("Search Record");
        btnSearch.setBounds(30, 105, 160, 40);
        guiStyleAccentButton(btnSearch);
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runEmployeeLookupAction();
            }
        });

        JButton btnClose = new JButton("Back");
        btnClose.setBounds(210, 105, 160, 40);
        styleStandardButton(btnClose);
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });

        txtLookupDisplay = new JTextArea();
        txtLookupDisplay.setFont(APP_FONT_PLAIN);
        txtLookupDisplay.setBackground(new Color(240, 246, 255));
        txtLookupDisplay.setForeground(new Color(11, 29, 58));
        txtLookupDisplay.setEditable(false);

        JScrollPane infoScroll = new JScrollPane(txtLookupDisplay);
        infoScroll.setBounds(30, 165, 340, 200);
        infoScroll.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        lookupPanel.add(lblPrompt);
        lookupPanel.add(txtLookupInput);
        lookupPanel.add(btnSearch);
        lookupPanel.add(btnClose);
        lookupPanel.add(infoScroll);

        frame.add(lookupPanel);
        updateDisplay();
    }

    /**
     * Executes internal logic string reads parsing lookup queries.
     * Combines both real-time clearing and button-click empty validation states
     * safely.
     */
    static void runEmployeeLookupAction() {
        String idInput = txtLookupInput.getText().trim();
        resetFieldBorder(txtLookupInput);
        txtLookupDisplay.setText("");

        List<String> lookupErrors = new ArrayList<>();

        if (idInput.isEmpty()) {
            setFieldError(txtLookupInput);
            lookupErrors.add("Employee ID is required.");
        } else if (!idInput.matches("\\d+")) {
            setFieldError(txtLookupInput);
            lookupErrors.add("Employee ID must be numeric (e.g. 10001).");
        } else if (!FileHandlerModule.employeeExists(idInput)) {
            setFieldError(txtLookupInput);
            lookupErrors.add("Employee ID \"" + idInput + "\" was not found in the employee records (CSV).");
        }

        if (!lookupErrors.isEmpty()) {
            txtLookupDisplay.setText(formatPlainBulletList(lookupErrors));
            showBulletErrorDialog(frame, lookupErrors, "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String data = FileHandlerModule.findEmployeeData(idInput);
        String[] emp = FileHandlerModule.smartSplit(data);
        txtLookupDisplay.setText(
                "Employee ID: " + emp[EmployeeModule.ID] + "\n" +
                        "Full Name:   " + EmployeeModule.fullName(emp) + "\n" +
                        "Birthday:    " + emp[EmployeeModule.BIRTHDAY]);
    }

    /**
     * Validation checks and execution routine for calculations.
     * Lesson: JOptionPane Dialog Mechanics
     */
    static void runPayrollCalculation() {
        txtResultArea.setText("");

        String id = txtEmployeeNo.getText().trim();
        String year = txtYear.getText().trim();

        resetPayrollFieldBorders();

        List<String> errors = new ArrayList<>();

        collectEmployeeNumberErrors(id, errors);

        if (monthCombo.getSelectedIndex() == 0) {
            setFieldError(monthCombo);
            errors.add("Pay Coverage Month is required.");
        }

        if (year.isEmpty()) {
            setFieldError(txtYear);
            errors.add("Pay Coverage Year is required.");
        } else if (!year.matches("\\d+")) {
            setFieldError(txtYear);
            errors.add("Pay Coverage Year must be numeric.");
        } else if (!year.equals("2024")) {
            setFieldError(txtYear);
            errors.add("Only year 2024 is currently supported.");
        }

        if (!errors.isEmpty()) {
            txtResultArea.setText(formatPlainBulletList(errors));
            showBulletErrorDialog(frame, errors, "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String data = FileHandlerModule.findEmployeeData(id);
        String[] emp = FileHandlerModule.smartSplit(data);
        txtEmployeeName.setText(EmployeeModule.fullName(emp));

        String actualMonth = String.valueOf(monthCombo.getSelectedIndex() + 5);
        SalaryComputationModule.calculatePayroll(emp, actualMonth, year, txtResultArea);
    }

    private static void resetLoginFieldBorders() {
        resetLoginFieldBorder(usernameField);
        resetLoginFieldBorder(passwordField);
    }

    private static void resetLoginFieldBorder(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private static void setLoginFieldError(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_ERROR, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private static void resetPayrollFieldBorders() {
        resetFieldBorder(txtEmployeeNo);
        resetFieldBorder(txtYear);
        resetFieldBorder(monthCombo);
    }

    private static void resetFieldBorder(JTextField field) {
        if (field != null) {
            field.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT, 1));
        }
    }

    private static void resetFieldBorder(JComboBox<?> combo) {
        if (combo != null) {
            combo.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT, 1));
        }
    }

    private static void setFieldError(JTextField field) {
        field.setBorder(BorderFactory.createLineBorder(BORDER_ERROR, 2));
    }

    private static void setFieldError(JComboBox<?> combo) {
        combo.setBorder(BorderFactory.createLineBorder(BORDER_ERROR, 2));
    }

    /**
     * Adds employee-number validation messages to the list (checks Employee Details CSV).
     */
    private static void collectEmployeeNumberErrors(String id, List<String> errors) {
        if (id == null || id.isEmpty()) {
            setFieldError(txtEmployeeNo);
            errors.add("Employee Number is required.");
            return;
        }
        if (!id.matches("\\d+")) {
            setFieldError(txtEmployeeNo);
            errors.add("Employee Number must be numeric (e.g. 10001).");
            return;
        }
        if (!FileHandlerModule.employeeExists(id)) {
            setFieldError(txtEmployeeNo);
            errors.add("Employee Number \"" + id + "\" was not found in the employee records (CSV).");
        }
    }

    /**
     * Live lookup for the employee name field. Optionally shows a dialog on Enter.
     */
    private static boolean validateEmployeeNumberField(boolean showDialog) {
        String id = txtEmployeeNo.getText().trim();
        resetFieldBorder(txtEmployeeNo);

        if (id.isEmpty()) {
            txtEmployeeName.setText("");
            return false;
        }

        List<String> errors = new ArrayList<>();
        collectEmployeeNumberErrors(id, errors);

        if (!errors.isEmpty()) {
            if (id.matches("\\d+")) {
                txtEmployeeName.setText("(Employee not found)");
            } else {
                txtEmployeeName.setText("");
            }
            if (showDialog) {
                showBulletErrorDialog(frame, errors, "Invalid Employee Number",
                        JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        String data = FileHandlerModule.findEmployeeData(id);
        String[] emp = FileHandlerModule.smartSplit(data);
        txtEmployeeName.setText(EmployeeModule.fullName(emp));
        return true;
    }

    private static void updateEmployeeNameFromId(boolean showDialog) {
        validateEmployeeNumberField(showDialog);
    }

    private static void showBulletErrorDialog(Component parent, List<String> items,
            String title, int messageType) {
        StringBuilder html = new StringBuilder(
                "<html><body style='width:320px;font-family:Segoe UI;font-size:13px;color:rgb(28,57,112);'>");
        html.append("<b>Please fix the following:</b>");
        html.append("<ul style='margin-top:8px;margin-bottom:0;padding-left:22px;'>");
        for (String item : items) {
            html.append("<li>").append(escapeHtml(item)).append("</li>");
        }
        html.append("</ul></body></html>");
        JOptionPane.showMessageDialog(parent, html.toString(), title, messageType);
    }

    private static String formatPlainBulletList(List<String> items) {
        StringBuilder text = new StringBuilder("Please fix the following:\n");
        for (String item : items) {
            text.append("• ").append(item).append("\n");
        }
        return text.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    
    // --- STYLE HELPER FUNCTIONS MATCHING SCRIPT CHOICES ---
    private static JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(APP_FONT_BOLD);
        label.setForeground(new Color(11, 29, 58));
        return label;
    }

    private static JTextField createStyledTextField(boolean isEditable) {
        JTextField field = new JTextField();
        field.setFont(APP_FONT_PLAIN);
        field.setEditable(isEditable);
        field.setForeground(new Color(11, 29, 58));
        field.setCaretColor(new Color(11, 29, 58));

        if (isEditable) {
            field.setBackground(Color.white);
        } else {
            field.setBackground(new Color(222, 233, 250));
        }

        field.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));
        return field;
    }

    private static void styleStandardButton(JButton button) {
        button.setFont(APP_FONT_BOLD);
        button.setFocusable(false);
        button.setBackground(Color.white);
        button.setForeground(new Color(11, 29, 58));
        button.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static void guiStyleAccentButton(JButton button) {
        button.setFont(APP_FONT_BOLD);
        button.setFocusable(false);
        button.setOpaque(true); // macOS fix: force background paint
        button.setContentAreaFilled(true); // macOS fix: fill the button area
        button.setBorderPainted(false); // macOS fix: remove native chrome
        button.setBackground(new Color(37, 119, 241)); // Clear royal accent blue
        button.setForeground(Color.white);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    static void updateDisplay() {
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true); // Always refreshed at the end to force accurate UI updates
    }
}