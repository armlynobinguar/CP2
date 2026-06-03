
package motorph_employeeapp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List; // Optional, see note below
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * MotorPH_GUI
 * -----------
 * Swing-based presentation layer for the MotorPH Employee Payroll System.
 *
 * This class builds and navigates all screens:
 * - Login dialog (modal authentication gate)
 * - Main menu (payroll, employee lookup, logout)
 * - Pay coverage / payroll processing form (MPHCRO1)
 * - Employee information lookup view
 *
 * Layout uses null layout ({@code setLayout(null)}) with explicit
 * {@code setBounds}
 * positioning, following BroCode Java Swing tutorial patterns for frames,
 * panels,
 * labels, text fields, buttons, and event listeners.
 *
 * Business logic is delegated to {@link FileHandlerModule},
 * {@link EmployeeModule},
 * and {@link SalaryComputationModule}; this class handles validation, styling,
 * navigation, and user feedback (dialogs and error borders, result text areas).
 */
public class MotorPH_GUI {

    // --- Main application window and shared payroll controls ---

    /** Primary JFrame container; content is swapped per screen via removeAll(). */
    static JFrame frame;

    /** Dropdown for pay coverage month (June–December in current dataset). */
    static JComboBox<String> monthCombo;

    // --- Payroll screen input and output widgets ---

    /** Employee ID entry on the pay coverage form. */
    static JTextField txtEmployeeNo;

    /** Read-only employee name; auto-filled from CSV when ID is valid. */
    static JTextField txtEmployeeName;

    /** Legacy month field (superseded by monthCombo on payroll UI). */
    static JTextField txtMonth;

    /** Pay coverage year; validated to "2024" only. */
    static JTextField txtYear;

    /** Scrollable payslip / validation message output on payroll screen. */
    static JTextArea txtResultArea;

    /**
     * Parallel month-number lookup for {@link #monthCombo}.
     *
     * Index 0 is a sentinel ("no selection"); subsequent entries map combo
     * positions
     * to actual calendar month numbers. Using this lookup avoids fragile arithmetic
     * like {@code getSelectedIndex() + 5}, which silently breaks if items change.
     */
    static final int[] MONTH_NUMBERS = { 0, 6, 7, 8, 9, 10, 11, 12 };

    // --- Employee lookup screen widgets ---

    /** Employee ID search box on the information lookup screen. */
    static JTextField txtLookupInput;

    /** Displays ID, full name, and birthday after a successful lookup. */
    static JTextArea txtLookupDisplay;

    // --- Typography: main app screens ---

    static final Font APP_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font APP_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    /** Monospace font for aligned payslip columns in the result area. */
    static final Font RECEIPT_FONT = new Font("Consolas", Font.PLAIN, 13);

    // --- Typography: login dialog ---

