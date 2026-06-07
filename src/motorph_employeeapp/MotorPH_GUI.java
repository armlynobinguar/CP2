

package motorph_employeeapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * MotorPH_GUI
 * -----------
 * Swing-based presentation layer for the MotorPH Employee Payroll System.
 *
 * This class builds and navigates all screens:
 *   - Login dialog (modal authentication gate)
 *   - Main menu (payroll, employee lookup, logout)
 *   - Pay coverage / payroll processing form (MPHCRO1)
 *   - Employee information lookup view
 *
 * Layout uses null layout ({@code setLayout(null)}) with explicit {@code setBounds}
 * positioning, following BroCode Java Swing tutorial patterns for frames, panels,
 * labels, text fields, buttons, and event listeners.
 *
 * Business logic is delegated to {@link FileHandlerModule}, {@link EmployeeModule},
 * and {@link SalaryComputationModule}; this class handles validation, styling,
 * navigation, and user feedback (dialogs and  error borders, result text areas).
 */
public class MotorPH_GUI {

    // --- Main application window and shared payroll controls ---
    static JPanel breadcrumbPanel = null;

    /** Primary JFrame container; content is swapped per screen via removeAll(). */
    static JFrame frame;

    /** Dropdown for pay coverage month (June-December in current dataset). */
    static JComboBox<String> monthCombo;

    // --- Payroll screen input and output widgets ---

    /** Employee ID entry on the pay coverage form. */
    static JTextField txtEmployeeNo;

    /** Read-only employee name; auto-filled from CSV when ID is valid. */
    static JTextField txtEmployeeName;

    /** Pay coverage year; validated to "2024" only. */
    static JTextField txtYear;

    /** Scrollable payslip / validation message output on payroll screen. */
    static JTextArea txtResultArea;

    /**
     * Parallel month-number lookup for {@link #monthCombo}.
     *
     * Index 0 is a sentinel ("no selection"); subsequent entries map combo positions
     * to actual calendar month numbers. Using this lookup avoids fragile arithmetic
     * like {@code getSelectedIndex() + 5}, which silently breaks if items change.
     */
    static final int[] MONTH_NUMBERS = {0, 6, 7, 8, 9, 10, 11, 12};

    // Calendar display state (month 1-12, year)
    static int CAL_MONTH = LocalDate.now().getMonthValue();
    static int CAL_YEAR = LocalDate.now().getYear();
    static boolean CAL_SHOW_BIRTHDAYS = true;
    static boolean CAL_SHOW_ATTENDANCE = true;
    static javax.swing.Timer statusBarTimer;

    // --- Employee lookup screen widgets ---

    /** Employee ID search box on the information lookup screen. */
    static JTextField txtLookupInput;

    /** Displays ID, full name, and birthday after a successful lookup. */
    static JTextArea txtLookupDisplay;

    // --- Employee records management (Feature 1 / Chantal CRUD) ---

    static JTable employeeTable;
    static DefaultTableModel employeeTableModel;
    static JTextField txtRecEmpNo;
    static JTextField txtRecLastName;
    static JTextField txtRecFirstName;
    static JTextField txtRecSSS;
    static JTextField txtRecPhilHealth;
    static JTextField txtRecTIN;
    static JTextField txtRecPagIBIG;
    static String selectedEmployeeId = null;

    // --- Typography: main app screens ---

