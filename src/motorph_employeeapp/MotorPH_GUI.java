
package motorph_employeeapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

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
     * Index 0 is a sentinel ("no selection"); subsequent entries map combo
     * positions
     * to actual calendar month numbers. Using this lookup avoids fragile arithmetic
     * like {@code getSelectedIndex() + 5}, which silently breaks if items change.
     */
    static final int[] MONTH_NUMBERS = { 0, 6, 7, 8, 9, 10, 11, 12 };

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
    static JTextField txtRecBirthday;
    static JTextField txtRecAddress;
    static JTextField txtRecPhone;
    static JTextField txtRecSSS;
    static JTextField txtRecPhilHealth;
    static JTextField txtRecTIN;
    static JTextField txtRecPagIBIG;
    static JTextField txtRecStatus;
    static JTextField txtRecPosition;
    static JTextField txtRecSupervisor;
    static JTextField txtRecBasicSalary;
    static JTextField txtRecHourlyRate;
    static JButton btnRecUpdate;
    static JButton btnRecDelete;
    static JButton btnRecComputePayroll;
    static JLabel lblRecFormHint;
    static JTextField txtRecSearch;
    static final List<Object[]> employeeTableAllRows = new ArrayList<>();
    static String recordFormBaseline = "";
    static int lastSelectedEmployeeRow = -1;
    static String selectedEmployeeId = null;
    static String pendingPayrollEmployeeId = null;
    static String currentView = "Dashboard";
    static JLabel statusToastLbl;
    static javax.swing.Timer toastTimer;
    static final Set<String> readNotificationKeys = new HashSet<>();
    static boolean resizeHandlerInstalled = false;
    static boolean reloadingLayout = false;

    /** Summary strip above the payslip output on the payroll screen. */
    static JLabel lblPayrollSummary;

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

    // Modern layout palette — single source of truth for the overhaul
    static final Color SIDEBAR_BG = new Color(15, 32, 72);
    static final Color SIDEBAR_TOP_BG = new Color(10, 24, 56);
    static final Color SIDEBAR_ACTIVE = new Color(30, 88, 200);
    static final Color SIDEBAR_HOVER = new Color(22, 54, 128);
    static final Color APP_BG = new Color(237, 242, 249);
    static final Color CARD_BORDER_COLOR = new Color(216, 224, 236);
    static final Color TEXT_MUTED = new Color(100, 112, 132);
    static final Color INPUT_BG = new Color(248, 250, 253);
    static final Color TABLE_HEADER_BG = new Color(241, 245, 251);
    static final Color TABLE_STRIPE_BG = new Color(250, 252, 255);
    static final int SIDEBAR_WIDTH = 224;
    static final int BTN_HEIGHT = 36;
    static final int FIELD_HEIGHT = 36;

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
    /** Font for the small "Logged in as: ..." status label. */
    static final Font STATUS_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    /** Shared layout rhythm for all post-login screens. */
    static final int PAGE_HEADER_H = 72;
    static final int PAGE_TOP = PAGE_HEADER_H + 20;
    static final int CONTENT_PAD = 24;
    static final int SIDEBAR_NAV_BTN_H = 42;
    static final int SIDEBAR_NAV_GAP = 4;
    static final int RECORDS_ACTION_BAR_H = 92;
    static final int DASHBOARD_CAL_W = 300;

    /** HR sidebar / screen label (replaces legacy "Lookup"). */
    static final String HR_DIRECTORY_NAV = "Directory";
    static final String HR_DIRECTORY_TITLE = "Employee Directory";

    static final int DASH_CARD_H = 156;
    static final int DASH_CARD_GAP = 20;
    static final int DASH_CARD_BTN_H = BTN_HEIGHT;
    static final int DASH_CARD_BTN_PAD = 16;
    static final int DASH_CARD_BTN_GAP = 10;
    static final int DASH_CARD_INSET = 20;
    static final int DASH_CARD_FOOTER_H = DASH_CARD_BTN_H + DASH_CARD_BTN_PAD * 2;
    static final int DASH_CARD_TEXT_GAP = 12;
    static final String DASH_SUBTITLE_BOTTOM_KEY = "dashboard.subtitleBottom";

    private static int getVisibleHeight() {
        if (frame == null) {
            return APP_FRAME_HEIGHT - STATUS_BAR_H;
        }
        java.awt.Insets ins = frame.getInsets();
        return frame.getHeight() - ins.top - ins.bottom - STATUS_BAR_H;
    }

    private static int getVisibleWidth() {
        if (frame == null) {
            return APP_FRAME_WIDTH;
        }
        java.awt.Insets ins = frame.getInsets();
        return frame.getWidth() - ins.left - ins.right;
    }

    /** Content region to the right of the sidebar, below the page header. */
    private static java.awt.Rectangle getContentBounds() {
        int x = SIDEBAR_WIDTH + CONTENT_PAD;
        int y = PAGE_TOP;
        int w = getVisibleWidth() - SIDEBAR_WIDTH - CONTENT_PAD * 2;
        int h = getVisibleHeight() - PAGE_TOP - CONTENT_PAD;
        return new java.awt.Rectangle(x, y, Math.max(480, w), Math.max(320, h));
    }

    private static javax.swing.border.Border cardBorder() {
        return BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1);
    }

    private static void styleScrollPane(JScrollPane sp) {
        sp.setBorder(cardBorder());
        sp.getViewport().setBackground(PALETTE_WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(24);
    }

    private static void applyModernTableStyle(JTable table) {
        table.setFont(APP_FONT_PLAIN);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setGridColor(new Color(236, 240, 246));
        table.setSelectionBackground(new Color(224, 238, 255));
        table.setSelectionForeground(TEXT_DARK_NAVY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(TEXT_DARK_NAVY);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    /**
     * Fixed column widths so ID columns stay readable; horizontal scroll when
     * needed.
     */
    private static void configureEmployeeTableColumns(JTable table) {
        int[] widths = { 88, 118, 118, 108, 118, 108, 112 };
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            col.setMinWidth(72);
        }
    }

    private static void enableTableSorting(JTable table) {
        if (table == null || table.getModel() == null) {
            return;
        }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(employeeTableModel);
        table.setRowSorter(sorter);
    }

    private static void showToast(String message) {
        showToast(message, new Color(34, 160, 90));
    }

    private static void showToast(String message, Color color) {
        if (statusToastLbl == null) {
            return;
        }
        statusToastLbl.setText(message);
        statusToastLbl.setForeground(color);
        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }
        toastTimer = new javax.swing.Timer(4500, e -> {
            if (statusToastLbl != null) {
                statusToastLbl.setText("");
            }
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private static String getLoggedInEmployeeId() {
        return MotorPH_EmployeeApp.getLinkedEmployeeId(loggedInUser);
    }

    private static int getDefaultPayrollMonthIndex() {
        int month = LocalDate.now().getMonthValue();
        for (int i = 1; i < MONTH_NUMBERS.length; i++) {
            if (MONTH_NUMBERS[i] == month) {
                return i;
            }
        }
        return MONTH_NUMBERS.length - 1;
    }

    private static String notificationKey(NotificationModule.Notification n) {
        return n.category + "::" + n.text;
    }

    private static List<NotificationModule.Notification> buildSystemNotifications() {
        List<NotificationModule.Notification> allNotifications = new ArrayList<>();
        LocalDate today = LocalDate.now();
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
                    "Next pay day: " + payMonth + " " + nextPay.getDayOfMonth()
                            + " (" + daysUntil + " day(s) away). Period covered: " + payPeriod + "."));
        }

        allNotifications.add(new NotificationModule.Notification("Payroll",
                "Reminder: SSS, PhilHealth, and Pag-IBIG contributions are automatically "
                        + "deducted each pay period. Review your payslip for the full breakdown."));

        for (String[] emp : FileHandlerModule.getAllEmployees()) {
            String b = safeColumn(emp, EmployeeModule.BIRTHDAY);
            if (b == null || b.isEmpty() || b.equals("-") || !b.contains("/")) {
                continue;
            }
            try {
                String[] parts = b.split("/");
                int m = Integer.parseInt(parts[0].trim());
                int d = Integer.parseInt(parts[1].trim());
                if (m == today.getMonthValue() && d == today.getDayOfMonth()) {
                    String fullName = EmployeeModule.fullName(emp);
                    boolean isThisUser = fullName.equalsIgnoreCase(loggedInUser)
                            || (emp.length > EmployeeModule.FIRST_NAME
                                    && emp[EmployeeModule.FIRST_NAME].equalsIgnoreCase(loggedInUser))
                            || getLoggedInEmployeeId() != null
                                    && getLoggedInEmployeeId().equals(safeColumn(emp, EmployeeModule.ID));
                    if (isThisUser || isHrUser()) {
                        allNotifications.add(new NotificationModule.Notification("Birthday",
                                "Today is " + fullName + "'s birthday. Send your greetings!"));
                    }
                }
            } catch (NumberFormatException ex) {
                /* ignore malformed */ }
        }

        allNotifications.add(new NotificationModule.Notification("Payroll",
                "Your most recent payslip is available. Open Payroll in the sidebar to view your full breakdown."));
        allNotifications.add(new NotificationModule.Notification("Attendance",
                "Ensure time-in and time-out are logged every working day. Missed entries may affect salary."));
        int cutoffDay = dom <= 15 ? 15 : lastDay;
        long daysToCut = cutoffDay - dom;
        if (daysToCut >= 0 && daysToCut <= 5) {
            allNotifications.add(new NotificationModule.Notification("Attendance",
                    "Attendance cut-off is " + (daysToCut == 0 ? "today" : "in " + daysToCut + " day(s)")
                            + ". Complete all time logs for this period."));
        }
        allNotifications.add(new NotificationModule.Notification("General",
                "Overtime is computed at 1.25x hourly rate per DOLE guidelines. See Help Center for details."));
        allNotifications.add(new NotificationModule.Notification("General",
                "Keep your employee record current. Report personal detail changes to HR for correct deductions."));
        allNotifications.add(new NotificationModule.Notification("General",
                "Have a question about your pay? Visit the Help Center for FAQs and the AI Assistant."));
        allNotifications.add(new NotificationModule.Notification("General",
                "For salary discrepancies, raise your concern with HR within 3 working days of pay day."));

        for (NotificationModule.Notification n : allNotifications) {
            n.read = readNotificationKeys.contains(notificationKey(n));
        }
        return allNotifications;
    }

    private static int countUnreadNotifications() {
        int count = 0;
        for (NotificationModule.Notification n : buildSystemNotifications()) {
            if (!n.read) {
                count++;
            }
        }
        return count;
    }

    private static void markNotificationRead(NotificationModule.Notification n) {
        if (n == null) {
            return;
        }
        n.read = true;
        readNotificationKeys.add(notificationKey(n));
    }

    private static void reloadCurrentView() {
        if (frame == null || currentView == null || reloadingLayout) {
            return;
        }
        reloadingLayout = true;
        try {
            reloadCurrentViewInner();
        } finally {
            reloadingLayout = false;
        }
    }

    private static void reloadCurrentViewInner() {
        switch (currentView) {
            case "Dashboard":
                showDashboard();
                break;
            case "Records":
                showEmployeeRecordsUI();
                break;
            case "Payroll":
                setupPayrollUI();
                break;
            case "Directory":
            case "My Profile":
                showEmployeeLookupUI();
                break;
            case "Edit My Profile":
                editMyProfile();
                break; // Added to prevent reset crashing on frame window resize logs
            case "Notifications":
                showNotificationsUI();
                break;
            case "Help":
                showHelpCenterUI();
                break;
            default:
                showDashboard();
                break;
        }
    }

    private static void installWindowResizeHandler() {
        if (frame == null || resizeHandlerInstalled) {
            return;
        }
        resizeHandlerInstalled = true;
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(MotorPH_GUI::reloadCurrentView);
            }
        });
    }

    private static void installGlobalShortcuts() {
        if (frame == null) {
            return;
        }
        InputMap im = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = frame.getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "uxFocusSearch");
        am.put("uxFocusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Records".equals(currentView) && txtRecSearch != null) {
                    txtRecSearch.requestFocusInWindow();
                }
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "uxEscape");
        am.put("uxEscape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Records".equals(currentView)) {
                    if (confirmDiscardRecordChanges()) {
                        clearEmployeeRecordForm();
                    }
                }
            }
        });
    }

    private static boolean rowMatchesEmployeeSearch(Object[] row, String q) {
        if (q == null || q.isEmpty()) {
            return true;
        }
        for (Object cell : row) {
            if (cell != null && String.valueOf(cell).toLowerCase().contains(q)) {
                return true;
            }
        }
        return false;
    }

    private static void applyEmployeeTableFilter() {
        if (employeeTableModel == null) {
            return;
        }
        String q = txtRecSearch != null ? txtRecSearch.getText().trim().toLowerCase() : "";
        employeeTableModel.setRowCount(0);
        for (Object[] row : employeeTableAllRows) {
            if (rowMatchesEmployeeSearch(row, q)) {
                employeeTableModel.addRow(row);
            }
        }
    }

    private static String serializeRecordForm() {
        EmployeeRecordsModule.RecordFormData form = readEmployeeRecordForm();
        return String.join("|",
                nz(form.empNo), nz(form.lastName), nz(form.firstName), nz(form.birthday),
                nz(form.address), nz(form.phone), nz(form.sss), nz(form.philHealth),
                nz(form.tin), nz(form.pagIbig), nz(form.status), nz(form.position),
                nz(form.supervisor), nz(form.basicSalary), nz(form.hourlyRate));
    }

    private static String nz(String v) {
        return v == null ? "" : v.trim();
    }

    private static void captureRecordFormBaseline() {
        recordFormBaseline = serializeRecordForm();
    }

    private static boolean isRecordFormDirty() {
        return !serializeRecordForm().equals(recordFormBaseline);
    }

    private static boolean confirmDiscardRecordChanges() {
        if (!isRecordFormDirty()) {
            return true;
        }
        int choice = JOptionPane.showConfirmDialog(frame,
                "You have unsaved changes in the form. Discard them?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private static int addRecordFormSection(JPanel panel, int fy, int fieldX, int fieldW,
            int rowGap, String title) {
        JLabel section = new JLabel(title);
        section.setFont(new Font("Segoe UI", Font.BOLD, 12));
        section.setForeground(ACCENT_BLUE);
        section.setBounds(0, fy, fieldX + fieldW, 20);
        panel.add(section);
        return fy + 20 + rowGap;
    }

    private static void attachFocusHighlight(JTextField field) {
        if (field == null) {
            return;
        }
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                        BorderFactory.createEmptyBorder(5, 9, 5, 9)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                resetFieldBorder(field);
            }
        });
    }

    private static void exportPayslipToFile() {
        if (txtResultArea == null || txtResultArea.getText().trim().isEmpty()) {
            showToast("Generate a payslip before exporting.", new Color(180, 90, 40));
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("payslip-" + txtEmployeeNo.getText().trim() + ".txt"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        try (FileWriter writer = new FileWriter(target)) {
            writer.write(txtResultArea.getText());
            showToast("Payslip saved to " + target.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not save payslip: " + ex.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void copyPayslipToClipboard() {
        if (txtResultArea == null || txtResultArea.getText().trim().isEmpty()) {
            showToast("Generate a payslip before copying.", new Color(180, 90, 40));
            return;
        }
        txtResultArea.selectAll();
        txtResultArea.copy();
        txtResultArea.setSelectionStart(0);
        txtResultArea.setSelectionEnd(0);
        showToast("Payslip copied to clipboard.");
    }

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
        loginDialog.setTitle("MotorPH — Sign In");
        loginDialog.setSize(420, 520);
        loginDialog.setModal(true);
        loginDialog.setLayout(null);
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setResizable(false);

        JPanel rootPanel = new JPanel(null);
        rootPanel.setBackground(APP_BG);
        rootPanel.setBounds(0, 0, 420, 520);

        // Single elevated card — header band + form in one surface
        JPanel loginCard = new JPanel(null);
        loginCard.setBackground(PALETTE_WHITE);
        loginCard.setBounds(32, 36, 356, 448);
        loginCard.setBorder(cardBorder());

        JPanel headerBand = new JPanel(null);
        headerBand.setBackground(SIDEBAR_BG);
        headerBand.setBounds(0, 0, 356, 100);

        JLabel lblTitle = new JLabel("MotorPH", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(PALETTE_WHITE);
        lblTitle.setBounds(0, 22, 356, 32);

        JLabel lblSubtitle = new JLabel("Employee & HR Portals", SwingConstants.CENTER);
        lblSubtitle.setFont(LOGIN_APP_FONT_PLAIN);
        lblSubtitle.setForeground(new Color(175, 205, 250));
        lblSubtitle.setBounds(0, 56, 356, 22);
        headerBand.add(lblTitle);
        headerBand.add(lblSubtitle);

        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(PALETTE_WHITE);
        formPanel.setBounds(0, 100, 356, 348);

        // Login form vertical rhythm (kept consistent across every row):
        // labelHeight=20, fieldHeight=38, checkboxHeight=22, buttonHeight=40
        // gap between a label and its field = 6px
        // gap between sections (fieldlabel) = 20px
        // Adjusting any of these values? Update every row below to keep the grid even.

        // Username row
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(LOGIN_APP_FONT_BOLD);
        lblUser.setForeground(TEXT_DARK_NAVY);
        lblUser.setBounds(32, 28, 292, 20);

        usernameField = new JTextField();
        usernameField.setBounds(32, 52, 292, FIELD_HEIGHT);
        styleInputField(usernameField);
        // Placeholder hint shown only when field is empty and unfocused (From Lesson:
        // FocusListener)
        attachPlaceholder(usernameField, "Enter username");

        // Password row (89 + 20 section gap = 109)
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(LOGIN_APP_FONT_BOLD);
        lblPass.setForeground(TEXT_DARK_NAVY);
        lblPass.setBounds(32, 108, 292, 20);

        passwordField = new JPasswordField();
        passwordField.setEchoChar(PASSWORD_ECHO_CHAR);
        passwordField.setFont(LOGIN_APP_FONT_PLAIN);
        passwordField.setBackground(INPUT_BG);
        passwordField.setForeground(TEXT_DARK_NAVY);
        passwordField.setCaretColor(TEXT_DARK_NAVY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        passwordField.setBounds(32, 132, 292, FIELD_HEIGHT);

        final JCheckBox chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setBounds(32, 180, 292, 22);
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

        JLabel lblDemoHint = new JLabel(
                "<html><center>Demo: <b>employee</b> / 12345 &nbsp;|&nbsp; <b>hr</b> / hr12345</center></html>");
        lblDemoHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDemoHint.setForeground(TEXT_MUTED);
        lblDemoHint.setBounds(32, 214, 292, 32);

        btnLogin = new JButton("Sign In");
        btnLogin.setBounds(32, 252, 292, BTN_HEIGHT + 4);
        styleAccentButton(btnLogin);

        btnLogin.addActionListener(e -> {
            if (e.getSource() == btnLogin) {
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

                if (MotorPH_EmployeeApp.authenticate(user, pass)) {
                    MotorPH_EmployeeApp.loginSuccessful = true;
                    loggedInUser = user;
                    loginDialog.dispose();
                } else {
                    setLoginFieldError(usernameField);
                    setLoginFieldError(passwordField);
                    List<String> authErrors = new ArrayList<>();
                    authErrors.add("Invalid username or password.");
                    authErrors.add("Please check your credentials and try again.");
                    showBulletErrorDialog(loginDialog, authErrors, "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
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

        formPanel.add(lblUser);
        formPanel.add(usernameField);
        formPanel.add(lblPass);
        formPanel.add(passwordField);
        formPanel.add(chkShowPassword);
        formPanel.add(lblDemoHint);
        formPanel.add(btnLogin);
        loginCard.add(headerBand);
        loginCard.add(formPanel);
        rootPanel.add(loginCard);

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
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_DARK_NAVY);
        field.setCaretColor(TEXT_DARK_NAVY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        attachFocusHighlight(field);
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
        frame.setTitle(isHrUser() ? "MotorPH HR Management System" : "MotorPH Employee Portal");
        frame.setSize(APP_FRAME_WIDTH, APP_FRAME_HEIGHT);
        frame.setMinimumSize(new Dimension(1024, 680));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        installWindowResizeHandler();
        installGlobalShortcuts();
        showDashboard();
    }

    private static void performLogout() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        loggedInUser = "";
        MotorPH_EmployeeApp.loggedInRole = null;
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
        currentView = "Dashboard";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Dashboard");
        addPageHeader(
                isHrUser() ? "HR Dashboard" : "Employee Dashboard",
                "Welcome back — " + getRoleDisplayName() + " Portal");

        java.awt.Rectangle bounds = getContentBounds();
        int calGap = 20;
        int leftW = bounds.width - DASHBOARD_CAL_W - calGap;
        int colW = (leftW - DASH_CARD_GAP) / 2;
        int dashRowH = Math.max(DASH_CARD_H, (bounds.height - DASH_CARD_GAP) / 2);
        int cardY = dashRowH * 2 + DASH_CARD_GAP;

        JPanel cards = new JPanel(null);
        cards.setBackground(APP_BG);

        if (isHrUser()) {
            cards.add(buildDashboardCard("Employee Records", "Add, update, and delete employee data",
                    0, 0, colW, dashRowH, "R", "Manage Records", true, e -> showEmployeeRecordsUI()));

            cards.add(buildDashboardCard("Payroll Processing", "Compute salaries for any employee",
                    colW + DASH_CARD_GAP, 0, colW, dashRowH, "P", "Open Payroll", true, e -> setupPayrollUI()));

            cards.add(buildDashboardCard("Employee Directory", "Search and view employee profiles",
                    0, dashRowH + DASH_CARD_GAP, colW, dashRowH, "D", "Open Directory", true,
                    e -> showEmployeeLookupUI()));

            cards.add(buildDashboardCard("HR Announcements", "Review payroll deadlines and HR tasks",
                    colW + DASH_CARD_GAP, dashRowH + DASH_CARD_GAP, colW, dashRowH, "A",
                    "Open Notifications", false, e -> showNotificationsUI()));
        } else {
            // Updated Employee view layout to present 4 symmetric boxes instead of 3
            cards.add(buildDashboardCard("My Payslip", "Salary breakdown & deductions",
                    0, 0, colW, dashRowH, "₱", "View Payslip", true, e -> setupPayrollUI()));

            cards.add(buildDashboardCard("My Profile", "Info & attendance history",
                    colW + DASH_CARD_GAP, 0, colW, dashRowH, "U", "Open Profile", true,
                    e -> showEmployeeLookupUI()));

            cards.add(buildDashboardCard("Edit My Profile", "Update home address and contact details",
                    0, dashRowH + DASH_CARD_GAP, colW, dashRowH, "E", "Edit Profile", true,
                    e -> editMyProfile()));

            cards.add(buildDashboardCard("Updates", "Pay day reminders & news",
                    colW + DASH_CARD_GAP, dashRowH + DASH_CARD_GAP, colW, dashRowH, "N",
                    "Open Notifications", false, e -> showNotificationsUI()));
        }

        cards.setPreferredSize(new java.awt.Dimension(leftW, cardY));

        JPanel calPanel = buildCalendarPanel(DASHBOARD_CAL_W, cardY, CAL_MONTH, CAL_YEAR);
        int calH = calPanel.getPreferredSize().height;

        JPanel leftCol = new JPanel(null);
        leftCol.setBackground(APP_BG);
        leftCol.setBounds(bounds.x, bounds.y, leftW, cardY);
        cards.setBounds(0, 0, leftW, cardY);
        leftCol.add(cards);
        frame.add(leftCol);

        JPanel calWrapper = new JPanel(null);
        calWrapper.setBackground(PALETTE_WHITE);
        calWrapper.setBounds(bounds.x + leftW + calGap, bounds.y, DASHBOARD_CAL_W, calH);
        calWrapper.setBorder(cardBorder());
        calPanel.setBounds(0, 0, DASHBOARD_CAL_W, calH);
        calPanel.setBorder(null);
        calWrapper.add(calPanel);
        frame.add(calWrapper);

        addStatusBar();
        updateDisplay();
    }

    private static void addCardAction(JPanel card, String label, int x, int y, int w, int h,
            boolean accent, ActionListener action) {
        JButton btn = new JButton(label);
        btn.setBounds(x, y, w, h);
        if (accent) {
            guiStyleAccentButton(btn);
        } else {
            styleStandardButton(btn);
        }
        btn.addActionListener(action);
        card.add(btn);
    }

    private static int[] splitButtonWidths(int cardW, int count) {
        int usable = cardW - DASH_CARD_INSET * 2 - DASH_CARD_BTN_GAP * (count - 1);
        int each = usable / count;
        int[] widths = new int[count];
        for (int i = 0; i < count; i++) {
            widths[i] = each;
        }
        return widths;
    }

    /**
     * Footer Y for dashboard card buttons — always below subtitle, anchored to card
     * bottom.
     */
    private static int resolveDashboardCardButtonY(JPanel card, int cardH) {
        int h = card.getBounds().height > 0 ? card.getBounds().height : cardH;
        int footerY = h - DASH_CARD_BTN_PAD - DASH_CARD_BTN_H;
        Object prop = card.getClientProperty(DASH_SUBTITLE_BOTTOM_KEY);
        int subtitleBottom = prop instanceof Integer ? (Integer) prop : 84;
        return Math.max(footerY, subtitleBottom + DASH_CARD_TEXT_GAP);
    }

    /**
     * Places one action button in the card footer — clear of title and subtitle.
     */
    private static void addDashboardCardButton(JPanel card, int cardW, int cardH,
            String label, int btnW, boolean accent,
            ActionListener action) {
        int btnY = resolveDashboardCardButtonY(card, cardH);
        addCardAction(card, label, DASH_CARD_INSET, btnY, btnW, DASH_CARD_BTN_H, accent, action);
    }

    /**
     * Places two side-by-side action buttons with consistent gap in the card
     * footer.
     */
    private static void addDashboardCardButtons(JPanel card, int cardW, int cardH,
            String[] labels, int[] widths, boolean[] accents,
            ActionListener[] actions) {
        int btnY = resolveDashboardCardButtonY(card, cardH);
        int x = DASH_CARD_INSET;
        for (int i = 0; i < labels.length; i++) {
            addCardAction(card, labels[i], x, btnY, widths[i], DASH_CARD_BTN_H, accents[i], actions[i]);
            x += widths[i] + DASH_CARD_BTN_GAP;
        }
    }

    private static JPanel buildDashboardCard(String title, String subtitle, int x, int y, int w, int h,
            String icon, String btnLabel, boolean accent, ActionListener action) {
        JPanel card = buildInfoCard(title, subtitle, x, y, w, h, icon);
        addDashboardCardButton(card, w, h, btnLabel, w - DASH_CARD_INSET * 2, accent, action);
        final Color normalBg = PALETTE_WHITE;
        final Color hoverBg = PALETTE_LIGHT_BLUE;
        Runnable navigate = () -> action.actionPerformed(
                new ActionEvent(card, ActionEvent.ACTION_PERFORMED, "dashboardCard"));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getY() >= h - DASH_CARD_FOOTER_H) {
                    return;
                }
                navigate.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(normalBg);
            }
        });
        for (Component child : card.getComponents()) {
            if (child instanceof JButton) {
                continue;
            }
            child.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    navigate.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(hoverBg);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    card.setBackground(normalBg);
                }
            });
        }
        return card;
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
        if (action != null)
            b.addActionListener(action);
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
        int sidebarH = frame.getHeight() - ins.top - ins.bottom - STATUS_BAR_H;
        JPanel sidebar = new JPanel(null);
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBounds(0, 0, sw, sidebarH);

        JPanel brandArea = new JPanel(null);
        brandArea.setBackground(SIDEBAR_TOP_BG);
        brandArea.setBounds(0, 0, sw, 96);
        JLabel brand = new JLabel("MotorPH");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(PALETTE_WHITE);
        brand.setBounds(20, 18, sw - 40, 28);
        JLabel brandSub = new JLabel(isHrUser() ? "HR Portal" : "Employee Portal");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(new Color(150, 180, 230));
        brandSub.setBounds(20, 46, sw - 40, 16);
        JPanel rolePill = new JPanel(null);
        rolePill.setBackground(isHrUser() ? new Color(55, 90, 160) : new Color(40, 100, 90));
        rolePill.setBounds(20, 68, 72, 20);
        JLabel roleBadge = new JLabel(getRoleDisplayName().toUpperCase(), SwingConstants.CENTER);
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        roleBadge.setForeground(PALETTE_WHITE);
        roleBadge.setBounds(0, 0, 72, 20);
        rolePill.add(roleBadge);
        brandArea.add(brand);
        brandArea.add(brandSub);
        brandArea.add(rolePill);
        sidebar.add(brandArea);

        JPanel sep = new JPanel();
        sep.setBackground(new Color(40, 72, 140));
        sep.setBounds(16, 96, sw - 32, 1);
        sidebar.add(sep);

        int btnY = 108;
        int btnH = SIDEBAR_NAV_BTN_H;
        addSidebarNavButton(sidebar, "Dashboard", 0, btnY, sw, btnH, "Dashboard".equals(activePage),
                e -> showDashboard());
        btnY += btnH + SIDEBAR_NAV_GAP;

        if (isHrUser()) {
            addSidebarNavButton(sidebar, "Records", 0, btnY, sw, btnH, "Records".equals(activePage),
                    e -> showEmployeeRecordsUI());
            btnY += btnH + SIDEBAR_NAV_GAP;
            addSidebarNavButton(sidebar, "Payroll", 0, btnY, sw, btnH, "Payroll".equals(activePage),
                    e -> setupPayrollUI());
            btnY += btnH + SIDEBAR_NAV_GAP;
            addSidebarNavButton(sidebar, HR_DIRECTORY_NAV, 0, btnY, sw, btnH,
                    HR_DIRECTORY_NAV.equals(activePage), e -> showEmployeeLookupUI());
            btnY += btnH + SIDEBAR_NAV_GAP;
        } else {
            addSidebarNavButton(sidebar, "My Profile", 0, btnY, sw, btnH,
                    "My Profile".equals(activePage), e -> showEmployeeLookupUI());
            btnY += btnH + SIDEBAR_NAV_GAP;
            addSidebarNavButton(sidebar, "Edit Profile", 0, btnY, sw, btnH,
                    "Edit My Profile".equals(activePage), e -> editMyProfile()); // Added row mapping hook
            btnY += btnH + SIDEBAR_NAV_GAP;
            addSidebarNavButton(sidebar, "My Payslip", 0, btnY, sw, btnH, "Payroll".equals(activePage),
                    e -> setupPayrollUI());
            btnY += btnH + SIDEBAR_NAV_GAP;
        }

        int unread = countUnreadNotifications();
        String notifLabel = unread > 0 ? "Notifications (" + unread + ")" : "Notifications";
        addSidebarNavButton(sidebar, notifLabel, 0, btnY, sw, btnH, "Notifications".equals(activePage),
                e -> showNotificationsUI());
        btnY += btnH + SIDEBAR_NAV_GAP;
        addSidebarNavButton(sidebar, "Help", 0, btnY, sw, btnH, "Help".equals(activePage), e -> showHelpCenterUI());

        JPanel logoutSep = new JPanel();
        logoutSep.setBackground(new Color(40, 72, 140));
        logoutSep.setBounds(16, sidebarH - 64, sw - 32, 1);
        sidebar.add(logoutSep);

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setBounds(16, sidebarH - 52, sw - 32, BTN_HEIGHT);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setForeground(PALETTE_WHITE);
        logoutBtn.setBackground(new Color(180, 48, 48));
        logoutBtn.setOpaque(true);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("Sign out and return to login");
        logoutBtn.addActionListener(e -> performLogout());
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(new Color(155, 38, 38));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(new Color(180, 48, 48));
            }
        });
        sidebar.add(logoutBtn);

        frame.add(sidebar);
    }

    /** Left-aligned sidebar nav row with active indicator and hover effect. */
    private static void addSidebarNavButton(JPanel sidebar, String text, int x, int y, int w, int h,
            boolean active, ActionListener action) {
        int rowW = w - 16;
        int rowH = h - 2;
        JPanel row = new JPanel(null);
        row.setBounds(x + 8, y + 1, rowW, rowH);
        row.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        row.setOpaque(true);

        if (active) {
            JPanel indicator = new JPanel();
            indicator.setBackground(PALETTE_WHITE);
            indicator.setBounds(0, 8, 3, rowH - 16);
            row.add(indicator);
        }

        JButton b = new JButton(text);
        b.setBounds(0, 0, rowW, rowH);
        b.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        b.setForeground(active ? PALETTE_WHITE : new Color(175, 200, 240));
        b.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        if (action != null)
            b.addActionListener(action);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!active) {
                    b.setBackground(SIDEBAR_HOVER);
                    row.setBackground(SIDEBAR_HOVER);
                    b.setForeground(PALETTE_WHITE);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!active) {
                    b.setBackground(SIDEBAR_BG);
                    row.setBackground(SIDEBAR_BG);
                    b.setForeground(new Color(175, 200, 240));
                }
            }
        });

        row.add(b);
        sidebar.add(row);
    }

    /**
     * Adds a white top-bar header (page title + logged-in user).
     * Content should begin at {@link #PAGE_TOP} after calling this.
     */
    private static void addPageHeader(String title) {
        addPageHeader(title, null);
    }

    private static void addPageHeader(String title, String subtitle) {
        int contentW = getVisibleWidth() - SIDEBAR_WIDTH;
        JPanel topBar = new JPanel(null);
        topBar.setBackground(APP_BG);
        topBar.setBounds(SIDEBAR_WIDTH, 0, contentW, PAGE_HEADER_H);

        boolean hasSubtitle = subtitle != null && !subtitle.isEmpty();
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(TEXT_DARK_NAVY);
        titleLbl.setBounds(CONTENT_PAD, hasSubtitle ? 12 : 22, 480, 28);
        topBar.add(titleLbl);

        if (hasSubtitle) {
            JLabel subLbl = new JLabel(subtitle);
            subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subLbl.setForeground(new Color(72, 84, 108));
            subLbl.setBounds(CONTENT_PAD, 42, contentW - 280, 20);
            topBar.add(subLbl);
        }
        
        frame.add(topBar);
    }

    static final int STATUS_BAR_H = 24;

    private static void addStatusBar() {
        if (statusBarTimer != null && statusBarTimer.isRunning())
            statusBarTimer.stop();

        // Use frame insets so the position is correct before the layout is validated
        java.awt.Insets ins = frame.getInsets();
        int visibleH = frame.getHeight() - ins.top - ins.bottom;
        int barY = visibleH - STATUS_BAR_H;

        int barW = getVisibleWidth();
        JPanel bar = new JPanel(null);
        bar.setBackground(PALETTE_WHITE);
        bar.setBounds(0, barY, barW, STATUS_BAR_H);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER_COLOR));

        JLabel timeLbl = new JLabel();
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(TEXT_MUTED);
        timeLbl.setBounds(SIDEBAR_WIDTH + 12, 4, 360, 16);
        bar.add(timeLbl);

        statusToastLbl = new JLabel("", SwingConstants.CENTER);
        statusToastLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusToastLbl.setForeground(new Color(34, 160, 90));
        statusToastLbl.setBounds(SIDEBAR_WIDTH + 200, 4, Math.max(200, barW - SIDEBAR_WIDTH - 360), 16);
        bar.add(statusToastLbl);

        JLabel onlineLbl = new JLabel("● Online");
        onlineLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        onlineLbl.setForeground(new Color(34, 160, 90));
        onlineLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        onlineLbl.setBounds(barW - 130, 4, 110, 16);
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

    private static JPanel buildInfoCard(String title, String subtitle, int x, int y, int w, int h, String icon) {
        JPanel p = new JPanel(null);
        p.setBackground(PALETTE_WHITE);
        p.setBounds(x, y, w, h);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT_BLUE),
                cardBorder()));

        JPanel iconCircle = new JPanel(null);
        iconCircle.setBackground(new Color(232, 242, 255));
        iconCircle.setBounds(DASH_CARD_INSET, 18, 44, 44);
        iconCircle.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 248), 1));
        JLabel iconLbl = new JLabel(icon == null ? "•" : icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconLbl.setForeground(ACCENT_BLUE);
        iconLbl.setBounds(0, 0, 44, 44);
        iconCircle.add(iconLbl);
        p.add(iconCircle);

        int textX = DASH_CARD_INSET + 56;
        int textW = Math.max(80, w - textX - DASH_CARD_INSET);
        int subtitleY = 46;
        int subtitleMaxH = Math.max(24, h - DASH_CARD_FOOTER_H - subtitleY - DASH_CARD_TEXT_GAP);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setForeground(TEXT_DARK_NAVY);
        t.setBounds(textX, 20, textW, 22);

        JLabel s;
        if (subtitle != null && subtitle.startsWith("Total:")) {
            s = new JLabel(subtitle);
            s.setFont(new Font("Segoe UI", Font.BOLD, 18));
            s.setForeground(ACCENT_BLUE);
            s.setBounds(textX, subtitleY, textW, Math.min(32, subtitleMaxH));
        } else {
            String subText = subtitle == null ? "" : subtitle;
            s = new JLabel("<html><body style='width:" + textW + "px'>" + escapeHtml(subText) + "</body></html>");
            s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            s.setForeground(TEXT_MUTED);
            s.setBounds(textX, subtitleY, textW, subtitleMaxH);
        }
        p.putClientProperty(DASH_SUBTITLE_BOTTOM_KEY, subtitleY + subtitleMaxH);
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
        // --- Layout constants (scale grid to avoid clipping on shorter panels) ---
        final int NAV_Y = 8;
        final int NAV_H = 26;
        final int DOW_Y = NAV_Y + NAV_H + 6;
        final int DOW_H = 20;
        final int GRID_TOP = DOW_Y + DOW_H;
        final int ROWS = 6;
        final int EVT_MAX_H = 112;
        final int EVT_MIN_H = 84;
        int gridMaxH = height - GRID_TOP - EVT_MIN_H - 16;
        final int CELL_H = Math.min(48, Math.max(28, gridMaxH / ROWS));
        final int GRID_BOT = GRID_TOP + ROWS * CELL_H;
        final int EVT_Y = GRID_BOT + 8;
        final int EVT_H = Math.min(EVT_MAX_H, Math.max(EVT_MIN_H, height - EVT_Y - 8));

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));

        LocalDate viewDate = LocalDate.of(year, month, 1);

        JButton btnPrev = new JButton("<");
        btnPrev.setBounds(6, NAV_Y, 28, NAV_H);
        styleCalNavButton(btnPrev);
        btnPrev.addActionListener(e -> {
            CAL_MONTH--;
            if (CAL_MONTH < 1) {
                CAL_MONTH = 12;
                CAL_YEAR--;
            }
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
            if (CAL_MONTH > 12) {
                CAL_MONTH = 1;
                CAL_YEAR++;
            }
            showDashboard();
        });
        panel.add(btnNext);

        Map<Integer, java.util.List<String>> birthdays = new HashMap<>();
        List<String[]> all = FileHandlerModule.getAllEmployees();
        for (String[] row : all) {
            String b = safeColumn(row, EmployeeModule.BIRTHDAY);
            if (b == null || b.isEmpty() || b.equals("-") || !b.contains("/"))
                continue;
            try {
                String[] parts = b.split("/");
                if (parts.length < 2)
                    continue;
                int m = Integer.parseInt(parts[0].trim());
                int d = Integer.parseInt(parts[1].trim());
                if (m == month)
                    birthdays.computeIfAbsent(d, k -> new LinkedList<>()).add(EmployeeModule.fullName(row));
            } catch (NumberFormatException ex) {
                /* ignore malformed */ }
        }

        Map<Integer, java.util.List<String>> attendanceMap = new HashMap<>();
        List<String[]> attendance = FileHandlerModule.getAllAttendanceRecords();
        for (String[] arow : attendance) {
            if (arow.length < 3)
                continue;
            String dateStr = arow[2];
            if (!dateStr.contains("/"))
                continue;
            try {
                String[] p = dateStr.split("/");
                if (p.length < 2)
                    continue;
                int m = Integer.parseInt(p[0].trim());
                int d = Integer.parseInt(p[1].trim());
                if (m == month) {
                    String name = arow.length > 1 ? arow[1] : arow[0];
                    attendanceMap.computeIfAbsent(d, k -> new LinkedList<>()).add(name + " (" + arow[0] + ")");
                }
            } catch (NumberFormatException ex) {
                /* ignore malformed */ }
        }

        int cellW = width / 7;
        String[] dow = { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" };
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
                    boolean isPayDay = (displayDay == 15) || (displayDay == totalDays);
                    boolean hasBirthday = birthdays.containsKey(displayDay);
                    boolean hasAttend = attendanceMap.containsKey(displayDay);
                    boolean isToday = (year == today.getYear()
                            && month == today.getMonthValue()
                            && displayDay == today.getDayOfMonth());

                    // Today always wins over other backgrounds; pay day / birthday shown via dots
                    Color cellBg = PALETTE_WHITE;
                    if (isPayDay)
                        cellBg = new Color(232, 244, 255);
                    if (hasBirthday && !isPayDay)
                        cellBg = new Color(232, 252, 232);
                    if (isToday)
                        cellBg = new Color(235, 245, 255);
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
                        int badgeW = Math.min(22, cellW - 8);
                        JPanel badge = new JPanel(null);
                        badge.setBackground(ACCENT_BLUE);
                        badge.setOpaque(true);
                        badge.setBounds((cellW - badgeW) / 2, 4, badgeW, 16);
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
                    if (isToday)
                        tip.append("Today\n");
                    if (isPayDay)
                        tip.append("Pay Day\n");
                    if (hasBirthday)
                        tip.append("Birthday(s): ").append(birthdays.get(displayDay).size()).append("\n");
                    if (hasAttend)
                        tip.append("Attendance: ").append(attendanceMap.get(displayDay).size());
                    if (tip.length() == 0)
                        tip.append("No events");
                    cell.setToolTipText(tip.toString().trim());

                    final int dnum = displayDay;
                    final Color normBg = cellBg;
                    cell.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            StringBuilder sb = new StringBuilder();
                            if ((dnum == 15) || (dnum == totalDays))
                                sb.append("Pay Day: ").append(dnum).append("/").append(month).append("\n\n");
                            if (birthdays.containsKey(dnum)) {
                                sb.append("Birthdays (").append(dnum).append("/").append(month).append("):\n");
                                for (String n : birthdays.get(dnum))
                                    sb.append("  - ").append(n).append("\n");
                                sb.append("\n");
                            }
                            if (attendanceMap.containsKey(dnum)) {
                                sb.append("Attendance (").append(dnum).append("/").append(month).append("):\n");
                                for (String n : attendanceMap.get(dnum))
                                    sb.append("  - ").append(n).append("\n");
                            }
                            if (sb.length() == 0)
                                sb.append("No events on ").append(dnum).append(" ").append(viewDate.getMonth().name())
                                        .append(".");
                            eventsArea.setText(sb.toString());
                            eventsArea.setCaretPosition(0);
                        }

                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            cell.setBackground(new Color(220, 235, 255));
                        }

                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            cell.setBackground(normBg);
                        }
                    });
                } else {
                    cell.setBackground(new Color(248, 250, 253));
                }
                panel.add(cell);
            }
        }

        panel.add(eventsScroll);

        int legendY = EVT_Y + EVT_H + 4;
        JLabel legend = new JLabel(
                "<html><span style='color:#1877F2;font-weight:bold;'>P</span> Pay day &nbsp; "
                        + "<span style='color:#28AA46;font-weight:bold;'>*</span> Birthday &nbsp; "
                        + "<span style='color:#1877F2;'>■</span> Today</html>");
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        legend.setForeground(TEXT_MUTED);
        legend.setBounds(8, legendY, width - 16, 16);
        panel.add(legend);

        panel.setPreferredSize(new java.awt.Dimension(width, legendY + 20));
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
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(210, 228, 255));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(237, 243, 255));
            }
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

        // Container Panel (Lesson: Panels) shifted just below the header strip.
        // x = (frameWidth 520 - cardWidth 400) / 2 = 60, so left and right margins both
        // equal 60.
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
     * Employee name auto-updates on key release; Enter on ID field triggers
     * validation.
     */
    static void setupPayrollUI() {
        currentView = "Payroll";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Payroll");
        addPageHeader(isHrUser() ? "Payroll Processing" : "My Payslip");

        java.awt.Rectangle bounds = getContentBounds();
        JPanel payrollCard = new JPanel(null);
        payrollCard.setBackground(PALETTE_WHITE);
        payrollCard.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        payrollCard.setBorder(cardBorder());

        int payrollWidth = Math.min(560, bounds.width - 80);
        int payrollX = (bounds.width - payrollWidth) / 2;

        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(PALETTE_WHITE);
        formPanel.setBounds(payrollX, 24, payrollWidth, 280);

        // Form columns: label x=30 width=230 (ends x=260),
        // field x=260 width=260 (ends x=520) matches scroll pane edge.
        JLabel lblEmpNo = createStyledLabel("Employee Number (ex. 10001):");
        lblEmpNo.setBounds(30, 20, 230, 30);
        txtEmployeeNo = createStyledTextField(true);
        txtEmployeeNo.setBounds(260, 20, 260, 30);

        // --- HARD-LOCK TO 10001 FOR EMPLOYEES ---
        if (isEmployeeUser()) {
            txtEmployeeNo.setText("10001");
            txtEmployeeNo.setEditable(false);
            txtEmployeeNo.setBackground(new Color(230, 236, 246));
        }

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
        monthCombo.setSelectedIndex(getDefaultPayrollMonthIndex());

        JLabel lblYear = createStyledLabel("Pay Coverage Year (2024 only):");
        lblYear.setBounds(30, 155, 230, 30);
        txtYear = createStyledTextField(true);
        txtYear.setBounds(260, 155, 260, 30);
        // Pre-fill the only valid year so users don't accidentally submit blank
        txtYear.setText("2024");

        // Core Trigger Processing Action Switches (Lesson: Buttons)
        // Two equal-width buttons spanning x=30 to x=520 with a 20px gap between them.
        int halfBtn = (payrollWidth - 70) / 2;
        JButton btnProcess = new JButton(isHrUser() ? "Compute Salaries" : "View Payslip");
        btnProcess.setBounds(30, 220, halfBtn, BTN_HEIGHT);
        guiStyleAccentButton(btnProcess);
        btnProcess.addActionListener(e -> runPayrollCalculation());

        JButton btnBack = new JButton("Back to Dashboard");
        btnBack.setBounds(40 + halfBtn, 220, halfBtn, BTN_HEIGHT);
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

        // Trigger dynamic auto-fill resolution chain right away if ID is pre-filled
        if (!txtEmployeeNo.getText().isEmpty()) {
            updateEmployeeNameFromId(false);
        }

        if (pendingPayrollEmployeeId != null && !pendingPayrollEmployeeId.isEmpty()) {
            // Respect selection override if HR clicked through from records panel dashboard
            if (isHrUser()) {
                txtEmployeeNo.setText(pendingPayrollEmployeeId);
                updateEmployeeNameFromId(false);
            }
            pendingPayrollEmployeeId = null;
        }

        JPanel divider = new JPanel();
        divider.setBackground(CARD_BORDER_COLOR);
        divider.setBounds(payrollX, 318, payrollWidth, 1);
        payrollCard.add(divider);

        JLabel lblOutputHeader = createStyledLabel("Payslip Output");
        lblOutputHeader.setBounds(payrollX, 332, 200, 24);
        payrollCard.add(lblOutputHeader);

        lblPayrollSummary = new JLabel("Gross: —    Deductions: —    Net: —");
        lblPayrollSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPayrollSummary.setForeground(ACCENT_BLUE);
        lblPayrollSummary.setBounds(payrollX, 358, payrollWidth, 22);
        payrollCard.add(lblPayrollSummary);

        txtResultArea = new JTextArea();
        txtResultArea.setBackground(INPUT_BG);
        txtResultArea.setForeground(TEXT_DARK_NAVY);
        txtResultArea.setFont(RECEIPT_FONT);
        txtResultArea.setEditable(false);

        int scrollY = 386;
        int scrollH = Math.max(140, bounds.height - scrollY - 16);
        JScrollPane scrollPane = new JScrollPane(txtResultArea);
        scrollPane.setBounds(payrollX, scrollY, payrollWidth, scrollH - 44);
        styleScrollPane(scrollPane);

        int exportY = scrollY + scrollH - 36;
        int exportBtnW = (payrollWidth - 50) / 2;
        JButton btnCopyPayslip = new JButton("Copy Payslip");
        btnCopyPayslip.setBounds(payrollX, exportY, exportBtnW, BTN_HEIGHT);
        styleStandardButton(btnCopyPayslip);
        btnCopyPayslip.addActionListener(e -> copyPayslipToClipboard());

        JButton btnExportPayslip = new JButton("Save to File");
        btnExportPayslip.setBounds(payrollX + exportBtnW + 10, exportY, exportBtnW, BTN_HEIGHT);
        styleStandardButton(btnExportPayslip);
        btnExportPayslip.addActionListener(e -> exportPayslipToFile());

        payrollCard.add(formPanel);
        payrollCard.add(scrollPane);
        payrollCard.add(btnCopyPayslip);
        payrollCard.add(btnExportPayslip);
        frame.add(payrollCard);
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
        currentView = isHrUser() ? "Directory" : "My Profile";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar(isHrUser() ? HR_DIRECTORY_NAV : "My Profile");
        addPageHeader(isHrUser() ? HR_DIRECTORY_TITLE : "My Profile");

        java.awt.Rectangle bounds = getContentBounds();
        JPanel lookupPanel = new JPanel();
        lookupPanel.setLayout(null);
        lookupPanel.setBackground(PALETTE_WHITE);
        lookupPanel.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        lookupPanel.setBorder(cardBorder());

        JLabel lblPrompt = createStyledLabel(isHrUser()
                ? "Employee ID or Name"
                : "Employee ID");
        lblPrompt.setBounds(32, 28, bounds.width - 64, 24);

        txtLookupInput = createStyledTextField(true);
        txtLookupInput.setBounds(32, 56, bounds.width - 64, FIELD_HEIGHT);
        attachFocusHighlight(txtLookupInput);
        attachPlaceholder(txtLookupInput, isHrUser() ? "e.g. 10001 or Garcia" : "e.g. 10024");

        // --- HARD-LOCK INPUT TO 10001 FOR EMPLOYEES ---
        if (isEmployeeUser()) {
            txtLookupInput.setText("10001");
            txtLookupInput.setEditable(false);
            txtLookupInput.setBackground(new Color(230, 236, 246));
        }

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

        int searchBtnW = 160;
        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(32, 108, searchBtnW, BTN_HEIGHT);
        guiStyleAccentButton(btnSearch);
        btnSearch.addActionListener(e -> runEmployeeLookupAction());

        JButton btnClose = new JButton("Back to Dashboard");
        btnClose.setBounds(32 + searchBtnW + 12, 108, searchBtnW, BTN_HEIGHT);
        styleStandardButton(btnClose);
        btnClose.addActionListener(e -> showDashboard());

        txtLookupDisplay = new JTextArea();
        txtLookupDisplay.setFont(RECEIPT_FONT);
        txtLookupDisplay.setBackground(INPUT_BG);
        txtLookupDisplay.setForeground(TEXT_DARK_NAVY);
        txtLookupDisplay.setEditable(false);

        int resultW = bounds.width - 64;
        int infoScrollH = Math.max(200, bounds.height - 168);
        JScrollPane infoScroll = new JScrollPane(txtLookupDisplay);
        infoScroll.setBounds(32, 160, resultW, infoScrollH);
        styleScrollPane(infoScroll);

        lookupPanel.add(lblPrompt);
        lookupPanel.add(txtLookupInput);
        lookupPanel.add(btnSearch);
        lookupPanel.add(btnClose);
        lookupPanel.add(infoScroll);

        frame.add(lookupPanel);
        addStatusBar();
        updateDisplay();

        // --- AUTOMATICALLY TRIGGER DATA FOLDER FETCH IMMEDIATELY ON WINDOW
        // INITIALIZATION ---
        if (isEmployeeUser()) {
            runEmployeeLookupAction();
        }
    }

    /** True when the logged-in user is on the HR portal. */
    private static boolean isHrUser() {
        return MotorPH_EmployeeApp.loggedInRole == MotorPH_EmployeeApp.UserRole.HR;
    }

    /** True when the logged-in user is on the employee portal. */
    private static boolean isEmployeeUser() {
        return MotorPH_EmployeeApp.loggedInRole == MotorPH_EmployeeApp.UserRole.EMPLOYEE;
    }

    private static String getRoleDisplayName() {
        return isHrUser() ? "HR" : "Employee";
    }

    /**
     * Employee Records screen (Feature 1): JTable, full form, view dialog, and CRUD
     * for payroll staff.
     */
    static void showEmployeeRecordsUI() {
        currentView = "Records";
        if (!isHrUser()) {
            JOptionPane.showMessageDialog(frame,
                    "Employee record management is only available in the HR portal.\n"
                            + "Log in with the HR credentials to access this screen.",
                    "HR Portal Required", JOptionPane.INFORMATION_MESSAGE);
            showDashboard();
            return;
        }

        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Records");
        addPageHeader("Employee Records");

        java.awt.Rectangle bounds = getContentBounds();
        int panelW = bounds.width;
        int panelH = bounds.height;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(bounds.x, bounds.y, panelW, panelH);
        panel.setBorder(cardBorder());

        JLabel tableTitle = new JLabel("Employee List");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(TEXT_DARK_NAVY);
        tableTitle.setBounds(20, 16, 200, 24);
        panel.add(tableTitle);

        final int refreshW = 88;
        final int viewDetailsW = 108;
        final int toolbarGap = 8;
        final int toolbarRight = 20;

        JButton btnViewDetails = new JButton("View Details");
        btnViewDetails.setBounds(panelW - toolbarRight - viewDetailsW, 10, viewDetailsW, 28);
        styleStandardButton(btnViewDetails);
        btnViewDetails.addActionListener(e -> showSelectedEmployeeDetailDialog());
        panel.add(btnViewDetails);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(panelW - toolbarRight - viewDetailsW - toolbarGap - refreshW, 10, refreshW, 28);
        styleStandardButton(btnRefresh);
        btnRefresh.addActionListener(e -> {
            refreshEmployeeTable();
            showToast("Employee table refreshed from CSV.");
        });
        panel.add(btnRefresh);

        employeeTableModel = new DefaultTableModel(EmployeeRecordsModule.TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(employeeTableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applyModernTableStyle(employeeTable);
        configureEmployeeTableColumns(employeeTable);
        enableTableSorting(employeeTable);

        final int ACTION_H = RECORDS_ACTION_BAR_H;
        int formW = Math.max(340, (int) (panelW * 0.36));
        int tableW = panelW - formW - 52;
        int formX = 20 + tableW + 16;

        txtRecSearch = createStyledTextField(true);
        txtRecSearch.setBounds(20, 42, Math.max(180, tableW - 16), 28);
        attachPlaceholder(txtRecSearch, "Search name or ID (Ctrl+F)");
        attachFocusHighlight(txtRecSearch);
        txtRecSearch.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                applyEmployeeTableFilter();
            }
        });
        panel.add(txtRecSearch);

        JLabel lblTableHint = new JLabel("Double-click a row for full details. Click column headers to sort.");
        lblTableHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTableHint.setForeground(TEXT_MUTED);
        lblTableHint.setBounds(20, panelH - 28, tableW, 18);
        panel.add(lblTableHint);

        final int TABLE_TOP = 78;
        final int TOOLBAR_H = TABLE_TOP;
        JScrollPane tableScroll = new JScrollPane(employeeTable);
        tableScroll.setBounds(20, TABLE_TOP, tableW, panelH - TABLE_TOP - 34);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        styleScrollPane(tableScroll);
        panel.add(tableScroll);

        {
            JLabel formTitle = new JLabel("Record Details");
            formTitle.setFont(APP_FONT_BOLD);
            formTitle.setForeground(TEXT_DARK_NAVY);
            formTitle.setBounds(formX, 16, formW, 24);
            panel.add(formTitle);

            lblRecFormHint = new JLabel("Select a row to edit, or click Add for a new employee.");
            lblRecFormHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblRecFormHint.setForeground(TEXT_MUTED);
            lblRecFormHint.setBounds(formX, 40, formW, 18);
            panel.add(lblRecFormHint);

            JPanel formInner = new JPanel(null);
            formInner.setBackground(PALETTE_WHITE);
            int labelW = 108;
            int fieldX = labelW + 8;
            int fieldW = Math.max(180, formW - fieldX - 8);
            int rowH = 28;
            int rowGap = 8;
            int fy = 0;

            fy = addRecordFormSection(formInner, fy, fieldX, fieldW, rowGap, "Personal Information");
            txtRecEmpNo = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Employee #:", true);
            fy += rowH + rowGap;
            txtRecLastName = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Last Name:", true);
            fy += rowH + rowGap;
            txtRecFirstName = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "First Name:", true);
            fy += rowH + rowGap;
            txtRecBirthday = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Birthday:", true);
            fy += rowH + rowGap;
            txtRecAddress = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Address:", true);
            fy += rowH + rowGap;
            txtRecPhone = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Phone:", true);
            fy += rowH + rowGap;

            fy = addRecordFormSection(formInner, fy, fieldX, fieldW, rowGap, "Government IDs");
            txtRecSSS = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "SSS #:", true);
            fy += rowH + rowGap;
            txtRecPhilHealth = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "PhilHealth #:",
                    true);
            fy += rowH + rowGap;
            txtRecTIN = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "TIN #:", true);
            fy += rowH + rowGap;
            txtRecPagIBIG = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Pag-IBIG #:", true);
            fy += rowH + rowGap;

            fy = addRecordFormSection(formInner, fy, fieldX, fieldW, rowGap, "Employment Details");
            txtRecStatus = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Status:", true);
            fy += rowH + rowGap;
            txtRecPosition = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Position:", true);
            fy += rowH + rowGap;
            txtRecSupervisor = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Supervisor:", true);
            fy += rowH + rowGap;
            txtRecBasicSalary = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Basic Salary:",
                    true);
            fy += rowH + rowGap;
            txtRecHourlyRate = addRecordFormField(formInner, 0, fy, labelW, fieldX, fieldW, rowH, "Hourly Rate:", true);
            formInner.setPreferredSize(new java.awt.Dimension(fieldX + fieldW, fy + rowH));

            int formScrollH = panelH - TOOLBAR_H - ACTION_H - 18;
            JScrollPane formScroll = new JScrollPane(formInner);
            formScroll.setBounds(formX, TOOLBAR_H + 18, formW, Math.max(120, formScrollH));
            styleScrollPane(formScroll);
            panel.add(formScroll);

            JPanel actionBar = new JPanel(null);
            actionBar.setBackground(TABLE_HEADER_BG);
            actionBar.setBounds(formX, panelH - ACTION_H - 6, formW, ACTION_H);
            actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER_COLOR));

            final int recBtnGap = 6;
            final int recBtnH = 32;
            int recBtnW = Math.max(64, (formW - recBtnGap * 3) / 4);

            JButton btnAdd = new JButton("Add");
            btnAdd.setBounds(0, 8, recBtnW, recBtnH);
            guiStyleAccentButton(btnAdd);
            btnAdd.addActionListener(e -> runAddEmployeeRecord());

            btnRecUpdate = new JButton("Update");
            btnRecUpdate.setBounds(recBtnW + recBtnGap, 8, recBtnW, recBtnH);
            styleStandardButton(btnRecUpdate);
            btnRecUpdate.setEnabled(false);
            btnRecUpdate.addActionListener(e -> runUpdateEmployeeRecord());

            btnRecDelete = new JButton("Delete");
            btnRecDelete.setBounds((recBtnW + recBtnGap) * 2, 8, recBtnW, recBtnH);
            styleStandardButton(btnRecDelete);
            btnRecDelete.setEnabled(false);
            btnRecDelete.addActionListener(e -> runDeleteEmployeeRecord());

            JButton btnClear = new JButton("Clear Form");
            btnClear.setBounds((recBtnW + recBtnGap) * 3, 8,
                    Math.max(64, formW - (recBtnW + recBtnGap) * 3), recBtnH);
            styleStandardButton(btnClear);
            btnClear.addActionListener(e -> {
                if (confirmDiscardRecordChanges()) {
                    clearEmployeeRecordForm();
                }
            });

            btnRecComputePayroll = new JButton("Compute Salary");
            btnRecComputePayroll.setBounds(0, 46, formW, recBtnH);
            guiStyleAccentButton(btnRecComputePayroll);
            btnRecComputePayroll.setEnabled(false);
            btnRecComputePayroll.addActionListener(e -> {
                if (selectedEmployeeId != null)
                    openPayrollForEmployee(selectedEmployeeId);
            });

            actionBar.add(btnAdd);
            actionBar.add(btnRecUpdate);
            actionBar.add(btnRecDelete);
            actionBar.add(btnClear);
            actionBar.add(btnRecComputePayroll);
            panel.add(actionBar);
        }

        employeeTable.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            if (e.getValueIsAdjusting())
                return;
            int viewRow = employeeTable.getSelectedRow();
            if (viewRow < 0) {
                updateEmployeeRecordActionState(false);
                return;
            }
            if (!confirmDiscardRecordChanges()) {
                if (lastSelectedEmployeeRow >= 0 && lastSelectedEmployeeRow < employeeTable.getRowCount()) {
                    employeeTable.setRowSelectionInterval(lastSelectedEmployeeRow, lastSelectedEmployeeRow);
                } else {
                    employeeTable.clearSelection();
                }
                return;
            }
            int modelRow = employeeTable.convertRowIndexToModel(viewRow);
            String id = String.valueOf(employeeTableModel.getValueAt(modelRow, 0)).trim();
            String data = FileHandlerModule.findEmployeeData(id);
            if (data == null)
                return;
            populateEmployeeRecordForm(FileHandlerModule.smartSplit(data));
            selectedEmployeeId = id;
            txtRecEmpNo.setEditable(false);
            captureRecordFormBaseline();
            lastSelectedEmployeeRow = viewRow;
            updateEmployeeRecordActionState(true);
        });

        employeeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2)
                    showSelectedEmployeeDetailDialog();
            }
        });

        refreshEmployeeTable();
        updateEmployeeRecordActionState(false);
        frame.add(panel);
        addStatusBar();
        updateDisplay();
    }

    /**
     * Builds the Self-Service profile dashboard exclusively for logged-in
     * Employees.
     * Refactored to seamlessly match the application's modern sidebar navigation
     * workspace.
     */
    public static void editMyProfile() {
        currentView = "Edit My Profile";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Edit My Profile");
        addPageHeader("Edit My Profile", "Self-Service Portal — Keep your contact information accurate");

        java.awt.Rectangle bounds = getContentBounds();

        // Single central elegant input card matching payroll card designs
        JPanel editCard = new JPanel(null);
        editCard.setBackground(PALETTE_WHITE);
        editCard.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        editCard.setBorder(cardBorder());

        int formWidth = Math.min(540, bounds.width - 80);
        int formX = (bounds.width - formWidth) / 2;
        int formY = 32;
        int rowGap = 12;

        JLabel lblFormTitle = createStyledLabel("Profile Information:");
        lblFormTitle.setBounds(formX, formY, 200, 25);
        editCard.add(lblFormTitle);

        int labelW = 140;
        int fieldX = formX + labelW;
        int fieldW = formWidth - labelW;
        int currentY = formY + 35;

        // Row 1: Target Employee ID Input
        JLabel lblEditId = createStyledLabel("Employee ID:");
        lblEditId.setBounds(formX, currentY, labelW, FIELD_HEIGHT);
        editCard.add(lblEditId);

        final JTextField txtEditId = createStyledTextField(true);
        txtEditId.setBounds(fieldX, currentY, fieldW, FIELD_HEIGHT);
        editCard.add(txtEditId);

        // --- HARD-LOCK TO 10001---
        if (isEmployeeUser()) {
            txtEditId.setText("10001");
            txtEditId.setEditable(false);
            txtEditId.setBackground(new Color(230, 236, 246));
        }

        // Row 2: Read-Only Employee Name Field
        currentY += FIELD_HEIGHT + rowGap;
        JLabel lblEmpName = createStyledLabel("Employee Name:");
        lblEmpName.setBounds(formX, currentY, labelW, FIELD_HEIGHT);
        editCard.add(lblEmpName);

        final JTextField txtEmpName = createStyledTextField(false);
        txtEmpName.setBounds(fieldX, currentY, fieldW, FIELD_HEIGHT);
        editCard.add(txtEmpName);

        // Row 3: Editable Home Address Field
        currentY += FIELD_HEIGHT + rowGap;
        JLabel lblEditAddress = createStyledLabel("Home Address:");
        lblEditAddress.setBounds(formX, currentY, labelW, FIELD_HEIGHT);
        editCard.add(lblEditAddress);

        final JTextField txtEditAddress = createStyledTextField(true);
        txtEditAddress.setBounds(fieldX, currentY, fieldW, FIELD_HEIGHT);
        editCard.add(txtEditAddress);

        // Row 4: Editable Contact Phone Number Field
        currentY += FIELD_HEIGHT + rowGap;
        JLabel lblEditPhone = createStyledLabel("Phone Number:");
        lblEditPhone.setBounds(formX, currentY, labelW, FIELD_HEIGHT);
        editCard.add(lblEditPhone);

        final JTextField txtEditPhone = createStyledTextField(true);
        txtEditPhone.setBounds(fieldX, currentY, fieldW, FIELD_HEIGHT);
        editCard.add(txtEditPhone);

        // Helper method inside listener to run data fetching
        Runnable fetchCsvData = () -> {
            String inputIdStr = txtEditId.getText().trim();
            resetFieldBorder(txtEditId);

            if (inputIdStr.isEmpty()) {
                txtEmpName.setText("");
                txtEditAddress.setText("");
                txtEditPhone.setText("");
                return;
            }

            if (FileHandlerModule.employeeExists(inputIdStr)) {
                String data = FileHandlerModule.findEmployeeData(inputIdStr);
                String[] emp = FileHandlerModule.smartSplit(data);

                txtEmpName.setText(EmployeeModule.fullName(emp));
                txtEditAddress.setText(safeColumn(emp, EmployeeModule.ADDRESS));
                txtEditPhone.setText(safeColumn(emp, EmployeeModule.PHONE));
            } else {
                txtEmpName.setText("");
                txtEditAddress.setText("");
                txtEditPhone.setText("");
            }
        };

        // --- DIRECT CSV LIVE SEARCH KEY LISTENER ---
        txtEditId.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                fetchCsvData.run();
            }
        });

        // Run initial data fetch if field populated
        if (!txtEditId.getText().isEmpty()) {
            fetchCsvData.run();
        }

        // --- CONTROL BUTTONS CONFIGURATION ---
        int buttonW = 180;
        int actionButtonsY = currentY + FIELD_HEIGHT + 30;
        int btnGap = 20;
        int combinedBtnW = (buttonW * 2) + btnGap;
        int startBtnX = formX + (formWidth - combinedBtnW) / 2;

        JButton btnSaveChanges = new JButton("Save Changes");
        btnSaveChanges.setBounds(startBtnX, actionButtonsY, buttonW, BTN_HEIGHT);
        guiStyleAccentButton(btnSaveChanges);
        btnSaveChanges.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String targetId = txtEditId.getText().trim();
                String newAddressValue = txtEditAddress.getText().trim();
                String newPhoneValue = txtEditPhone.getText().trim();

                if (targetId.isEmpty() || !targetId.matches("\\d+")) {
                    setFieldError(txtEditId);
                    JOptionPane.showMessageDialog(frame, "Please provide a valid numeric Employee ID first.",
                            "Input Validation Failure", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!FileHandlerModule.employeeExists(targetId)) {
                    setFieldError(txtEditId);
                    JOptionPane.showMessageDialog(frame,
                            "The entered Employee ID \"" + targetId + "\" does not exist in our CSV dataset.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Commit direct record modifications back to data stream via your I/O routine
                boolean writeStatusSuccess = FileHandlerModule.updateEmployeeContactInfo(targetId, newAddressValue,
                        newPhoneValue);

                if (writeStatusSuccess) {
                    JOptionPane.showMessageDialog(frame, "Successfully updated records for Employee #" + targetId + "!",
                            "Database Saved", JOptionPane.INFORMATION_MESSAGE);

                    showToast("Profile changes committed directly to records file.");

                    // Re-render dashboard rather than manually clearing
                    showDashboard();
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Critical failure: Could not update the CSV disk file data records.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        editCard.add(btnSaveChanges);

        JButton btnBackToMenu = new JButton("Back to Dashboard");
        btnBackToMenu.setBounds(startBtnX + buttonW + btnGap, actionButtonsY, buttonW, BTN_HEIGHT);
        styleStandardButton(btnBackToMenu);
        btnBackToMenu.addActionListener(e -> showDashboard());
        editCard.add(btnBackToMenu);

        frame.add(editCard);
        addStatusBar();
        updateDisplay();
    }

    static void openPayrollForEmployee(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty())
            return;
        pendingPayrollEmployeeId = employeeId.trim();
        setupPayrollUI();
    }

    private static void showSelectedEmployeeDetailDialog() {
        if (selectedEmployeeId == null || selectedEmployeeId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Select an employee from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String data = FileHandlerModule.findEmployeeData(selectedEmployeeId);
        if (data == null) {
            JOptionPane.showMessageDialog(frame,
                    "Employee record could not be loaded.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showEmployeeRecordDetailDialog(FileHandlerModule.smartSplit(data));
    }

    private static void showEmployeeRecordDetailDialog(String[] emp) {
        JDialog dialog = new JDialog(frame, "Employee Details", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new java.awt.BorderLayout());

        JPanel strip = new JPanel(null);
        strip.setBackground(ACCENT_BLUE);
        strip.setPreferredSize(new java.awt.Dimension(0, 44));
        JLabel title = new JLabel("  " + EmployeeModule.fullName(emp));
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(PALETTE_WHITE);
        title.setBounds(0, 10, 500, 24);
        strip.add(title);
        dialog.add(strip, java.awt.BorderLayout.NORTH);

        JTextArea body = new JTextArea(EmployeeRecordsModule.formatFullProfile(emp));
        body.setFont(RECEIPT_FONT);
        body.setEditable(false);
        body.setBackground(PALETTE_WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        dialog.add(sp, java.awt.BorderLayout.CENTER);

        JButton close = new JButton("Close");
        guiStyleAccentButton(close);
        close.addActionListener(e -> dialog.dispose());
        JPanel south = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        south.setBackground(APP_BG);
        south.add(close);
        dialog.add(south, java.awt.BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static void updateEmployeeRecordActionState(boolean hasSelection) {
        if (btnRecUpdate != null) {
            btnRecUpdate.setEnabled(hasSelection && isHrUser());
            btnRecUpdate.setToolTipText(hasSelection ? "Save changes to this employee record"
                    : "Select an employee from the table first");
            refreshStandardButtonState(btnRecUpdate);
        }
        if (btnRecDelete != null) {
            btnRecDelete.setEnabled(hasSelection && isHrUser());
            btnRecDelete.setToolTipText(hasSelection ? "Permanently delete this employee record"
                    : "Select an employee from the table first");
            refreshStandardButtonState(btnRecDelete);
        }
        if (btnRecComputePayroll != null) {
            btnRecComputePayroll.setEnabled(hasSelection);
            btnRecComputePayroll.setToolTipText(hasSelection ? "Open payroll for the selected employee"
                    : "Select an employee from the table first");
            refreshAccentButtonState(btnRecComputePayroll);
        }
        if (lblRecFormHint != null) {
            lblRecFormHint.setText(hasSelection
                    ? "Editing employee #" + selectedEmployeeId + ". Click Clear Form to start over."
                    : "Select a row to edit, or click Add for a new employee.");
        }
    }

    private static void selectEmployeeInTable(String employeeId) {
        if (employeeTable == null || employeeTableModel == null || employeeId == null) {
            return;
        }
        for (int modelRow = 0; modelRow < employeeTableModel.getRowCount(); modelRow++) {
            if (employeeId.equals(String.valueOf(employeeTableModel.getValueAt(modelRow, 0)).trim())) {
                int viewRow = employeeTable.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    employeeTable.setRowSelectionInterval(viewRow, viewRow);
                    employeeTable.scrollRectToVisible(employeeTable.getCellRect(viewRow, 0, true));
                    lastSelectedEmployeeRow = viewRow;
                }
                return;
            }
        }
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
        employeeTableAllRows.clear();
        for (String[] emp : FileHandlerModule.getAllEmployees()) {
            employeeTableAllRows.add(EmployeeRecordsModule.toTableRow(emp));
        }
        applyEmployeeTableFilter();
    }

    private static EmployeeRecordsModule.RecordFormData readEmployeeRecordForm() {
        EmployeeRecordsModule.RecordFormData form = new EmployeeRecordsModule.RecordFormData();
        if (txtRecEmpNo != null)
            form.empNo = txtRecEmpNo.getText().trim();
        if (txtRecLastName != null)
            form.lastName = txtRecLastName.getText().trim();
        if (txtRecFirstName != null)
            form.firstName = txtRecFirstName.getText().trim();
        if (txtRecBirthday != null)
            form.birthday = txtRecBirthday.getText().trim();
        if (txtRecAddress != null)
            form.address = txtRecAddress.getText().trim();
        if (txtRecPhone != null)
            form.phone = txtRecPhone.getText().trim();
        if (txtRecSSS != null)
            form.sss = txtRecSSS.getText().trim();
        if (txtRecPhilHealth != null)
            form.philHealth = txtRecPhilHealth.getText().trim();
        if (txtRecTIN != null)
            form.tin = txtRecTIN.getText().trim();
        if (txtRecPagIBIG != null)
            form.pagIbig = txtRecPagIBIG.getText().trim();
        if (txtRecStatus != null)
            form.status = txtRecStatus.getText().trim();
        if (txtRecPosition != null)
            form.position = txtRecPosition.getText().trim();
        if (txtRecSupervisor != null)
            form.supervisor = txtRecSupervisor.getText().trim();
        if (txtRecBasicSalary != null)
            form.basicSalary = txtRecBasicSalary.getText().trim();
        if (txtRecHourlyRate != null)
            form.hourlyRate = txtRecHourlyRate.getText().trim();
        return form;
    }

    private static void populateEmployeeRecordForm(String[] emp) {
        resetEmployeeRecordFieldBorders();
        if (txtRecEmpNo != null)
            txtRecEmpNo.setText(val(emp, EmployeeModule.ID));
        if (txtRecLastName != null)
            txtRecLastName.setText(val(emp, EmployeeModule.LAST_NAME));
        if (txtRecFirstName != null)
            txtRecFirstName.setText(val(emp, EmployeeModule.FIRST_NAME));
        if (txtRecBirthday != null)
            txtRecBirthday.setText(val(emp, EmployeeModule.BIRTHDAY));
        if (txtRecAddress != null)
            txtRecAddress.setText(val(emp, EmployeeModule.ADDRESS));
        if (txtRecPhone != null)
            txtRecPhone.setText(val(emp, EmployeeModule.PHONE));
        if (txtRecSSS != null)
            txtRecSSS.setText(val(emp, EmployeeModule.SSS));
        if (txtRecPhilHealth != null)
            txtRecPhilHealth.setText(val(emp, EmployeeModule.PHILHEALTH));
        if (txtRecTIN != null)
            txtRecTIN.setText(val(emp, EmployeeModule.TIN));
        if (txtRecPagIBIG != null)
            txtRecPagIBIG.setText(val(emp, EmployeeModule.PAGIBIG));
        if (txtRecStatus != null)
            txtRecStatus.setText(val(emp, EmployeeModule.STATUS));
        if (txtRecPosition != null)
            txtRecPosition.setText(val(emp, EmployeeModule.POSITION));
        if (txtRecSupervisor != null)
            txtRecSupervisor.setText(val(emp, EmployeeModule.IMMEDIATE_SUPERVISOR));
        if (txtRecBasicSalary != null)
            txtRecBasicSalary.setText(val(emp, EmployeeModule.BASIC_SALARY));
        if (txtRecHourlyRate != null)
            txtRecHourlyRate.setText(val(emp, EmployeeModule.HOURLY_RATE));
        captureRecordFormBaseline();
    }

    private static String val(String[] emp, int idx) {
        String v = safeColumn(emp, idx);
        return "-".equals(v) ? "" : v;
    }

    private static void clearEmployeeRecordForm() {
        selectedEmployeeId = null;
        lastSelectedEmployeeRow = -1;
        if (employeeTable != null)
            employeeTable.clearSelection();
        if (txtRecEmpNo != null) {
            txtRecEmpNo.setText("");
            txtRecEmpNo.setEditable(true);
        }
        if (txtRecLastName != null)
            txtRecLastName.setText("");
        if (txtRecFirstName != null)
            txtRecFirstName.setText("");
        if (txtRecBirthday != null)
            txtRecBirthday.setText("");
        if (txtRecAddress != null)
            txtRecAddress.setText("");
        if (txtRecPhone != null)
            txtRecPhone.setText("");
        if (txtRecSSS != null)
            txtRecSSS.setText("");
        if (txtRecPhilHealth != null)
            txtRecPhilHealth.setText("");
        if (txtRecTIN != null)
            txtRecTIN.setText("");
        if (txtRecPagIBIG != null)
            txtRecPagIBIG.setText("");
        if (txtRecStatus != null)
            txtRecStatus.setText("");
        if (txtRecPosition != null)
            txtRecPosition.setText("");
        if (txtRecSupervisor != null)
            txtRecSupervisor.setText("");
        if (txtRecBasicSalary != null)
            txtRecBasicSalary.setText("");
        if (txtRecHourlyRate != null)
            txtRecHourlyRate.setText("");
        resetEmployeeRecordFieldBorders();
        captureRecordFormBaseline();
        updateEmployeeRecordActionState(false);
    }

    private static void resetEmployeeRecordFieldBorders() {
        resetFieldBorder(txtRecEmpNo);
        resetFieldBorder(txtRecLastName);
        resetFieldBorder(txtRecFirstName);
        resetFieldBorder(txtRecBirthday);
        resetFieldBorder(txtRecAddress);
        resetFieldBorder(txtRecPhone);
        resetFieldBorder(txtRecSSS);
        resetFieldBorder(txtRecPhilHealth);
        resetFieldBorder(txtRecTIN);
        resetFieldBorder(txtRecPagIBIG);
        resetFieldBorder(txtRecStatus);
        resetFieldBorder(txtRecPosition);
        resetFieldBorder(txtRecSupervisor);
        resetFieldBorder(txtRecBasicSalary);
        resetFieldBorder(txtRecHourlyRate);
    }

    private static void markEmployeeRecordFieldErrors(List<String> errors) {
        for (String err : errors) {
            if (err.contains("Employee Number"))
                setFieldError(txtRecEmpNo);
            if (err.contains("Last Name"))
                setFieldError(txtRecLastName);
            if (err.contains("First Name"))
                setFieldError(txtRecFirstName);
            if (err.contains("SSS"))
                setFieldError(txtRecSSS);
            if (err.contains("PhilHealth"))
                setFieldError(txtRecPhilHealth);
            if (err.contains("TIN"))
                setFieldError(txtRecTIN);
            if (err.contains("Pag-IBIG"))
                setFieldError(txtRecPagIBIG);
            if (err.contains("Basic Salary"))
                setFieldError(txtRecBasicSalary);
            if (err.contains("Hourly Rate"))
                setFieldError(txtRecHourlyRate);
        }
    }

    private static void runAddEmployeeRecord() {
        resetEmployeeRecordFieldBorders();
        EmployeeRecordsModule.RecordFormData form = readEmployeeRecordForm();
        List<String> errors = EmployeeRecordsModule.validateForm(form, false, null);
        if (!errors.isEmpty()) {
            markEmployeeRecordFieldErrors(errors);
            showBulletErrorDialog(frame, errors, "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] row = EmployeeRecordsModule.createNewRow(form);
        if (!FileHandlerModule.appendEmployeeRecord(FileHandlerModule.joinCsvLine(row))) {
            JOptionPane.showMessageDialog(frame,
                    "Could not save the employee record. Please check file permissions.",
                    "Save Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newId = form.empNo;
        refreshEmployeeTable();
        clearEmployeeRecordForm();
        selectEmployeeInTable(newId);
        showToast("Employee #" + newId + " added successfully.");
    }

    private static void runUpdateEmployeeRecord() {
        if (selectedEmployeeId == null || selectedEmployeeId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Select a record from the table before updating.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resetEmployeeRecordFieldBorders();
        EmployeeRecordsModule.RecordFormData form = readEmployeeRecordForm();
        List<String> errors = EmployeeRecordsModule.validateForm(form, true, selectedEmployeeId);
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
                FileHandlerModule.smartSplit(existingLine), form);
        if (!FileHandlerModule.updateEmployeeRecord(selectedEmployeeId, updated)) {
            JOptionPane.showMessageDialog(frame,
                    "Could not update the employee record. Please try again.",
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String savedId = form.empNo;
        refreshEmployeeTable();
        selectEmployeeInTable(savedId);
        selectedEmployeeId = savedId;
        String data = FileHandlerModule.findEmployeeData(savedId);
        if (data != null)
            populateEmployeeRecordForm(FileHandlerModule.smartSplit(data));
        txtRecEmpNo.setEditable(false);
        updateEmployeeRecordActionState(true);
        showToast("Employee #" + savedId + " updated successfully.");
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
        if (confirm != JOptionPane.YES_OPTION)
            return;

        if (!FileHandlerModule.deleteEmployeeRecord(selectedEmployeeId)) {
            JOptionPane.showMessageDialog(frame,
                    "Could not delete the employee record. Please try again.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshEmployeeTable();
        clearEmployeeRecordForm();
        showToast("Employee record deleted successfully.");
    }

    static void showHelpCenterUI() {
        currentView = "Help";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);
        buildAndAddSidebar("Help");
        addPageHeader("Help Center");

        java.awt.Rectangle bounds = getContentBounds();
        int panelW = bounds.width;
        int panelH = bounds.height;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(bounds.x, bounds.y, panelW, panelH);
        panel.setBorder(cardBorder());

        JLabel faqTitle = new JLabel("Frequently Asked Questions");
        faqTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        faqTitle.setForeground(TEXT_DARK_NAVY);
        faqTitle.setBounds(16, 16, panelW - 32, 24);
        panel.add(faqTitle);

        String[][] faqs = {
                { "How is my basic salary calculated?",
                        "Your basic salary is your agreed monthly rate. It is divided by the number of working days to get your daily rate, then multiplied by actual attendance days for the pay period." },
                { "What are the pay periods at MotorPH?",
                        "MotorPH pays twice a month - on the 15th and at the end of the month. The 15th cutoff covers days 1-15; the end-of-month covers day 16 to the last day of the month." },
                { "How is SSS contribution computed?",
                        "SSS is based on your Monthly Salary Credit (MSC). Both you and MotorPH contribute according to the latest SSS contribution table. The employee share is deducted from your gross pay each period." },
                { "How is PhilHealth computed?",
                        "PhilHealth premium is 5% of your monthly basic salary (as of 2024), split equally - 2.5% employee + 2.5% employer. Minimum is ₱500/month, maximum ₱5,000/month." },
                { "How is Pag-IBIG computed?",
                        "Pag-IBIG (HDMF) employee share is 2% of monthly salary, with a salary basis cap of ₱5,000 - so the maximum employee contribution is ₱100/month. MotorPH matches this amount." },
                { "How is withholding tax calculated?",
                        "Withholding tax uses BIR tax tables. Taxable income = gross pay minus SSS, PhilHealth, and Pag-IBIG contributions. A graduated rate is then applied on the net taxable income." },
                { "How do I check my attendance records?",
                        "Click \"Directory\" in the sidebar, search for your employee name or ID, and your attendance records will appear - including login and logout times for each day." },
                { "What counts as overtime?",
                        "Any work beyond 8 hours a day is overtime. Regular-day OT pay is your hourly rate x 1.25. Special holiday and rest-day OT rates are higher per DOLE rules." },
                { "Who do I contact for payroll issues?",
                        "For salary discrepancies, contact your HR or Payroll Officer. You can also raise concerns via the Notifications panel in this system." },
                { "When are payslips available?",
                        "Payslips are generated each pay period (15th and end of month) and can be viewed under the Pay Coverage section via the sidebar." }
        };

        DefaultListModel<String> qModel = new DefaultListModel<>();
        for (String[] faq : faqs)
            qModel.addElement(faq[0]);

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

        int qListH = (int) (panelH * 0.52);
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
        if (q.contains("philhealth") || q.contains("phil health") || q.contains("health insurance")
                || q.contains("health contribution")) {
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
        if (q.contains("payslip") || q.contains("pay slip") || q.contains("pay stub")
                || q.contains("salary breakdown")) {
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
        if (q.contains("attendance") || q.contains("time in") || q.contains("time-in") || q.contains("time out")
                || q.contains("log") && q.contains("time")) {
            return "Attendance Policy:\n\n"
                    + "  - Log time-in and time-out every working day\n"
                    + "  - Standard shift: 8 hours/day\n"
                    + "  - Tardiness and undertime are deducted from your salary\n"
                    + "  - Missed punch: Report to HR the same day for correction\n\n"
                    + "View your own attendance records:\n"
                    + "  Sidebar > Directory > Enter your name or employee ID";
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
                && (q.contains("name") || q.contains("address") || q.contains("record") || q.contains("info")
                        || q.contains("profile"))) {
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
        currentView = "Notifications";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);
        buildAndAddSidebar("Notifications");
        addPageHeader("Notifications");

        java.awt.Rectangle bounds = getContentBounds();
        int panelW = Math.min(860, bounds.width);
        int panelH = bounds.height;
        int panelX = bounds.x + (bounds.width - panelW) / 2;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(panelX, bounds.y, panelW, panelH);
        panel.setBorder(cardBorder());

        List<NotificationModule.Notification> allNotifications = buildSystemNotifications();
        DefaultListModel<NotificationModule.Notification> model = new DefaultListModel<>();
        for (NotificationModule.Notification n : allNotifications)
            model.addElement(n);

        // ── Filter bar (two rows so chips never overlap) ───────────────────
        final int filterTop = 14;
        final int filterRowH = 28;
        final int filterRowGap = 6;
        final int filterBtnW = 88;
        final int filterBtnGap = 8;
        final int filterStartX = 20;
        final int filterRow2Y = filterTop + filterRowH + filterRowGap;
        final int listTop = filterRow2Y + filterRowH + 12;

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLbl.setForeground(TEXT_DARK_NAVY);
        filterLbl.setBounds(filterStartX, filterTop + 4, 48, 20);
        panel.add(filterLbl);

        String[] filters = { "All", "Unread", "Payroll", "Attendance", "Birthday", "General" };
        JButton[] filterBtns = new JButton[filters.length];
        for (int fi = 0; fi < filters.length; fi++) {
            JButton fb = new JButton(filters[fi]);
            fb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fb.setFocusable(false);
            fb.setOpaque(true);
            fb.setBackground(fi == 0 ? ACCENT_BLUE : new Color(240, 244, 252));
            fb.setForeground(fi == 0 ? PALETTE_WHITE : TEXT_DARK_NAVY);
            fb.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
            int row = fi < 3 ? 0 : 1;
            int col = fi < 3 ? fi : fi - 3;
            int fx = filterStartX + 56 + col * (filterBtnW + filterBtnGap);
            int fy = row == 0 ? filterTop : filterRow2Y;
            fb.setBounds(fx, fy, filterBtnW, filterRowH);
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
        sp.setBounds(20, listTop, listW, panelH - listTop - 44);
        sp.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(sp);

        // ── Double-click to open notification detail popup ─────────────────
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() != 2)
                    return;
                int idx = list.locationToIndex(e.getPoint());
                if (idx < 0)
                    return;
                NotificationModule.Notification n = ((DefaultListModel<NotificationModule.Notification>) list
                        .getModel()).getElementAt(idx);
                if (n == null)
                    return;
                markNotificationRead(n);
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
        actTitle.setBounds(actionX, listTop, actionW, 22);
        panel.add(actTitle);

        JButton btnMarkRead = new JButton("Mark as Read");
        btnMarkRead.setBounds(actionX, listTop + 30, actionW, 32);
        guiStyleAccentButton(btnMarkRead);
        btnMarkRead.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                NotificationModule.Notification n = ((DefaultListModel<NotificationModule.Notification>) list
                        .getModel()).getElementAt(idx);
                markNotificationRead(n);
                list.repaint();
                showToast("Notification marked as read.");
            }
        });
        panel.add(btnMarkRead);

        JButton btnDismiss = new JButton("Dismiss");
        btnDismiss.setBounds(actionX, listTop + 72, actionW, 32);
        styleStandardButton(btnDismiss);
        btnDismiss.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                ((DefaultListModel<NotificationModule.Notification>) list.getModel()).remove(idx);
            }
        });
        panel.add(btnDismiss);

        JButton btnMarkAll = new JButton("Mark All Read");
        btnMarkAll.setBounds(actionX, listTop + 114, actionW, 32);
        styleStandardButton(btnMarkAll);
        btnMarkAll.addActionListener(e -> {
            for (int i = 0; i < model.size(); i++)
                model.getElementAt(i).read = true;
            list.repaint();
        });
        panel.add(btnMarkAll);

        // Unread count badge
        JLabel unreadLbl = new JLabel();
        unreadLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        unreadLbl.setForeground(new Color(100, 115, 145));
        unreadLbl.setBounds(actionX, listTop + 162, actionW, 18);
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
                    if ("All".equals(cat)) {
                        filtered.addElement(n);
                    } else if ("Unread".equals(cat) && !n.read) {
                        filtered.addElement(n);
                    } else if (n.category.equals(cat)) {
                        filtered.addElement(n);
                    }
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
            case "Payroll":
                badgeColor = new Color(220, 20, 60);
                break;
            case "Attendance":
                badgeColor = new Color(255, 165, 0);
                break;
            case "Birthday":
                badgeColor = new Color(34, 139, 34);
                break;
            case "System":
                badgeColor = new Color(70, 130, 255);
                break;
            default:
                badgeColor = new Color(100, 110, 130);
                break;
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
        } catch (Exception ignored) {
        }
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

    // Renderer: show colored circular badges per category and neutral row
    // background
    static class NotificationCellRenderer implements ListCellRenderer<NotificationModule.Notification> {
        @Override
        public java.awt.Component getListCellRendererComponent(
                javax.swing.JList<? extends NotificationModule.Notification> list,
                NotificationModule.Notification value,
                int index, boolean isSelected, boolean cellHasFocus) {
            String text = value == null ? "" : value.toString();
            JLabel lbl = new JLabel(text);
            lbl.setOpaque(true);
            Color fg = new Color(11, 29, 58);
            Color bg = Color.white;
            Icon icon = null;
            if (value != null) {
                String c = value.category == null ? "General" : value.category;
                Color badge = new Color(200, 200, 200);
                if ("System".equals(c)) {
                    badge = new Color(70, 130, 255);
                } else if ("Attendance".equals(c)) {
                    badge = new Color(255, 195, 0);
                } else if ("Payroll".equals(c)) {
                    badge = new Color(220, 20, 60);
                } else if ("Birthday".equals(c)) {
                    badge = new Color(34, 139, 34);
                } else if ("General".equals(c)) {
                    badge = new Color(128, 128, 128);
                }
                icon = new ColoredCircleIcon(badge, 12);
                if (value.read)
                    fg = new Color(120, 130, 140);
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
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return lbl;
        }
    }

    // Simple colored circle icon for badges
    static class ColoredCircleIcon implements Icon {
        private final Color color;
        private final int size;

        ColoredCircleIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
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
        String rawInput = txtLookupInput.getText().trim();
        if (txtLookupInput.getForeground().equals(TEXT_PLACEHOLDER_GRAY)) {
            rawInput = "";
        }
        resetFieldBorder(txtLookupInput);
        txtLookupDisplay.setText("");

        if (isEmployeeUser()) {
            rawInput = "10001";
        }

        List<String> lookupErrors = new ArrayList<>();
        if (rawInput.isEmpty()) {
            setFieldError(txtLookupInput);
            lookupErrors.add(isHrUser()
                    ? "Enter an employee ID or name to search."
                    : "Employee ID is required.");
            txtLookupDisplay.setText(formatPlainBulletList(lookupErrors));
            showBulletErrorDialog(frame, lookupErrors, "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] emp = null;
        String resolvedId = rawInput;

        if (rawInput.matches("\\d+")) {
            if (!FileHandlerModule.employeeExists(rawInput)) {
                setFieldError(txtLookupInput);
                lookupErrors.add("Employee ID \"" + rawInput + "\" was not found in the employee records (CSV).");
                txtLookupDisplay.setText(formatPlainBulletList(lookupErrors));
                showBulletErrorDialog(frame, lookupErrors, "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String data = FileHandlerModule.findEmployeeData(rawInput);
            emp = FileHandlerModule.smartSplit(data);
        } else {
            // HR searching by text name string parameters
            List<String[]> matches = EmployeeModule.searchByNameOrId(rawInput);
            if (matches.isEmpty()) {
                setFieldError(txtLookupInput);
                lookupErrors.add("No employees matched \"" + rawInput + "\".");
                txtLookupDisplay.setText(formatPlainBulletList(lookupErrors));
                showBulletErrorDialog(frame, lookupErrors, "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (matches.size() > 1) {
                emp = promptEmployeeSelection(matches);
                if (emp == null) {
                    return;
                }
            } else {
                emp = matches.get(0);
            }
            resolvedId = emp[EmployeeModule.ID].trim();
        }

        txtLookupDisplay.setText(formatEmployeeProfileWithAttendance(emp, resolvedId));
        showToast("Loaded profile for employee #" + resolvedId);
    }

    private static String[] promptEmployeeSelection(List<String[]> matches) {
        String[] labels = new String[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            String[] emp = matches.get(i);
            labels[i] = emp[EmployeeModule.ID] + " — " + EmployeeModule.fullName(emp)
                    + " (" + safeColumn(emp, EmployeeModule.POSITION) + ")";
        }
        String chosen = (String) JOptionPane.showInputDialog(frame,
                "Multiple employees matched. Select one:",
                "Select Employee", JOptionPane.QUESTION_MESSAGE, null, labels, labels[0]);
        if (chosen == null) {
            return null;
        }
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(chosen)) {
                return matches.get(i);
            }
        }
        return matches.get(0);
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

    private static String formatEmployeeProfileWithAttendance(String[] emp, String id) {
        StringBuilder sb = new StringBuilder(formatEmployeeProfile(emp));
        sb.append("\n\n--- Attendance Records ---\n");
        List<String> records = FileHandlerModule.findAttendanceData(id);
        if (records.isEmpty()) {
            sb.append("No attendance records found for this employee.");
        } else {
            int shown = 0;
            for (String line : records) {
                if (shown >= 20) {
                    sb.append("... showing first 20 of ").append(records.size()).append(" records.\n");
                    break;
                }
                String[] row = FileHandlerModule.smartSplit(line);
                if (row.length >= 6) {
                    sb.append("  ").append(row[3]).append("  In: ").append(row[4])
                            .append("  Out: ").append(row[5]).append("\n");
                    shown++;
                }
            }
        }
        return sb.toString();
    }

    /** Returns the trimmed CSV column or "-" if the row is too short / null. */
    private static String safeColumn(String[] row, int idx) {
        if (row == null || idx < 0 || idx >= row.length)
            return "-";
        String value = row[idx];
        if (value == null || value.trim().isEmpty())
            return "-";
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

        if (lblPayrollSummary != null) {
            lblPayrollSummary.setText(String.format(
                    "Gross: PHP %,.2f    Deductions: PHP %,.2f    Net: PHP %,.2f",
                    SalaryComputationModule.summaryGross,
                    SalaryComputationModule.summaryDeductions,
                    SalaryComputationModule.summaryNet));
        }

        showToast("Payslip generated. Use Copy or Save to export.");
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
        field.setForeground(TEXT_DARK_NAVY);
        field.setCaretColor(TEXT_DARK_NAVY);
        field.setBackground(isEditable ? INPUT_BG : new Color(230, 236, 246));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        if (isEditable) {
            attachFocusHighlight(field);
        }
        return field;
    }

    private static void styleStandardButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusable(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        refreshStandardButtonState(button);

        // Mirrors guiStyleAccentButton's pattern so secondary buttons also feel
        // interactive
        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PALETTE_LIGHT_BLUE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                refreshStandardButtonState(button);
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

    private static void refreshStandardButtonState(JButton button) {
        if (button == null)
            return;
        if (button.isEnabled()) {
            button.setBackground(PALETTE_WHITE);
            button.setForeground(TEXT_DARK_NAVY);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            button.setBackground(new Color(244, 247, 252));
            button.setForeground(TEXT_MUTED);
            button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private static void refreshAccentButtonState(JButton button) {
        if (button == null)
            return;
        if (button.isEnabled()) {
            button.setBackground(ACCENT_BLUE);
            button.setForeground(PALETTE_WHITE);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            button.setBackground(new Color(220, 228, 240));
            button.setForeground(TEXT_MUTED);
            button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
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

    // Breadcrumb utilities breadcrumb bar removed; method kept as no-op for callers
    static void setBreadcrumb(String... parts) {
        // breadcrumb bar is hidden; nothing to render
    }

    static void handleBreadcrumbClick(String label) {
        if (label == null)
            return;
        String l = label.toLowerCase();
        if (l.contains("dashboard") || l.contains("main menu"))
            showDashboard();
        else if (l.contains("notification"))
            showNotificationsUI();
        else if (l.contains("pay") || l.contains("coverage"))
            setupPayrollUI();
        else if (l.contains("employee") || l.contains("record"))
            showEmployeeRecordsUI();
        else if (l.contains("lookup") || l.contains("directory"))
            showEmployeeLookupUI();
        else if (l.contains("help"))
            showHelpCenterUI();
        else
            showDashboard();
    }

    /**
     * Adds a small bottom-right "Logged in as: {user}" status label to the frame.
     *
     * The label's right edge is aligned with the screen's content right edge so the
     * footer visually lines up with the card / scroll pane above it instead of
     * floating in the dialog gutter.
     *
     * Skipped silently when no username is recorded (defensive only - main()
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusable(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        refreshAccentButtonState(button);
        button.addMouseListener(new MouseListener() {
            private boolean cursorInside = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.isEnabled())
                    return;
                cursorInside = true;
                button.setBackground(HOVER_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cursorInside = false;
                refreshAccentButtonState(button);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PRESSED_BLUE);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!button.isEnabled())
                    return;
                button.setBackground(cursorInside ? HOVER_BLUE : ACCENT_BLUE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
    }

    static void updateDisplay() {
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true); // Always refreshed at the end to force accurate UI updates
    }
}