    static final Font LOGIN_APP_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font LOGIN_APP_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);

    // --- Color palette (MotorPH blue theme) ---

    static final Color PALETTE_WHITE = Color.WHITE;
    static final Color PALETTE_LIGHT_BLUE = new Color(235, 243, 255);
    static final Color ACCENT_BLUE = new Color(24, 119, 242);
    static final Color HOVER_BLUE = new Color(12, 103, 222);
    /** Darker accent shade applied to the login button on mouse press. */
    static final Color PRESSED_BLUE = new Color(5, 85, 200);
    static final Color TEXT_DARK_NAVY = new Color(28, 57, 112);
    /** Soft gray used for placeholder/hint text inside empty input fields. */
    static final Color TEXT_PLACEHOLDER_GRAY = new Color(140, 155, 180);
    static final Color BORDER_BLUE = new Color(180, 205, 240);
    static final Color BORDER_DEFAULT = new Color(163, 196, 243);
    /** Red border highlight for fields that failed validation. */
    static final Color BORDER_ERROR = Color.RED;

    /**
     * Default echo character for the password field (used when toggling
     * visibility).
     */
    static final char PASSWORD_ECHO_CHAR = '\u2022';

    // --- Login dialog component references (used by listeners and validators) ---

    static JDialog loginDialog;
    static JTextField usernameField;
    static JPasswordField passwordField;
    static JButton btnLogin;

    /**
     * Username captured at login; rendered in each screen's bottom-right footer.
     */
    static String loggedInUser = "";

    /** Height of the colored header strip at the top of every main screen. */
    static final int HEADER_STRIP_HEIGHT = 50;
    /** Font used for the white screen title inside the colored header strip. */
    static final Font HEADER_STRIP_FONT = new Font("Segoe UI", Font.BOLD, 20);
    /** Font for the small "Logged in as: …" status label. */
    static final Font STATUS_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    /* For ShowEmployeeSelfServiceDashboard */
    static javax.swing.JTable profileTable;
    static JButton btnUpdateExistingRecord, btnDeleteRecord;
    static DefaultTableModel tableModel;

    /**
     * Builds and shows the modal login dialog before the main application loads.
     *
     * Validates non-empty username/password, then checks credentials against
     * {@link MotorPH_EmployeeApp} auth constants. On success, sets
     * {@link MotorPH_EmployeeApp#loginSuccessful} and disposes the dialog flow
     * so {@code main()} can continue to {@link #initialize()}.
     */
    public static void showCustomLoginDialog() {
        loginDialog = new JDialog();
        loginDialog.setTitle("MotorPH Payroll System - Login");
        // Sized so the header card + form card + small bottom margin fit cleanly
        loginDialog.setSize(436, 460);
        loginDialog.setModal(true); // Replaces Frame parent wrapper injection cleanly
        loginDialog.setLayout(null); // Setting layout manager to null for explicit setBounds tracking
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setResizable(false);

        // Base Root Panel Container (From Lesson: Panels)
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(null);
        rootPanel.setBackground(PALETTE_LIGHT_BLUE);
        rootPanel.setBounds(0, 0, 420, 460);

        // System Branding Header Box (Using clear absolute grid positions instead of
        // layout wrappers).
        // Card width 360 + dialog width 420 → 30px margin on both left and right.
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBackground(PALETTE_WHITE);
        headerPanel.setBounds(30, 20, 360, 80);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE, 1),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)));

        // Header labels span the full inner width (360 - 10 - 10 = 340)
        JLabel lblTitle = new JLabel("MotorPH", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_DARK_NAVY);
        lblTitle.setBounds(10, 12, 340, 30);

        JLabel lblSubtitle = new JLabel("Employee App", SwingConstants.CENTER);
        lblSubtitle.setFont(LOGIN_APP_FONT_PLAIN);
        lblSubtitle.setForeground(TEXT_DARK_NAVY);
        lblSubtitle.setBounds(10, 42, 340, 20);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);

        // Core Form Inputs Panel Sheet
        // Width 360 matches the header card so both align with equal 30px dialog
        // margins.
        // Height = 25 top padding + button bottom (y=275) + 25 bottom padding = 300
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null); // Explicit layout alignment matching absolute canvas
        formPanel.setBackground(PALETTE_WHITE);
        formPanel.setBounds(30, 100, 360, 300);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, BORDER_BLUE),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        // Login form vertical rhythm (kept consistent across every row):
        // labelHeight=20, fieldHeight=38, checkboxHeight=22, buttonHeight=40
        // gap between a label and its field = 6px
        // gap between sections (field→label) = 20px
        // Adjusting any of these values? Update every row below to keep the grid even.

        // Username row
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(LOGIN_APP_FONT_BOLD);
        lblUser.setForeground(TEXT_DARK_NAVY);
        lblUser.setBounds(30, 25, 300, 20); // y=25, ends y=45

        usernameField = new JTextField();
        usernameField.setBounds(30, 51, 300, 38); // 45 + 6 label-to-field gap, ends y=89
        styleInputField(usernameField);
        // Placeholder hint shown only when field is empty and unfocused (From Lesson:
        // FocusListener)
        attachPlaceholder(usernameField, "e.g. employee");

        // Password row (89 + 20 section gap = 109)
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(LOGIN_APP_FONT_BOLD);
        lblPass.setForeground(TEXT_DARK_NAVY);
        lblPass.setBounds(30, 109, 300, 20); // ends y=129

        passwordField = new JPasswordField();
        passwordField.setEchoChar(PASSWORD_ECHO_CHAR);
        passwordField.setFont(LOGIN_APP_FONT_PLAIN);
        passwordField.setBackground(PALETTE_LIGHT_BLUE);
        passwordField.setForeground(TEXT_DARK_NAVY);
        passwordField.setCaretColor(TEXT_DARK_NAVY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_BLUE, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        passwordField.setBounds(30, 135, 300, 38); // 129 + 6 label-to-field gap, ends y=173

        // Show/Hide password toggle (173 + 20 section gap = 193)
        final JCheckBox chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setBounds(30, 193, 300, 22); // ends y=215
        chkShowPassword.setFont(LOGIN_APP_FONT_PLAIN);
        chkShowPassword.setBackground(PALETTE_WHITE);
        chkShowPassword.setForeground(TEXT_DARK_NAVY);
        chkShowPassword.setFocusable(false);
        chkShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkShowPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chkShowPassword.isSelected()) {
                    passwordField.setEchoChar((char) 0); // 0 = display characters as-is
                } else {
                    passwordField.setEchoChar(PASSWORD_ECHO_CHAR);
                }
            }
        });

        // Login button (215 + 20 section gap = 235)
        btnLogin = new JButton("Login");
        btnLogin.setBounds(30, 235, 300, 40); // ends y=275
        styleAccentButton(btnLogin);

        // --- STICKING TO STANDARD ACTIONLISTENER INTERFACE ---
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == btnLogin) {
                    // Treat placeholder/hint text as an empty value so validators don't
                    // accidentally see "e.g. employee" as a real submitted username.
                    String user = usernameField.getForeground().equals(TEXT_PLACEHOLDER_GRAY)
                            ? ""
                            : usernameField.getText().trim();
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
                        loggedInUser = user; // Remembered for the "Logged in as:" footer
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
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnLogin.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // Assemble all elements back onto absolute panel structures
        formPanel.add(lblUser);
        formPanel.add(usernameField);
        formPanel.add(lblPass);
        formPanel.add(passwordField);
        formPanel.add(chkShowPassword);
        formPanel.add(btnLogin);

        rootPanel.add(headerPanel);
        rootPanel.add(formPanel);

        loginDialog.add(rootPanel);
        loginDialog.setVisible(true);
    }

    /**
     * Applies login-screen styling to a text field (font, colors, padded border).
     *
     * @param field JTextField or subclass to style (username field)
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
     * Styles the primary login button and wires|hover feedback via
     * {@link MouseListener}.
     *
     * macOS-specific flags ({@code setOpaque}, {@code setContentAreaFilled},
     * {@code setBorderPainted}) ensure custom background colors render correctly.
     *
     * @param button login submit button
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
        // Tracks whether the cursor is still over the button when the mouse is released
        // so we can pick the right post-press color (hover vs. resting accent).
        button.addMouseListener(new MouseListener() {
            private boolean cursorInside = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                cursorInside = true;
                button.setBackground(HOVER_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cursorInside = false;
                button.setBackground(ACCENT_BLUE);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Tactile feedback: darker shade while the user is holding the button down
                button.setBackground(PRESSED_BLUE);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Restore hover color if still over the button, otherwise resting accent
                button.setBackground(cursorInside ? HOVER_BLUE : ACCENT_BLUE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
    }

    /**
     * Attaches placeholder/hint behavior to a text field via {@link FocusListener}.
     *
     * Shows {@code hint} in gray when the field is empty and unfocused. On focus
     * the hint
     * is cleared and the foreground reverts to the regular text color so user input
     * is
     * displayed normally. The hint string is treated as empty by validators
     * (callers should
     * still trim and check via {@link JTextField#getText()} after focus).
     *
     * @param field text input to decorate
     * @param hint  placeholder copy (e.g. {@code "e.g. employee"})
     */
    private static void attachPlaceholder(final JTextField field, final String hint) {
        // Initial state: field is empty and unfocused, so paint the hint
        field.setForeground(TEXT_PLACEHOLDER_GRAY);
        field.setText(hint);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(hint)
                        && field.getForeground().equals(TEXT_PLACEHOLDER_GRAY)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK_NAVY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(TEXT_PLACEHOLDER_GRAY);
                    field.setText(hint);
                }
            }
        });
    }

    /**
     * Creates the main {@link JFrame} after successful login and opens the main
     * menu.
     *
     * Window is fixed-size, centered, and exits the JVM on close.
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
     * Clears the frame and displays the main navigation hub with three actions:
     * pay coverage (payroll), employee information lookup, and logout.
     */
    static void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.setLayout(null); // Setting layout to null to use setBounds explicitly
        frame.setSize(476, 530);
        frame.getContentPane().setBackground(new Color(212, 228, 252)); // Hex conversion color match

        // Branded blue header strip replaces the prior plain title label
        addColoredHeaderStrip("MAIN MENU", 460);

        // Container Panel (Lesson: Panels) — shifted just below the header strip.
        // x = (frameWidth 520 - cardWidth 400) / 2 = 60, so left and right margins both
        // equal 60.
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.white);
        menuPanel.setLayout(null); // Using null layout within the panel container
        menuPanel.setBounds(30, 70, 400, 380);
        menuPanel.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        // --- Uniform dimensions for a neat layout grid ---
        int btnWidth = 320;
        int btnHeight = 50;
        int startX = 40;

        // Button 1: Payroll processing
        JButton btnPayroll = new JButton("1. MPHCRO1: Pay Coverage");
        btnPayroll.setBounds(startX, 40, btnWidth, btnHeight); // Occupies y=40 to y=90
        guiStyleAccentButton(btnPayroll);
        btnPayroll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setupPayrollUI();
            }
        });

        // Button 2: Look up records
        JButton btnInfo = new JButton("2. Employee Information");
        btnInfo.setBounds(startX, 110, btnWidth, btnHeight); // Occupies y=110 to y=160
        guiStyleAccentButton(btnInfo);
        btnInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEmployeeLookupUI();
            }
        });

        // Button 3: Self Service Dashboard (Shifted cleanly down to y=180)
        JButton btnSelfUpdate = new JButton("3. Employee Update");
        btnSelfUpdate.setBounds(startX, 180, btnWidth, btnHeight); // Occupies y=180 to y=230
        guiStyleAccentButton(btnSelfUpdate);
        btnSelfUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEmployeeSelfServiceDashboard();
            }
        });

        // Button 4: System exit sequence
        JButton btnLogout = new JButton("4. Logout");
        btnLogout.setBounds(startX, 250, btnWidth, btnHeight); // Occupies y=250 to y=300
        styleStandardButton(btnLogout);
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // --- Add elements to panel ---
        menuPanel.add(btnPayroll);
        menuPanel.add(btnInfo);
        menuPanel.add(btnSelfUpdate);
        menuPanel.add(btnLogout);

        frame.add(menuPanel);
        addLoggedInFooter(460, 500, 30); // Right margin 60 matches menuPanel right edge
        updateDisplay();
    }

    /**
     * Builds the MPHCRO1 pay coverage screen: employee ID, month/year selection,
     * process/back buttons, and scrollable payslip output area.
     *
     * Employee name auto-updates on key release; Enter on ID field triggers
     * validation.
     */
    static void setupPayrollUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(566, 750);
        frame.getContentPane().setBackground(new Color(212, 228, 252));

        // Branded blue header strip across the top
        addColoredHeaderStrip("PAY COVERAGE", 550);

        // Form Control container panel — shifted below the header strip
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(new Color(212, 228, 252));
        formPanel.setBounds(0, HEADER_STRIP_HEIGHT, 550, 280);

        // Form columns: label x=30 width=230 (ends x=260),
        // field x=260 width=260 (ends x=520) — matches scroll pane edge.
        JLabel lblEmpNo = createStyledLabel("Employee Number (ex. 10001):");
        lblEmpNo.setBounds(30, 20, 230, 30);
        txtEmployeeNo = createStyledTextField(true);
        txtEmployeeNo.setBounds(260, 20, 260, 30);

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
        txtEmployeeName.setBounds(260, 65, 260, 30);

        JLabel lblMonth = createStyledLabel("Pay Coverage Month:");
        lblMonth.setBounds(30, 110, 230, 30);
        String[] months = { " ", "06 - June",
                "07 - July", "08 - August", "09 - September",
                "10 - October", "11 - November", "12 - December" };
        monthCombo = new JComboBox<>(months);
        monthCombo.setBounds(260, 110, 260, 30);
        monthCombo.setFont(APP_FONT_PLAIN);
        monthCombo.setBackground(Color.white);
        monthCombo.setForeground(new Color(11, 29, 58));
        monthCombo.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
        ((JLabel) monthCombo.getRenderer()).setBorder(
                BorderFactory.createEmptyBorder(0, 2, 0, 0));

        JLabel lblYear = createStyledLabel("Pay Coverage Year (2024 only):");
        lblYear.setBounds(30, 155, 230, 30);
        txtYear = createStyledTextField(true);
        txtYear.setBounds(260, 155, 260, 30);
        // Pre-fill the only valid year so users don't accidentally submit blank
        txtYear.setText("2024");

        // Core Trigger Processing Action Switches (Lesson: Buttons)
        // Two equal-width buttons spanning x=30 to x=520 with a 20px gap between them.
        JButton btnProcess = new JButton("Process Payroll");
        btnProcess.setBounds(30, 215, 235, 45);
        guiStyleAccentButton(btnProcess);
        btnProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runPayrollCalculation();
            }
        });

        JButton btnBack = new JButton("Back to Menu");
        btnBack.setBounds(285, 215, 235, 45);
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

        // Section header above the result area for clearer visual grouping
        JLabel lblOutputHeader = createStyledLabel("Payroll Output:");
        lblOutputHeader.setBounds(30, 335, 200, 20);
        frame.add(lblOutputHeader);

        // Text display output panel container
        txtResultArea = new JTextArea();
        txtResultArea.setBackground(Color.white);
        txtResultArea.setForeground(new Color(11, 29, 58));
        txtResultArea.setFont(RECEIPT_FONT);
        txtResultArea.setEditable(false);

        // JScrollPane allows text wrapping views inside layout sheets
        JScrollPane scrollPane = new JScrollPane(txtResultArea);
        scrollPane.setBounds(30, 365, 490, 315);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        frame.add(formPanel);
        frame.add(scrollPane);
        addLoggedInFooter(550, 720, 30); // Right margin 30 matches scrollPane right edge

        updateDisplay();
    }

    /**
     * Shows the employee information lookup screen.
     *
     * User enters an employee ID; search loads ID, full name, and birthday from CSV
     * or displays validation errors in the text area and a JOptionPane dialog.
     */
    static void showEmployeeLookupUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(476, 550);
        frame.getContentPane().setBackground(new Color(212, 228, 252));

        // Branded blue header strip across the top
        addColoredHeaderStrip("EMPLOYEE LOOKUP", 460);

        // Center the card: x = (frameWidth 480 - cardWidth 400) / 2 = 40
        JPanel lookupPanel = new JPanel();
        lookupPanel.setLayout(null);
        lookupPanel.setBackground(Color.white);
        lookupPanel.setBounds(30, 70, 400, 400);
        lookupPanel.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        JLabel lblPrompt = createStyledLabel("Enter Employee ID:");
        lblPrompt.setBounds(30, 25, 340, 25);

        txtLookupInput = createStyledTextField(true);
        txtLookupInput.setBounds(30, 55, 340, 35);
        txtLookupInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runEmployeeLookupAction();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Wipe stale results as soon as the user edits the ID,
                // but skip the Enter key (which just populated the display).
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    txtLookupDisplay.setText("");
                    resetFieldBorder(txtLookupInput);
                }
            }
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
        // Monospace font keeps the multi-row "Field: value" output cleanly aligned
        txtLookupDisplay.setFont(RECEIPT_FONT);
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
        addLoggedInFooter(460, 520, 30); // Right margin 40 matches lookupPanel right edge
        updateDisplay();
    }

    /**
     * Handles employee lookup when Search is clicked or Enter is pressed.
     *
     * Validates: non-empty ID, numeric format, and existence in Employee Details
     * CSV.
     * On success, formats and displays ID, name, and birthday in
     * {@link #txtLookupDisplay}.
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
        txtLookupDisplay.setText(formatEmployeeProfile(emp));
    }

    /**
     * Builds a multi-line profile block for the lookup screen.
     *
     * Pulls richer columns (status, position, supervisor, basic salary) so the
     * lookup is genuinely useful, not just an ID/name echo. Missing fields are
     * tolerated with safe fallback strings so a short array never crashes the UI.
     *
     * @param emp split CSV row
     * @return formatted text suitable for {@link #txtLookupDisplay}
     */
    private static String formatEmployeeProfile(String[] emp) {
        return "Employee ID:           " + safeColumn(emp, EmployeeModule.ID) + "\n"
                + "Full Name:             " + EmployeeModule.fullName(emp) + "\n"
                + "Birthday:              " + safeColumn(emp, EmployeeModule.BIRTHDAY) + "\n"
                + "Status:                " + safeColumn(emp, EmployeeModule.STATUS) + "\n"
                + "Position:              " + safeColumn(emp, EmployeeModule.POSITION) + "\n"
                + "Immediate Supervisor:  " + safeColumn(emp, EmployeeModule.IMMEDIATE_SUPERVISOR) + "\n"
                + "Basic Salary:          PHP " + safeColumn(emp, EmployeeModule.BASIC_SALARY);
    }

    /** Returns the trimmed CSV column or "—" if the row is too short / null. */
    private static String safeColumn(String[] row, int idx) {
        if (row == null || idx < 0 || idx >= row.length)
            return "—";
        String value = row[idx];
        if (value == null || value.trim().isEmpty())
            return "—";
        return value.trim();
    }

    /**
     * Validates payroll form inputs and runs
     * {@link SalaryComputationModule#calculatePayroll}.
     *
     * Checks employee ID (required, numeric, exists in CSV), month selection,
     * and year (required, numeric, must be 2024). Errors are listed in the result
     * area and shown in an HTML bullet-list dialog.
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

        // Use parallel array instead of fragile arithmetic on the combo index
        String actualMonth = String.valueOf(MONTH_NUMBERS[monthCombo.getSelectedIndex()]);
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
     * Adds employee-number validation messages to the list (checks Employee Details
     * CSV).
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
        button.setOpaque(true); // macOS fix: force background paint so hover color renders
        button.setContentAreaFilled(true);
        button.setBackground(Color.white);
        button.setForeground(new Color(11, 29, 58));
        button.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Mirrors guiStyleAccentButton's pattern so secondary buttons also feel
        // interactive
        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PALETTE_LIGHT_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.white);
            }

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
     * Builds a full-width colored header strip and adds it to the frame at y=0.
     *
     * The strip uses {@link #ACCENT_BLUE} as background with white bold title text,
     * giving every main screen a consistent branded top bar. Callers must add
     * remaining content below {@link #HEADER_STRIP_HEIGHT}.
     *
     * @param title      white screen title (e.g. "MAIN MENU")
     * @param frameWidth current frame width so the strip spans the full content
     *                   area
     */
    private static void addColoredHeaderStrip(String title, int frameWidth) {
        JPanel header = new JPanel();
        header.setLayout(null);
        header.setBackground(ACCENT_BLUE);
        header.setBounds(0, 0, frameWidth, HEADER_STRIP_HEIGHT);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(HEADER_STRIP_FONT);
        lblTitle.setForeground(PALETTE_WHITE);
        lblTitle.setBounds(0, 0, frameWidth, HEADER_STRIP_HEIGHT);

        header.add(lblTitle);
        frame.add(header);
    }

    /**
     * Adds a small bottom-right "Logged in as: {user}" status label to the frame.
     *
     * The label's right edge is aligned with the screen's content right edge so the
     * footer visually lines up with the card / scroll pane above it instead of
     * floating in the dialog gutter.
     *
     * Skipped silently when no username is recorded (defensive only — main()
     * requires login).
     *
     * @param frameWidth    current frame width
     * @param contentHeight visible content-area height used to position the footer
     * @param rightMargin   right margin of the screen's main content (e.g. card
     *                      right margin),
     *                      so the footer right edge matches the card right edge
     */
    private static void addLoggedInFooter(int frameWidth, int contentHeight, int rightMargin) {
        if (loggedInUser == null || loggedInUser.isEmpty()) {
            return;
        }
        JLabel lblStatus = new JLabel("Logged in as: " + loggedInUser);
        lblStatus.setFont(STATUS_FONT);
        lblStatus.setForeground(TEXT_DARK_NAVY);
        lblStatus.setHorizontalAlignment(SwingConstants.RIGHT);
        int labelWidth = 250;
        lblStatus.setBounds(frameWidth - rightMargin - labelWidth, contentHeight - 28,
                labelWidth, 18);
        frame.add(lblStatus);
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

    /**
     * Builds the Self-Service profile dashboard exclusively for logged-in Employees.
     */
    public static void showEmployeeSelfServiceDashboard() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(870, 600);
        frame.getContentPane().setBackground(new Color(212, 228, 252));

        addColoredHeaderStrip("EMPLOYEE PROFILE SELF-SERVICE DASHBOARD", 870);

        String[] columnHeaders = { "Employee ID", "Last Name", "First Name", "Current Address", "Phone Number" };

        tableModel = new javax.swing.table.DefaultTableModel(columnHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        profileTable = new javax.swing.JTable(tableModel);
        profileTable.setFont(APP_FONT_PLAIN);
        profileTable.setRowHeight(28);
        profileTable.getTableHeader().setFont(APP_FONT_BOLD);
        profileTable.getTableHeader().setReorderingAllowed(false);

        List<String[]> allRecords = FileHandlerModule.getAllEmployees();
        for (String[] row : allRecords) {
            if (row.length > 5) {
                Object[] displayRow = {
                        safeColumn(row, EmployeeModule.ID),
                        safeColumn(row, EmployeeModule.LAST_NAME),
                        safeColumn(row, EmployeeModule.FIRST_NAME),
                        safeColumn(row, EmployeeModule.ADDRESS),
                        safeColumn(row, EmployeeModule.PHONE_NUMBER)
                };
                tableModel.addRow(displayRow);
            }
        }

        JScrollPane tableScrollPane = new JScrollPane(profileTable);
        tableScrollPane.setBounds(40, 75, 790, 200);
        frame.add(tableScrollPane);

        int formY = 290; 
        int fieldHeight = 30;
        int fieldXPosition = 250;   
        // Shortened field length down to 350px so it cuts off cleanly exactly where you drew the line
        int uniformFieldWidth = 350; 

        JLabel lblFormTitle = createStyledLabel("Profile:");
        lblFormTitle.setBounds(40, formY, 200, 25);
        frame.add(lblFormTitle);

        // Row 1: Target Employee ID Input
        JLabel lblEditId = createStyledLabel("Employee ID:");
        lblEditId.setBounds(40, formY + 30, 200, fieldHeight);
        frame.add(lblEditId);

        final JTextField txtEditId = createStyledTextField(true);
        txtEditId.setBounds(fieldXPosition, formY + 30, uniformFieldWidth, fieldHeight);
        frame.add(txtEditId);

        // Row 2: Editable Home Address Field
        JLabel lblEditAddress = createStyledLabel("Home Address:");
        lblEditAddress.setBounds(40, formY + 70, 200, fieldHeight);
        frame.add(lblEditAddress);

        final JTextField txtEditAddress = createStyledTextField(true);
        txtEditAddress.setBounds(fieldXPosition, formY + 70, uniformFieldWidth, fieldHeight);
        frame.add(txtEditAddress);

        // Row 3: Editable Contact Phone Number Field
        JLabel lblEditPhone = createStyledLabel("Phone Number:");
        lblEditPhone.setBounds(40, formY + 110, 200, fieldHeight);
        frame.add(lblEditPhone);

        final JTextField txtEditPhone = createStyledTextField(true);
        txtEditPhone.setBounds(fieldXPosition, formY + 110, uniformFieldWidth, fieldHeight);
        frame.add(txtEditPhone);

        // --- TABLE ROW CLICK LISTENER ---
        profileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRowIdx = profileTable.getSelectedRow();
                if (selectedRowIdx != -1) {
                    String idValue = tableModel.getValueAt(selectedRowIdx, 0).toString();
                    String addressValue = tableModel.getValueAt(selectedRowIdx, 3).toString();
                    String phoneValue = tableModel.getValueAt(selectedRowIdx, 4).toString();
                    
                    txtEditId.setText(idValue);
                    txtEditAddress.setText(addressValue);
                    txtEditPhone.setText(phoneValue);
                    
                    resetFieldBorder(txtEditId);
                }
            }
        });

        // --- LIVE SEARCH KEY LISTENER ---
        txtEditId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String inputIdStr = txtEditId.getText().trim();
                resetFieldBorder(txtEditId);

                if (inputIdStr.isEmpty()) {
                    txtEditAddress.setText("");
                    txtEditPhone.setText("");
                    profileTable.clearSelection();
                    return;
                }

                boolean recordMatchFound = false;
                for (int rowIdx = 0; rowIdx < tableModel.getRowCount(); rowIdx++) {
                    String tableId = tableModel.getValueAt(rowIdx, 0).toString().trim();
                    
                    if (tableId.equals(inputIdStr)) {
                        txtEditAddress.setText(tableModel.getValueAt(rowIdx, 3).toString());
                        txtEditPhone.setText(tableModel.getValueAt(rowIdx, 4).toString());
                        profileTable.setRowSelectionInterval(rowIdx, rowIdx);
                        recordMatchFound = true;
                        break;
                    }
                }

                if (!recordMatchFound) {
                    txtEditAddress.setText("");
                    txtEditPhone.setText("");
                    profileTable.clearSelection();
                }
            }
        });

        // --- RE-CENTERED CONTROL BUTTONS ---
        int dashboardButtonWidth = 180;
        int dashboardButtonHeight = 40;
        int actionButtonsY = formY + 165; 
        
        JButton btnSaveChanges = new JButton("Save Changes");
        btnSaveChanges.setBounds(245, actionButtonsY, dashboardButtonWidth, dashboardButtonHeight);
        guiStyleAccentButton(btnSaveChanges);
        btnSaveChanges.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String targetId = txtEditId.getText().trim();
                String newAddressValue = txtEditAddress.getText().trim();
                String newPhoneValue = txtEditPhone.getText().trim();

                if (targetId.isEmpty() || !targetId.matches("\\d+")) {
                    setFieldError(txtEditId);
                    JOptionPane.showMessageDialog(frame, "Please provide or select a valid Employee ID first.", 
                            "Input Validation Failure", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!FileHandlerModule.employeeExists(targetId)) {
                    setFieldError(txtEditId);
                    JOptionPane.showMessageDialog(frame, "The entered Employee ID \"" + targetId + "\" does not exist in our CSV dataset.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean writeStatusSuccess = FileHandlerModule.updateEmployeeContactInfo(targetId, newAddressValue, newPhoneValue);

                if (writeStatusSuccess) {
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (tableModel.getValueAt(i, 0).toString().trim().equals(targetId)) {
                            tableModel.setValueAt(newAddressValue, i, 3);
                            tableModel.setValueAt(newPhoneValue, i, 4);
                            break;
                        }
                    }
                    
                    JOptionPane.showMessageDialog(frame, "Successfully updated records for Employee #" + targetId + "!", 
                            "Database Saved", JOptionPane.INFORMATION_MESSAGE);
                    
                    txtEditId.setText("");
                    txtEditAddress.setText("");
                    txtEditPhone.setText("");
                    resetFieldBorder(txtEditId);
                    profileTable.clearSelection();
                } else {
                    JOptionPane.showMessageDialog(frame, "Critical failure: Could not update the CSV disk file data records.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        frame.add(btnSaveChanges);

        JButton btnBackToMenu = new JButton("Back to Main Menu");
        btnBackToMenu.setBounds(445, actionButtonsY, dashboardButtonWidth, dashboardButtonHeight);
        styleStandardButton(btnBackToMenu);
        btnBackToMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainMenu();
            }
        });
        frame.add(btnBackToMenu);

        addLoggedInFooter(870, 570, 40);
        updateDisplay();
    }
}