    static final Font APP_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font APP_FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);
    static final int APP_FRAME_WIDTH = 1200;
    static final int APP_FRAME_HEIGHT = 800;
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

    // Refined sidebar and layout constants
    static final Color SIDEBAR_BG        = new Color(12, 35, 90);
    static final Color SIDEBAR_TOP_BG    = new Color(8, 25, 68);
    static final Color APP_BG            = new Color(243, 246, 252);
    static final Color CARD_BORDER_COLOR = new Color(225, 231, 240);
    static final int   SIDEBAR_WIDTH     = 200;

    /** Default echo character for the password field (used when toggling visibility). */
    static final char PASSWORD_ECHO_CHAR = '\u2022';

    // --- Login dialog component references (used by listeners and validators) ---

    static JDialog loginDialog;
    static JTextField usernameField;
    static JPasswordField passwordField;
    static JButton btnLogin;

    /** Username captured at login; rendered in each screen's bottom-right footer. */
    static String loggedInUser = "";

    /** Height of the colored header strip at the top of every main screen. */
    static final int HEADER_STRIP_HEIGHT = 50;
    /** Font used for the white screen title inside the colored header strip. */
    static final Font HEADER_STRIP_FONT = new Font("Segoe UI", Font.BOLD, 20);
    /** Font for the small "Logged in as: ..." status label. */
    static final Font STATUS_FONT = new Font("Segoe UI", Font.PLAIN, 12);

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
        loginDialog.setSize(436, 480);
        loginDialog.setModal(true); // Replaces Frame parent wrapper injection cleanly
        loginDialog.setLayout(null); // Setting layout manager to null for explicit setBounds tracking
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setResizable(false);

        // Base Root Panel Container (From Lesson: Panels)
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(null);
        rootPanel.setBackground(APP_BG);
        rootPanel.setBounds(0, 0, 420, 480);

        // Branded header card  solid accent blue with white text
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(null);
        headerPanel.setBackground(SIDEBAR_BG);
        headerPanel.setBounds(30, 20, 360, 90);
        headerPanel.setBorder(BorderFactory.createEmptyBorder());

        JLabel lblTitle = new JLabel("MotorPH", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(PALETTE_WHITE);
        lblTitle.setBounds(10, 14, 340, 30);

        JLabel lblSubtitle = new JLabel("Employee Payroll System", SwingConstants.CENTER);
        lblSubtitle.setFont(LOGIN_APP_FONT_PLAIN);
        lblSubtitle.setForeground(new Color(175, 205, 250));
        lblSubtitle.setBounds(10, 48, 340, 22);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);

        // Core Form Inputs Panel Sheet  sits directly below the blue header card
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null); // Explicit layout alignment matching absolute canvas
        formPanel.setBackground(PALETTE_WHITE);
        formPanel.setBounds(30, 110, 360, 310);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        // Login form vertical rhythm (kept consistent across every row):
        //   labelHeight=20, fieldHeight=38, checkboxHeight=22, buttonHeight=40
        //   gap between a label and its field   = 6px
        //   gap between sections (fieldlabel)  = 20px
        // Adjusting any of these values? Update every row below to keep the grid even.

        // Username row
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(LOGIN_APP_FONT_BOLD);
        lblUser.setForeground(TEXT_DARK_NAVY);
        lblUser.setBounds(30, 25, 300, 20); // y=25, ends y=45

        usernameField = new JTextField();
        usernameField.setBounds(30, 51, 300, 38); // 45 + 6 label-to-field gap, ends y=89
        styleInputField(usernameField);
        // Placeholder hint shown only when field is empty and unfocused (From Lesson: FocusListener)
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
        chkShowPassword.addActionListener(e -> {
                if (chkShowPassword.isSelected()) {
                    passwordField.setEchoChar((char) 0); // 0 = display characters as-is
                } else {
                    passwordField.setEchoChar(PASSWORD_ECHO_CHAR);
                }
        });

        // Login button (215 + 20 section gap = 235)
        btnLogin = new JButton("Login");
        btnLogin.setBounds(30, 235, 300, 40); // ends y=275
        styleAccentButton(btnLogin);

        // --- STICKING TO STANDARD ACTIONLISTENER INTERFACE ---
        btnLogin.addActionListener(e -> {
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
     * Styles the primary login button and wires|hover feedback via {@link MouseListener}.
     *
     * macOS-specific flags ({@code setOpaque}, {@code setContentAreaFilled},
     * {@code setBorderPainted}) ensure custom background colors render correctly.
     *
     * @param button login submit button
     */
    private static void styleAccentButton(JButton button) {
        button.setFont(LOGIN_APP_FONT_BOLD);
        button.setFocusable(false);
        button.setOpaque(true); // makes the button paint its background
        button.setContentAreaFilled(true); // forces the button area to be filled
        button.setBorderPainted(false); // removes the native macOS button chrome
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
     * Shows {@code hint} in gray when the field is empty and unfocused. On focus the hint
     * is cleared and the foreground reverts to the regular text color so user input is
     * displayed normally. The hint string is treated as empty by validators (callers should
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
     * Creates the main {@link JFrame} after successful login and opens the main menu.
     *
     * Window is fixed-size, centered, and exits the JVM on close.
     */
    public static void initialize() {
        frame = new JFrame();
        frame.setTitle("MotorPH Management System");
        // Larger dashboard-oriented window
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); // Matching BroCode default window properties
        frame.setLocationRelativeTo(null);

        // Start on the new dashboard layout which keeps all existing features.
        showDashboard();
    }

    private static void performLogout() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        loggedInUser = "";
        MotorPH_EmployeeApp.loginSuccessful = false;
        showCustomLoginDialog();
        if (MotorPH_EmployeeApp.loginSuccessful) {
            initialize();
        } else {
            System.exit(0);
        }
    }

    /**
     * Shows the new Dashboard screen featuring a left navigation bar, central
     * cards area and a right column for calendar/schedule. Existing features
     * (Payroll, Employee Lookup) are reachable from the sidebar or the cards.
     */
    static void showDashboard() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        int sidebarWidth = SIDEBAR_WIDTH;
        int gap = 20;
        int rightWidth = 280;
        int outerPadding = 20;

        buildAndAddSidebar("Dashboard");

        // Use frame insets for both axes so layout respects OS chrome and status bar
        java.awt.Insets ins = frame.getInsets();
        int visibleW = APP_FRAME_WIDTH - ins.left - ins.right;
        int visibleH = APP_FRAME_HEIGHT - ins.top - ins.bottom - STATUS_BAR_H;
        // header starts at y=16, height=70, then 16px gap → content starts at y=102
        int contentHeight = visibleH - 16 - 70 - 16 - 8; // 8px bottom margin

        int contentX = sidebarWidth + gap;
        int contentWidth = visibleW - sidebarWidth - gap - rightWidth - outerPadding;

        int headerWidth = contentWidth + rightWidth + outerPadding;
        int headerHeight = 70;

        JPanel header = new JPanel(null);
        header.setBackground(PALETTE_WHITE);
        header.setBounds(contentX, 16, headerWidth, headerHeight);
        header.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 235), 1));

        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pageTitle.setForeground(TEXT_DARK_NAVY);
        pageTitle.setBounds(20, 12, 300, 30);
        header.add(pageTitle);

        JLabel welcome = new JLabel("Welcome back, " + (loggedInUser.isEmpty() ? "User" : loggedInUser));
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcome.setForeground(new Color(100, 110, 135));
        welcome.setBounds(20, 43, 300, 18);
        header.add(welcome);

        int dateW = 210;
        JLabel dateLabel = new JLabel(LocalDate.now().getMonth().name() + " " + LocalDate.now().getYear(), SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(100, 110, 135));
        dateLabel.setBounds(headerWidth - dateW - 80, (headerHeight - 18) / 2, dateW, 18);
        header.add(dateLabel);

        JPanel content = new JPanel(null);
        content.setBackground(APP_BG);
        content.setBounds(contentX, header.getY() + header.getHeight() + 16, contentWidth, contentHeight);

        int cardGap = 24;
        int cardHeight = 120;
        int cardW = (contentWidth - cardGap) / 2; // evenly split with defined gap

        JPanel checksCard = buildInfoCard("Check Stubs", "View payroll and stubs", 0, 0, contentWidth, cardHeight);
        JButton btnOpenPayroll = new JButton("Open Pay");
        btnOpenPayroll.setBounds(14, 76, 100, 28);
        guiStyleAccentButton(btnOpenPayroll);
        btnOpenPayroll.addActionListener(e -> setupPayrollUI());
        checksCard.add(btnOpenPayroll);
        if (isPayrollStaff()) {
            JButton btnOpenRecords = new JButton("Manage Records");
            btnOpenRecords.setBounds(124, 76, 140, 28);
            guiStyleAccentButton(btnOpenRecords);
            btnOpenRecords.addActionListener(e -> showEmployeeRecordsUI());
            checksCard.add(btnOpenRecords);
        }
        content.add(checksCard);

        JPanel meetingsCard = buildInfoCard("Product Meetings", "No meetings scheduled", 0, cardHeight + cardGap, contentWidth, 220);
        content.add(meetingsCard);

        JPanel rightCol = new JPanel(null);
        rightCol.setBackground(PALETTE_WHITE);
        rightCol.setBounds(visibleW - rightWidth - outerPadding, content.getY(), rightWidth, contentHeight);
        rightCol.setBorder(null); // calPanel draws its own border

        // calPanel fills rightCol exactly: 280/7 = 40px per cell — no rounding gap
        JPanel calPanel = buildCalendarPanel(rightWidth, contentHeight, CAL_MONTH, CAL_YEAR);
        calPanel.setBounds(0, 0, rightWidth, contentHeight);
        rightCol.add(calPanel);

        frame.add(header);
        frame.add(content);
        frame.add(rightCol);
        addStatusBar();
        updateDisplay();
    }

    private static JButton createSidebarButton(String text, int x, int y, int w, int h, ActionListener action) {
        JButton b = new JButton(text);
        b.setBounds(x, y, w, h);
        b.setFont(APP_FONT_BOLD);
        b.setForeground(PALETTE_WHITE);
        b.setBackground(new Color(28, 78, 196));
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (action != null) b.addActionListener(action);
        return b;
    }

    /**
     * Builds and adds the deep-navy navigation sidebar to the frame.
     * The active page label is highlighted so users know where they are.
     */
    private static void buildAndAddSidebar(String activePage) {
        int sw = SIDEBAR_WIDTH;
        java.awt.Insets ins = frame.getInsets();
        // Sidebar stops at the top of the status bar so they never overlap
        int sidebarH = APP_FRAME_HEIGHT - ins.top - ins.bottom - STATUS_BAR_H;
        JPanel sidebar = new JPanel(null);
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBounds(0, 0, sw, sidebarH);

        // Darker brand header area
        JPanel brandArea = new JPanel(null);
        brandArea.setBackground(SIDEBAR_TOP_BG);
        brandArea.setBounds(0, 0, sw, 80);
        JLabel brand = new JLabel("MotorPH", SwingConstants.CENTER);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 21));
        brand.setForeground(PALETTE_WHITE);
        brand.setBounds(0, 14, sw, 28);
        JLabel brandSub = new JLabel("Payroll System", SwingConstants.CENTER);
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(new Color(150, 180, 230));
        brandSub.setBounds(0, 45, sw, 16);
        brandArea.add(brand);
        brandArea.add(brandSub);
        sidebar.add(brandArea);

        // Thin separator line
        JPanel sep = new JPanel();
        sep.setBackground(new Color(35, 70, 150));
        sep.setBounds(20, 80, sw - 40, 1);
        sidebar.add(sep);

        // Nav buttons
        int btnY = 96;
        int btnH = 44;
        sidebar.add(makeSidebarNavButton("Dashboard",     0, btnY, sw, btnH, "Dashboard".equals(activePage),     e -> showDashboard()));
        btnY += btnH + 4;
        sidebar.add(makeSidebarNavButton("Lookup",        0, btnY, sw, btnH, "Lookup".equals(activePage),        e -> showEmployeeLookupUI()));
        btnY += btnH + 4;
        if (isPayrollStaff()) {
            sidebar.add(makeSidebarNavButton("Records",   0, btnY, sw, btnH, "Records".equals(activePage),       e -> showEmployeeRecordsUI()));
            btnY += btnH + 4;
        }
        sidebar.add(makeSidebarNavButton("Payroll",       0, btnY, sw, btnH, "Payroll".equals(activePage),       e -> setupPayrollUI()));
        btnY += btnH + 4;
        sidebar.add(makeSidebarNavButton("Notifications", 0, btnY, sw, btnH, "Notifications".equals(activePage), e -> showNotificationsUI()));
        btnY += btnH + 4;
        sidebar.add(makeSidebarNavButton("Help",          0, btnY, sw, btnH, "Help".equals(activePage),          e -> showHelpCenterUI()));

        // Logout pinned near the bottom
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(16, sidebarH - 58, sw - 32, 40);
        logoutBtn.setFont(APP_FONT_BOLD);
        logoutBtn.setForeground(PALETTE_WHITE);
        logoutBtn.setBackground(new Color(200, 50, 45));
        logoutBtn.setOpaque(true);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("Sign out and return to login");
        logoutBtn.addActionListener(e -> performLogout());
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { logoutBtn.setBackground(new Color(170, 35, 30)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { logoutBtn.setBackground(new Color(200, 50, 45)); }
        });
        sidebar.add(logoutBtn);

        frame.add(sidebar);
    }

    /** Left-aligned sidebar nav button with active highlight and hover effect. */
    private static JButton makeSidebarNavButton(String text, int x, int y, int w, int h,
                                                boolean active, ActionListener action) {
        JButton b = new JButton(text);
        b.setBounds(x, y, w, h);
        b.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        b.setForeground(active ? PALETTE_WHITE : new Color(175, 200, 240));
        b.setBackground(active ? new Color(24, 80, 180) : SIDEBAR_BG);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        if (action != null) b.addActionListener(action);
        if (!active) {
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    b.setBackground(new Color(24, 58, 135));
                    b.setForeground(PALETTE_WHITE);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    b.setBackground(SIDEBAR_BG);
                    b.setForeground(new Color(175, 200, 240));
                }
            });
        }
        return b;
    }

    /**
     * Adds a white top-bar header (page title + logged-in user) for non-dashboard screens.
     * Content should begin at y=62 after calling this.
     */
    private static void addPageHeader(String title) {
        int contentW = APP_FRAME_WIDTH - SIDEBAR_WIDTH;
        JPanel topBar = new JPanel(null);
        topBar.setBackground(PALETTE_WHITE);
        topBar.setBounds(SIDEBAR_WIDTH, 0, contentW, 60);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER_COLOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(TEXT_DARK_NAVY);
        titleLbl.setBounds(24, 16, 400, 28);
        topBar.add(titleLbl);

        if (!loggedInUser.isEmpty()) {
            JLabel userLbl = new JLabel("Logged in as: " + loggedInUser, SwingConstants.RIGHT);
            userLbl.setFont(STATUS_FONT);
            userLbl.setForeground(new Color(110, 120, 145));
            userLbl.setBounds(contentW - 280, 22, 200, 18);
            topBar.add(userLbl);
        }
        frame.add(topBar);
    }

    static final int STATUS_BAR_H = 24;

    private static void addStatusBar() {
        if (statusBarTimer != null && statusBarTimer.isRunning()) statusBarTimer.stop();

        // Use frame insets so the position is correct before the layout is validated
        java.awt.Insets ins = frame.getInsets();
        int visibleH = APP_FRAME_HEIGHT - ins.top - ins.bottom;
        int barY = visibleH - STATUS_BAR_H;

        JPanel bar = new JPanel(null);
        bar.setBackground(SIDEBAR_BG);
        bar.setBounds(0, barY, APP_FRAME_WIDTH, STATUS_BAR_H);

        JLabel timeLbl = new JLabel();
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(new Color(175, 200, 240));
        timeLbl.setBounds(10, 4, 320, 16);
        bar.add(timeLbl);

        JLabel onlineLbl = new JLabel("  Online");
        onlineLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        onlineLbl.setForeground(new Color(72, 210, 120));
        onlineLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        onlineLbl.setBounds(APP_FRAME_WIDTH - 120, 4, 110, 16);
        bar.add(onlineLbl);

        Runnable tick = () -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String dow = now.getDayOfWeek().name();
            dow = dow.charAt(0) + dow.substring(1, 3).toLowerCase();
            String mon = now.getMonth().name();
            mon = mon.charAt(0) + mon.substring(1, 3).toLowerCase();
            timeLbl.setText("  " + dow + ", " + mon + " " + now.getDayOfMonth()
                + ", " + now.getYear()
                + "    "
                + String.format("%02d:%02d:%02d", now.getHour(), now.getMinute(), now.getSecond()));
        };
        tick.run();

        statusBarTimer = new javax.swing.Timer(1000, e -> tick.run());
        statusBarTimer.start();

        frame.add(bar);
        // z-order 0 = painted on top of all other components (sidebar, panels, etc.)
        frame.getContentPane().setComponentZOrder(bar, 0);
    }

    private static JPanel buildInfoCard(String title, String subtitle, int x, int y, int w, int h) {
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(PALETTE_WHITE);
        p.setBounds(x, y, w, h);
        p.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        // 4 px accent bar on the left edge
        JPanel accent = new JPanel();
        accent.setBackground(ACCENT_BLUE);
        accent.setBounds(0, 0, 4, h);
        p.add(accent);

        JLabel t = new JLabel(title);
        t.setFont(APP_FONT_BOLD);
        t.setForeground(new Color(11, 29, 58));
        t.setBounds(18, 12, w - 28, 22);

        JLabel s;
        if (subtitle != null && subtitle.startsWith("Total:")) {
            s = new JLabel(subtitle);
            s.setFont(new Font("Segoe UI", Font.BOLD, 18));
            s.setForeground(ACCENT_BLUE);
        } else {
            s = new JLabel(subtitle == null ? "" : subtitle);
            s.setFont(APP_FONT_PLAIN);
            s.setForeground(new Color(90, 100, 125));
        }
        s.setBounds(18, 40, w - 28, 26);
        p.add(t);
        p.add(s);
        return p;
    }

    /**
     * Builds a small calendar panel showing the current month and highlights
     * birthdays pulled from the Employee CSV (EmployeeModule.BIRTHDAY).
     * The panel includes a grid of days and an event list for the selected day.
     *
     * @param width  desired panel width
     * @param height desired panel height
     * @return JPanel containing calendar and events list
     */
    private static JPanel buildCalendarPanel(int width, int height, int month, int year) {
        // --- Layout constants ---
        final int NAV_Y    = 8;
        final int NAV_H    = 26;
        final int DOW_Y    = NAV_Y + NAV_H + 6;
        final int DOW_H    = 20;
        final int GRID_TOP = DOW_Y + DOW_H;
        final int ROWS     = 6;
        final int CELL_H   = 44;
        final int GRID_BOT = GRID_TOP + ROWS * CELL_H;
        final int EVT_Y    = GRID_BOT + 10;
        final int EVT_H    = height - EVT_Y - 8;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        LocalDate viewDate = LocalDate.of(year, month, 1);

        JButton btnPrev = new JButton("<");
        btnPrev.setBounds(6, NAV_Y, 28, NAV_H);
        styleCalNavButton(btnPrev);
        btnPrev.addActionListener(e -> {
            CAL_MONTH--;
            if (CAL_MONTH < 1) { CAL_MONTH = 12; CAL_YEAR--; }
            showDashboard();
        });
        panel.add(btnPrev);

        JLabel monthLabel = new JLabel(viewDate.getMonth().name() + " " + year, SwingConstants.CENTER);
        monthLabel.setFont(APP_FONT_BOLD);
        monthLabel.setForeground(TEXT_DARK_NAVY);
        monthLabel.setBounds(38, NAV_Y, width - 76, NAV_H);
        panel.add(monthLabel);

        JButton btnNext = new JButton(">");
        btnNext.setBounds(width - 34, NAV_Y, 28, NAV_H);
        styleCalNavButton(btnNext);
        btnNext.addActionListener(e -> {
            CAL_MONTH++;
            if (CAL_MONTH > 12) { CAL_MONTH = 1; CAL_YEAR++; }
            showDashboard();
        });
        panel.add(btnNext);

        Map<Integer, java.util.List<String>> birthdays = new HashMap<>();
        List<String[]> all = FileHandlerModule.getAllEmployees();
        for (String[] row : all) {
            String b = safeColumn(row, EmployeeModule.BIRTHDAY);
            if (b == null || b.isEmpty() || b.equals("-") || !b.contains("/")) continue;
            try {
                String[] parts = b.split("/");
                if (parts.length < 2) continue;
                int m = Integer.parseInt(parts[0].trim());
                int d = Integer.parseInt(parts[1].trim());
                if (m == month) birthdays.computeIfAbsent(d, k -> new LinkedList<>()).add(EmployeeModule.fullName(row));
            } catch (NumberFormatException ex) { /* ignore malformed */ }
        }

        Map<Integer, java.util.List<String>> attendanceMap = new HashMap<>();
        List<String[]> attendance = FileHandlerModule.getAllAttendanceRecords();
        for (String[] arow : attendance) {
            if (arow.length < 3) continue;
            String dateStr = arow[2];
            if (!dateStr.contains("/")) continue;
            try {
                String[] p = dateStr.split("/");
                if (p.length < 2) continue;
                int m = Integer.parseInt(p[0].trim());
                int d = Integer.parseInt(p[1].trim());
                if (m == month) {
                    String name = arow.length > 1 ? arow[1] : arow[0];
                    attendanceMap.computeIfAbsent(d, k -> new LinkedList<>()).add(name + " (" + arow[0] + ")");
                }
            } catch (NumberFormatException ex) { /* ignore malformed */ }
        }

        int cellW = width / 7;
        String[] dow = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (int c = 0; c < 7; c++) {
            JLabel l = new JLabel(dow[c], SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            l.setForeground(new Color(90, 100, 130));
            l.setOpaque(true);
            l.setBackground(new Color(245, 247, 252));
            l.setBounds(c * cellW, DOW_Y, cellW, DOW_H);
            l.setBorder(BorderFactory.createMatteBorder(1, 0, 1, c < 6 ? 1 : 0, new Color(230, 234, 242)));
            panel.add(l);
        }

        JTextArea eventsArea = new JTextArea("Select a day to see events.");
        eventsArea.setEditable(false);
        eventsArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        eventsArea.setForeground(new Color(40, 55, 90));
        eventsArea.setBackground(new Color(248, 250, 254));
        eventsArea.setLineWrap(true);
        eventsArea.setWrapStyleWord(true);
        eventsArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane eventsScroll = new JScrollPane(eventsArea);
        eventsScroll.setBounds(0, EVT_Y, width, EVT_H);
        eventsScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER_COLOR));
        eventsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        LocalDate first = LocalDate.of(year, month, 1);
        int shift = first.getDayOfWeek().getValue() % 7;
        int totalDays = first.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < 7; c++) {
                int index = r * 7 + c;
                int displayDay = index - shift + 1;
                JPanel cell = new JPanel(null);
                cell.setOpaque(true);
                cell.setBounds(c * cellW, GRID_TOP + r * CELL_H, cellW, CELL_H);

                if (displayDay >= 1 && displayDay <= totalDays) {
                    boolean isPayDay    = (displayDay == 15) || (displayDay == totalDays);
                    boolean hasBirthday = birthdays.containsKey(displayDay);
                    boolean hasAttend   = attendanceMap.containsKey(displayDay);
                    boolean isToday     = (year == today.getYear()
                                       && month == today.getMonthValue()
                                       && displayDay == today.getDayOfMonth());

                    // Today always wins over other backgrounds; pay day / birthday shown via dots
                    Color cellBg = PALETTE_WHITE;
                    if (isPayDay)                 cellBg = new Color(232, 244, 255);
                    if (hasBirthday && !isPayDay) cellBg = new Color(232, 252, 232);
                    if (isToday)                  cellBg = new Color(235, 245, 255);
                    cell.setBackground(cellBg);

                    // Today gets a 2 px accent-blue top bar and a clean right/bottom divider
                    if (isToday) {
                        cell.setBorder(BorderFactory.createMatteBorder(
                            2, 0, 1, c < 6 ? 1 : 0, ACCENT_BLUE));
                    } else {
                        cell.setBorder(BorderFactory.createMatteBorder(
                            0, 0, 1, c < 6 ? 1 : 0, new Color(232, 236, 244)));
                    }

                    // Day number: solid blue badge with white text for today, plain otherwise
                    if (isToday) {
                        int badgeW = Math.min(cellW - 8, 22);
                        JPanel badge = new JPanel(null);
                        badge.setBackground(ACCENT_BLUE);
                        badge.setOpaque(true);
                        badge.setBounds(4, 3, badgeW, 16);
                        JLabel lblDay = new JLabel(String.valueOf(displayDay), SwingConstants.CENTER);
                        lblDay.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        lblDay.setForeground(PALETTE_WHITE);
                        lblDay.setBounds(0, 0, badgeW, 16);
                        badge.add(lblDay);
                        cell.add(badge);
                    } else {
                        JLabel lblDay = new JLabel(String.valueOf(displayDay));
                        lblDay.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        lblDay.setForeground(new Color(25, 40, 65));
                        lblDay.setBounds(4, 3, cellW - 8, 14);
                        cell.add(lblDay);
                    }

                    int dotX = 4;
                    if (hasBirthday) {
                        JLabel dot = new JLabel("*");
                        dot.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        dot.setForeground(new Color(40, 170, 70));
                        dot.setBounds(dotX, CELL_H - 16, 12, 12);
                        cell.add(dot);
                        dotX += 12;
                    }
                    if (isPayDay) {
                        JLabel dot = new JLabel("P");
                        dot.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        dot.setForeground(ACCENT_BLUE);
                        dot.setBounds(dotX, CELL_H - 16, 12, 12);
                        cell.add(dot);
                    }
                    if (hasAttend) {
                        JLabel dot = new JLabel(String.valueOf(attendanceMap.get(displayDay).size()));
                        dot.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                        dot.setForeground(new Color(130, 130, 130));
                        dot.setBounds(cellW - 16, CELL_H - 16, 14, 12);
                        cell.add(dot);
                    }

                    StringBuilder tip = new StringBuilder();
                    if (isToday)     tip.append("Today\n");
                    if (isPayDay)    tip.append("Pay Day\n");
                    if (hasBirthday) tip.append("Birthday(s): ").append(birthdays.get(displayDay).size()).append("\n");
                    if (hasAttend)   tip.append("Attendance: ").append(attendanceMap.get(displayDay).size());
                    if (tip.length() == 0) tip.append("No events");
                    cell.setToolTipText(tip.toString().trim());

                    final int dnum    = displayDay;
                    final Color normBg = cellBg;
                    cell.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                            StringBuilder sb = new StringBuilder();
                            if ((dnum == 15) || (dnum == totalDays))
                                sb.append("Pay Day: ").append(dnum).append("/").append(month).append("\n\n");
                            if (birthdays.containsKey(dnum)) {
                                sb.append("Birthdays (").append(dnum).append("/").append(month).append("):\n");
                                for (String n : birthdays.get(dnum)) sb.append("  - ").append(n).append("\n");
                                sb.append("\n");
                            }
                            if (attendanceMap.containsKey(dnum)) {
                                sb.append("Attendance (").append(dnum).append("/").append(month).append("):\n");
                                for (String n : attendanceMap.get(dnum)) sb.append("  - ").append(n).append("\n");
                            }
                            if (sb.length() == 0)
                                sb.append("No events on ").append(dnum).append(" ").append(viewDate.getMonth().name()).append(".");
                            eventsArea.setText(sb.toString());
                            eventsArea.setCaretPosition(0);
                        }
                        @Override public void mouseEntered(java.awt.event.MouseEvent e) { cell.setBackground(new Color(220, 235, 255)); }
                        @Override public void mouseExited(java.awt.event.MouseEvent e)  { cell.setBackground(normBg); }
                    });
                } else {
                    cell.setBackground(new Color(248, 250, 253));
                }
                panel.add(cell);
            }
        }

        panel.add(eventsScroll);
        return panel;
    }

    /** Minimal styling for the calendar prev/next navigation buttons. */
    private static void styleCalNavButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusable(false);
        b.setOpaque(true);
        b.setBackground(new Color(237, 243, 255));
        b.setForeground(TEXT_DARK_NAVY);
        b.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(new Color(210, 228, 255)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(new Color(237, 243, 255)); }
        });
    }


    /**
     * Clears the frame and displays the main navigation hub with three actions:
     * pay coverage (payroll), employee information lookup, and logout.
     */
    @SuppressWarnings("unused")
    static void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.setLayout(null); // Setting layout to null to use setBounds explicitly
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.getContentPane().setBackground(new Color(212, 228, 252));

        // Branded blue header strip replaces the prior plain title label
        addColoredHeaderStrip("MAIN MENU", APP_FRAME_WIDTH);
        setBreadcrumb("Main Menu");

        // Container Panel (Lesson: Panels)  shifted just below the header strip.
        // x = (frameWidth 520 - cardWidth 400) / 2 = 60, so left and right margins both equal 60.
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.white);
        menuPanel.setLayout(null); // Using null layout within the panel container
        menuPanel.setBounds(30, 70, 400, 380);
        menuPanel.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        // Navigation Menu Interactive Buttons (Lesson: Buttons)
        JButton btnPayroll = new JButton("1. MPHCRO1: Pay Coverage");
        btnPayroll.setBounds(40, 50, 320, 60);
        guiStyleAccentButton(btnPayroll);
        btnPayroll.addActionListener(e -> setupPayrollUI());

        JButton btnInfo = new JButton("2. Employee Information");
        btnInfo.setBounds(40, 160, 320, 60);
        guiStyleAccentButton(btnInfo);
        btnInfo.addActionListener(e -> showEmployeeLookupUI());

        JButton btnLogout = new JButton("3. Logout");
        btnLogout.setBounds(40, 270, 320, 60);
        styleStandardButton(btnLogout);
        btnLogout.addActionListener(e -> System.exit(0));

        menuPanel.add(btnPayroll);
        menuPanel.add(btnInfo);
        menuPanel.add(btnLogout);

        frame.add(menuPanel);
        addLoggedInFooter(APP_FRAME_WIDTH, APP_FRAME_HEIGHT - 20, 770); // Align with menu panel right edge
        updateDisplay();
    }

    /**
     * Builds the MPHCRO1 pay coverage screen: employee ID, month/year selection,
     * process/back buttons, and scrollable payslip output area.
     *
     * Employee name auto-updates on key release; Enter on ID field triggers validation.
     */
    static void setupPayrollUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Payroll");
        addPageHeader("Pay Coverage");

        // Form card centred within the content area (right of sidebar)
        int payrollWidth = 580;
        int contentAreaW = APP_FRAME_WIDTH - SIDEBAR_WIDTH;
        int payrollX = SIDEBAR_WIDTH + (contentAreaW - payrollWidth) / 2;
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(PALETTE_WHITE);
        formPanel.setBounds(payrollX, 76, payrollWidth, 285);
        formPanel.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        // Form columns: label x=30 width=230 (ends x=260),
        //               field x=260 width=260 (ends x=520)  matches scroll pane edge.
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
        String[] months = {" ", "06 - June",
                        "07 - July", "08 - August", "09 - September",
                        "10 - October", "11 - November", "12 - December"};
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
        JButton btnProcess = new JButton("Compute Salaries");
        btnProcess.setBounds(30, 215, 235, 45);
        guiStyleAccentButton(btnProcess);
        btnProcess.addActionListener(e -> runPayrollCalculation());

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(285, 215, 235, 45);
        styleStandardButton(btnBack);
        btnBack.addActionListener(e -> showDashboard());

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

        // Output label + scrollable result area below the form card
        JLabel lblOutputHeader = createStyledLabel("Payroll Output:");
        lblOutputHeader.setBounds(payrollX, 371, 200, 20);
        frame.add(lblOutputHeader);

        txtResultArea = new JTextArea();
        txtResultArea.setBackground(PALETTE_WHITE);
        txtResultArea.setForeground(new Color(11, 29, 58));
        txtResultArea.setFont(RECEIPT_FONT);
        txtResultArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(txtResultArea);
        scrollPane.setBounds(payrollX, 393, payrollWidth, 320);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        frame.add(formPanel);
        frame.add(scrollPane);
        addStatusBar();
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
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Lookup");
        addPageHeader("Employee Lookup");

        int lookupPanelWidth = 560;
        int contentAreaW = APP_FRAME_WIDTH - SIDEBAR_WIDTH;
        int lookupPanelX = SIDEBAR_WIDTH + (contentAreaW - lookupPanelWidth) / 2;
        JPanel lookupPanel = new JPanel();
        lookupPanel.setLayout(null);
        lookupPanel.setBackground(PALETTE_WHITE);
        lookupPanel.setBounds(lookupPanelX, 76, lookupPanelWidth, 450);
        lookupPanel.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        JLabel lblPrompt = createStyledLabel("Enter Employee ID:");
        lblPrompt.setBounds(30, 25, lookupPanelWidth - 60, 25);

        txtLookupInput = createStyledTextField(true);
        txtLookupInput.setBounds(30, 55, lookupPanelWidth - 60, 35);
        txtLookupInput.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    runEmployeeLookupAction();
                }
            }
            @Override public void keyReleased(KeyEvent e) {
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
        btnSearch.addActionListener(e -> runEmployeeLookupAction());

        JButton btnClose = new JButton("Back");
        btnClose.setBounds(210, 105, 160, 40);
        styleStandardButton(btnClose);
        btnClose.addActionListener(e -> showDashboard());

        txtLookupDisplay = new JTextArea();
        // Monospace font keeps the multi-row "Field: value" output cleanly aligned
        txtLookupDisplay.setFont(RECEIPT_FONT);
        txtLookupDisplay.setBackground(new Color(240, 246, 255));
        txtLookupDisplay.setForeground(new Color(11, 29, 58));
        txtLookupDisplay.setEditable(false);

        JScrollPane infoScroll = new JScrollPane(txtLookupDisplay);
        infoScroll.setBounds(30, 165, lookupPanelWidth - 60, 220);
        infoScroll.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        lookupPanel.add(lblPrompt);
        lookupPanel.add(txtLookupInput);
        lookupPanel.add(btnSearch);
        lookupPanel.add(btnClose);
        lookupPanel.add(infoScroll);

        frame.add(lookupPanel);
        addStatusBar();
        updateDisplay();
    }

    /** True when the logged-in user has payroll-staff privileges (employee record CRUD). */
    private static boolean isPayrollStaff() {
        return MotorPH_EmployeeApp.AUTH_VAL_2.equals(loggedInUser);
    }

    /**
     * Employee Records screen (Feature 1): JTable listing, form fields, and Add / Update / Delete.
     * Available to payroll staff only.
     */
    static void showEmployeeRecordsUI() {
        if (!isPayrollStaff()) {
            JOptionPane.showMessageDialog(frame,
                    "Employee record management is available to payroll staff only.",
                    "Access Restricted", JOptionPane.WARNING_MESSAGE);
            showDashboard();
            return;
        }

        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Records");
        addPageHeader("Employee Records");

        int panelX = SIDEBAR_WIDTH + 16;
        int panelY = 76;
        int panelW = APP_FRAME_WIDTH - SIDEBAR_WIDTH - 32;
        int contentH = frame.getContentPane().getHeight();
        int panelH = (contentH > 100 ? contentH : APP_FRAME_HEIGHT - 34) - panelY - 36;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(panelX, panelY, panelW, panelH);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        JLabel tableTitle = new JLabel("Employee List");
        tableTitle.setFont(APP_FONT_BOLD);
        tableTitle.setForeground(TEXT_DARK_NAVY);
        tableTitle.setBounds(16, 14, 200, 22);
        panel.add(tableTitle);

        employeeTableModel = new DefaultTableModel(EmployeeRecordsModule.TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(employeeTableModel);
        employeeTable.setFont(APP_FONT_PLAIN);
        employeeTable.setRowHeight(28);
        employeeTable.setSelectionBackground(new Color(220, 236, 255));
        employeeTable.setSelectionForeground(TEXT_DARK_NAVY);
        employeeTable.setGridColor(CARD_BORDER_COLOR);
        employeeTable.getTableHeader().setFont(APP_FONT_BOLD);
        employeeTable.getTableHeader().setBackground(new Color(245, 248, 252));
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int tableW = (int) (panelW * 0.58);
        int formX = tableW + 24;
        int formW = panelW - formX - 16;

        JScrollPane tableScroll = new JScrollPane(employeeTable);
        tableScroll.setBounds(16, 42, tableW, panelH - 58);
        tableScroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(tableScroll);

        JLabel formTitle = new JLabel("Record Details");
        formTitle.setFont(APP_FONT_BOLD);
        formTitle.setForeground(TEXT_DARK_NAVY);
        formTitle.setBounds(formX, 14, formW, 22);
        panel.add(formTitle);

        int fy = 46;
        int labelW = 110;
        int fieldX = formX + labelW + 8;
        int fieldW = formW - labelW - 8;
        int rowH = 30;
        int rowGap = 10;

        txtRecEmpNo = addRecordFormField(panel, formX, fy, labelW, fieldX, fieldW, rowH, "Employee #:", true);
        fy += rowH + rowGap;
        txtRecLastName = addRecordFormField(panel, formX, fy, labelW, fieldX, fieldW, rowH, "Last Name:", true);
        fy += rowH + rowGap;
        txtRecFirstName = addRecordFormField(panel, formX, fy, labelW, fieldX, fieldW, rowH, "First Name:", true);
        fy += rowH + rowGap;
        txtRecSSS = addRecordFormField(panel, formX, fy, labelW, fieldX, fieldW, rowH, "SSS #:", true);
        fy += rowH + rowGap;
        txtRecPhilHealth = addRecordFormField(panel, formX, fy, labelW, fieldX, fieldW, rowH, "PhilHealth #:", true);
        fy += rowH + rowGap;
        txtRecTIN = addRecordFormField(panel, formX, fy, labelW, fieldX, fieldW, rowH, "TIN #:", true);
        fy += rowH + rowGap;
        txtRecPagIBIG = addRecordFormField(panel, formX, fy, labelW, fieldX, fieldW, rowH, "Pag-IBIG #:", true);
        fy += rowH + 18;

        JButton btnAdd = new JButton("Add Record");
        btnAdd.setBounds(formX, fy, 108, 34);
        guiStyleAccentButton(btnAdd);
        btnAdd.addActionListener(e -> runAddEmployeeRecord());

        JButton btnUpdate = new JButton("Update");
        btnUpdate.setBounds(formX + 116, fy, 88, 34);
        styleStandardButton(btnUpdate);
        btnUpdate.addActionListener(e -> runUpdateEmployeeRecord());

        JButton btnDelete = new JButton("Delete");
        btnDelete.setBounds(formX + 212, fy, 88, 34);
        styleStandardButton(btnDelete);
        btnDelete.addActionListener(e -> runDeleteEmployeeRecord());

        JButton btnClear = new JButton("Clear");
        btnClear.setBounds(formX, fy + 42, formW, 34);
        styleStandardButton(btnClear);
        btnClear.addActionListener(e -> clearEmployeeRecordForm());

        employeeTable.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = employeeTable.getSelectedRow();
            if (row < 0) {
                return;
            }
            String id = String.valueOf(employeeTableModel.getValueAt(row, 0)).trim();
            String data = FileHandlerModule.findEmployeeData(id);
            if (data == null) {
                return;
            }
            populateEmployeeRecordForm(FileHandlerModule.smartSplit(data));
            selectedEmployeeId = id;
            txtRecEmpNo.setEditable(false);
        });

        refreshEmployeeTable();
        frame.add(panel);
        addStatusBar();
        updateDisplay();
    }

    private static JTextField addRecordFormField(JPanel panel, int labelX, int y, int labelW,
            int fieldX, int fieldW, int height, String labelText, boolean editable) {
        JLabel label = createStyledLabel(labelText);
        label.setBounds(labelX, y, labelW, height);
        panel.add(label);

        JTextField field = createStyledTextField(editable);
        field.setBounds(fieldX, y, fieldW, height);
        panel.add(field);
        return field;
    }

    private static void refreshEmployeeTable() {
        if (employeeTableModel == null) {
            return;
        }
        employeeTableModel.setRowCount(0);
        for (String[] emp : FileHandlerModule.getAllEmployees()) {
            employeeTableModel.addRow(EmployeeRecordsModule.toTableRow(emp));
        }
    }

    private static void populateEmployeeRecordForm(String[] emp) {
        resetEmployeeRecordFieldBorders();
        txtRecEmpNo.setText(safeColumn(emp, EmployeeModule.ID).equals("-") ? "" : safeColumn(emp, EmployeeModule.ID));
        txtRecLastName.setText(safeColumn(emp, EmployeeModule.LAST_NAME).equals("-") ? "" : safeColumn(emp, EmployeeModule.LAST_NAME));
        txtRecFirstName.setText(safeColumn(emp, EmployeeModule.FIRST_NAME).equals("-") ? "" : safeColumn(emp, EmployeeModule.FIRST_NAME));
        txtRecSSS.setText(safeColumn(emp, EmployeeModule.SSS).equals("-") ? "" : safeColumn(emp, EmployeeModule.SSS));
        txtRecPhilHealth.setText(safeColumn(emp, EmployeeModule.PHILHEALTH).equals("-") ? "" : safeColumn(emp, EmployeeModule.PHILHEALTH));
        txtRecTIN.setText(safeColumn(emp, EmployeeModule.TIN).equals("-") ? "" : safeColumn(emp, EmployeeModule.TIN));
        txtRecPagIBIG.setText(safeColumn(emp, EmployeeModule.PAGIBIG).equals("-") ? "" : safeColumn(emp, EmployeeModule.PAGIBIG));
    }

    private static void clearEmployeeRecordForm() {
        selectedEmployeeId = null;
        if (employeeTable != null) {
            employeeTable.clearSelection();
        }
        if (txtRecEmpNo != null) {
            txtRecEmpNo.setText("");
            txtRecEmpNo.setEditable(true);
        }
        if (txtRecLastName != null) txtRecLastName.setText("");
        if (txtRecFirstName != null) txtRecFirstName.setText("");
        if (txtRecSSS != null) txtRecSSS.setText("");
        if (txtRecPhilHealth != null) txtRecPhilHealth.setText("");
        if (txtRecTIN != null) txtRecTIN.setText("");
        if (txtRecPagIBIG != null) txtRecPagIBIG.setText("");
        resetEmployeeRecordFieldBorders();
    }

    private static void resetEmployeeRecordFieldBorders() {
        resetFieldBorder(txtRecEmpNo);
        resetFieldBorder(txtRecLastName);
        resetFieldBorder(txtRecFirstName);
        resetFieldBorder(txtRecSSS);
        resetFieldBorder(txtRecPhilHealth);
        resetFieldBorder(txtRecTIN);
        resetFieldBorder(txtRecPagIBIG);
    }

    private static void markEmployeeRecordFieldErrors(List<String> errors) {
        for (String err : errors) {
            if (err.contains("Employee Number")) setFieldError(txtRecEmpNo);
            if (err.contains("Last Name")) setFieldError(txtRecLastName);
            if (err.contains("First Name")) setFieldError(txtRecFirstName);
            if (err.contains("SSS")) setFieldError(txtRecSSS);
            if (err.contains("PhilHealth")) setFieldError(txtRecPhilHealth);
            if (err.contains("TIN")) setFieldError(txtRecTIN);
            if (err.contains("Pag-IBIG")) setFieldError(txtRecPagIBIG);
        }
    }

    private static void runAddEmployeeRecord() {
        resetEmployeeRecordFieldBorders();
        String empNo = txtRecEmpNo.getText().trim();
        String last = txtRecLastName.getText().trim();
        String first = txtRecFirstName.getText().trim();
        String sss = txtRecSSS.getText().trim();
        String phil = txtRecPhilHealth.getText().trim();
        String tin = txtRecTIN.getText().trim();
        String pagibig = txtRecPagIBIG.getText().trim();

        List<String> errors = EmployeeRecordsModule.validateForm(
                empNo, last, first, sss, phil, tin, pagibig, false, null);
        if (!errors.isEmpty()) {
            markEmployeeRecordFieldErrors(errors);
            showBulletErrorDialog(frame, errors, "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] row = EmployeeRecordsModule.createNewRow(empNo, last, first, sss, phil, tin, pagibig);
        if (!FileHandlerModule.appendEmployeeRecord(FileHandlerModule.joinCsvLine(row))) {
            JOptionPane.showMessageDialog(frame,
                    "Could not save the employee record. Please check file permissions.",
                    "Save Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshEmployeeTable();
        clearEmployeeRecordForm();
        JOptionPane.showMessageDialog(frame,
                "Employee record added successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void runUpdateEmployeeRecord() {
        if (selectedEmployeeId == null || selectedEmployeeId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Select a record from the table before updating.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resetEmployeeRecordFieldBorders();
        String empNo = txtRecEmpNo.getText().trim();
        String last = txtRecLastName.getText().trim();
        String first = txtRecFirstName.getText().trim();
        String sss = txtRecSSS.getText().trim();
        String phil = txtRecPhilHealth.getText().trim();
        String tin = txtRecTIN.getText().trim();
        String pagibig = txtRecPagIBIG.getText().trim();

        List<String> errors = EmployeeRecordsModule.validateForm(
                empNo, last, first, sss, phil, tin, pagibig, true, selectedEmployeeId);
        if (!errors.isEmpty()) {
            markEmployeeRecordFieldErrors(errors);
            showBulletErrorDialog(frame, errors, "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String existingLine = FileHandlerModule.findEmployeeData(selectedEmployeeId);
        if (existingLine == null) {
            JOptionPane.showMessageDialog(frame,
                    "The selected employee record could not be found.",
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] updated = EmployeeRecordsModule.applyFormToRow(
                FileHandlerModule.smartSplit(existingLine), empNo, last, first, sss, phil, tin, pagibig);
        if (!FileHandlerModule.updateEmployeeRecord(selectedEmployeeId, updated)) {
            JOptionPane.showMessageDialog(frame,
                    "Could not update the employee record. Please try again.",
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshEmployeeTable();
        clearEmployeeRecordForm();
        JOptionPane.showMessageDialog(frame,
                "Employee record updated successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void runDeleteEmployeeRecord() {
        if (selectedEmployeeId == null || selectedEmployeeId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Select a record from the table before deleting.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Delete employee #" + selectedEmployeeId + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (!FileHandlerModule.deleteEmployeeRecord(selectedEmployeeId)) {
            JOptionPane.showMessageDialog(frame,
                    "Could not delete the employee record. Please try again.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshEmployeeTable();
        clearEmployeeRecordForm();
        JOptionPane.showMessageDialog(frame,
                "Employee record deleted successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    static void showHelpCenterUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.getContentPane().setBackground(APP_BG);
        buildAndAddSidebar("Help");
        addPageHeader("Help Center");

        int panelX = SIDEBAR_WIDTH + 12;
        int panelY = 76;
        int panelW = APP_FRAME_WIDTH - SIDEBAR_WIDTH - 24;
        int contentPaneH = frame.getContentPane().getHeight();
        int panelH = (contentPaneH > 100 ? contentPaneH : APP_FRAME_HEIGHT - 34) - panelY - 12;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(panelX, panelY, panelW, panelH);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        // ── FAQ section (full width) ───────────────────────────────────────
        JLabel faqTitle = new JLabel("Frequently Asked Questions");
        faqTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        faqTitle.setForeground(TEXT_DARK_NAVY);
        faqTitle.setBounds(16, 16, panelW - 32, 24);
        panel.add(faqTitle);

        String[][] faqs = {
            {"How is my basic salary calculated?",
             "Your basic salary is your agreed monthly rate. It is divided by the number of working days to get your daily rate, then multiplied by actual attendance days for the pay period."},
            {"What are the pay periods at MotorPH?",
             "MotorPH pays twice a month - on the 15th and at the end of the month. The 15th cutoff covers days 1-15; the end-of-month covers day 16 to the last day of the month."},
            {"How is SSS contribution computed?",
             "SSS is based on your Monthly Salary Credit (MSC). Both you and MotorPH contribute according to the latest SSS contribution table. The employee share is deducted from your gross pay each period."},
            {"How is PhilHealth computed?",
             "PhilHealth premium is 5% of your monthly basic salary (as of 2024), split equally - 2.5% employee + 2.5% employer. Minimum is ₱500/month, maximum ₱5,000/month."},
            {"How is Pag-IBIG computed?",
             "Pag-IBIG (HDMF) employee share is 2% of monthly salary, with a salary basis cap of ₱5,000 - so the maximum employee contribution is ₱100/month. MotorPH matches this amount."},
            {"How is withholding tax calculated?",
             "Withholding tax uses BIR tax tables. Taxable income = gross pay minus SSS, PhilHealth, and Pag-IBIG contributions. A graduated rate is then applied on the net taxable income."},
            {"How do I check my attendance records?",
             "Click \"Lookup\" in the sidebar, search for your employee name or ID, and your attendance records will appear - including login and logout times for each day."},
            {"What counts as overtime?",
             "Any work beyond 8 hours a day is overtime. Regular-day OT pay is your hourly rate x 1.25. Special holiday and rest-day OT rates are higher per DOLE rules."},
            {"Who do I contact for payroll issues?",
             "For salary discrepancies, contact your HR or Payroll Officer. You can also raise concerns via the Notifications panel in this system."},
            {"When are payslips available?",
             "Payslips are generated each pay period (15th and end of month) and can be viewed under the Pay Coverage section via the sidebar."}
        };

        DefaultListModel<String> qModel = new DefaultListModel<>();
        for (String[] faq : faqs) qModel.addElement(faq[0]);

        JList<String> qList = new JList<>(qModel);
        qList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qList.setBackground(PALETTE_WHITE);
        qList.setSelectionBackground(new Color(220, 236, 255));
        qList.setSelectionForeground(ACCENT_BLUE);
        qList.setFixedCellHeight(38);
        qList.setCellRenderer((lst, value, index, isSel, hasFocus) -> {
            JLabel lbl = new JLabel("  •  " + value);
            lbl.setFont(new Font("Segoe UI", isSel ? Font.BOLD : Font.PLAIN, 12));
            lbl.setForeground(isSel ? ACCENT_BLUE : TEXT_DARK_NAVY);
            lbl.setBackground(isSel ? new Color(232, 244, 255) : PALETTE_WHITE);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createMatteBorder(0, isSel ? 3 : 0, 1, 0,
                    isSel ? ACCENT_BLUE : new Color(238, 241, 247)));
            return lbl;
        });

        int qListH = (int)(panelH * 0.52);
        JScrollPane qScroll = new JScrollPane(qList);
        qScroll.setBounds(0, 48, panelW, qListH);
        qScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, CARD_BORDER_COLOR));
        qScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(qScroll);

        JLabel ansHeader = new JLabel("Answer");
        ansHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ansHeader.setForeground(new Color(100, 115, 140));
        ansHeader.setBounds(16, 48 + qListH + 10, panelW - 32, 18);
        panel.add(ansHeader);

        JTextArea ansArea = new JTextArea("Select a question above to read the answer here.");
        ansArea.setEditable(false);
        ansArea.setLineWrap(true);
        ansArea.setWrapStyleWord(true);
        ansArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ansArea.setForeground(new Color(40, 55, 90));
        ansArea.setBackground(new Color(246, 249, 255));
        ansArea.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        int ansY = 48 + qListH + 32;
        int ansH = panelH - ansY - 12;
        JScrollPane ansScroll = new JScrollPane(ansArea);
        ansScroll.setBounds(0, ansY, panelW, ansH);
        ansScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, CARD_BORDER_COLOR));
        panel.add(ansScroll);

        qList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && qList.getSelectedIndex() >= 0) {
                ansArea.setText(faqs[qList.getSelectedIndex()][1]);
                ansArea.setCaretPosition(0);
            }
        });

        frame.add(panel);
        addStatusBar();
        updateDisplay();
    }

    private static void scrollToBottom(JTextArea ta) {
        ta.setCaretPosition(ta.getDocument().getLength());
    }

    private static String jsonEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }

    private static String getLocalAIResponse(String question, java.util.List<String[]> history) {
        String q = question.toLowerCase().trim();

        // Greetings
        if (q.matches("hi|hello|hey|good morning|good afternoon|good evening|sup|howdy")) {
            return "Hello! I'm your MotorPH HR Assistant. I can help you with:\n"
                + "  - Salary computation and pay periods\n"
                + "  - SSS, PhilHealth, and Pag-IBIG contributions\n"
                + "  - Overtime, holiday, and leave pay\n"
                + "  - Tax / withholding questions\n"
                + "  - 13th month pay, loans, and payslips\n"
                + "  - Attendance and employee record queries\n\n"
                + "What would you like to know?";
        }

        // Thanks
        if (q.matches(".*\\b(thank|thanks|thank you|ty|salamat)\\b.*") && q.length() < 40) {
            return "You're welcome! Feel free to ask any other payroll or HR questions.";
        }

        // Salary / basic pay calculation
        if (q.contains("calculat") || q.contains("comput") || q.contains("how is") || q.contains("how do")) {
            if (q.contains("salary") || q.contains("basic pay") || q.contains("gross pay")) {
                return "Basic Salary Computation:\n\n"
                    + "  Daily rate  = Monthly salary / 22 working days\n"
                    + "  Pay period  = Daily rate x days actually worked\n\n"
                    + "MotorPH pays twice a month:\n"
                    + "  - 15th: covers days 1-15\n"
                    + "  - Last day: covers days 16 to end of month\n\n"
                    + "Example: Php 20,000/month\n"
                    + "  Daily rate = 20,000 / 22 = Php 909.09\n"
                    + "  Half-month (11 days) = Php 10,000";
            }
        }

        // Net pay
        if (q.contains("net pay") || q.contains("take home") || q.contains("take-home") || q.contains("actual pay")) {
            return "Net Pay Calculation:\n\n"
                + "  Net Pay = Gross Pay - Total Deductions\n\n"
                + "Mandatory deductions per pay period:\n"
                + "  - SSS employee share\n"
                + "  - PhilHealth: 2.5% of monthly salary\n"
                + "  - Pag-IBIG: Php 100 max\n"
                + "  - Withholding tax (if applicable)\n\n"
                + "Use the Payroll section (sidebar) to compute your exact net pay for any period.";
        }

        // SSS
        if (q.contains("sss") || q.contains("social security")) {
            return "SSS Contribution:\n\n"
                + "  Employee share : 4.5% of Monthly Salary Credit (MSC)\n"
                + "  Employer share : 9.5% of MSC\n"
                + "  MSC range      : Php 4,000 - Php 30,000\n\n"
                + "Example at Php 20,000 salary:\n"
                + "  MSC = Php 20,000\n"
                + "  Employee pays = 20,000 x 4.5% = Php 900/month\n\n"
                + "Deducted equally: Php 450 per cut-off (15th and end of month).";
        }

        // PhilHealth
        if (q.contains("philhealth") || q.contains("phil health") || q.contains("health insurance") || q.contains("health contribution")) {
            return "PhilHealth Contribution (2024):\n\n"
                + "  Total premium : 5% of monthly basic salary\n"
                + "  Employee share: 2.5%\n"
                + "  Employer share: 2.5%\n"
                + "  Minimum       : Php 500/month\n"
                + "  Maximum       : Php 5,000/month\n\n"
                + "Example at Php 20,000 salary:\n"
                + "  Monthly premium = 20,000 x 5% = Php 1,000\n"
                + "  Employee pays   = Php 500/month (Php 250 per cut-off)";
        }

        // Pag-IBIG / HDMF
        if (q.contains("pag-ibig") || q.contains("pagibig") || q.contains("pag ibig") || q.contains("hdmf")) {
            return "Pag-IBIG (HDMF) Contribution:\n\n"
                + "  Employee share: 2% of monthly salary\n"
                + "  Employer match: 2%\n"
                + "  Salary basis cap: Php 5,000\n"
                + "  Maximum employee contribution: Php 100/month\n\n"
                + "Example at Php 20,000 salary:\n"
                + "  Contribution = Php 5,000 x 2% = Php 100 (max)\n"
                + "  Php 50 deducted per pay cut-off";
        }

        // Withholding tax / BIR
        if (q.contains("tax") || q.contains("withhold") || q.contains("bir") || q.contains("income tax")) {
            return "Withholding Tax (BIR Graduated Rates):\n\n"
                + "  Taxable income = Gross Pay - SSS - PhilHealth - Pag-IBIG\n\n"
                + "  Monthly taxable income brackets:\n"
                + "  Up to Php 20,833       : 0%  (tax-exempt)\n"
                + "  Php 20,834 - 33,332    : 20% on excess over Php 20,833\n"
                + "  Php 33,333 - 66,666    : 25% on excess + Php 2,500\n"
                + "  Php 66,667 - 166,666   : 30% on excess + Php 10,833\n"
                + "  Php 166,667 - 666,666  : 32% on excess + Php 40,833\n"
                + "  Over Php 666,667       : 35% on excess + Php 200,833\n\n"
                + "Employees earning Php 250,000/year or less pay zero income tax.";
        }

        // Pay period / payday
        if (q.contains("pay day") || q.contains("payday") || q.contains("pay period") || q.contains("pay date")
                || (q.contains("when") && (q.contains("pay") || q.contains("salary")))) {
            return "MotorPH Pay Schedule:\n\n"
                + "  1st pay day : 15th of the month (covers days 1-15)\n"
                + "  2nd pay day : Last day of the month (covers days 16-end)\n\n"
                + "If a pay day falls on a weekend or public holiday, pay is released on the last working day before it.\n\n"
                + "The calendar on the Dashboard highlights both pay days for the current month.";
        }

        // Overtime
        if (q.contains("overtime") || q.contains("ot rate") || q.contains("extra hours")) {
            return "Overtime Pay (DOLE Rules):\n\n"
                + "  Regular day OT       : Hourly rate x 1.25\n"
                + "  Rest day work        : Hourly rate x 1.30\n"
                + "  Rest day OT          : Hourly rate x 1.30 x 1.30 = x1.69\n"
                + "  Special holiday work : Hourly rate x 1.30\n"
                + "  Regular holiday work : Hourly rate x 2.00\n"
                + "  Regular holiday OT   : Hourly rate x 2.00 x 1.30 = x2.60\n\n"
                + "Hourly rate = (Monthly salary / 22 days) / 8 hours";
        }

        // Holiday pay
        if (q.contains("holiday") && (q.contains("pay") || q.contains("rate") || q.contains("work"))) {
            return "Holiday Pay (DOLE):\n\n"
                + "Regular Holidays (Christmas, New Year, etc.):\n"
                + "  Did not work : 100% of daily rate (paid)\n"
                + "  Worked       : 200% of daily rate\n\n"
                + "Special Non-Working Holidays (EDSA Day, etc.):\n"
                + "  Did not work : No pay (no work, no pay)\n"
                + "  Worked       : 130% of daily rate\n\n"
                + "Check the current DOLE proclamation for the official list of holidays each year.";
        }

        // Leave
        if (q.contains("leave") || q.contains("vacation") || q.contains("sick day") || q.contains("absent")) {
            return "Leave Benefits at MotorPH:\n\n"
                + "  Service Incentive Leave (SIL) : 5 days/year (DOLE-mandated)\n"
                + "  Maternity Leave               : 105 days paid (RA 11210)\n"
                + "  Paternity Leave               : 7 days paid\n"
                + "  Solo Parent Leave             : 7 days paid\n"
                + "  VAWC Leave                    : 10 days paid\n\n"
                + "Unused SIL may be converted to cash at year-end.\n"
                + "Contact HR for your current leave balance.";
        }

        // 13th month
        if (q.contains("13th month") || q.contains("thirteenth") || q.contains("christmas bonus")) {
            return "13th Month Pay:\n\n"
                + "  Formula : Total basic salary earned in year / 12\n"
                + "  Who     : All rank-and-file employees who worked at least 1 month\n"
                + "  When    : On or before December 24\n"
                + "  Tax     : Exempt up to Php 90,000\n\n"
                + "Example: Php 20,000/month x 12 months / 12 = Php 20,000\n"
                + "Proportional for those who did not work the full year.";
        }

        // Payslip
        if (q.contains("payslip") || q.contains("pay slip") || q.contains("pay stub") || q.contains("salary breakdown")) {
            return "Viewing Your Payslip:\n\n"
                + "  1. Click 'Payroll' in the left sidebar\n"
                + "  2. Enter your Employee ID and the coverage period\n"
                + "  3. Your payslip shows:\n"
                + "     - Gross pay (basic + OT + allowances)\n"
                + "     - Deductions (SSS, PhilHealth, Pag-IBIG, tax)\n"
                + "     - Net pay (take-home)\n\n"
                + "Payslips are generated every 15th and last day of the month.";
        }

        // Attendance / time log
        if (q.contains("attendance") || q.contains("time in") || q.contains("time-in") || q.contains("time out") || q.contains("log") && q.contains("time")) {
            return "Attendance Policy:\n\n"
                + "  - Log time-in and time-out every working day\n"
                + "  - Standard shift: 8 hours/day\n"
                + "  - Tardiness and undertime are deducted from your salary\n"
                + "  - Missed punch: Report to HR the same day for correction\n\n"
                + "View your own attendance records:\n"
                + "  Sidebar > Lookup > Enter your name or employee ID";
        }

        // Loans
        if (q.contains("loan") || q.contains("cash advance") || q.contains("salary loan")) {
            return "Employee Loan Options:\n\n"
                + "SSS Salary Loan:\n"
                + "  - Requires 36+ monthly contributions\n"
                + "  - Loanable: up to 2 months' MSC\n"
                + "  - Repaid via monthly payroll deductions\n\n"
                + "Pag-IBIG Multi-Purpose Loan:\n"
                + "  - Requires 24 monthly contributions\n"
                + "  - Loanable: up to 80% of total Pag-IBIG savings\n\n"
                + "Apply through HR. Approved deductions appear on your next payslip.";
        }

        // Employee record update
        if ((q.contains("change") || q.contains("update") || q.contains("correct") || q.contains("edit"))
                && (q.contains("name") || q.contains("address") || q.contains("record") || q.contains("info") || q.contains("profile"))) {
            return "Updating Your Employee Record:\n\n"
                + "Personal information (name, address, civil status, etc.) must be updated through HR.\n\n"
                + "Steps:\n"
                + "  1. Submit a written request to HR\n"
                + "  2. Attach supporting documents (e.g., PSA certificate for name change)\n"
                + "  3. HR processes and updates the system within 3-5 working days\n\n"
                + "Important: Name or civil status changes must also be updated with SSS, PhilHealth, and Pag-IBIG directly.";
        }

        // Deductions general
        if (q.contains("deduction") || q.contains("how much is deducted") || q.contains("what is deducted")) {
            return "Mandatory Salary Deductions:\n\n"
                + "  SSS          : ~4.5% of Monthly Salary Credit\n"
                + "  PhilHealth   : 2.5% of monthly salary (min Php 250/cut-off)\n"
                + "  Pag-IBIG     : 2% of salary, max Php 100/month\n"
                + "  Withholding tax: based on BIR graduated rates\n\n"
                + "Total mandatory deductions at Php 20,000/month:\n"
                + "  SSS ~Php 900 + PhilHealth Php 500 + Pag-IBIG Php 100 = ~Php 1,500/month\n\n"
                + "Ask me about any specific deduction for a detailed breakdown.";
        }

        // HR contact / dispute
        if (q.contains("dispute") || q.contains("discrepancy") || q.contains("wrong") && q.contains("pay")
                || q.contains("contact") && q.contains("hr")) {
            return "Payroll Disputes:\n\n"
                + "  - Raise your concern with HR within 3 working days of pay day\n"
                + "  - Late disputes may be processed in the next pay cycle\n"
                + "  - Bring your payslip as reference when reporting\n\n"
                + "You can also send a concern via the Notifications panel in this app.";
        }

        // Default / unknown
        return "I can help with MotorPH payroll and HR topics. Here are some things you can ask:\n\n"
            + "  'How is my SSS contribution computed?'\n"
            + "  'When is the next pay day?'\n"
            + "  'What are my leave benefits?'\n"
            + "  'How is overtime calculated?'\n"
            + "  'What deductions are taken from my salary?'\n"
            + "  'How is the 13th month pay computed?'\n\n"
            + "Try rephrasing your question, or ask about a specific topic above.";
    }

    static void showNotificationsUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.getContentPane().setBackground(APP_BG);
        buildAndAddSidebar("Notifications");
        addPageHeader("Notifications");

        int panelW = 860;
        int contentAreaW = APP_FRAME_WIDTH - SIDEBAR_WIDTH;
        int panelX = SIDEBAR_WIDTH + (contentAreaW - panelW) / 2;
        int contentH = frame.getContentPane().getHeight();
        int panelH = (contentH > 100 ? contentH : APP_FRAME_HEIGHT - 34) - 76 - 30;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(panelX, 76, panelW, panelH);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        // ── Build employee-relevant notifications ──────────────────────────
        java.util.List<NotificationModule.Notification> allNotifications = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Pay day alert (dynamic: next pay is 15th or last day of month)
        int dom = today.getDayOfMonth();
        int lastDay = today.lengthOfMonth();
        LocalDate nextPay = dom < 15 ? today.withDayOfMonth(15)
                          : dom < lastDay ? today.withDayOfMonth(lastDay)
                          : today.plusMonths(1).withDayOfMonth(15);
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextPay);
        String payMonth = nextPay.getMonth().name().charAt(0)
            + nextPay.getMonth().name().substring(1, 3).toLowerCase();
        String payPeriod = nextPay.getDayOfMonth() == 15 ? "1st-15th" : "16th-end of month";

        if (daysUntil == 0) {
            allNotifications.add(new NotificationModule.Notification("Payroll",
                "Pay day is today! Your salary for the " + payPeriod + " period of "
                + payMonth + " has been processed. Check Pay Coverage for your breakdown."));
        } else if (daysUntil <= 3) {
            allNotifications.add(new NotificationModule.Notification("Payroll",
                "Pay day in " + daysUntil + " day(s) — " + payMonth + " " + nextPay.getDayOfMonth()
                + ". Your salary for the " + payPeriod + " period will be released then."));
        } else {
            allNotifications.add(new NotificationModule.Notification("Payroll",
                "Next pay day: " + payMonth + " " + nextPay.getDayOfMonth() + ", " + nextPay.getYear()
                + " (" + payPeriod + " period) — " + daysUntil + " days from now."));
        }

        // 2. Payslip available (previous period)
        allNotifications.add(new NotificationModule.Notification("Payroll",
            "Your most recent payslip is available. Go to Pay Coverage in the sidebar "
            + "to view your full salary breakdown including deductions."));

        // 3. Government deductions reminder
        allNotifications.add(new NotificationModule.Notification("Payroll",
            "Reminder: SSS, PhilHealth, and Pag-IBIG contributions are automatically "
            + "deducted from your salary each pay period as required by Philippine law."));

        // 4. Birthday notification — check if it is the logged-in employee's birthday month
        try {
            List<String[]> emps = FileHandlerModule.getAllEmployees();
            for (String[] emp : emps) {
                String fullName = EmployeeModule.fullName(emp);
                boolean isThisUser = fullName.equalsIgnoreCase(loggedInUser)
                    || (emp.length > EmployeeModule.FIRST_NAME
                        && emp[EmployeeModule.FIRST_NAME].equalsIgnoreCase(loggedInUser))
                    || (emp.length > EmployeeModule.LAST_NAME
                        && emp[EmployeeModule.LAST_NAME].equalsIgnoreCase(loggedInUser));
                if (!isThisUser) continue;
                String b = safeColumn(emp, EmployeeModule.BIRTHDAY);
                if (b == null || !b.contains("/")) break;
                String[] bp = b.split("/");
                if (bp.length < 2) break;
                int bMonth = Integer.parseInt(bp[0].trim());
                int bDay   = Integer.parseInt(bp[1].trim());
                if (bMonth == today.getMonthValue()) {
                    String bdayMon = today.getMonth().name().charAt(0)
                        + today.getMonth().name().substring(1, 3).toLowerCase();
                    if (bDay == dom) {
                        allNotifications.add(new NotificationModule.Notification("Birthday",
                            "Happy Birthday, " + emp[EmployeeModule.FIRST_NAME] + "! "
                            + "Wishing you a wonderful day from the MotorPH team!"));
                    } else if (bDay > dom) {
                        allNotifications.add(new NotificationModule.Notification("Birthday",
                            "Your birthday is coming up on " + bdayMon + " " + bDay + "! "
                            + "The MotorPH team wishes you a great celebration ahead."));
                    }
                }
                break;
            }
        } catch (Exception ignored) {}

        // 5. Attendance reminder
        allNotifications.add(new NotificationModule.Notification("Attendance",
            "Attendance reminder: Ensure your time-in and time-out are logged every working day. "
            + "Missed entries may affect your salary computation. Contact HR immediately for corrections."));

        // 6. Cut-off deadline reminder
        int cutoffDay = dom <= 15 ? 15 : lastDay;
        long daysTocut = cutoffDay - dom;
        if (daysTocut >= 0 && daysTocut <= 5) {
            allNotifications.add(new NotificationModule.Notification("Attendance",
                "Attendance cut-off is " + (daysTocut == 0 ? "today" : "in " + daysTocut + " day(s)")
                + ". Make sure all your time logs for this period are complete."));
        } else {
            allNotifications.add(new NotificationModule.Notification("Attendance",
                "Current pay period ends on the " + cutoffDay + "th. "
                + "Review your attendance records under Employee Lookup."));
        }

        // 7. Leave and overtime info
        allNotifications.add(new NotificationModule.Notification("General",
            "Overtime and holiday pay are computed based on DOLE guidelines. "
            + "Regular OT rate is 1.25x your hourly rate. For holiday rates, refer to the Help Center."));

        // 8. Profile update reminder
        allNotifications.add(new NotificationModule.Notification("General",
            "Keep your employee record current. Any changes to your personal details, "
            + "civil status, or dependents must be reported to HR to ensure correct deductions."));

        // 9. Help center nudge
        allNotifications.add(new NotificationModule.Notification("General",
            "Have a question about your pay? Visit the Help Center for FAQs and our AI Assistant "
            + "that can answer payroll and HR questions instantly."));

        // 10. HR contact reminder
        allNotifications.add(new NotificationModule.Notification("General",
            "For salary discrepancies, raise your concern with HR within 3 working days of pay day. "
            + "Late disputes may be processed in the next pay cycle."));

        DefaultListModel<NotificationModule.Notification> model = new DefaultListModel<>();
        for (NotificationModule.Notification n : allNotifications) model.addElement(n);

        // ── Filter bar ────────────────────────────────────────────────────
        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLbl.setForeground(TEXT_DARK_NAVY);
        filterLbl.setBounds(20, 18, 48, 26);
        panel.add(filterLbl);

        String[] filters = {"All", "Unread", "Payroll", "Attendance", "Birthday", "General"};
        int btnFX = 72;
        JButton[] filterBtns = new JButton[filters.length];
        for (int fi = 0; fi < filters.length; fi++) {
            JButton fb = new JButton(filters[fi]);
            fb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fb.setFocusable(false);
            fb.setOpaque(true);
            fb.setBackground(fi == 0 ? ACCENT_BLUE : new Color(240, 244, 252));
            fb.setForeground(fi == 0 ? PALETTE_WHITE : TEXT_DARK_NAVY);
            fb.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
            fb.setBounds(btnFX, 18, 80, 26);
            btnFX += 86;
            filterBtns[fi] = fb;
            panel.add(fb);
        }

        // ── Notification list ──────────────────────────────────────────────
        JList<NotificationModule.Notification> list = new JList<>(model);
        list.setCellRenderer(new NotificationCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(64);
        list.setBackground(new Color(248, 250, 254));

        JScrollPane sp = new JScrollPane(list);
        int listW = panelW - 220;
        sp.setBounds(20, 56, listW, panelH - 100);
        sp.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(sp);

        // ── Double-click to open notification detail popup ─────────────────
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int idx = list.locationToIndex(e.getPoint());
                if (idx < 0) return;
                NotificationModule.Notification n =
                    ((DefaultListModel<NotificationModule.Notification>) list.getModel()).getElementAt(idx);
                if (n == null) return;
                n.read = true;
                list.repaint();
                showNotificationDetail(n);
            }
        });

        // ── Action panel (right column) ────────────────────────────────────
        int actionX = listW + 36;
        int actionW = panelW - actionX - 16;

        JLabel actTitle = new JLabel("Actions");
        actTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        actTitle.setForeground(TEXT_DARK_NAVY);
        actTitle.setBounds(actionX, 56, actionW, 22);
        panel.add(actTitle);

        JButton btnMarkRead = new JButton("Mark as Read");
        btnMarkRead.setBounds(actionX, 86, actionW, 32);
        guiStyleAccentButton(btnMarkRead);
        btnMarkRead.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                NotificationModule.Notification n = ((DefaultListModel<NotificationModule.Notification>) list.getModel()).getElementAt(idx);
                n.read = true;
                list.repaint();
            }
        });
        panel.add(btnMarkRead);

        JButton btnDismiss = new JButton("Dismiss");
        btnDismiss.setBounds(actionX, 128, actionW, 32);
        styleStandardButton(btnDismiss);
        btnDismiss.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                ((DefaultListModel<NotificationModule.Notification>) list.getModel()).remove(idx);
            }
        });
        panel.add(btnDismiss);

        JButton btnMarkAll = new JButton("Mark All Read");
        btnMarkAll.setBounds(actionX, 172, actionW, 32);
        styleStandardButton(btnMarkAll);
        btnMarkAll.addActionListener(e -> {
            for (int i = 0; i < model.size(); i++) model.getElementAt(i).read = true;
            list.repaint();
        });
        panel.add(btnMarkAll);

        // Unread count badge
        JLabel unreadLbl = new JLabel();
        unreadLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        unreadLbl.setForeground(new Color(100, 115, 145));
        unreadLbl.setBounds(actionX, 220, actionW, 18);
        long unread = allNotifications.stream().filter(n -> !n.read).count();
        unreadLbl.setText(unread + " unread of " + allNotifications.size());
        panel.add(unreadLbl);

        // ── Filter button wiring ───────────────────────────────────────────
        for (int fi = 0; fi < filters.length; fi++) {
            final String cat = filters[fi];
            final int idx = fi;
            filterBtns[fi].addActionListener(e -> {
                // Update button styles
                for (int j = 0; j < filterBtns.length; j++) {
                    filterBtns[j].setBackground(j == idx ? ACCENT_BLUE : new Color(240, 244, 252));
                    filterBtns[j].setForeground(j == idx ? PALETTE_WHITE : TEXT_DARK_NAVY);
                }
                // Rebuild model
                DefaultListModel<NotificationModule.Notification> filtered = new DefaultListModel<>();
                for (NotificationModule.Notification n : allNotifications) {
                    if ("All".equals(cat)) { filtered.addElement(n); }
                    else if ("Unread".equals(cat) && !n.read) { filtered.addElement(n); }
                    else if (n.category.equals(cat)) { filtered.addElement(n); }
                }
                list.setModel(filtered);
            });
        }

        frame.add(panel);
        addStatusBar();
        updateDisplay();
    }

    private static void showNotificationDetail(NotificationModule.Notification n) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(frame, "Notification", true);
        dialog.setSize(480, 220);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);
        dialog.setLayout(new java.awt.BorderLayout());

        // Category badge strip — BorderLayout so labels never overflow
        Color badgeColor;
        switch (n.category) {
            case "Payroll":    badgeColor = new Color(220, 20, 60);   break;
            case "Attendance": badgeColor = new Color(255, 165, 0);   break;
            case "Birthday":   badgeColor = new Color(34, 139, 34);   break;
            case "System":     badgeColor = new Color(70, 130, 255);  break;
            default:           badgeColor = new Color(100, 110, 130); break;
        }
        JPanel strip = new JPanel(new java.awt.BorderLayout(0, 0));
        strip.setBackground(badgeColor);
        strip.setPreferredSize(new java.awt.Dimension(0, 40));
        strip.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel catLbl = new JLabel(n.category.toUpperCase());
        catLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        catLbl.setForeground(PALETTE_WHITE);
        strip.add(catLbl, java.awt.BorderLayout.WEST);

        String ts = n.timestamp;
        try {
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(ts);
            String mon = ldt.getMonth().name();
            mon = mon.charAt(0) + mon.substring(1).toLowerCase();
            ts = mon + " " + ldt.getDayOfMonth() + ", " + ldt.getYear()
               + "  " + String.format("%02d:%02d", ldt.getHour(), ldt.getMinute());
        } catch (Exception ignored) {}
        JLabel tsLbl = new JLabel(ts);
        tsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tsLbl.setForeground(new Color(220, 230, 245));
        strip.add(tsLbl, java.awt.BorderLayout.EAST);

        dialog.add(strip, java.awt.BorderLayout.NORTH);

        // Message body fills remaining space
        JTextArea body = new JTextArea(n.text);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        body.setForeground(TEXT_DARK_NAVY);
        body.setBackground(PALETTE_WHITE);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JScrollPane bodyScroll = new JScrollPane(body,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.setBorder(null);
        dialog.add(bodyScroll, java.awt.BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    // Renderer: show colored circular badges per category and neutral row background
    static class NotificationCellRenderer implements ListCellRenderer<NotificationModule.Notification> {
        @Override
        public java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends NotificationModule.Notification> list,
                                                               NotificationModule.Notification value,
                                                               int index, boolean isSelected, boolean cellHasFocus) {
            String text = value == null ? "" : value.toString();
            JLabel lbl = new JLabel(text);
            lbl.setOpaque(true);
            Color fg = new Color(11,29,58);
            Color bg = Color.white;
            Icon icon = null;
            if (value != null) {
                String c = value.category == null ? "General" : value.category;
                Color badge = new Color(200,200,200);
                if ("System".equals(c)) {
                    badge = new Color(70,130,255);
                } else if ("Attendance".equals(c)) {
                    badge = new Color(255,195,0);
                } else if ("Payroll".equals(c)) {
                    badge = new Color(220,20,60);
                } else if ("Birthday".equals(c)) {
                    badge = new Color(34,139,34);
                } else if ("General".equals(c)) {
                    badge = new Color(128,128,128);
                }
                icon = new ColoredCircleIcon(badge, 12);
                if (value.read) fg = new Color(120,130,140);
            }
            lbl.setIcon(icon);
            lbl.setIconTextGap(10);
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                lbl.setBackground(bg);
                lbl.setForeground(fg);
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
            return lbl;
        }
    }

    // Simple colored circle icon for badges
    static class ColoredCircleIcon implements Icon {
        private final Color color;
        private final int size;
        ColoredCircleIcon(Color color, int size) { this.color = color; this.size = size; }
        @Override public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    /**
     * Handles employee lookup when Search is clicked or Enter is pressed.
     *
     * Validates: non-empty ID, numeric format, and existence in Employee Details CSV.
     * On success, formats and displays ID, name, and birthday in {@link #txtLookupDisplay}.
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

    /** Returns the trimmed CSV column or "-" if the row is too short / null. */
    private static String safeColumn(String[] row, int idx) {
        if (row == null || idx < 0 || idx >= row.length) return "-";
        String value = row[idx];
        if (value == null || value.trim().isEmpty()) return "-";
        return value.trim();
    }

    /**
     * Validates payroll form inputs and runs {@link SalaryComputationModule#calculatePayroll}.
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
        JOptionPane.showMessageDialog(frame,
                "Salary computation completed successfully.\n"
                + "Gross pay, deductions, and net pay are shown in the output area.",
                "Computation Complete", JOptionPane.INFORMATION_MESSAGE);
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
            text.append("* ").append(item).append("\n");
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

        // Mirrors guiStyleAccentButton's pattern so secondary buttons also feel interactive
        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PALETTE_LIGHT_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.white);
            }

            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
        });
    }

    /**
     * Builds a full-width colored header strip and adds it to the frame at y=0.
     *
     * The strip uses {@link #ACCENT_BLUE} as background with white bold title text,
     * giving every main screen a consistent branded top bar. Callers must add
     * remaining content below {@link #HEADER_STRIP_HEIGHT}.
     *
     * @param title       white screen title (e.g. "MAIN MENU")
     * @param frameWidth  current frame width so the strip spans the full content area
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

    // Breadcrumb utilities  breadcrumb bar removed; method kept as no-op for callers
    static void setBreadcrumb(String... parts) {
        // breadcrumb bar is hidden; nothing to render
    }

    static void handleBreadcrumbClick(String label) {
        if (label == null) return;
        String l = label.toLowerCase();
        if (l.contains("dashboard") || l.contains("main menu")) showDashboard();
        else if (l.contains("notification")) showNotificationsUI();
        else if (l.contains("pay") || l.contains("coverage")) setupPayrollUI();
        else if (l.contains("employee") || l.contains("record")) showEmployeeRecordsUI();
        else if (l.contains("lookup")) showEmployeeLookupUI();
        else if (l.contains("help")) showHelpCenterUI();
        else showDashboard();
    }

    /**
     * Adds a small bottom-right "Logged in as: {user}" status label to the frame.
     *
     * The label's right edge is aligned with the screen's content right edge so the
     * footer visually lines up with the card / scroll pane above it instead of
     * floating in the dialog gutter.
     *
     * Skipped silently when no username is recorded (defensive only - main() requires login).
     *
     * @param frameWidth    current frame width
     * @param contentHeight visible content-area height used to position the footer
     * @param rightMargin   right margin of the screen's main content (e.g. card right margin),
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
        button.setBackground(ACCENT_BLUE);
        button.setForeground(Color.white);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseListener() {
            private boolean cursorInside = false;

            @Override public void mouseEntered(MouseEvent e) {
                cursorInside = true;
                button.setBackground(HOVER_BLUE);
            }

            @Override public void mouseExited(MouseEvent e) {
                cursorInside = false;
                button.setBackground(ACCENT_BLUE);
            }

            @Override public void mousePressed(MouseEvent e) {
                button.setBackground(PRESSED_BLUE);
            }

            @Override public void mouseReleased(MouseEvent e) {
                button.setBackground(cursorInside ? HOVER_BLUE : ACCENT_BLUE);
            }

            @Override public void mouseClicked(MouseEvent e) {}
        });
    }

    static void updateDisplay() {
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true); // Always refreshed at the end to force accurate UI updates
    }
}
