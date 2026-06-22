
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

    /**
     * MotorPH_GUI
     * -----------
     * Swing presentation layer for the MotorPH Employee Payroll System (~10,000 lines).
     *
     * <p>Two role-based portals share this class:</p>
     * <ul>
     *   <li><b>Employee</b> — dashboard, PDF-style My Payslip (period navigation + filter),
     *       profile, notifications, help</li>
     *   <li><b>HR</b> — employee records CRUD, revision history, single/batch payroll,
     *       attendance view, notifications</li>
     * </ul>
     *
     * <p>Layout uses null layout ({@code setBounds}) with responsive breakpoints via
     * {@link #getContentBounds()}. Business logic is delegated to {@link FileHandlerModule},
     * {@link EmployeeModule}, {@link SalaryComputationModule}, and {@link EmployeeRecordsModule}.</p>
     *
     * <p><b>Major code sections (approximate line ranges):</b></p>
     * <ul>
     *   <li>99–318 — static fields: frame, payroll widgets, employee payslip state, theme constants</li>
     *   <li>399–1195 — table styling, HR batch payroll selection UI, export helpers</li>
     *   <li>1196–2230 — employee payslip PDF viewer, cut-off navigation, filter dialog, bulk PDF export</li>
     *   <li>2237–2680 — toasts, notifications seeding, view reload, live search filters</li>
     *   <li>2681–3310 — payslip PDF rendering, CSV undo/redo snapshot helpers</li>
     *   <li>3315–3688 — login dialog and {@link #initialize()} bootstrap</li>
     *   <li>3689–4290 — dashboards (employee + HR cards, calendar, sidebar)</li>
     *   <li>4291–5050 — page headers, breadcrumbs, shared layout helpers</li>
     *   <li>5051–6045 — HR payroll (single + batch), employee payslip entry via {@link #setupPayrollUI()}</li>
     *   <li>6047–6420 — employee lookup, My Profile</li>
     *   <li>6424–8205 — HR Employee Records CRUD, add/edit popups, date picker, attendance dialog</li>
     *   <li>8733–9530 — Help Center and Notifications screens</li>
     *   <li>9530–10177 — payroll calculation runners, validation dialogs, display refresh</li>
     * </ul>
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

    /** Plain-text payslip output used for copy/export. */
    static JTextArea txtResultArea;
    /** Styled payslip display shown in the payroll results panel. */
    static JTextPane richPane;
    /** Last computed batch payroll summaries; used for ZIP PDF export. */
    private static java.util.List<SalaryComputationModule.EmployeePayrollSummary> lastBatchSummaries = new java.util.ArrayList<>();
    private static javax.swing.text.Style rsNormal, rsBold, rsHeader, rsNet, rsMuted,
            rsSectionTitle, rsDeduct, rsWarn;

    /** Employee payslip PDF-style viewer host (employee portal only). */
    static JPanel employeePayslipViewport;
    static JScrollPane employeePayslipScroll;
    static int employeePayslipViewportW;
    static int employeePayslipViewportH;
    static int employeePayslipCutoffIndex = -1;
    static final List<EmployeePayCutoff> employeePayCutoffPeriods = new ArrayList<>();
    static JLabel employeeCutoffPeriodLbl;
    static JButton btnEmployeePayslipFilter;
    static JButton btnEmployeePayslipOlder;
    static JButton btnEmployeePayslipNewer;
    static JDialog employeePayslipFilterDialog;
    static JComboBox<String> cmbEmployeePayPeriod;
    static JComboBox<String> cmbEmployeePayFrom;
    static JComboBox<String> cmbEmployeePayTo;
    static boolean employeePayslipSuppressToast;

    /** One semi-monthly pay cut-off window for employee payslip navigation. */
    private static final class EmployeePayCutoff {
        final int month;
        final int year;
        /** 1 = days 1–15, 2 = days 16–end of month */
        final int half;

        EmployeePayCutoff(int month, int year, int half) {
            this.month = month;
            this.year = year;
            this.half = half;
        }
    }

    static final Color PAYSLIP_DOC_HEADER_BG = new Color(33, 64, 141);
    static final Color PAYSLIP_DOC_RIBBON_BG = new Color(56, 107, 191);
    static final Color PAYSLIP_DOC_BAND_BG = new Color(224, 235, 250);
    static final Color PAYSLIP_DOC_DETAIL_BG = new Color(242, 245, 250);
    static final Color PAYSLIP_DOC_DEDUCT_BG = new Color(235, 240, 250);
    static final Color PAYSLIP_VIEWER_BG = new Color(210, 218, 230);
    static final int PAYSLIP_DOC_WIDTH = 495;

    /**
     * Parallel month-number lookup for {@link #monthCombo}.
     *
     * Index 0 is a sentinel ("no selection"); subsequent entries map combo
     * positions
     * to actual calendar month numbers. Using this lookup avoids fragile arithmetic
     * like {@code getSelectedIndex() + 5}, which silently breaks if items change.
     */
    static final int[] MONTH_NUMBERS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };

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
    static JScrollPane employeeRecordsScrollPane;
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
    static JButton btnRecView;
    static JButton btnRecDelete;
    static JButton btnRecComputePayroll;
    static JButton btnRecViewAttendance;
    static JButton btnRecUndo;
    static JButton btnRecRedo;
    static JButton btnRecRevert;
    static JLabel lblRecFormHint;
    // CSV-level undo/redo stacks (add / delete operations)
    static final java.util.Deque<List<String[]>> csvUndoStack = new java.util.ArrayDeque<>();
    static final java.util.Deque<List<String[]>> csvRedoStack = new java.util.ArrayDeque<>();
    static List<String[]> csvOriginalSnapshot = null;
    static JTextField txtRecSearch;
    static JComboBox<String> cmbRecDeptFilter;
    static JComboBox<String> cmbRecStatusFilter;
    static JLabel lblRecFilterCount;
    static final List<String[]> employeeRecordsCache = new ArrayList<>();
    static final List<Object[]> employeeTableAllRows = new ArrayList<>();
    static String recordFormBaseline = "";
    static final java.util.Deque<String> formUndoStack = new java.util.ArrayDeque<>();
    static final java.util.Deque<String> formRedoStack = new java.util.ArrayDeque<>();
    static String formSnapshotOnFocus = "";
    static boolean formRestoringState = false;
    static int lastSelectedEmployeeRow = -1;
    static String selectedEmployeeId = null;
    static String pendingPayrollEmployeeId = null;
    static String payrollSubView = "Batch";
    static String currentView = "Dashboard";
    static JLabel statusToastLbl;
    static javax.swing.Timer toastTimer;
    static final Set<String> readNotificationKeys = new HashSet<>();
    static boolean resizeHandlerInstalled = false;
    static boolean reloadingLayout = false;

    /** Summary strip above the payslip output on the payroll screen. */
    static JTable payrollSelectTable;
    static DefaultTableModel payrollSelectTableModel;
    static JTextField txtPayrollEmpSearch;
    static JComboBox<String> cmbPayrollDeptFilter;
    static JComboBox<String> cmbPayrollStatusFilter;
    static JButton btnPayrollBatchFilter;
    static JButton btnPayrollSelectAll;
    static JDialog payrollFilterDialog;
    static Runnable payrollEmployeeListRefresh;
    static JPanel payrollStatSelectedChip;
    static JPanel payrollStatGeneratedChip;
    static JPanel payrollStatNetChip;
    static boolean batchPayrollComputedOnce;
    static boolean bulkPayrollSelectionUpdate;
    static javax.swing.Timer batchPayrollSyncTimer;
    static DefaultTableModel singlePayrollAttTableModel;
    static JLabel lblSingleAttDays;
    static JLabel lblSingleAttHours;
    static JLabel lblSingleAttStatus;

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
    static final int RECORDS_ACTION_BAR_H = 136;
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
    /** Stack dashboard calendar below cards when content is narrower than this. */
    static final int RESP_DASH_STACK_CAL = 720;
    /** Single-column dashboard cards below this content width. */
    static final int RESP_DASH_SINGLE_COL = 880;
    /** Single-column profile fields below this content width. */
    static final int RESP_PROFILE_SINGLE_COL = 640;
    /** Stack notification actions below the list below this width. */
    static final int RESP_NOTIF_STACK_ACTIONS = 720;
    /** Two-row payslip toolbar below this content width. */
    static final int RESP_PAYSLIP_NARROW_TOOLBAR = 720;
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
        return new java.awt.Rectangle(x, y, Math.max(0, w), Math.max(0, h));
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

    /**
     * Stable table scrolling with horizontal access when columns exceed the
     * viewport.
     */
    private static void styleEmployeeRecordsScrollPane(JScrollPane sp) {
        styleScrollPane(sp);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        sp.getVerticalScrollBar().setBlockIncrement(102);
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
     * Fixed column widths so ID and name columns stay readable; scroll horizontally
     * when needed.
     */
    private static void configureEmployeeTableColumns(JTable table) {
        configureEmployeeTableColumns(table, -1);
    }

    private static void configureEmployeeTableColumns(JTable table, int availableWidth) {
        int[] widths = { 88, 118, 118, 108, 118, 108, 112, 112 };
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            col.setMinWidth(72);
            col.setResizable(true);
        }
    }

    private static void configurePayrollSelectTableColumns(JTable table, int availableWidth) {
        if (table.getColumnCount() < 5) {
            return;
        }
        int nameW = Math.max(168, availableWidth - 40 - 88 - 128 - 96 - 24);
        int[] widths = { 44, 88, nameW, 128, 96 };
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            col.setMinWidth(i == 0 ? 44 : 64);
            col.setResizable(i != 0);
            if (i == 0) {
                col.setMaxWidth(44);
                col.setHeaderValue("");
            }
        }
    }

    private static void configurePayslipIssueTableColumns(JTable table, int availableWidth) {
        if (table == null || table.getColumnCount() < 6 || availableWidth <= 0) {
            return;
        }
        int[] weights = { 18, 10, 22, 20, 18, 12 };
        TableColumnModel cm = table.getColumnModel();
        int assigned = 0;
        for (int i = 0; i < 5; i++) {
            int w = Math.max(64, (availableWidth * weights[i]) / 100);
            TableColumn col = cm.getColumn(i);
            col.setMinWidth(56);
            col.setPreferredWidth(w);
            col.setResizable(true);
            assigned += w;
        }
        int lastW = Math.max(56, availableWidth - assigned);
        TableColumn lastCol = cm.getColumn(5);
        lastCol.setMinWidth(56);
        lastCol.setPreferredWidth(lastW);
        lastCol.setResizable(true);
    }

    private static final Color PAYROLL_SECTION_BG = new Color(248, 251, 255);
    private static final int PAYROLL_SECTION_GAP = 10;
    private static final int PAYROLL_PAD = 12;

    /** Filter/toolbar strip matching the Employee Records screen. */
    private static JPanel createRecordsStyleBar(int width, int height) {
        JPanel bar = new JPanel(null);
        bar.setBackground(PAYROLL_SECTION_BG);
        bar.setBounds(0, 0, width, height);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return bar;
    }

    private static JPanel addPayrollSummaryColumn(JPanel bar, int x, int y, int colW, int colH,
            String title, String value, Color accent, boolean showDivider) {
        JPanel col = new JPanel(null);
        col.setOpaque(false);
        col.setBounds(x, y, colW, colH);
        if (showDivider) {
            col.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(214, 222, 236)));
        }
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(TEXT_MUTED);
        lblTitle.setBounds(0, 0, colW, 14);
        col.add(lblTitle);
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValue.setForeground(accent);
        lblValue.setBounds(0, 16, colW, 24);
        col.add(lblValue);
        col.putClientProperty("valueLabel", lblValue);
        bar.add(col);
        return col;
    }

    private static int addPayrollSummaryBar(JPanel panel, int y, int width,
            String label1, String label2, String label3) {
        return addPayrollSummaryBar(panel, y, width, label1, label2, label3, PAYROLL_PAD);
    }

    private static int addPayrollSummaryBar(JPanel panel, int y, int width,
            String label1, String label2, String label3, int inset) {
        final int barH = 64;
        if (y < inset) {
            y = inset;
        }
        JPanel bar = createRecordsStyleBar(width - inset * 2, barH);
        bar.setLocation(inset, y);

        java.awt.Insets barInsets = bar.getBorder().getBorderInsets(bar);
        int innerW = bar.getWidth() - barInsets.left - barInsets.right;
        int innerH = barH - barInsets.top - barInsets.bottom;
        int colW = innerW / 3;
        int colY = barInsets.top;
        int colX = barInsets.left;
        payrollStatSelectedChip = addPayrollSummaryColumn(bar, colX, colY, colW, innerH, label1, "—",
                ACCENT_BLUE, true);
        payrollStatGeneratedChip = addPayrollSummaryColumn(bar, colX + colW, colY, colW, innerH, label2, "—",
                new Color(180, 90, 40), true);
        payrollStatNetChip = addPayrollSummaryColumn(bar, colX + colW * 2, colY, colW, innerH, label3, "—",
                new Color(22, 130, 70), false);
        panel.add(bar);
        return y + barH + 8;
    }

    /**
     * Places a labeled field inside a payroll toolbar row. Returns x after the
     * field.
     */
    private static int addPayrollToolbarField(JPanel bar, int x, int y, String label,
            JComponent field, int fieldW) {
        JLabel lbl = createPayrollCaptionLabel(label);
        lbl.setBounds(x, y, fieldW, 14);
        bar.add(lbl);
        field.setBounds(x, y + 16, fieldW, FIELD_HEIGHT);
        bar.add(field);
        return x + fieldW + 16;
    }

    private static javax.swing.table.TableCellRenderer payrollStatusRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
                String status = value == null ? "" : value.toString();
                if ("Regular".equalsIgnoreCase(status)) {
                    setForeground(new Color(22, 130, 70));
                } else if ("Probationary".equalsIgnoreCase(status)) {
                    setForeground(new Color(180, 110, 20));
                } else {
                    setForeground(TEXT_DARK_NAVY);
                }
                return this;
            }
        };
    }

    private static void preparePayrollSelectTable(JTable table) {
        applyModernTableStyle(table);
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        TableColumn checkCol = table.getColumnModel().getColumn(0);
        JCheckBox checkEditor = new JCheckBox();
        checkEditor.setHorizontalAlignment(SwingConstants.CENTER);
        checkEditor.setOpaque(true);
        checkCol.setCellEditor(new DefaultCellEditor(checkEditor) {
            @Override
            public boolean isCellEditable(EventObject e) {
                if (e instanceof MouseEvent) {
                    return ((MouseEvent) e).getClickCount() >= 1;
                }
                return super.isCellEditable(e);
            }
        });
        checkCol.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JCheckBox box = new JCheckBox();
                box.setHorizontalAlignment(SwingConstants.CENTER);
                box.setSelected(Boolean.TRUE.equals(value));
                box.setOpaque(true);
                if (isSelected) {
                    box.setBackground(tbl.getSelectionBackground());
                } else {
                    box.setBackground(row % 2 == 0 ? PALETTE_WHITE : TABLE_STRIPE_BG);
                }
                return box;
            }
        });
        checkCol.setMaxWidth(44);
        checkCol.setMinWidth(44);
        checkCol.setPreferredWidth(44);
        checkCol.setHeaderValue("");

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PALETTE_WHITE : TABLE_STRIPE_BG);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
                return c;
            }
        });
        if (table.getColumnCount() > 4) {
            table.getColumnModel().getColumn(4).setCellRenderer(payrollStatusRenderer());
        }

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>((DefaultTableModel) table.getModel());
        sorter.setSortable(0, false);
        table.setRowSorter(sorter);

        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);
                lbl.setBackground(new Color(235, 240, 250));
                lbl.setForeground(TEXT_DARK_NAVY);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, CARD_BORDER_COLOR),
                        BorderFactory.createEmptyBorder(0, 10, 0, 4)));
                lbl.setOpaque(true);
                if (tbl.getRowSorter() != null && col > 0) {
                    java.util.List<? extends RowSorter.SortKey> keys = tbl.getRowSorter().getSortKeys();
                    int modelCol = tbl.convertColumnIndexToModel(col);
                    for (RowSorter.SortKey key : keys) {
                        if (key.getColumn() == modelCol) {
                            String arrow = key.getSortOrder() == SortOrder.ASCENDING ? " ▲" : " ▼";
                            lbl.setText((value == null ? "" : value.toString()) + arrow);
                            break;
                        }
                    }
                }
                lbl.setHorizontalAlignment(col == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return lbl;
            }
        });

        installPayrollSelectAllTableHeader(table);

        table.setSurrendersFocusOnKeystroke(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewCol != 0 || payrollSelectTableModel == null) {
                    return;
                }
                int modelRow = table.convertRowIndexToModel(viewRow);
                boolean next = !Boolean.TRUE.equals(payrollSelectTableModel.getValueAt(modelRow, 0));
                payrollSelectTableModel.setValueAt(next, modelRow, 0);
                table.repaint(table.getCellRect(viewRow, viewCol, false));
                e.consume();
            }
        });
    }

    private static Set<String> getCheckedPayrollEmployeeIds() {
        Set<String> ids = new LinkedHashSet<>();
        if (payrollSelectTableModel == null) {
            return ids;
        }
        for (int row = 0; row < payrollSelectTableModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(payrollSelectTableModel.getValueAt(row, 0))) {
                ids.add(String.valueOf(payrollSelectTableModel.getValueAt(row, 1)).trim());
            }
        }
        return ids;
    }

    private static void initPayrollResultArea() {
        txtResultArea = new JTextArea();
        txtResultArea.setFont(RECEIPT_FONT);
        txtResultArea.setEditable(false);
        txtResultArea.setLineWrap(true);
        txtResultArea.setWrapStyleWord(true);

        richPane = new JTextPane();
        richPane.setEditable(false);
        richPane.setBackground(INPUT_BG);
        richPane.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        rsNormal = richPane.addStyle("normal", null);
        javax.swing.text.StyleConstants.setFontFamily(rsNormal, "Segoe UI");
        javax.swing.text.StyleConstants.setFontSize(rsNormal, 12);
        javax.swing.text.StyleConstants.setForeground(rsNormal, TEXT_DARK_NAVY);

        rsBold = richPane.addStyle("bold", rsNormal);
        javax.swing.text.StyleConstants.setBold(rsBold, true);

        rsMuted = richPane.addStyle("muted", rsNormal);
        javax.swing.text.StyleConstants.setForeground(rsMuted, TEXT_MUTED);

        rsWarn = richPane.addStyle("warn", rsNormal);
        javax.swing.text.StyleConstants.setForeground(rsWarn, new Color(180, 60, 40));

        rsHeader = richPane.addStyle("header", null);
        javax.swing.text.StyleConstants.setFontFamily(rsHeader, "Segoe UI");
        javax.swing.text.StyleConstants.setFontSize(rsHeader, 13);
        javax.swing.text.StyleConstants.setBold(rsHeader, true);
        javax.swing.text.StyleConstants.setBackground(rsHeader, SIDEBAR_BG);
        javax.swing.text.StyleConstants.setForeground(rsHeader, Color.WHITE);

        rsSectionTitle = richPane.addStyle("section", rsNormal);
        javax.swing.text.StyleConstants.setBold(rsSectionTitle, true);
        javax.swing.text.StyleConstants.setForeground(rsSectionTitle, ACCENT_BLUE);
        javax.swing.text.StyleConstants.setFontSize(rsSectionTitle, 11);

        rsNet = richPane.addStyle("net", rsNormal);
        javax.swing.text.StyleConstants.setBold(rsNet, true);
        javax.swing.text.StyleConstants.setFontSize(rsNet, 13);
        javax.swing.text.StyleConstants.setForeground(rsNet, new Color(22, 130, 70));

        rsDeduct = richPane.addStyle("deduct", rsNormal);
        javax.swing.text.StyleConstants.setForeground(rsDeduct, new Color(180, 60, 40));

        rpSet("Select employees and click Calculate Payroll to view payslip details.", rsMuted);
    }

    private static void rpAppend(String text, javax.swing.text.Style style) {
        if (richPane == null) {
            return;
        }
        javax.swing.text.StyledDocument doc = richPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (javax.swing.text.BadLocationException ignored) {
        }
    }

    private static void rpSet(String text, javax.swing.text.Style style) {
        if (richPane == null) {
            return;
        }
        richPane.setText("");
        rpAppend(text, style);
    }

    private static void rpClear() {
        if (richPane != null) {
            richPane.setText("");
        }
        batchCardCollapseActions.clear();
        batchCardCount = 0;
    }

    private static void rpRenderEmployeeCard(String id, String name) {
        if (richPane == null || !SalaryComputationModule.lastCalculationSucceeded) {
            return;
        }
        rpAppend("  " + id + "  ·  " + name + "  \n", rsHeader);
        rpAppend("\n", rsNormal);
        rpAppend("  1ST CUTOFF (Days 1–15)\n", rsSectionTitle);
        rpAppend("  Period:   " + SalaryComputationModule.lastMonthName + " 1–15, "
                + SalaryComputationModule.lastYear + "\n", rsMuted);
        rpAppend("  Hours:    " + String.format("%.2f", SalaryComputationModule.lastHoursFirst) + "\n",
                rsNormal);
        rpAppend("  Gross:    PHP " + String.format("%,.2f", SalaryComputationModule.lastGrossFirst) + "\n",
                rsNormal);
        rpAppend("  Net Pay:  ", rsBold);
        rpAppend("PHP " + String.format("%,.2f", SalaryComputationModule.lastNetFirst) + "\n\n", rsNet);

        rpAppend("  2ND CUTOFF (Days 16–31)\n", rsSectionTitle);
        rpAppend("  Period:   " + SalaryComputationModule.lastMonthName + " 16–31, "
                + SalaryComputationModule.lastYear + "\n", rsMuted);
        rpAppend("  Hours:    " + String.format("%.2f", SalaryComputationModule.lastHoursSecond) + "\n",
                rsNormal);
        rpAppend("  Gross:    PHP " + String.format("%,.2f", SalaryComputationModule.lastGrossSecond) + "\n",
                rsNormal);
        rpAppend("  Deductions\n", rsBold);
        rpAppend("    SSS:             PHP " + String.format("%,.2f", SalaryComputationModule.lastSss) + "\n",
                rsDeduct);
        rpAppend("    PhilHealth:      PHP " + String.format("%,.2f", SalaryComputationModule.lastPhilHealth)
                + "\n", rsDeduct);
        rpAppend("    Pag-IBIG:        PHP " + String.format("%,.2f", SalaryComputationModule.lastPagIbig) + "\n",
                rsDeduct);
        rpAppend("    Withholding Tax: PHP " + String.format("%,.2f", SalaryComputationModule.lastTax) + "\n",
                rsDeduct);
        rpAppend("  Total Deductions: PHP "
                + String.format("%,.2f", SalaryComputationModule.lastTotalDeductions) + "\n", rsBold);
        rpAppend("  Net Pay:  ", rsBold);
        rpAppend("PHP " + String.format("%,.2f", SalaryComputationModule.lastNetSecond) + "\n", rsNet);
        rpAppend("\n", rsNormal);
    }

    private static void rpRenderSkippedEmployee(String id, String name) {
        rpAppend("  " + id + "  ·  " + name + "  \n", rsHeader);
        rpAppend("  No attendance data for this pay period.\n\n", rsWarn);
    }

    // Heights used by every collapsible employee card in the batch results pane
    private static final int BCRD_HDR_H   = 32;   // clickable header bar
    private static final int BCRD_COL_H   = 200;  // two-column cutoff panel
    private static final int BCRD_TOT_H   = 50;   // centered total strip
    private static final int BCRD_EXP_H   = BCRD_HDR_H + BCRD_COL_H + BCRD_TOT_H + 4;

    // Accordion state: all cards register their collapse Runnable here so expanding
    // one card can collapse the currently open one.
    private static final java.util.List<Runnable> batchCardCollapseActions = new java.util.ArrayList<>();
    private static int batchCardCount = 0;

    /** Embeds one collapsible employee card into the rich-text pane. */
    private static void rpRenderBulkEmployeeSummary(SalaryComputationModule.EmployeePayrollSummary summary) {
        if (richPane == null || summary == null) return;
        boolean isFirst = (batchCardCount == 0);
        batchCardCount++;
        richPane.insertComponent(buildCollapsibleEmployeeCard(summary, isFirst));
        rpAppend("\n", rsNormal);
    }

    private static JPanel buildCollapsibleEmployeeCard(SalaryComputationModule.EmployeePayrollSummary s, boolean startExpanded) {
        int pw = richPane.getWidth() > 32 ? richPane.getWidth() - 32 : 420;
        int detH = BCRD_COL_H + BCRD_TOT_H + 4;

        JPanel card = new JPanel(null);
        card.setBackground(APP_BG);
        card.setPreferredSize(new java.awt.Dimension(pw, startExpanded ? BCRD_EXP_H : BCRD_HDR_H));

        // ── header (always visible, click to toggle) ──
        JPanel header = new JPanel(null);
        header.setBackground(TEXT_DARK_NAVY);
        header.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        header.setBounds(0, 0, pw, BCRD_HDR_H);

        JLabel nameLbl = new JLabel("  " + s.employeeId + "  ·  " + s.employeeName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(java.awt.Color.WHITE);
        nameLbl.setBounds(0, 0, pw - 190, BCRD_HDR_H);
        header.add(nameLbl);

        JLabel summaryLbl = new JLabel("NET: PHP " + String.format("%,.2f", s.netPay));
        summaryLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        summaryLbl.setForeground(new Color(120, 220, 160));
        summaryLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryLbl.setBounds(pw - 190, 0, 158, BCRD_HDR_H);
        header.add(summaryLbl);

        JLabel arrowLbl = new JLabel(startExpanded ? "v" : ">");
        arrowLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        arrowLbl.setForeground(java.awt.Color.WHITE);
        arrowLbl.setHorizontalAlignment(SwingConstants.CENTER);
        arrowLbl.setBounds(pw - 32, 0, 32, BCRD_HDR_H);
        header.add(arrowLbl);

        card.add(header);

        // ── details (collapsible) ──
        JPanel details = new JPanel(null);
        details.setBackground(APP_BG);
        details.setBounds(0, BCRD_HDR_H, pw, detH);
        details.setVisible(startExpanded);

        JPanel twoCol = buildCutoffTwoColPanel(s, pw);
        twoCol.setBounds(0, 0, pw, BCRD_COL_H);
        details.add(twoCol);

        JPanel totalStrip = buildEmployeeTotalPanel(s, pw);
        totalStrip.setBounds(0, BCRD_COL_H + 2, pw, BCRD_TOT_H);
        details.add(totalStrip);

        card.add(details);

        // ── accordion toggle ──
        boolean[] expanded = { startExpanded };

        // Register collapse callback so any card can close this one
        Runnable collapseThis = () -> {
            if (expanded[0]) {
                expanded[0] = false;
                details.setVisible(false);
                arrowLbl.setText(">");
                card.setPreferredSize(new java.awt.Dimension(pw, BCRD_HDR_H));
                card.revalidate();
            }
        };
        batchCardCollapseActions.add(collapseThis);

        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (expanded[0]) {
                    // Collapse this card
                    collapseThis.run();
                } else {
                    // Collapse every other card first
                    for (Runnable collapse : batchCardCollapseActions) {
                        collapse.run();
                    }
                    // Then expand this card
                    expanded[0] = true;
                    details.setVisible(true);
                    arrowLbl.setText("v");
                    card.setPreferredSize(new java.awt.Dimension(pw, BCRD_HDR_H + detH));
                    card.revalidate();
                }
                if (richPane != null) { richPane.revalidate(); richPane.repaint(); }
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                header.setBackground(new Color(38, 71, 128));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                header.setBackground(TEXT_DARK_NAVY);
            }
        });

        return card;
    }

    /** Two-column panel: 1st cutoff (left) and 2nd cutoff + deductions (right). */
    private static JPanel buildCutoffTwoColPanel(SalaryComputationModule.EmployeePayrollSummary s, int pw) {
        String mn = s.monthName;
        String yr = s.year;
        Color colBg      = new Color(245, 248, 254);
        Color divCol     = new Color(200, 210, 230);
        Color deductCol  = new Color(180, 60, 40);
        double netSecond = s.grossSecond - s.totalDeductions;

        JPanel panel = new JPanel(new java.awt.GridLayout(1, 2, 0, 0));
        panel.setBackground(colBg);
        panel.setBorder(BorderFactory.createLineBorder(divCol, 1));

        // Left – 1st cutoff (no deductions)
        JPanel left = new JPanel(new java.awt.GridLayout(5, 1, 0, 1));
        left.setBackground(colBg);
        left.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));
        left.add(makeCutoffLabel("1ST CUTOFF  ·  " + mn + " 1–15, " + yr, ACCENT_BLUE, true, 11));
        left.add(makeCutoffLabel("Hours:   " + String.format("%.2f", s.hoursFirst) + " hrs", TEXT_DARK_NAVY, false, 11));
        left.add(makeCutoffLabel("Gross:   PHP " + String.format("%,.2f", s.grossFirst), TEXT_DARK_NAVY, false, 11));
        left.add(makeCutoffLabel("Net Pay: PHP " + String.format("%,.2f", s.grossFirst), new Color(22, 130, 70), true, 11));
        left.add(makeCutoffLabel("(no deductions)", TEXT_MUTED, false, 10));
        panel.add(left);

        // Right – 2nd cutoff with deductions
        JPanel right = new JPanel(new java.awt.GridLayout(9, 1, 0, 1));
        right.setBackground(colBg);
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, divCol),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        right.add(makeCutoffLabel("2ND CUTOFF  ·  " + mn + " 16–31, " + yr, ACCENT_BLUE, true, 11));
        right.add(makeCutoffLabel("Hours:   " + String.format("%.2f", s.hoursSecond) + " hrs", TEXT_DARK_NAVY, false, 11));
        right.add(makeCutoffLabel("Gross:   PHP " + String.format("%,.2f", s.grossSecond), TEXT_DARK_NAVY, false, 11));
        right.add(makeCutoffLabel("SSS:  PHP " + String.format("%,.2f", s.sss), deductCol, false, 10));
        right.add(makeCutoffLabel("PhilHealth:  PHP " + String.format("%,.2f", s.philHealth), deductCol, false, 10));
        right.add(makeCutoffLabel("Pag-IBIG:  PHP " + String.format("%,.2f", s.pagIbig), deductCol, false, 10));
        right.add(makeCutoffLabel("Tax:  PHP " + String.format("%,.2f", s.tax), deductCol, false, 10));
        right.add(makeCutoffLabel("Total Deductions:  PHP " + String.format("%,.2f", s.totalDeductions), TEXT_DARK_NAVY, true, 10));
        right.add(makeCutoffLabel("Net Pay:  PHP " + String.format("%,.2f", netSecond), new Color(22, 130, 70), true, 11));
        panel.add(right);

        return panel;
    }

    /** Centered total strip displayed beneath both cutoff columns. */
    private static JPanel buildEmployeeTotalPanel(SalaryComputationModule.EmployeePayrollSummary s, int pw) {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(236, 241, 252));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 230)));

        int h2 = BCRD_TOT_H / 2;
        String row1 = "TOTAL  ·  " + s.monthName + " " + s.year
                + "    │    " + String.format("%.2f", s.hoursWorked) + " hrs total";
        JLabel lbl1 = makeCutoffLabel(row1, TEXT_DARK_NAVY, true, 11);
        lbl1.setHorizontalAlignment(SwingConstants.CENTER);
        lbl1.setBounds(0, 3, pw, h2 - 1);
        p.add(lbl1);

        String row2 = "Gross: PHP " + String.format("%,.2f", s.grossPay)
                + "     Deductions: PHP " + String.format("%,.2f", s.totalDeductions)
                + "     NET PAY: PHP " + String.format("%,.2f", s.netPay);
        JLabel lbl2 = makeCutoffLabel(row2, new Color(22, 130, 70), true, 11);
        lbl2.setHorizontalAlignment(SwingConstants.CENTER);
        lbl2.setBounds(0, h2 + 2, pw, h2 - 2);
        p.add(lbl2);

        return p;
    }

    private static JLabel makeCutoffLabel(String text, Color color, boolean bold, int size) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        lbl.setForeground(color);
        return lbl;
    }

    private static void rpRenderBatchTotals(int processed, int selected, double totalGross,
            double totalDed, double totalNet) {
        rpAppend("  ------------------------------------\n", rsMuted);
        rpAppend("  BATCH SUMMARY\n", rsSectionTitle);
        rpAppend("  Processed:  " + processed + " of " + selected + " selected\n\n", rsNormal);
    }

    private static int addPayrollOutputBlock(JPanel panel, int y, int width, int blockHeight,
            String chipLabel1, String chipLabel2, String chipLabel3) {
        y = addPayrollSummaryBar(panel, y, width, chipLabel1, chipLabel2, chipLabel3);

        initPayrollResultArea();

        int exportGap = 10;
        int exportH = BTN_HEIGHT * 2 + 6;
        int scrollH = Math.max(120, blockHeight - (y + exportH + exportGap));
        JScrollPane outScroll = new JScrollPane(richPane != null ? richPane : txtResultArea);
        outScroll.setBounds(PAYROLL_PAD, y, width - PAYROLL_PAD * 2, scrollH);
        styleScrollPane(outScroll);
        panel.add(outScroll);

        addPayrollExportButtons(panel, y + scrollH + exportGap, width - PAYROLL_PAD * 2, PAYROLL_PAD);
        return y + scrollH + exportGap + exportH;
    }

    private static void setPayrollStatChipValue(JPanel chip, String value) {
        if (chip == null) {
            return;
        }
        Object tag = chip.getClientProperty("valueLabel");
        if (tag instanceof JLabel) {
            JLabel lbl = (JLabel) tag;
            lbl.setText(value == null || value.isEmpty() ? "—" : value);
            int fontSize = value != null && value.length() > 13 ? 12 : 14;
            lbl.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        }
    }

    private static void resetPayrollStatChips() {
        setPayrollStatChipValue(payrollStatSelectedChip, "—");
        setPayrollStatChipValue(payrollStatGeneratedChip, "—");
        setPayrollStatChipValue(payrollStatNetChip, "—");
    }

    private static void clearBatchPayrollOutput() {
        rpClear();
        if (txtResultArea != null) {
            txtResultArea.setText("");
        }
        resetPayrollStatChips();
    }

    private static boolean isBatchPayPeriodReady(String[] outMonth, String[] outYear) {
        if (monthCombo == null || txtYear == null || monthCombo.getSelectedIndex() == 0) {
            return false;
        }
        String year = txtYear.getText().trim();
        if (year.isEmpty() || !year.matches("\\d+") || !year.equals("2024")) {
            return false;
        }
        if (outMonth != null && outMonth.length > 0) {
            outMonth[0] = String.valueOf(MONTH_NUMBERS[monthCombo.getSelectedIndex()]);
        }
        if (outYear != null && outYear.length > 0) {
            outYear[0] = year;
        }
        return true;
    }

    private static void scheduleBatchPayrollResultsSync() {
        if (!batchPayrollComputedOnce) {
            clearBatchPayrollOutput();
            return;
        }
        if (batchPayrollSyncTimer == null) {
            batchPayrollSyncTimer = new javax.swing.Timer(280, e -> syncBatchPayrollResultsToFilter());
            batchPayrollSyncTimer.setRepeats(false);
        }
        batchPayrollSyncTimer.restart();
    }

    /** Recomputes batch payroll for the current filtered selection (silent, no dialogs). */
    private static void syncBatchPayrollResultsToFilter() {
        if (!batchPayrollComputedOnce || txtResultArea == null) {
            clearBatchPayrollOutput();
            return;
        }
        String[] monthHolder = new String[1];
        String[] yearHolder = new String[1];
        if (!isBatchPayPeriodReady(monthHolder, yearHolder)) {
            clearBatchPayrollOutput();
            return;
        }
        if (countCheckedPayrollRows() < 1) {
            clearBatchPayrollOutput();
            return;
        }
        executeBatchPayrollComputation(monthHolder[0], yearHolder[0], false);
    }

    private static void updatePayrollSelectionCount() {
        refreshPayrollSelectAllButtonState();
    }

    private static void updatePayrollStatChips(double gross, double deductions, double net) {
        setPayrollStatChipValue(payrollStatSelectedChip, String.format("PHP %,.2f", gross));
        setPayrollStatChipValue(payrollStatGeneratedChip, String.format("PHP %,.2f", deductions));
        setPayrollStatChipValue(payrollStatNetChip, String.format("PHP %,.2f", net));
    }

    private static int countCheckedPayrollRows() {
        if (payrollSelectTableModel == null) {
            return 0;
        }
        int count = 0;
        for (int row = 0; row < payrollSelectTableModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(payrollSelectTableModel.getValueAt(row, 0))) {
                count++;
            }
        }
        return count;
    }

    private static void setAllPayrollRowsChecked(boolean checked) {
        if (payrollSelectTableModel == null) {
            return;
        }
        bulkPayrollSelectionUpdate = true;
        try {
            for (int row = 0; row < payrollSelectTableModel.getRowCount(); row++) {
                payrollSelectTableModel.setValueAt(checked, row, 0);
            }
        } finally {
            bulkPayrollSelectionUpdate = false;
        }
        updatePayrollSelectionCount();
        if (payrollSelectTable != null) {
            payrollSelectTable.repaint();
        }
        scheduleBatchPayrollResultsSync();
    }

    private static boolean areAllPayrollRowsChecked() {
        if (payrollSelectTableModel == null || payrollSelectTableModel.getRowCount() == 0) {
            return false;
        }
        for (int row = 0; row < payrollSelectTableModel.getRowCount(); row++) {
            if (!Boolean.TRUE.equals(payrollSelectTableModel.getValueAt(row, 0))) {
                return false;
            }
        }
        return true;
    }

    private static void togglePayrollSelectAll() {
        if (payrollSelectTableModel == null || payrollSelectTableModel.getRowCount() == 0) {
            return;
        }
        setAllPayrollRowsChecked(!areAllPayrollRowsChecked());
        refreshPayrollSelectAllButtonState();
    }

    private static void applyPayrollSelectAllButtonStyle(boolean primary, boolean allSelected) {
        if (btnPayrollSelectAll == null) {
            return;
        }
        btnPayrollSelectAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPayrollSelectAll.setFocusable(false);
        btnPayrollSelectAll.setOpaque(true);
        btnPayrollSelectAll.setContentAreaFilled(true);
        if (primary) {
            btnPayrollSelectAll.setBackground(ACCENT_BLUE);
            btnPayrollSelectAll.setForeground(PALETTE_WHITE);
            btnPayrollSelectAll.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        } else if (allSelected) {
            btnPayrollSelectAll.setBackground(new Color(224, 238, 255));
            btnPayrollSelectAll.setForeground(ACCENT_BLUE);
            btnPayrollSelectAll.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        } else {
            btnPayrollSelectAll.setBackground(PALETTE_WHITE);
            btnPayrollSelectAll.setForeground(TEXT_DARK_NAVY);
            btnPayrollSelectAll.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        }
    }

    private static void refreshPayrollSelectAllButtonState() {
        if (btnPayrollSelectAll == null) {
            return;
        }
        int visible = payrollSelectTableModel != null ? payrollSelectTableModel.getRowCount() : 0;
        boolean all = areAllPayrollRowsChecked();
        btnPayrollSelectAll.setEnabled(visible > 0);
        if (visible == 0) {
            btnPayrollSelectAll.setText("All Employees");
            btnPayrollSelectAll.setToolTipText("No employees to select");
            applyPayrollSelectAllButtonStyle(false, false);
        } else if (all) {
            btnPayrollSelectAll.setText("Clear All");
            btnPayrollSelectAll.setToolTipText("Deselect all " + visible + " employees");
            applyPayrollSelectAllButtonStyle(false, true);
        } else {
            btnPayrollSelectAll.setText("All Employees");
            btnPayrollSelectAll.setToolTipText("Select all " + visible + " employees for batch payroll");
            applyPayrollSelectAllButtonStyle(true, false);
        }
        if (payrollSelectTable != null && payrollSelectTable.getTableHeader() != null) {
            payrollSelectTable.getTableHeader().repaint();
        }
    }

    private static void installPayrollSelectAllTableHeader(JTable table) {
        if (Boolean.TRUE.equals(table.getClientProperty("payrollSelectAllHeaderInstalled"))) {
            return;
        }
        table.putClientProperty("payrollSelectAllHeaderInstalled", Boolean.TRUE);

        javax.swing.table.JTableHeader header = table.getTableHeader();
        TableColumn checkCol = table.getColumnModel().getColumn(0);
        checkCol.setHeaderValue("");

        checkCol.setHeaderRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private final JCheckBox headerBox = new JCheckBox();

            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                headerBox.setHorizontalAlignment(SwingConstants.CENTER);
                headerBox.setSelected(areAllPayrollRowsChecked());
                headerBox.setEnabled(tbl.getRowCount() > 0);
                headerBox.setOpaque(true);
                headerBox.setBackground(header.getBackground());
                headerBox.setBorder(BorderFactory.createEmptyBorder());
                headerBox.setToolTipText("Select or clear all employees");
                return headerBox;
            }
        });

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                if (col == 0) {
                    togglePayrollSelectAll();
                    e.consume();
                }
            }
        });
    }

    private static java.util.List<Integer> getCheckedPayrollModelRows() {
        java.util.List<Integer> rows = new ArrayList<>();
        if (payrollSelectTableModel == null) {
            return rows;
        }
        for (int row = 0; row < payrollSelectTableModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(payrollSelectTableModel.getValueAt(row, 0))) {
                rows.add(row);
            }
        }
        return rows;
    }

    /** Loads CSV rows for every checked employee in the batch payroll table. */
    private static java.util.List<String[]> getCheckedPayrollEmployees() {
        java.util.List<String[]> selectedEmps = new ArrayList<>();
        for (int modelRow : getCheckedPayrollModelRows()) {
            String id = String.valueOf(payrollSelectTableModel.getValueAt(modelRow, 1)).trim();
            String data = FileHandlerModule.findEmployeeData(id);
            if (data != null) {
                selectedEmps.add(FileHandlerModule.smartSplit(data));
            }
        }
        return selectedEmps;
    }

    private static JPanel buildPayrollWorkflowStep(int step, String label, int x, int width) {
        JPanel stepPanel = new JPanel(null);
        stepPanel.setOpaque(false);
        stepPanel.setBounds(x, 8, width, 28);

        JLabel circle = new JLabel(String.valueOf(step), SwingConstants.CENTER);
        circle.setBounds(0, 2, 24, 24);
        circle.setOpaque(true);
        circle.setBackground(ACCENT_BLUE);
        circle.setForeground(PALETTE_WHITE);
        circle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        circle.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 255), 1));
        stepPanel.add(circle);

        JLabel text = new JLabel(label);
        text.setBounds(32, 4, width - 36, 20);
        text.setFont(new Font("Segoe UI", Font.BOLD, 12));
        text.setForeground(TEXT_DARK_NAVY);
        stepPanel.add(text);
        return stepPanel;
    }

    private static String[] payrollMonthOptions() {
        return new String[] { " ",
                "01 - January", "02 - February", "03 - March",
                "04 - April", "05 - May", "06 - June",
                "07 - July", "08 - August", "09 - September",
                "10 - October", "11 - November", "12 - December" };
    }

    private static JComboBox<String> createPayrollMonthCombo() {
        JComboBox<String> combo = new JComboBox<>(payrollMonthOptions());
        combo.setFont(APP_FONT_PLAIN);
        combo.setBackground(PALETTE_WHITE);
        combo.setForeground(TEXT_DARK_NAVY);
        combo.setSelectedIndex(getDefaultPayrollMonthIndex());
        return combo;
    }

    /** Compact month picker for batch payroll toolbar (short labels, narrow width). */
    private static JComboBox<String> createCompactPayrollMonthCombo() {
        String[] opts = { " ",
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        JComboBox<String> combo = new JComboBox<>(opts);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(PALETTE_WHITE);
        combo.setForeground(TEXT_DARK_NAVY);
        combo.setSelectedIndex(getDefaultPayrollMonthIndex());
        return combo;
    }

    private static JLabel createPayrollCaptionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(88, 100, 122));
        return lbl;
    }

    private static void wireEmployeeNumberField(JTextField field) {
        field.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
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
    }

    private static void applyLinkedEmployeePortalLock() {
        if (isHrUser() || txtEmployeeNo == null) {
            return;
        }
        String linkedId = MotorPH_EmployeeApp.getLinkedEmployeeId(loggedInUser);
        if (linkedId != null) {
            txtEmployeeNo.setText(linkedId);
            txtEmployeeNo.setEditable(false);
            txtEmployeeNo.setBackground(new Color(241, 245, 251));
            updateEmployeeNameFromId(false);
        }
    }

    private static void addPayrollExportButtons(JPanel panel, int y, int width, int x) {
        int exportW = (width - 8) / 2;
        JButton btnCopy = new JButton("Copy to Clipboard");
        btnCopy.setBounds(x, y, exportW, BTN_HEIGHT);
        styleStandardButton(btnCopy);
        btnCopy.addActionListener(e -> copyPayslipToClipboard());
        panel.add(btnCopy);

        JButton btnExport = new JButton("Download .txt");
        btnExport.setBounds(x + exportW + 8, y, exportW, BTN_HEIGHT);
        styleStandardButton(btnExport);
        btnExport.addActionListener(e -> exportPayrollTextToFile());
        panel.add(btnExport);

        JButton btnPdf = new JButton("Download .pdf");
        btnPdf.setBounds(x, y + BTN_HEIGHT + 6, width, BTN_HEIGHT);
        styleStandardButton(btnPdf);
        btnPdf.addActionListener(e -> exportBatchPayslipsAsZip());
        panel.add(btnPdf);
    }

    /** Employee-only payslip viewer with PDF-style document layout and issue reporting. */
    private static int addEmployeePayslipOutputBlock(JPanel panel, int y, int width, int blockHeight) {
        int topY = y;
        y = addPayrollSummaryBar(panel, y, width, "Gross Pay", "Deductions", "Net Pay", 0);

        initPayrollResultArea();

        int actionH = BTN_HEIGHT + 8;
        int scrollH = Math.max(200, blockHeight - (y - topY) - actionH);
        employeePayslipViewportW = width;
        employeePayslipViewportH = scrollH;

        employeePayslipViewport = new JPanel(new java.awt.BorderLayout());
        employeePayslipViewport.setBackground(PAYSLIP_VIEWER_BG);
        refreshEmployeePayslipViewport(buildEmployeePayslipPlaceholder());

        employeePayslipScroll = new JScrollPane(employeePayslipViewport);
        employeePayslipScroll.setBounds(0, y, width, scrollH);
        employeePayslipScroll.setBorder(null);
        employeePayslipScroll.getViewport().setBackground(PAYSLIP_VIEWER_BG);
        employeePayslipScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        employeePayslipScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        employeePayslipScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(employeePayslipScroll);

        addEmployeePayslipActionButtons(panel, y + scrollH + 4, width, 0);
        return y + scrollH + actionH;
    }

    static final int PAYSLIP_DOC_H_MARGIN = 20; // horizontal margin each side within viewport

    private static int resolveEmployeePayslipDocWidth() {
        int vw = employeePayslipViewportW > 0 ? employeePayslipViewportW : getContentBounds().width;
        return Math.max(320, vw - PAYSLIP_DOC_H_MARGIN * 2);
    }

    private static void refreshEmployeePayslipViewport(JComponent content) {
        if (employeePayslipViewport == null) {
            return;
        }
        int docW = resolveEmployeePayslipDocWidth();
        java.awt.Dimension pref = content.getPreferredSize();
        int docH = pref.height > 0 ? pref.height : 640;
        content.setPreferredSize(new java.awt.Dimension(docW, docH));
        content.setMinimumSize(new java.awt.Dimension(Math.min(docW, 280), docH));

        employeePayslipViewport.removeAll();
        JPanel wrapper = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 12));
        wrapper.setBackground(PAYSLIP_VIEWER_BG);
        content.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        wrapper.add(content);
        employeePayslipViewport.add(wrapper, java.awt.BorderLayout.NORTH);
        employeePayslipViewport.revalidate();
        employeePayslipViewport.repaint();
        if (employeePayslipScroll != null) {
            employeePayslipScroll.getViewport().setViewPosition(new java.awt.Point(0, 0));
        }
    }

    private static JPanel buildEmployeePayslipShell(int docW, int docH) {
        JPanel doc = new JPanel(null);
        doc.setBackground(PALETTE_WHITE);
        doc.setPreferredSize(new java.awt.Dimension(docW, docH));
        doc.setMinimumSize(new java.awt.Dimension(docW, docH));
        return doc;
    }

    private static JPanel buildEmployeePayslipPlaceholder() {
        int docW = resolveEmployeePayslipDocWidth();
        int docH = 360;
        JPanel doc = buildEmployeePayslipShell(docW, docH);

        JPanel header = new JPanel(null);
        header.setBackground(PAYSLIP_DOC_HEADER_BG);
        header.setBounds(0, 0, docW, 56);
        JLabel brand = new JLabel("MOTORPH", SwingConstants.CENTER);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(PALETTE_WHITE);
        brand.setBounds(0, 10, docW, 28);
        header.add(brand);
        JLabel sub = new JLabel("Motor Parts Hub Philippines, Inc.", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(210, 225, 250));
        sub.setBounds(0, 36, docW, 16);
        header.add(sub);
        doc.add(header);

        JPanel ribbon = new JPanel(null);
        ribbon.setBackground(PAYSLIP_DOC_RIBBON_BG);
        ribbon.setBounds(0, 56, docW, 26);
        JLabel ribbonLbl = new JLabel("PAYSLIP", SwingConstants.CENTER);
        ribbonLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ribbonLbl.setForeground(PALETTE_WHITE);
        ribbonLbl.setBounds(0, 0, docW, 26);
        ribbon.add(ribbonLbl);
        doc.add(ribbon);

        int hintY = 100;
        String cutoffHint = employeePayCutoffPeriods.isEmpty()
                ? "Loading your payslip..."
                : "Tap the filter icon to choose a pay period or export multiple payslips.";
        JLabel hint = new JLabel(
                "<html><div style='text-align:center;color:#647088;'>"
                        + cutoffHint + "</div></html>",
                SwingConstants.CENTER);
        hint.setBounds(24, hintY, docW - 48, 80);
        doc.add(hint);

        JPanel footer = new JPanel(null);
        footer.setBackground(new Color(240, 242, 246));
        footer.setBounds(0, docH - 48, docW, 48);
        JLabel foot = new JLabel("MotorPH Employee Portal  ·  Confidential", SwingConstants.CENTER);
        foot.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        foot.setForeground(new Color(107, 112, 128));
        foot.setBounds(0, 16, docW, 16);
        footer.add(foot);
        doc.add(footer);
        return doc;
    }

    private static JPanel buildEmployeePayslipMessagePanel(String message, boolean error) {
        int docW = resolveEmployeePayslipDocWidth();
        int docH = 280;
        JPanel doc = buildEmployeePayslipShell(docW, docH);
        JLabel lbl = new JLabel(
                "<html><div style='text-align:center;color:"
                        + (error ? "#b44a28" : "#647088") + ";'>"
                        + escapeHtml(message).replace("\n", "<br>") + "</div></html>",
                SwingConstants.CENTER);
        lbl.setBounds(24, docH / 2 - 40, docW - 48, 80);
        doc.add(lbl);
        return doc;
    }

    private static void addPayslipDocBand(JPanel doc, int y, int h, String title, Color bg, Color fg, int docW) {
        JPanel band = new JPanel(null);
        band.setBackground(bg);
        band.setBounds(0, y, docW, h);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, title.length() > 24 ? 10 : 11));
        lbl.setForeground(fg);
        lbl.setBounds(14, 0, docW - 28, h);
        band.add(lbl);
        doc.add(band);
    }

    private static void addPayslipDocDivider(JPanel doc, int y, Color color, int thickness, int docW) {
        JPanel line = new JPanel();
        line.setBackground(color);
        line.setBounds(0, y, docW, thickness);
        doc.add(line);
    }

    private static int addPayslipDocRow(JPanel doc, int y, String label, String value,
            boolean labelBold, boolean valueBold, Color valueColor, int docW) {
        final int pad = 16;
        final int valW = Math.max(160, docW / 3);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", labelBold ? Font.BOLD : Font.PLAIN, 12));
        lbl.setForeground(TEXT_DARK_NAVY);
        lbl.setBounds(pad, y, docW - valW - pad * 2, 18);
        doc.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", valueBold ? Font.BOLD : Font.PLAIN, 12));
        val.setForeground(valueColor != null ? valueColor : TEXT_DARK_NAVY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        val.setBounds(docW - pad - valW, y, valW, 18);
        doc.add(val);
        return y + 22;
    }

    private static int addPayslipDocDetailPair(JPanel doc, int y, int leftX, int rightX,
            String leftLabel, String leftValue, String rightLabel, String rightValue, int docW) {
        JLabel ll = new JLabel(leftLabel);
        ll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ll.setForeground(TEXT_DARK_NAVY);
        ll.setBounds(leftX, y, 120, 16);
        doc.add(ll);
        JLabel lv = new JLabel(leftValue);
        lv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lv.setForeground(TEXT_DARK_NAVY);
        lv.setBounds(leftX + 124, y, rightX - leftX - 130, 16);
        doc.add(lv);

        if (rightLabel != null) {
            JLabel rl = new JLabel(rightLabel);
            rl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            rl.setForeground(TEXT_DARK_NAVY);
            rl.setBounds(rightX, y, 90, 16);
            doc.add(rl);
            JLabel rv = new JLabel(rightValue);
            rv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            rv.setForeground(TEXT_DARK_NAVY);
            rv.setBounds(rightX + 94, y, docW - rightX - 110, 16);
            doc.add(rv);
        }
        return y + 20;
    }

    private static JPanel buildEmployeePayslipDocumentView() {
        String empId = SalaryComputationModule.lastEmpId;
        String empName = SalaryComputationModule.lastEmpName;
        String birthday = SalaryComputationModule.lastEmpBirthday;
        String mName = SalaryComputationModule.lastMonthName;
        String yr = SalaryComputationModule.lastYear;
        double hrsFirst = SalaryComputationModule.lastHoursFirst;
        double hrsSecond = SalaryComputationModule.lastHoursSecond;
        double gFirst = SalaryComputationModule.lastGrossFirst;
        double gSecond = SalaryComputationModule.lastGrossSecond;
        double nFirst = SalaryComputationModule.lastNetFirst;
        double nSecond = SalaryComputationModule.lastNetSecond;
        double dSss = SalaryComputationModule.lastSss;
        double dPh = SalaryComputationModule.lastPhilHealth;
        double dPi = SalaryComputationModule.lastPagIbig;
        double dTax = SalaryComputationModule.lastTax;
        double dTotal = SalaryComputationModule.lastTotalDeductions;
        double tGross = gFirst + gSecond;
        double tNet = nFirst + nSecond;

        int lastDay = 31;
        try {
            String[] mNames = { "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December" };
            int mi = java.util.Arrays.asList(mNames).indexOf(mName);
            if (mi >= 0) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(Integer.parseInt(yr), mi, 1);
                lastDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            }
        } catch (Exception ignored) {
        }

        final int docW = resolveEmployeePayslipDocWidth();
        final int rightX = Math.max(docW / 2 + 16, docW - 220);
        JPanel doc = buildEmployeePayslipShell(docW, 640);

        JPanel header = new JPanel(null);
        header.setBackground(PAYSLIP_DOC_HEADER_BG);
        header.setBounds(0, 0, docW, 56);
        JLabel brand = new JLabel("MOTORPH", SwingConstants.CENTER);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(PALETTE_WHITE);
        brand.setBounds(0, 8, docW, 26);
        header.add(brand);
        JLabel sub = new JLabel("Motor Parts Hub Philippines, Inc.", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(PALETTE_WHITE);
        sub.setBounds(0, 30, docW, 14);
        header.add(sub);
        JLabel addr = new JLabel("Kalayaan Avenue, Makati City 1200", SwingConstants.CENTER);
        addr.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        addr.setForeground(new Color(210, 225, 250));
        addr.setBounds(0, 44, docW, 12);
        header.add(addr);
        doc.add(header);

        JPanel ribbon = new JPanel(null);
        ribbon.setBackground(PAYSLIP_DOC_RIBBON_BG);
        ribbon.setBounds(0, 56, docW, 26);
        JLabel ribbonLbl = new JLabel("PAYSLIP", SwingConstants.CENTER);
        ribbonLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ribbonLbl.setForeground(PALETTE_WHITE);
        ribbonLbl.setBounds(0, 0, docW, 26);
        ribbon.add(ribbonLbl);
        doc.add(ribbon);

        addPayslipDocBand(doc, 82, 22, "EMPLOYEE INFORMATION", PAYSLIP_DOC_BAND_BG, TEXT_DARK_NAVY, docW);

        JPanel detailBox = new JPanel(null);
        detailBox.setBackground(PAYSLIP_DOC_DETAIL_BG);
        detailBox.setBounds(0, 104, docW, 68);
        doc.add(detailBox);
        int dy = 10;
        dy = addPayslipDocDetailPair(detailBox, dy, 16, rightX,
                "Employee No.", empId, "Pay Period", mName + " " + yr, docW);
        dy = addPayslipDocDetailPair(detailBox, dy, 16, rightX,
                "Full Name", empName, null, null, docW);
        addPayslipDocDetailPair(detailBox, dy, 16, rightX,
                "Birthday", birthday, null, null, docW);

        int y = 176;
        addPayslipDocDivider(doc, y, new Color(200, 208, 220), 1, docW);
        y += 10;

        addPayslipDocBand(doc, y, 22,
                "1ST CUTOFF  ·  " + mName + " 1 - 15, " + yr, PAYSLIP_DOC_BAND_BG, TEXT_DARK_NAVY, docW);
        y += 28;
        y = addPayslipDocRow(doc, y, "Hours Worked", String.format("%.2f hrs", hrsFirst),
                false, false, null, docW);
        y = addPayslipDocRow(doc, y, "Gross Pay", "PHP " + payFmt(gFirst), false, true, null, docW);
        y = addPayslipDocRow(doc, y, "Net Pay  (No Deductions)", "PHP " + payFmt(nFirst),
                false, true, new Color(22, 130, 70), docW);
        y += 8;
        addPayslipDocDivider(doc, y, new Color(200, 208, 220), 1, docW);
        y += 12;

        addPayslipDocBand(doc, y, 22,
                "2ND CUTOFF  ·  " + mName + " 16 - " + lastDay + ", " + yr, PAYSLIP_DOC_BAND_BG, TEXT_DARK_NAVY, docW);
        y += 28;
        y = addPayslipDocRow(doc, y, "Hours Worked", String.format("%.2f hrs", hrsSecond),
                false, false, null, docW);
        y = addPayslipDocRow(doc, y, "Gross Pay", "PHP " + payFmt(gSecond), false, true, null, docW);
        y += 6;

        addPayslipDocBand(doc, y, 20, "DEDUCTIONS", PAYSLIP_DOC_DEDUCT_BG, new Color(76, 82, 102), docW);
        y += 24;
        y = addPayslipDocRow(doc, y, "SSS Contribution", "PHP " + payFmt(dSss), false, false, TEXT_MUTED, docW);
        y = addPayslipDocRow(doc, y, "PhilHealth Premium", "PHP " + payFmt(dPh), false, false, TEXT_MUTED, docW);
        y = addPayslipDocRow(doc, y, "Pag-IBIG Contribution", "PHP " + payFmt(dPi), false, false, TEXT_MUTED, docW);
        y = addPayslipDocRow(doc, y, "Withholding Tax", "PHP " + payFmt(dTax), false, false, TEXT_MUTED, docW);
        y += 6;
        addPayslipDocDivider(doc, y, new Color(155, 162, 175), 1, docW);
        y += 10;
        y = addPayslipDocRow(doc, y, "Total Deductions", "PHP " + payFmt(dTotal), true, true, null, docW);
        y = addPayslipDocRow(doc, y, "Net Pay", "PHP " + payFmt(nSecond), true, true, new Color(22, 130, 70), docW);
        y += 10;
        addPayslipDocDivider(doc, y, PAYSLIP_DOC_HEADER_BG, 2, docW);
        y += 14;

        addPayslipDocBand(doc, y, 22, "PAY SUMMARY", PAYSLIP_DOC_HEADER_BG, PALETTE_WHITE, docW);
        y += 30;
        y = addPayslipDocRow(doc, y, "Total Gross Pay", "PHP " + payFmt(tGross), false, false, null, docW);
        y = addPayslipDocRow(doc, y, "Total Deductions", "PHP " + payFmt(dTotal), false, false, null, docW);
        y += 6;
        addPayslipDocDivider(doc, y, PAYSLIP_DOC_HEADER_BG, 1, docW);
        y += 12;
        y = addPayslipDocRow(doc, y, "TOTAL NET PAY", "PHP " + payFmt(tNet), true, true, new Color(22, 130, 70), docW);
        y += 14;
        addPayslipDocDivider(doc, y, PAYSLIP_DOC_HEADER_BG, 2, docW);
        y += 12;

        final int footerH = 52;
        JPanel footer = new JPanel(null);
        footer.setBackground(new Color(240, 242, 246));
        footer.setBounds(0, y, docW, footerH);
        String genDate = new java.text.SimpleDateFormat("MMMM d, yyyy 'at' h:mm a").format(new java.util.Date());
        JLabel foot1 = new JLabel("This payslip is system-generated and confidential.");
        foot1.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        foot1.setForeground(new Color(107, 112, 128));
        foot1.setBounds(16, 10, docW - 32, 14);
        footer.add(foot1);
        JLabel foot2 = new JLabel("MotorPH Payroll System  |  Generated on " + genDate);
        foot2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        foot2.setForeground(new Color(107, 112, 128));
        foot2.setBounds(16, 28, docW - 32, 14);
        footer.add(foot2);
        doc.add(footer);

        int naturalH = y + footerH;
        doc.setPreferredSize(new java.awt.Dimension(docW, naturalH));
        doc.setMinimumSize(new java.awt.Dimension(docW, naturalH));

        return doc;
    }

    private static void showEmployeePayslipDocument() {
        if (!SalaryComputationModule.lastCalculationSucceeded) {
            refreshEmployeePayslipViewport(buildEmployeePayslipPlaceholder());
            return;
        }
        refreshEmployeePayslipViewport(buildEmployeePayslipDocumentView());
    }

    private static void showEmployeePayslipError(String message) {
        refreshEmployeePayslipViewport(buildEmployeePayslipMessagePanel(message, true));
    }

    private static void addEmployeePayslipActionButtons(JPanel panel, int y, int width, int x) {
        int gap = 8;
        int btnW = Math.max(96, (width - gap * 3 - x * 2) / 4);
        int bx = x;

        JButton btnCopy = new JButton("Copy");
        btnCopy.setBounds(bx, y, btnW, BTN_HEIGHT);
        styleStandardButton(btnCopy);
        btnCopy.addActionListener(e -> copyPayslipToClipboard());
        panel.add(btnCopy);
        bx += btnW + gap;

        JButton btnPdf = new JButton("Download PDF");
        btnPdf.setBounds(bx, y, btnW, BTN_HEIGHT);
        guiStyleAccentButton(btnPdf);
        btnPdf.addActionListener(e -> exportPayslipToFile());
        panel.add(btnPdf);
        bx += btnW + gap;

        JButton btnTxt = new JButton("Download .txt");
        btnTxt.setBounds(bx, y, btnW, BTN_HEIGHT);
        styleStandardButton(btnTxt);
        btnTxt.addActionListener(e -> exportPayrollTextToFile());
        panel.add(btnTxt);
        bx += btnW + gap;

        JButton btnReport = new JButton("Report Issue");
        btnReport.setBounds(bx, y, Math.max(96, width - bx - x), BTN_HEIGHT);
        btnReport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReport.setForeground(new Color(160, 70, 30));
        btnReport.setBackground(new Color(255, 244, 236));
        btnReport.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 150, 100), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnReport.setFocusPainted(false);
        btnReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReport.addActionListener(e -> showReportPayslipIssueDialog());
        panel.add(btnReport);
    }

    private static void showReportPayslipIssueDialog() {
        if (!SalaryComputationModule.lastCalculationSucceeded) {
            showToast("Generate a payslip before reporting an issue.", new Color(180, 90, 40));
            return;
        }

        JDialog dlg = new JDialog(frame, "Report Payslip Issue", true);
        dlg.setLayout(null);
        dlg.getContentPane().setBackground(PALETTE_WHITE);
        dlg.setSize(480, 380);
        dlg.setLocationRelativeTo(frame);
        dlg.setResizable(false);

        int pad = 24;
        int w = 480 - pad * 2;

        JLabel title = new JLabel("Report a Payslip Concern");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK_NAVY);
        title.setBounds(pad, 20, w, 22);
        dlg.add(title);

        String period = SalaryComputationModule.lastMonthName + " " + SalaryComputationModule.lastYear;
        JLabel sub = new JLabel("<html>Employee #" + escapeHtml(SalaryComputationModule.lastEmpId)
                + " · " + escapeHtml(period) + "</html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_MUTED);
        sub.setBounds(pad, 44, w, 18);
        dlg.add(sub);

        JLabel lblType = new JLabel("Issue Type");
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblType.setForeground(TEXT_MUTED);
        lblType.setBounds(pad, 76, w, 16);
        dlg.add(lblType);

        String[] issueTypes = {
                "Incorrect gross pay",
                "Incorrect deductions (SSS / PhilHealth / Pag-IBIG / Tax)",
                "Missing or wrong attendance hours",
                "Missing allowance or benefit",
                "Other computation concern"
        };
        JComboBox<String> cmbType = new JComboBox<>(issueTypes);
        cmbType.setFont(APP_FONT_PLAIN);
        cmbType.setBounds(pad, 96, w, FIELD_HEIGHT);
        dlg.add(cmbType);

        JLabel lblDesc = new JLabel("Describe the issue");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setBounds(pad, 142, w, 16);
        dlg.add(lblDesc);

        JTextArea txtDesc = new JTextArea();
        txtDesc.setFont(APP_FONT_PLAIN);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setBounds(pad, 162, w, 110);
        dlg.add(descScroll);

        JButton btnSubmit = new JButton("Submit Report");
        btnSubmit.setBounds(pad, 290, 148, BTN_HEIGHT);
        guiStyleAccentButton(btnSubmit);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(pad + 160, 290, 100, BTN_HEIGHT);
        styleStandardButton(btnCancel);
        btnCancel.addActionListener(e -> dlg.dispose());
        dlg.add(btnCancel);

        btnSubmit.addActionListener(e -> {
            String desc = txtDesc.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "Please describe the issue so HR can investigate.",
                        "Description Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String issueType = String.valueOf(cmbType.getSelectedItem());
            boolean saved = FileHandlerModule.appendPayslipIssueReport(
                    SalaryComputationModule.lastEmpId,
                    SalaryComputationModule.lastEmpName,
                    period,
                    issueType,
                    desc);
            if (saved) {
                dlg.dispose();
                showToast("Issue reported. HR will review your concern.");
                JOptionPane.showMessageDialog(frame,
                        "Your payslip concern has been submitted.\n\n"
                                + "HR / Payroll will review it within 3 working days.\n"
                                + "Reference: " + period + " · " + issueType,
                        "Report Submitted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dlg,
                        "Could not save your report. Please try again or contact HR directly.",
                        "Submit Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.add(btnSubmit);

        dlg.setVisible(true);
    }

    private static int addPayrollResultPanel(JPanel panel, int y, int width, int height) {
        addPayrollOutputBlock(panel, y, width, height,
                "Gross Pay", "Deductions", "Net Pay");
        return height;
    }

    /**
     * Employee portal payslip screen: period header (Older/Newer + filter funnel),
     * auto-loaded PDF-style document, and action bar (Copy, Download PDF/txt, Report Issue).
     * HR users never reach this method — they use {@link #setupHrPayrollWithSubMenu()} instead.
     */
    private static void setupEmployeePayslipContent() {
        java.awt.Rectangle bounds = getContentBounds();
        int panelW = bounds.width;
        int panelH = bounds.height;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(bounds.x, bounds.y, panelW, panelH);
        panel.setBorder(null);

        initEmployeePayrollHiddenFields();
        rebuildEmployeePayCutoffPeriods();

        int y = 0;
        y = addEmployeeCutoffHeaderBar(panel, y, panelW);

        addEmployeePayslipOutputBlock(panel, y, panelW, panelH - y - 4);
        runEmployeePayslipForCurrentCutoff();

        frame.add(panel);
    }

    private static void initEmployeePayrollHiddenFields() {
        if (txtEmployeeNo == null) {
            txtEmployeeNo = createStyledTextField(true);
        }
        if (txtEmployeeName == null) {
            txtEmployeeName = createStyledTextField(false);
        }
        if (monthCombo == null) {
            monthCombo = createPayrollMonthCombo();
        }
        if (txtYear == null) {
            txtYear = createStyledTextField(true);
        }
        txtYear.setText("2024");
        wireEmployeeNumberField(txtEmployeeNo);
        applyLinkedEmployeePortalLock();
        if (pendingPayrollEmployeeId != null && !pendingPayrollEmployeeId.isEmpty()) {
            txtEmployeeNo.setText(pendingPayrollEmployeeId);
            updateEmployeeNameFromId(false);
            pendingPayrollEmployeeId = null;
        }
    }

    private static void rebuildEmployeePayCutoffPeriods() {
        employeePayCutoffPeriods.clear();
        String empId = getLoggedInEmployeeId();
        java.util.Set<Integer> months = empId != null ? getAttendanceMonths(empId) : new java.util.HashSet<>();
        java.util.List<Integer> sorted = new ArrayList<>(months);
        java.util.Collections.sort(sorted);
        for (int month : sorted) {
            employeePayCutoffPeriods.add(new EmployeePayCutoff(month, 2024, 1));
            employeePayCutoffPeriods.add(new EmployeePayCutoff(month, 2024, 2));
        }
        if (employeePayCutoffPeriods.isEmpty()) {
            employeePayCutoffPeriods.add(new EmployeePayCutoff(6, 2024, 1));
            employeePayCutoffPeriods.add(new EmployeePayCutoff(6, 2024, 2));
        }
        if (employeePayslipCutoffIndex < 0
                || employeePayslipCutoffIndex >= employeePayCutoffPeriods.size()) {
            employeePayslipCutoffIndex = defaultEmployeePayCutoffIndex();
        }
        syncEmployeePayPeriodCombos();
    }

    private static int defaultEmployeePayCutoffIndex() {
        if (employeePayCutoffPeriods.isEmpty()) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        int targetMonth = today.getMonthValue();
        int targetHalf = today.getDayOfMonth() <= 15 ? 1 : 2;
        for (int i = employeePayCutoffPeriods.size() - 1; i >= 0; i--) {
            EmployeePayCutoff p = employeePayCutoffPeriods.get(i);
            if (p.month == targetMonth && p.half == targetHalf) {
                return i;
            }
        }
        for (int i = employeePayCutoffPeriods.size() - 1; i >= 0; i--) {
            EmployeePayCutoff p = employeePayCutoffPeriods.get(i);
            if (p.month == targetMonth) {
                return i;
            }
        }
        return employeePayCutoffPeriods.size() - 1;
    }

    private static int employeePayCutoffLastDay(int month, int year) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, 1);
        return cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
    }

    private static String employeePayCutoffMonthName(int month) {
        String[] names = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };
        if (month >= 1 && month <= 12) {
            return names[month - 1];
        }
        return "Month " + month;
    }

    private static String formatEmployeePayCutoffPeriod(EmployeePayCutoff period) {
        String monthName = employeePayCutoffMonthName(period.month);
        String shortMonth = monthName.substring(0, 3);
        if (period.half == 1) {
            return monthName + " " + period.year + " · " + shortMonth + " 1–15";
        }
        int last = employeePayCutoffLastDay(period.month, period.year);
        return monthName + " " + period.year + " · " + shortMonth + " 16–" + last;
    }

    private static String formatEmployeePayCutoffHeader(EmployeePayCutoff period) {
        return formatEmployeePayCutoffPeriod(period);
    }

    private static void syncEmployeePayPeriodCombos() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (EmployeePayCutoff period : employeePayCutoffPeriods) {
            model.addElement(formatEmployeePayCutoffPeriod(period));
        }
        if (cmbEmployeePayPeriod != null) {
            cmbEmployeePayPeriod.setModel(model);
            if (employeePayslipCutoffIndex >= 0 && employeePayslipCutoffIndex < model.getSize()) {
                cmbEmployeePayPeriod.setSelectedIndex(employeePayslipCutoffIndex);
            }
        }
        DefaultComboBoxModel<String> fromModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<String> toModel = new DefaultComboBoxModel<>();
        for (int i = 0; i < model.getSize(); i++) {
            String label = model.getElementAt(i);
            fromModel.addElement(label);
            toModel.addElement(label);
        }
        if (cmbEmployeePayFrom != null) {
            cmbEmployeePayFrom.setModel(fromModel);
            cmbEmployeePayFrom.setSelectedIndex(0);
        }
        if (cmbEmployeePayTo != null) {
            cmbEmployeePayTo.setModel(toModel);
            if (toModel.getSize() > 0) {
                cmbEmployeePayTo.setSelectedIndex(toModel.getSize() - 1);
            }
        }
    }

    private static int monthNumberToComboIndex(int month) {
        for (int i = 1; i < MONTH_NUMBERS.length; i++) {
            if (MONTH_NUMBERS[i] == month) {
                return i;
            }
        }
        return 1;
    }

    private static void applyEmployeePayCutoffToPayrollFields() {
        if (employeePayCutoffPeriods.isEmpty()) {
            return;
        }
        EmployeePayCutoff period = employeePayCutoffPeriods.get(employeePayslipCutoffIndex);
        String empId = getLoggedInEmployeeId();
        if (empId != null) {
            txtEmployeeNo.setText(empId);
            updateEmployeeNameFromId(false);
        }
        monthCombo.setSelectedIndex(monthNumberToComboIndex(period.month));
        txtYear.setText(String.valueOf(period.year));
    }

    private static int addEmployeeCutoffHeaderBar(JPanel panel, int y, int width) {
        boolean narrow = width < RESP_PAYSLIP_NARROW_TOOLBAR;
        final int barH = narrow ? 72 : 64;
        final int pad = narrow ? 8 : 12;
        final int filterBtnSz = FIELD_HEIGHT;
        final int navBtnW = narrow ? 64 : 72;
        final int btnGap = 6;
        final int rightBlockW = filterBtnSz + btnGap + navBtnW * 2 + btnGap + pad;

        JPanel bar = new JPanel(null);
        bar.setBackground(PAYROLL_SECTION_BG);
        bar.setBounds(0, y, width, barH);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER_COLOR));

        JLabel intro = new JLabel("Your payslip for");
        intro.setFont(new Font("Segoe UI", Font.PLAIN, narrow ? 11 : 12));
        intro.setForeground(TEXT_MUTED);
        intro.setBounds(pad, narrow ? 10 : 12, width - rightBlockW - pad, 16);
        bar.add(intro);

        employeeCutoffPeriodLbl = new JLabel("", SwingConstants.LEFT);
        employeeCutoffPeriodLbl.setFont(new Font("Segoe UI", Font.BOLD, narrow ? 14 : 16));
        employeeCutoffPeriodLbl.setForeground(TEXT_DARK_NAVY);
        employeeCutoffPeriodLbl.setBounds(pad, narrow ? 28 : 30, width - rightBlockW - pad, 26);
        bar.add(employeeCutoffPeriodLbl);
        updateEmployeeCutoffHeaderLabel();

        int btnY = narrow ? 18 : 14;
        int filterX = width - pad - filterBtnSz;
        int newerX = filterX - btnGap - navBtnW;
        int olderX = newerX - btnGap - navBtnW;

        btnEmployeePayslipOlder = new JButton(narrow ? "◀" : "Older");
        btnEmployeePayslipOlder.setBounds(olderX, btnY, navBtnW, filterBtnSz);
        btnEmployeePayslipOlder.setToolTipText("View an older payslip");
        styleStandardButton(btnEmployeePayslipOlder);
        btnEmployeePayslipOlder.addActionListener(e -> navigateEmployeePayCutoff(-1));
        bar.add(btnEmployeePayslipOlder);

        btnEmployeePayslipNewer = new JButton(narrow ? "▶" : "Newer");
        btnEmployeePayslipNewer.setBounds(newerX, btnY, navBtnW, filterBtnSz);
        btnEmployeePayslipNewer.setToolTipText("View a newer payslip");
        styleStandardButton(btnEmployeePayslipNewer);
        btnEmployeePayslipNewer.addActionListener(e -> navigateEmployeePayCutoff(1));
        bar.add(btnEmployeePayslipNewer);

        btnEmployeePayslipFilter = new JButton(new FilterFunnelIcon(TEXT_DARK_NAVY, 18));
        btnEmployeePayslipFilter.setBounds(filterX, btnY, filterBtnSz, filterBtnSz);
        btnEmployeePayslipFilter.setFocusable(false);
        btnEmployeePayslipFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEmployeePayslipFilter.setToolTipText("Filter pay periods or export payslips");
        styleStandardButton(btnEmployeePayslipFilter);
        btnEmployeePayslipFilter.addActionListener(e -> showEmployeePayslipFilterDialog());
        bar.add(btnEmployeePayslipFilter);

        buildEmployeePayslipFilterDialog();
        syncEmployeePayPeriodCombos();

        panel.add(bar);
        return y + barH;
    }

    private static void buildEmployeePayslipFilterDialog() {
        if (employeePayslipFilterDialog != null) {
            employeePayslipFilterDialog.dispose();
            employeePayslipFilterDialog = null;
        }

        employeePayslipFilterDialog = new JDialog(frame, "Pay Period Filter", false);
        JPanel shell = new JPanel(new java.awt.BorderLayout());
        shell.setBackground(PALETTE_WHITE);
        shell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JPanel inner = new JPanel(null);
        inner.setBackground(PALETTE_WHITE);
        int innerW = 340;
        int fy = 0;

        JLabel title = new JLabel("Choose a pay period");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_DARK_NAVY);
        title.setBounds(0, fy, innerW, 20);
        inner.add(title);
        fy += 28;

        JLabel lblPeriod = new JLabel("Pay period");
        lblPeriod.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPeriod.setForeground(TEXT_MUTED);
        lblPeriod.setBounds(0, fy, innerW, 14);
        inner.add(lblPeriod);
        fy += 18;

        cmbEmployeePayPeriod = new JComboBox<>();
        cmbEmployeePayPeriod.setFont(APP_FONT_PLAIN);
        cmbEmployeePayPeriod.setBounds(0, fy, innerW, FIELD_HEIGHT);
        inner.add(cmbEmployeePayPeriod);
        fy += FIELD_HEIGHT + 18;

        JLabel lblRange = new JLabel("Or select a date range");
        lblRange.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRange.setForeground(TEXT_MUTED);
        lblRange.setBounds(0, fy, innerW, 14);
        inner.add(lblRange);
        fy += 18;

        JLabel lblFrom = new JLabel("From");
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFrom.setForeground(TEXT_MUTED);
        lblFrom.setBounds(0, fy, 40, 14);
        inner.add(lblFrom);
        fy += 16;

        cmbEmployeePayFrom = new JComboBox<>();
        cmbEmployeePayFrom.setFont(APP_FONT_PLAIN);
        cmbEmployeePayFrom.setBounds(0, fy, innerW, FIELD_HEIGHT);
        inner.add(cmbEmployeePayFrom);
        fy += FIELD_HEIGHT + 10;

        JLabel lblTo = new JLabel("To");
        lblTo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTo.setForeground(TEXT_MUTED);
        lblTo.setBounds(0, fy, 40, 14);
        inner.add(lblTo);
        fy += 16;

        cmbEmployeePayTo = new JComboBox<>();
        cmbEmployeePayTo.setFont(APP_FONT_PLAIN);
        cmbEmployeePayTo.setBounds(0, fy, innerW, FIELD_HEIGHT);
        inner.add(cmbEmployeePayTo);
        fy += FIELD_HEIGHT + 20;

        JButton btnApply = new JButton("View Selected Period");
        btnApply.setBounds(0, fy, innerW, BTN_HEIGHT);
        guiStyleAccentButton(btnApply);
        btnApply.addActionListener(e -> {
            applyEmployeePayPeriodFromFilter(false);
            employeePayslipFilterDialog.setVisible(false);
        });
        inner.add(btnApply);
        fy += BTN_HEIGHT + 10;

        JButton btnExportRange = new JButton("Save Range as PDFs");
        btnExportRange.setBounds(0, fy, innerW, BTN_HEIGHT);
        styleStandardButton(btnExportRange);
        btnExportRange.addActionListener(e -> exportEmployeePayslipPdfsFromFilter(false));
        inner.add(btnExportRange);
        fy += BTN_HEIGHT + 10;

        JButton btnExportAll = new JButton("Save All Payslips (PDF)");
        btnExportAll.setBounds(0, fy, innerW, BTN_HEIGHT);
        styleStandardButton(btnExportAll);
        btnExportAll.setToolTipText("Export every payslip from your employment record");
        btnExportAll.addActionListener(e -> exportEmployeePayslipPdfsFromFilter(true));
        inner.add(btnExportAll);
        fy += BTN_HEIGHT + 14;

        inner.setPreferredSize(new java.awt.Dimension(innerW, fy));
        shell.add(inner, java.awt.BorderLayout.CENTER);

        employeePayslipFilterDialog.setContentPane(shell);
        employeePayslipFilterDialog.pack();
        employeePayslipFilterDialog.setResizable(false);
    }

    private static void showEmployeePayslipFilterDialog() {
        if (employeePayslipFilterDialog == null || btnEmployeePayslipFilter == null) {
            buildEmployeePayslipFilterDialog();
        }
        syncEmployeePayPeriodCombos();
        try {
            java.awt.Point anchor = btnEmployeePayslipFilter.getLocationOnScreen();
            int dlgW = employeePayslipFilterDialog.getWidth();
            int dlgH = employeePayslipFilterDialog.getHeight();
            int x = anchor.x + btnEmployeePayslipFilter.getWidth() - dlgW;
            int popY = anchor.y + btnEmployeePayslipFilter.getHeight() + 8;
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            java.awt.Rectangle screen = ge.getMaximumWindowBounds();
            x = Math.max(screen.x + 8, Math.min(x, screen.x + screen.width - dlgW - 8));
            popY = Math.max(screen.y + 8, Math.min(popY, screen.y + screen.height - dlgH - 8));
            employeePayslipFilterDialog.setLocation(x, popY);
        } catch (Exception ex) {
            employeePayslipFilterDialog.setLocationRelativeTo(btnEmployeePayslipFilter);
        }
        employeePayslipFilterDialog.setVisible(true);
        employeePayslipFilterDialog.toFront();
        if (cmbEmployeePayPeriod != null) {
            cmbEmployeePayPeriod.requestFocusInWindow();
        }
    }

    private static void applyEmployeePayPeriodFromFilter(boolean fromRange) {
        if (employeePayCutoffPeriods.isEmpty()) {
            return;
        }
        int idx;
        if (fromRange) {
            int fromIdx = cmbEmployeePayFrom != null ? cmbEmployeePayFrom.getSelectedIndex() : 0;
            int toIdx = cmbEmployeePayTo != null ? cmbEmployeePayTo.getSelectedIndex() : fromIdx;
            if (fromIdx < 0 || toIdx < 0) {
                return;
            }
            if (fromIdx > toIdx) {
                int swap = fromIdx;
                fromIdx = toIdx;
                toIdx = swap;
            }
            idx = fromIdx;
        } else {
            idx = cmbEmployeePayPeriod != null ? cmbEmployeePayPeriod.getSelectedIndex() : employeePayslipCutoffIndex;
        }
        if (idx < 0 || idx >= employeePayCutoffPeriods.size()) {
            return;
        }
        employeePayslipCutoffIndex = idx;
        updateEmployeeCutoffHeaderLabel();
        if (cmbEmployeePayPeriod != null && cmbEmployeePayPeriod.getSelectedIndex() != idx) {
            cmbEmployeePayPeriod.setSelectedIndex(idx);
        }
        runEmployeePayslipForCurrentCutoff();
    }

    private static void exportEmployeePayslipPdfsFromFilter(boolean exportAll) {
        if (employeePayCutoffPeriods.isEmpty()) {
            showToast("No pay periods available to export.", new Color(180, 90, 40));
            return;
        }
        int fromIdx = 0;
        int toIdx = employeePayCutoffPeriods.size() - 1;
        if (!exportAll) {
            fromIdx = cmbEmployeePayFrom != null ? cmbEmployeePayFrom.getSelectedIndex() : 0;
            toIdx = cmbEmployeePayTo != null ? cmbEmployeePayTo.getSelectedIndex() : fromIdx;
            if (fromIdx < 0 || toIdx < 0) {
                showToast("Select a valid date range.", new Color(180, 90, 40));
                return;
            }
            if (fromIdx > toIdx) {
                int swap = fromIdx;
                fromIdx = toIdx;
                toIdx = swap;
            }
        }
        exportEmployeePayslipPdfs(fromIdx, toIdx);
    }

    private static void exportEmployeePayslipPdfs(int fromIdx, int toIdx) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select folder to save payslip PDFs");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File dir = chooser.getSelectedFile();
        if (dir == null || !dir.isDirectory()) {
            showToast("Please choose a folder.", new Color(180, 90, 40));
            return;
        }

        int savedIdx = employeePayslipCutoffIndex;
        int savedCount = 0;
        java.util.LinkedHashSet<String> seenMonths = new java.util.LinkedHashSet<>();
        employeePayslipSuppressToast = true;
        try {
            for (int i = fromIdx; i <= toIdx; i++) {
                EmployeePayCutoff period = employeePayCutoffPeriods.get(i);
                String monthKey = period.month + "/" + period.year;
                if (!seenMonths.add(monthKey)) {
                    continue;
                }
                employeePayslipCutoffIndex = i;
                applyEmployeePayCutoffToPayrollFields();
                runPayrollCalculation();
                if (!SalaryComputationModule.lastCalculationSucceeded) {
                    continue;
                }
                String fname = "Payslip_" + SalaryComputationModule.lastEmpId + "_"
                        + employeePayCutoffMonthName(period.month) + "_" + period.year + ".pdf";
                File target = new File(dir, fname);
                try {
                    byte[] pdf = buildPayslipPdf();
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(target)) {
                        fos.write(pdf);
                    }
                    savedCount++;
                } catch (Exception ex) {
                    System.out.println("Export failed for " + fname + ": " + ex.getMessage());
                }
            }
        } finally {
            employeePayslipSuppressToast = false;
            employeePayslipCutoffIndex = savedIdx;
            updateEmployeeCutoffHeaderLabel();
            syncEmployeePayPeriodCombos();
            runEmployeePayslipForCurrentCutoff();
        }

        if (savedCount > 0) {
            showToast("Saved " + savedCount + " payslip PDF(s) to " + dir.getName() + ".");
            try {
                java.awt.Desktop.getDesktop().open(dir);
            } catch (Exception ignored) {
            }
        } else {
            showToast("No payslips could be exported for the selected range.", new Color(180, 90, 40));
        }
    }

    private static void updateEmployeeCutoffHeaderLabel() {
        if (employeeCutoffPeriodLbl == null || employeePayCutoffPeriods.isEmpty()) {
            return;
        }
        EmployeePayCutoff period = employeePayCutoffPeriods.get(employeePayslipCutoffIndex);
        employeeCutoffPeriodLbl.setText(formatEmployeePayCutoffHeader(period));
        if (cmbEmployeePayPeriod != null && cmbEmployeePayPeriod.getSelectedIndex() != employeePayslipCutoffIndex) {
            cmbEmployeePayPeriod.setSelectedIndex(employeePayslipCutoffIndex);
        }
    }

    private static void navigateEmployeePayCutoff(int delta) {
        if (employeePayCutoffPeriods.isEmpty()) {
            return;
        }
        int next = employeePayslipCutoffIndex + delta;
        if (next < 0 || next >= employeePayCutoffPeriods.size()) {
            showToast(delta < 0 ? "No older payslip." : "No newer payslip.",
                    new Color(100, 115, 140));
            return;
        }
        employeePayslipCutoffIndex = next;
        updateEmployeeCutoffHeaderLabel();
        runEmployeePayslipForCurrentCutoff();
    }

    private static void runEmployeePayslipForCurrentCutoff() {
        applyEmployeePayCutoffToPayrollFields();
        runPayrollCalculation();
    }

    private static void enableTableSorting(JTable table) {
        if (table == null || !(table.getModel() instanceof DefaultTableModel)) {
            return;
        }
        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);
    }

    private static void showToast(String message) {
        showToast(message, new Color(34, 160, 90));
    }

    private static void showPopupSuccessAndClose(JDialog dialog, String toastMessage,
            String dialogMessage, String dialogTitle) {
        dialog.dispose();
        SwingUtilities.invokeLater(() -> {
            showToast(toastMessage);
            JOptionPane.showMessageDialog(frame, dialogMessage, dialogTitle,
                    JOptionPane.INFORMATION_MESSAGE);
        });
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
        return 1; // Default to January (index 1 in MONTH_NUMBERS)
    }

    private static String notificationKey(NotificationModule.Notification n) {
        return n.category + "::" + n.text;
    }

    private static List<NotificationModule.Notification> buildSystemNotifications() {
        List<NotificationModule.Notification> allNotifications = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int dom = today.getDayOfMonth();
        int lastDay = today.lengthOfMonth();

        // Shared cut-off math
        int cutoffDay = dom <= 15 ? 15 : lastDay;
        long daysToCut = cutoffDay - dom;
        String cutoffPeriod = cutoffDay == 15 ? "1st–15th" : "16th–end of month";
        String cutoffMonth = today.getMonth().name().charAt(0)
                + today.getMonth().name().substring(1, 3).toLowerCase();

        if (isHrUser()) {
            // ── HR notifications: payroll processing, attendance oversight, birthdays ──

            // Payroll cut-off reminder (processing frame, not receiving)
            if (daysToCut == 0) {
                allNotifications.add(new NotificationModule.Notification("Payroll",
                        "Payroll cut-off is today (" + cutoffMonth + " " + cutoffDay
                                + "). Finalize all salary computations for the " + cutoffPeriod + " period now."));
            } else if (daysToCut <= 3) {
                allNotifications.add(new NotificationModule.Notification("Payroll",
                        "Payroll cut-off in " + daysToCut + " day(s) — " + cutoffMonth + " " + cutoffDay
                                + ". Prepare and review salary computations for the " + cutoffPeriod + " period."));
            } else {
                allNotifications.add(new NotificationModule.Notification("Payroll",
                        "Next payroll cut-off: " + cutoffMonth + " " + cutoffDay
                                + " (" + daysToCut + " day(s) away). Period: " + cutoffPeriod + "."));
            }

            allNotifications.add(new NotificationModule.Notification("Payroll",
                    "Reminder: Verify SSS, PhilHealth, and Pag-IBIG deduction amounts for all "
                            + "employees before finalizing this period's payroll run."));

            // Attendance cut-off — HR oversight framing
            if (daysToCut >= 0 && daysToCut <= 5) {
                allNotifications.add(new NotificationModule.Notification("Attendance",
                        "Attendance cut-off is " + (daysToCut == 0 ? "today" : "in " + daysToCut + " day(s)")
                                + ". Review all employee time-in/out entries before processing payroll."));
            }

            // Birthday notifications for ALL employees
            for (String[] emp : FileHandlerModule.getAllEmployees()) {
                String b = safeColumn(emp, EmployeeModule.BIRTHDAY);
                if (b == null || b.isEmpty() || b.equals("-") || !b.contains("/"))
                    continue;
                try {
                    String[] parts = b.split("/");
                    int m = Integer.parseInt(parts[0].trim());
                    int d = Integer.parseInt(parts[1].trim());
                    if (m == today.getMonthValue() && d == today.getDayOfMonth()) {
                        allNotifications.add(new NotificationModule.Notification("Birthday",
                                "Today is " + EmployeeModule.fullName(emp)
                                        + "'s birthday. Don't forget to send greetings!"));
                    }
                } catch (NumberFormatException ex) {
                    /* ignore malformed */ }
            }

        } else {
            // ── Employee notifications: personal pay, payslip, and attendance ──

            LocalDate nextPay = dom < 15 ? today.withDayOfMonth(15)
                    : dom < lastDay ? today.withDayOfMonth(lastDay)
                            : today.plusMonths(1).withDayOfMonth(15);
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextPay);
            String payMonth = nextPay.getMonth().name().charAt(0)
                    + nextPay.getMonth().name().substring(1, 3).toLowerCase();
            String payPeriod = nextPay.getDayOfMonth() == 15 ? "1st–15th" : "16th–end of month";

            if (daysUntil == 0) {
                allNotifications.add(new NotificationModule.Notification("Payroll",
                        "Pay day is today! Your salary for the " + payPeriod + " period of "
                                + payMonth + " has been processed. Check My Payslip for your breakdown."));
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

            allNotifications.add(new NotificationModule.Notification("Payroll",
                    "Your most recent payslip is ready. Open My Payslip in the sidebar to view your full breakdown."));

            // Birthday only for the logged-in employee
            for (String[] emp : FileHandlerModule.getAllEmployees()) {
                String b = safeColumn(emp, EmployeeModule.BIRTHDAY);
                if (b == null || b.isEmpty() || b.equals("-") || !b.contains("/"))
                    continue;
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
                        if (isThisUser) {
                            allNotifications.add(new NotificationModule.Notification("Birthday",
                                    "Happy Birthday, " + emp[EmployeeModule.FIRST_NAME]
                                            + "! Wishing you a wonderful day!"));
                        }
                    }
                } catch (NumberFormatException ex) {
                    /* ignore malformed */ }
            }

            // Attendance cut-off — personal framing
            if (daysToCut >= 0 && daysToCut <= 5) {
                allNotifications.add(new NotificationModule.Notification("Attendance",
                        "Attendance cut-off is " + (daysToCut == 0 ? "today" : "in " + daysToCut + " day(s)")
                                + ". Make sure all your time-in/out entries are complete for this period."));
            }
        }

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
            case "My Profile":
                if (isHrUser()) {
                    showEmployeeLookupUI();
                } else {
                    showMyProfileUI();
                }
                break;
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

    private static String getEffectiveSearchQuery(JTextField field, String placeholderHint) {
        if (field == null) {
            return "";
        }
        String raw = field.getText();
        if (raw == null) {
            return "";
        }
        raw = raw.trim();
        if (raw.isEmpty()) {
            return "";
        }
        if (placeholderHint != null && raw.equalsIgnoreCase(placeholderHint.trim())) {
            return "";
        }
        if (TEXT_PLACEHOLDER_GRAY.equals(field.getForeground())) {
            return "";
        }
        return raw.toLowerCase();
    }

    private static void attachLiveSearchFilter(JTextField field, String placeholderHint, Runnable onFilter) {
        if (field == null || onFilter == null) {
            return;
        }
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                SwingUtilities.invokeLater(onFilter);
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        });
    }

    private static boolean rowMatchesEmployeeSearch(Object[] row, String[] emp, String q) {
        if (q == null || q.isEmpty()) {
            return true;
        }
        if (row != null) {
            for (Object cell : row) {
                if (cell != null && String.valueOf(cell).toLowerCase().contains(q)) {
                    return true;
                }
            }
        }
        if (emp != null) {
            String fullName = EmployeeModule.fullName(emp).toLowerCase();
            if (!"unknown".equals(fullName) && fullName.contains(q)) {
                return true;
            }
            String combined = (safeColumn(emp, EmployeeModule.LAST_NAME) + " "
                    + safeColumn(emp, EmployeeModule.FIRST_NAME)).trim().toLowerCase();
            if (!combined.isEmpty() && combined.contains(q)) {
                return true;
            }
            if (safeColumn(emp, EmployeeModule.POSITION).toLowerCase().contains(q)) {
                return true;
            }
            if (safeColumn(emp, EmployeeModule.STATUS).toLowerCase().contains(q)) {
                return true;
            }
        }
        return false;
    }

    private static boolean rowMatchesDepartmentFilter(String[] emp, String deptFilter) {
        if (deptFilter == null || deptFilter.isEmpty() || "All Departments".equals(deptFilter)) {
            return true;
        }
        if (emp == null || emp.length <= EmployeeModule.DEPARTMENT) {
            return false;
        }
        return deptFilter.equalsIgnoreCase(emp[EmployeeModule.DEPARTMENT].trim());
    }

    private static boolean rowMatchesStatusFilter(String[] emp, String statusFilter) {
        if (statusFilter == null || statusFilter.isEmpty() || "All Statuses".equals(statusFilter)) {
            return true;
        }
        if (emp == null || emp.length <= EmployeeModule.STATUS) {
            return false;
        }
        return statusFilter.equalsIgnoreCase(emp[EmployeeModule.STATUS].trim());
    }

    private static void applyEmployeeTableFilter() {
        if (employeeTableModel == null) {
            return;
        }
        int savedScroll = employeeRecordsScrollPane != null
                ? employeeRecordsScrollPane.getVerticalScrollBar().getValue()
                : 0;
        String q = getEffectiveSearchQuery(txtRecSearch, "Name or employee #");
        String deptFilter = cmbRecDeptFilter != null
                ? String.valueOf(cmbRecDeptFilter.getSelectedItem())
                : "All Departments";
        String statusFilter = cmbRecStatusFilter != null
                ? String.valueOf(cmbRecStatusFilter.getSelectedItem())
                : "All Statuses";

        employeeTableModel.setRowCount(0);
        int shown = 0;
        for (int i = 0; i < employeeTableAllRows.size(); i++) {
            Object[] row = employeeTableAllRows.get(i);
            String[] emp = i < employeeRecordsCache.size() ? employeeRecordsCache.get(i) : null;
            if (rowMatchesEmployeeSearch(row, emp, q)
                    && rowMatchesDepartmentFilter(emp, deptFilter)
                    && rowMatchesStatusFilter(emp, statusFilter)) {
                employeeTableModel.addRow(row);
                shown++;
            }
        }
        if (lblRecFilterCount != null) {
            lblRecFilterCount.setText(shown + " of " + employeeTableAllRows.size() + " employees shown");
        }
        if (employeeRecordsScrollPane != null) {
            JScrollBar vertical = employeeRecordsScrollPane.getVerticalScrollBar();
            vertical.setValue(Math.min(savedScroll, vertical.getMaximum()));
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
        clearFormHistory();
    }

    private static void clearFormHistory() {
        formUndoStack.clear();
        formRedoStack.clear();
        formSnapshotOnFocus = serializeRecordForm();
        updateUndoRedoButtonStates();
    }

    private static void updateUndoRedoButtonStates() {
        if (btnRecUndo != null) {
            btnRecUndo.setEnabled(!formUndoStack.isEmpty());
            refreshStandardButtonState(btnRecUndo);
        }
        if (btnRecRedo != null) {
            btnRecRedo.setEnabled(!formRedoStack.isEmpty());
            refreshStandardButtonState(btnRecRedo);
        }
    }

    // ── CSV snapshot helpers for Undo / Redo / Revert ─────────────────────

    private static List<String[]> takeCsvSnapshot() {
        List<String[]> snap = new java.util.ArrayList<>();
        for (String[] r : FileHandlerModule.getAllEmployees())
            snap.add(r.clone());
        return snap;
    }

    private static void pushCsvSnapshot() {
        pushCsvSnapshotWithLog("CHANGE", selectedEmployeeId, "Employee records updated");
    }

    private static void pushCsvSnapshotWithLog(String action, String employeeId, String summary) {
        List<String[]> snap = takeCsvSnapshot();
        EmployeeRevisionModule.logChange(action, employeeId, summary, snap, loggedInUser);
        csvUndoStack.push(snap);
        csvRedoStack.clear();
        updateCsvHistoryButtonStates();
    }

    private static void restoreCsvSnapshot(List<String[]> snap) {
        FileHandlerModule.rewriteEmployeeFile(snap);
        refreshEmployeeTable();
        clearEmployeeRecordForm();
        updateCsvHistoryButtonStates();
    }

    private static void updateCsvHistoryButtonStates() {
        if (btnRecUndo != null) {
            btnRecUndo.setEnabled(!csvUndoStack.isEmpty());
            btnRecUndo.setToolTipText(csvUndoStack.isEmpty() ? "Nothing to undo"
                    : "Undo last add/delete (" + csvUndoStack.size() + " step" + (csvUndoStack.size() == 1 ? "" : "s")
                            + ")");
            refreshStandardButtonState(btnRecUndo);
        }
        if (btnRecRedo != null) {
            btnRecRedo.setEnabled(!csvRedoStack.isEmpty());
            btnRecRedo.setToolTipText(csvRedoStack.isEmpty() ? "Nothing to redo"
                    : "Redo (" + csvRedoStack.size() + " step" + (csvRedoStack.size() == 1 ? "" : "s") + ")");
            refreshStandardButtonState(btnRecRedo);
        }
        if (btnRecRevert != null) {
            boolean canRevert = csvOriginalSnapshot != null && !csvUndoStack.isEmpty();
            btnRecRevert.setEnabled(canRevert);
            btnRecRevert.setToolTipText(canRevert ? "Restore employee list to its state when the session started"
                    : "No changes to revert");
            refreshStandardButtonState(btnRecRevert);
        }
    }

    private static void attachFormHistoryListener(JTextField field) {
        if (field == null)
            return;
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                formSnapshotOnFocus = serializeRecordForm();
            }
        });
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private boolean pushed = false;

            private void onChanged() {
                if (formRestoringState)
                    return;
                if (!pushed) {
                    String top = formUndoStack.isEmpty() ? "" : formUndoStack.peek();
                    if (!formSnapshotOnFocus.equals(top)) {
                        formUndoStack.push(formSnapshotOnFocus);
                        formRedoStack.clear();
                        updateUndoRedoButtonStates();
                    }
                    pushed = true;
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                onChanged();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                onChanged();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });
        // reset "pushed" flag when focus is re-gained so next edit session pushes again
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            // Note: focusGained already captures snapshot; pushed flag resets per-focus
            // naturally
            // because each DocumentListener instance is per-field and per-focus-session
            // tracking
            // is handled by formSnapshotOnFocus being refreshed on every focusGained.
        });
    }

    private static void restoreFormFromSnapshot(String snapshot) {
        if (snapshot == null)
            return;
        String[] p = snapshot.split("\\|", -1);
        setFieldText(txtRecEmpNo, p, 0);
        setFieldText(txtRecLastName, p, 1);
        setFieldText(txtRecFirstName, p, 2);
        setFieldText(txtRecBirthday, p, 3);
        setFieldText(txtRecAddress, p, 4);
        setFieldText(txtRecPhone, p, 5);
        setFieldText(txtRecSSS, p, 6);
        setFieldText(txtRecPhilHealth, p, 7);
        setFieldText(txtRecTIN, p, 8);
        setFieldText(txtRecPagIBIG, p, 9);
        setFieldText(txtRecStatus, p, 10);
        setFieldText(txtRecPosition, p, 11);
        setFieldText(txtRecSupervisor, p, 12);
        setFieldText(txtRecBasicSalary, p, 13);
        setFieldText(txtRecHourlyRate, p, 14);
    }

    private static void setFieldText(JTextField f, String[] arr, int i) {
        if (f != null)
            f.setText(i < arr.length ? arr[i] : "");
    }

    private static void performFormUndo() {
        if (formUndoStack.isEmpty())
            return;
        String current = serializeRecordForm();
        formRedoStack.push(current);
        formRestoringState = true;
        restoreFormFromSnapshot(formUndoStack.pop());
        formRestoringState = false;
        formSnapshotOnFocus = serializeRecordForm();
        updateUndoRedoButtonStates();
    }

    private static void performFormRedo() {
        if (formRedoStack.isEmpty())
            return;
        String current = serializeRecordForm();
        formUndoStack.push(current);
        formRestoringState = true;
        restoreFormFromSnapshot(formRedoStack.pop());
        formRestoringState = false;
        formSnapshotOnFocus = serializeRecordForm();
        updateUndoRedoButtonStates();
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
                        BorderFactory.createEmptyBorder(6, 14, 6, 14)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                resetFieldBorder(field);
            }
        });
    }

    private static void exportPayslipToFile() {
        if (txtResultArea == null || txtResultArea.getText().trim().isEmpty()) {
            showToast("Generate a payslip before downloading.", new Color(180, 90, 40));
            return;
        }
        if (!SalaryComputationModule.lastCalculationSucceeded) {
            showToast("No payslip data available — select a period with attendance records.", new Color(180, 90, 40));
            return;
        }
        String empId = SalaryComputationModule.lastEmpId;
        String mName = SalaryComputationModule.lastMonthName;
        String yr = SalaryComputationModule.lastYear;
        String defName = "Payslip_" + empId + "_" + mName + "_" + yr + ".pdf";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Payslip as PDF");
        chooser.setSelectedFile(new File(defName));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
            return;

        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf"))
            target = new File(target.getAbsolutePath() + ".pdf");

        try {
            byte[] pdf = buildPayslipPdf();
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(target)) {
                fos.write(pdf);
            }
            showToast("Payslip saved: " + target.getName());
            try {
                java.awt.Desktop.getDesktop().open(target);
            } catch (Exception ignored) {
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not save PDF: " + ex.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void exportBatchPayslipsAsZip() {
        java.util.List<SalaryComputationModule.EmployeePayrollSummary> computed = new java.util.ArrayList<>();
        for (SalaryComputationModule.EmployeePayrollSummary s : lastBatchSummaries) {
            if (s.computed) computed.add(s);
        }
        if (computed.isEmpty()) {
            showToast("No payslip data — run Calculate Payroll first.", new Color(180, 90, 40));
            return;
        }

        String mName = computed.get(0).monthName;
        String yr    = computed.get(0).year;

        if (computed.size() == 1) {
            // Single employee — save a plain PDF
            SalaryComputationModule.EmployeePayrollSummary s = computed.get(0);
            String defName = "Payslip_" + s.employeeId + "_" + mName + "_" + yr + ".pdf";
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Payslip as PDF");
            chooser.setSelectedFile(new File(defName));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));
            if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".pdf")) target = new File(target.getAbsolutePath() + ".pdf");
            try {
                byte[] pdf = buildPayslipPdfForSummary(s);
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(target)) { fos.write(pdf); }
                showToast("Payslip saved: " + target.getName());
                try { java.awt.Desktop.getDesktop().open(target); } catch (Exception ignored) {}
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Could not save PDF: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // Multiple employees — offer a ZIP
        String defZip = "Payslips_" + mName + "_" + yr + ".zip";
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Payslips as ZIP");
        chooser.setSelectedFile(new File(defZip));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ZIP Archive (*.zip)", "zip"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File zipTarget = chooser.getSelectedFile();
        if (!zipTarget.getName().toLowerCase().endsWith(".zip")) zipTarget = new File(zipTarget.getAbsolutePath() + ".zip");

        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipTarget))) {
            for (SalaryComputationModule.EmployeePayrollSummary s : computed) {
                byte[] pdf = buildPayslipPdfForSummary(s);
                String entryName = "Payslip_" + s.employeeId + "_" + s.employeeName.replace(" ", "_") + ".pdf";
                zos.putNextEntry(new java.util.zip.ZipEntry(entryName));
                zos.write(pdf);
                zos.closeEntry();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Could not save ZIP: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showToast(computed.size() + " payslips saved to " + zipTarget.getName());
        try { java.awt.Desktop.getDesktop().open(zipTarget.getParentFile()); } catch (Exception ignored) {}
    }

    private static byte[] buildPayslipPdfForSummary(SalaryComputationModule.EmployeePayrollSummary s)
            throws java.io.IOException {
        // Temporarily populate the last* fields so buildPayslipPdf() can read them
        boolean prevOk    = SalaryComputationModule.lastCalculationSucceeded;
        String prevId     = SalaryComputationModule.lastEmpId;
        String prevName   = SalaryComputationModule.lastEmpName;
        String prevBday   = SalaryComputationModule.lastEmpBirthday;
        String prevMon    = SalaryComputationModule.lastMonthName;
        String prevYr     = SalaryComputationModule.lastYear;
        double prevHF     = SalaryComputationModule.lastHoursFirst;
        double prevHS     = SalaryComputationModule.lastHoursSecond;
        double prevGF     = SalaryComputationModule.lastGrossFirst;
        double prevGS     = SalaryComputationModule.lastGrossSecond;
        double prevNF     = SalaryComputationModule.lastNetFirst;
        double prevNS     = SalaryComputationModule.lastNetSecond;
        double prevSss    = SalaryComputationModule.lastSss;
        double prevPh     = SalaryComputationModule.lastPhilHealth;
        double prevPi     = SalaryComputationModule.lastPagIbig;
        double prevTax    = SalaryComputationModule.lastTax;
        double prevDed    = SalaryComputationModule.lastTotalDeductions;
        try {
            SalaryComputationModule.lastCalculationSucceeded = true;
            SalaryComputationModule.lastEmpId            = s.employeeId;
            SalaryComputationModule.lastEmpName          = s.employeeName;
            SalaryComputationModule.lastEmpBirthday      = s.birthday;
            SalaryComputationModule.lastMonthName        = s.monthName;
            SalaryComputationModule.lastYear             = s.year;
            SalaryComputationModule.lastHoursFirst       = s.hoursFirst;
            SalaryComputationModule.lastHoursSecond      = s.hoursSecond;
            SalaryComputationModule.lastGrossFirst       = s.grossFirst;
            SalaryComputationModule.lastGrossSecond      = s.grossSecond;
            SalaryComputationModule.lastNetFirst         = s.grossFirst;
            SalaryComputationModule.lastNetSecond        = s.grossSecond - s.totalDeductions;
            SalaryComputationModule.lastSss              = s.sss;
            SalaryComputationModule.lastPhilHealth       = s.philHealth;
            SalaryComputationModule.lastPagIbig          = s.pagIbig;
            SalaryComputationModule.lastTax              = s.tax;
            SalaryComputationModule.lastTotalDeductions  = s.totalDeductions;
            return buildPayslipPdf();
        } finally {
            SalaryComputationModule.lastCalculationSucceeded = prevOk;
            SalaryComputationModule.lastEmpId            = prevId;
            SalaryComputationModule.lastEmpName          = prevName;
            SalaryComputationModule.lastEmpBirthday      = prevBday;
            SalaryComputationModule.lastMonthName        = prevMon;
            SalaryComputationModule.lastYear             = prevYr;
            SalaryComputationModule.lastHoursFirst       = prevHF;
            SalaryComputationModule.lastHoursSecond      = prevHS;
            SalaryComputationModule.lastGrossFirst       = prevGF;
            SalaryComputationModule.lastGrossSecond      = prevGS;
            SalaryComputationModule.lastNetFirst         = prevNF;
            SalaryComputationModule.lastNetSecond        = prevNS;
            SalaryComputationModule.lastSss              = prevSss;
            SalaryComputationModule.lastPhilHealth       = prevPh;
            SalaryComputationModule.lastPagIbig          = prevPi;
            SalaryComputationModule.lastTax              = prevTax;
            SalaryComputationModule.lastTotalDeductions  = prevDed;
        }
    }

    private static byte[] buildPayslipPdf() throws java.io.IOException {
        String empId = SalaryComputationModule.lastEmpId;
        String empName = SalaryComputationModule.lastEmpName;
        String birthday = SalaryComputationModule.lastEmpBirthday;
        String mName = SalaryComputationModule.lastMonthName;
        String yr = SalaryComputationModule.lastYear;
        double hrsFirst = SalaryComputationModule.lastHoursFirst;
        double hrsSecond = SalaryComputationModule.lastHoursSecond;
        double gFirst = SalaryComputationModule.lastGrossFirst;
        double gSecond = SalaryComputationModule.lastGrossSecond;
        double nFirst = SalaryComputationModule.lastNetFirst;
        double nSecond = SalaryComputationModule.lastNetSecond;
        double dSss = SalaryComputationModule.lastSss;
        double dPh = SalaryComputationModule.lastPhilHealth;
        double dPi = SalaryComputationModule.lastPagIbig;
        double dTax = SalaryComputationModule.lastTax;
        double dTotal = SalaryComputationModule.lastTotalDeductions;
        double tGross = gFirst + gSecond;
        double tNet = nFirst + nSecond;

        int lastDay = 31;
        try {
            String[] mNames = { "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December" };
            int mi = java.util.Arrays.asList(mNames).indexOf(mName);
            if (mi >= 0) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(Integer.parseInt(yr), mi, 1);
                lastDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            }
        } catch (Exception ignored) {
        }

        StringBuilder cs = new StringBuilder();

        final int contentLeft = 50;
        final int contentWidth = 495;
        final int contentRight = contentLeft + contentWidth;
        final int padX = 6;

        // Layout stack (PDF y increases upward; each block sits below the previous)
        // Content shifted 44pt down (36pt page-top margin + 8pt inner top padding).
        final int companyHeaderBottom = 750;
        final int companyHeaderHeight = 48;
        final int companyHeaderTop = companyHeaderBottom + companyHeaderHeight; // = 798
        final int payslipRibbonBottom = 732;
        final int payslipRibbonHeight = 18;
        final int empInfoBandBottom = 714;
        final int empInfoBandHeight = 14;
        final int empDetailBottom = 668;
        final int empDetailHeight = 46;

        // ── Background fills first (text drawn afterward so nothing is covered) ──
        pdfFillRect(cs, 0.13f, 0.25f, 0.55f, contentLeft, companyHeaderBottom,
                contentWidth, companyHeaderHeight);
        pdfFillRect(cs, 0.22f, 0.42f, 0.75f, contentLeft, payslipRibbonBottom,
                contentWidth, payslipRibbonHeight);
        pdfFillRect(cs, 0.88f, 0.92f, 0.97f, contentLeft, empInfoBandBottom,
                contentWidth, empInfoBandHeight);
        pdfFillRect(cs, 0.95f, 0.96f, 0.98f, contentLeft, empDetailBottom,
                contentWidth, empDetailHeight);

        // ── Company header text (white, fully inside dark-blue box) ───────────
        pdfDrawTextCentered(cs, contentLeft, contentWidth, companyHeaderTop - 16,
                "MOTORPH", 18f, true, 1f, 1f, 1f);
        pdfDrawTextCentered(cs, contentLeft, contentWidth, companyHeaderTop - 30,
                "Motor Parts Hub Philippines, Inc.", 9f, false, 1f, 1f, 1f);
        pdfDrawTextCentered(cs, contentLeft, contentWidth, companyHeaderTop - 42,
                "Kalayaan Avenue, Makati City 1200", 8f, false, 1f, 1f, 1f);

        // ── PAYSLIP ribbon text ─────────────────────────────────────────────
        pdfDrawTextCentered(cs, contentLeft, contentWidth,
                payslipRibbonBottom + (payslipRibbonHeight / 2) + 2,
                "PAYSLIP", 10f, true, 1f, 1f, 1f);

        // ── EMPLOYEE INFORMATION ──────────────────────────────────────────────
        pdfDrawBandTitle(cs, contentLeft + padX, empInfoBandBottom, empInfoBandHeight,
                "EMPLOYEE INFORMATION", 8f);

        // Employee detail rows inside gray box
        final int row1 = empDetailBottom + empDetailHeight - 12;
        final int row2 = row1 - 14;
        final int row3 = row2 - 14;
        cs.append("0 0 0 rg\n");
        pdfDrawText(cs, contentLeft + padX + 2, row1, "Employee No.", 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, 148, row1, empId, 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 318, row1, "Pay Period", 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, 398, row1, mName + " " + yr, 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, contentLeft + padX + 2, row2, "Full Name", 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, 148, row2, empName, 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, contentLeft + padX + 2, row3, "Birthday", 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, 148, row3, birthday, 9f, false, 0f, 0f, 0f);

        pdfDrawLine(cs, 0.78f, 0.80f, 0.85f, 0.5f, contentLeft, empDetailBottom - 4,
                contentRight, empDetailBottom - 4);

        // ── 1ST CUTOFF ──────────────────────────────────────────────────────
        final int cutoff1BandBottom = 650;
        final int cutoff1BandHeight = 14;
        pdfFillRect(cs, 0.88f, 0.92f, 0.97f, contentLeft, cutoff1BandBottom,
                contentWidth, cutoff1BandHeight);
        pdfDrawBandTitle(cs, contentLeft + padX, cutoff1BandBottom, cutoff1BandHeight,
                "1ST CUTOFF  \u00b7  " + mName + " 1 - 15, " + yr, 8.5f);

        pdfDrawText(cs, contentLeft + padX + 2, 635, "Hours Worked", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 400, 635, String.format("%.2f hrs", hrsFirst), 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, contentLeft + padX + 2, 621, "Gross Pay", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 621, "PHP " + payFmt(gFirst), 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, contentLeft + padX + 2, 607, "Net Pay  (No Deductions)", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 607, "PHP " + payFmt(nFirst), 9f, true, 0f, 0f, 0f);

        pdfDrawLine(cs, 0.78f, 0.80f, 0.85f, 0.5f, contentLeft, 597,
                contentRight, 597);

        // ── 2ND CUTOFF ──────────────────────────────────────────────────────
        final int cutoff2BandBottom = 584;
        final int cutoff2BandHeight = 14;
        pdfFillRect(cs, 0.88f, 0.92f, 0.97f, contentLeft, cutoff2BandBottom,
                contentWidth, cutoff2BandHeight);
        pdfDrawBandTitle(cs, contentLeft + padX, cutoff2BandBottom, cutoff2BandHeight,
                "2ND CUTOFF  \u00b7  " + mName + " 16 - " + lastDay + ", " + yr, 8.5f);

        pdfDrawText(cs, contentLeft + padX + 2, 567, "Hours Worked", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 400, 567, String.format("%.2f hrs", hrsSecond), 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, contentLeft + padX + 2, 553, "Gross Pay", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 553, "PHP " + payFmt(gSecond), 9f, true, 0f, 0f, 0f);

        // Deductions sub-header
        final int deductBandBottom = 539;
        final int deductBandHeight = 14;
        pdfFillRect(cs, 0.92f, 0.94f, 0.98f, contentLeft, deductBandBottom,
                contentWidth, deductBandHeight);
        pdfDrawText(cs, contentLeft + padX, deductBandBottom + 5, "DEDUCTIONS", 8f, true, 0.30f, 0.30f, 0.40f);

        pdfDrawText(cs, 66, 527, "SSS Contribution", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 527, "PHP " + payFmt(dSss), 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 66, 513, "PhilHealth Premium", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 513, "PHP " + payFmt(dPh), 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 66, 499, "Pag-IBIG Contribution", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 499, "PHP " + payFmt(dPi), 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 66, 485, "Withholding Tax", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 485, "PHP " + payFmt(dTax), 9f, false, 0f, 0f, 0f);

        pdfDrawLine(cs, 0.60f, 0.62f, 0.68f, 0.5f, contentLeft, 475,
                contentRight, 475);
        pdfDrawText(cs, contentLeft + padX + 2, 461, "Total Deductions", 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 461, "PHP " + payFmt(dTotal), 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, contentLeft + padX + 2, 447, "Net Pay", 9f, true, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 447, "PHP " + payFmt(nSecond), 9f, true, 0f, 0f, 0f);

        pdfDrawLine(cs, 0.13f, 0.25f, 0.55f, 2f, contentLeft, 434,
                contentRight, 434);

        // ── PAY SUMMARY ────────────────────────────────────────────────────
        final int summaryBandBottom = 421;
        final int summaryBandHeight = 14;
        pdfFillRect(cs, 0.13f, 0.25f, 0.55f, contentLeft, summaryBandBottom,
                contentWidth, summaryBandHeight);
        pdfDrawText(cs, contentLeft + padX, summaryBandBottom + 5, "PAY SUMMARY", 9f, true, 1f, 1f, 1f);

        pdfDrawText(cs, contentLeft + padX + 2, 407, "Total Gross Pay", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 407, "PHP " + payFmt(tGross), 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, contentLeft + padX + 2, 393, "Total Deductions", 9f, false, 0f, 0f, 0f);
        pdfDrawText(cs, 390, 393, "PHP " + payFmt(dTotal), 9f, false, 0f, 0f, 0f);

        pdfDrawLine(cs, 0.13f, 0.25f, 0.55f, 1f, contentLeft, 383,
                contentRight, 383);

        pdfDrawText(cs, contentLeft + padX + 2, 366, "TOTAL NET PAY", 12f, true, 0f, 0f, 0f);
        pdfDrawText(cs, 370, 366, "PHP " + payFmt(tNet), 12f, true, 0f, 0f, 0f);

        pdfDrawLine(cs, 0.13f, 0.25f, 0.55f, 2f, contentLeft, 353,
                contentRight, 353);

        // ── Footer ─────────────────────────────────────────────────────────
        final int footerBottom = 325;
        final int footerHeight = 26;
        pdfFillRect(cs, 0.94f, 0.95f, 0.97f, contentLeft, footerBottom,
                contentWidth, footerHeight);
        String genDate = new java.text.SimpleDateFormat("MMMM d, yyyy 'at' h:mm a").format(new java.util.Date());
        pdfDrawText(cs, contentLeft + padX, footerBottom + 17,
                "This payslip is system-generated and confidential. Intended for the named employee only.",
                7.5f, false, 0.42f, 0.44f, 0.50f);
        pdfDrawText(cs, contentLeft + padX, footerBottom + 6,
                "MotorPH Payroll System  |  Generated on " + genDate,
                7.5f, false, 0.42f, 0.44f, 0.50f);

        // Outer border — 36pt margin from page top, 8pt inner padding at top & bottom
        final int borderBottom = footerBottom - 8;   // 317
        final int borderTop    = 842 - 36;            // 806
        pdfDrawRectBorder(cs, 0.13f, 0.25f, 0.55f, 1f, contentLeft, borderBottom,
                contentWidth, borderTop - borderBottom);

        byte[] csBytes = cs.toString().getBytes("ISO-8859-1");

        // Build PDF objects
        String s1 = "1 0 obj\n<</Type /Catalog /Pages 2 0 R>>\nendobj\n";
        String s2 = "2 0 obj\n<</Type /Pages /Kids [3 0 R] /Count 1>>\nendobj\n";
        String s3 = "3 0 obj\n<</Type /Page /Parent 2 0 R /MediaBox [0 0 595 842]"
                + " /Contents 4 0 R /Resources <</Font <</F1 5 0 R /F2 6 0 R>>>>>>\nendobj\n";
        String s4h = "4 0 obj\n<</Length " + csBytes.length + ">>\nstream\n";
        String s4f = "\nendstream\nendobj\n";
        String s5 = "5 0 obj\n<</Type /Font /Subtype /Type1 /BaseFont /Helvetica"
                + " /Encoding /WinAnsiEncoding>>\nendobj\n";
        String s6 = "6 0 obj\n<</Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold"
                + " /Encoding /WinAnsiEncoding>>\nendobj\n";

        byte[] hdr = "%PDF-1.4\n".getBytes("ISO-8859-1");
        byte[] b1 = s1.getBytes("ISO-8859-1");
        byte[] b2 = s2.getBytes("ISO-8859-1");
        byte[] b3 = s3.getBytes("ISO-8859-1");
        byte[] b4h = s4h.getBytes("ISO-8859-1");
        byte[] b4f = s4f.getBytes("ISO-8859-1");
        byte[] b5 = s5.getBytes("ISO-8859-1");
        byte[] b6 = s6.getBytes("ISO-8859-1");

        int[] off = new int[7];
        int pos = hdr.length;
        off[1] = pos;
        pos += b1.length;
        off[2] = pos;
        pos += b2.length;
        off[3] = pos;
        pos += b3.length;
        off[4] = pos;
        pos += b4h.length + csBytes.length + b4f.length;
        off[5] = pos;
        pos += b5.length;
        off[6] = pos;
        pos += b6.length;
        int xpos = pos;

        StringBuilder xref = new StringBuilder("xref\n0 7\n0000000000 65535 f \n");
        for (int i = 1; i <= 6; i++)
            xref.append(String.format("%010d 00000 n \n", off[i]));
        xref.append("trailer\n<</Size 7 /Root 1 0 R>>\nstartxref\n").append(xpos).append("\n%%EOF\n");

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(hdr);
        baos.write(b1);
        baos.write(b2);
        baos.write(b3);
        baos.write(b4h);
        baos.write(csBytes);
        baos.write(b4f);
        baos.write(b5);
        baos.write(b6);
        baos.write(xref.toString().getBytes("ISO-8859-1"));
        return baos.toByteArray();
    }

    private static String pdfe(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /** Computes the PDF x position to horizontally center text within a content area. */
    private static int pdfCenteredTextX(int areaLeft, int areaWidth, String text, float fontSizePt, boolean bold) {
        String safe = text == null ? "" : text;
        Font font = new Font(bold ? "Helvetica-Bold" : "Helvetica", Font.PLAIN, Math.max(1, Math.round(fontSizePt)));
        java.awt.image.BufferedImage canvas = new java.awt.image.BufferedImage(1, 1,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = canvas.createGraphics();
        g.setFont(font);
        int textWidth = g.getFontMetrics().stringWidth(safe);
        g.dispose();
        return areaLeft + Math.max(0, (areaWidth - textWidth) / 2);
    }

    private static void pdfFillRect(StringBuilder cs, float r, float g, float b,
            int x, int y, int w, int h) {
        cs.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f rg\n", r, g, b));
        cs.append(x).append(' ').append(y).append(' ').append(w).append(' ').append(h).append(" re\nf\n");
    }

    private static void pdfDrawRectBorder(StringBuilder cs, float r, float g, float b,
            float width, int x, int y, int w, int h) {
        cs.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f RG\n", r, g, b));
        cs.append(String.format(java.util.Locale.US, "%.1f w\n", width));
        cs.append(x).append(' ').append(y).append(' ').append(w).append(' ').append(h).append(" re S\n");
        cs.append("0 0 0 RG\n0.5 w\n");
    }

    private static void pdfDrawLine(StringBuilder cs, float r, float g, float b,
            float width, int x1, int y1, int x2, int y2) {
        cs.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f RG\n", r, g, b));
        cs.append(String.format(java.util.Locale.US, "%.1f w\n", width));
        cs.append(x1).append(' ').append(y1).append(" m ").append(x2).append(' ').append(y2).append(" l S\n");
        cs.append("0 0 0 RG\n0.5 w\n");
    }

    private static void pdfDrawText(StringBuilder cs, int x, int baseline, String text,
            float fontSize, boolean bold, float r, float g, float b) {
        cs.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f rg\n", r, g, b));
        cs.append("BT /").append(bold ? "F2" : "F1").append(' ')
                .append(String.format(java.util.Locale.US, "%.1f", fontSize)).append(" Tf ")
                .append(x).append(' ').append(baseline).append(" Td (")
                .append(pdfe(text)).append(") Tj ET\n");
    }

    private static void pdfDrawTextCentered(StringBuilder cs, int areaLeft, int areaWidth,
            int baseline, String text, float fontSize, boolean bold, float r, float g, float b) {
        pdfDrawText(cs, pdfCenteredTextX(areaLeft, areaWidth, text, fontSize, bold),
                baseline, text, fontSize, bold, r, g, b);
    }

    private static void pdfDrawBandTitle(StringBuilder cs, int x, int bandBottom, int bandHeight,
            String text, float fontSize) {
        int baseline = bandBottom + Math.max(4, (bandHeight - Math.round(fontSize)) / 2 + 1);
        pdfDrawText(cs, x, baseline, text, fontSize, true, 0.13f, 0.25f, 0.55f);
    }

    private static String payFmt(double v) {
        return String.format("%,.2f", v);
    }

    private static String getPayrollExportText() {
        if (txtResultArea != null) {
            String plain = txtResultArea.getText();
            if (plain != null && !plain.trim().isEmpty() && !plain.trim().startsWith("Results will appear")) {
                return plain;
            }
        }
        if (richPane != null) {
            String rich = richPane.getText();
            if (rich != null && !rich.trim().isEmpty()) {
                return rich;
            }
        }
        return "";
    }

    private static boolean hasPayrollTextOutput() {
        return !getPayrollExportText().trim().isEmpty();
    }

    private static void copyPayslipToClipboard() {
        String text = getPayrollExportText().trim();
        if (text.isEmpty()) {
            notifyPayrollAction("Nothing to copy yet — calculate payroll first.", new Color(180, 90, 40));
            return;
        }
        try {
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(text);
            java.awt.datatransfer.Clipboard clipboard =
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            notifyPayrollAction("Copied to clipboard successfully.", new Color(34, 160, 90));
        } catch (IllegalStateException ex) {
            notifyPayrollAction("Copy failed — clipboard is unavailable.", new Color(200, 60, 60));
        }
    }

    private static void notifyPayrollAction(String message, Color color) {
        if (statusToastLbl != null) {
            showToast(message, color);
            return;
        }
        int type = color.getGreen() > color.getRed()
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.WARNING_MESSAGE;
        JOptionPane.showMessageDialog(frame, message, "Payroll", type);
    }

    private static void exportPayrollTextToFile() {
        String text = getPayrollExportText().trim();
        if (text.isEmpty()) {
            showToast("Nothing to download yet — calculate payroll first.", new Color(180, 90, 40));
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Payroll Output");
        chooser.setSelectedFile(new File("Payroll_Output.txt"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text File (*.txt)", "txt"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".txt")) {
            target = new File(target.getAbsolutePath() + ".txt");
        }
        try (java.io.FileWriter writer = new java.io.FileWriter(target)) {
            writer.write(text);
            showToast("Saved: " + target.getName());
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not save file: " + ex.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
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
        loginDialog.setModal(true);
        loginDialog.setResizable(false);

        JPanel rootPanel = new JPanel(null);
        rootPanel.setBackground(APP_BG);
        rootPanel.setPreferredSize(new java.awt.Dimension(420, 520));

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
        // Vertical grid: top=35, label-h=20, gap=6, field-h=36, section-gap=20,
        // bottom=35
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(LOGIN_APP_FONT_BOLD);
        lblUser.setForeground(TEXT_DARK_NAVY);
        lblUser.setBounds(32, 35, 292, 20);

        usernameField = new JTextField();
        usernameField.setBounds(32, 61, 292, FIELD_HEIGHT);
        styleInputField(usernameField);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 8)));
        usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                        BorderFactory.createEmptyBorder(5, 9, 5, 7)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 8)));
            }
        });
        // Placeholder hint shown only when field is empty and unfocused (From Lesson:
        // FocusListener)
        attachPlaceholder(usernameField, "Enter username");

        // Password row (61 + 36 + 20 = 117)
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(LOGIN_APP_FONT_BOLD);
        lblPass.setForeground(TEXT_DARK_NAVY);
        lblPass.setBounds(32, 117, 292, 20);

        passwordField = new JPasswordField();
        passwordField.setEchoChar(PASSWORD_ECHO_CHAR);
        passwordField.setFont(LOGIN_APP_FONT_PLAIN);
        passwordField.setBackground(INPUT_BG);
        passwordField.setForeground(TEXT_DARK_NAVY);
        passwordField.setCaretColor(TEXT_DARK_NAVY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 8)));
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                        BorderFactory.createEmptyBorder(5, 9, 5, 7)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 8)));
            }
        });
        passwordField.setBounds(32, 143, 292, FIELD_HEIGHT);

        final JCheckBox chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setBounds(32, 191, 292, 22);
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
        lblDemoHint.setBounds(32, 225, 292, 32);

        btnLogin = new JButton("Sign In");
        btnLogin.setBounds(32, 273, 292, BTN_HEIGHT + 4);
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

        loginDialog.setContentPane(rootPanel);
        loginDialog.pack();
        loginDialog.setLocationRelativeTo(null);
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
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
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
        String existing = field.getText();
        boolean hasValue = existing != null && !existing.trim().isEmpty();
        if (!hasValue) {
            field.setForeground(TEXT_PLACEHOLDER_GRAY);
            field.setText(hint);
        } else {
            field.setForeground(TEXT_DARK_NAVY);
        }

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (field.getText().equals(hint)
                        && TEXT_PLACEHOLDER_GRAY.equals(field.getForeground())) {
                    field.setText("");
                    field.setForeground(TEXT_DARK_NAVY);
                }
            }
        });

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
        frame.setMinimumSize(new Dimension(1024, 680));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        String dashSubtitle;
        if (!isHrUser()) {
            String empId = MotorPH_EmployeeApp.getLinkedEmployeeId(loggedInUser);
            String empName = loggedInUser;
            if (empId != null) {
                String rawLine = FileHandlerModule.findEmployeeData(empId);
                if (rawLine != null) {
                    String[] empRow = FileHandlerModule.smartSplit(rawLine);
                    String full = EmployeeModule.fullName(empRow);
                    if (!"Unknown".equals(full))
                        empName = full;
                }
            }
            dashSubtitle = "Welcome back, " + empName + ".";
        } else {
            dashSubtitle = "Welcome back — " + getRoleDisplayName() + " Portal";
        }
        addPageHeader(
                isHrUser() ? "HR Dashboard" : "Employee Dashboard",
                dashSubtitle);

        java.awt.Rectangle bounds = getContentBounds();
        int calGap = 20;
        int contentW = bounds.width;
        // Stack calendar below cards only on very narrow windows; at min 1024px width
        // content is 752px so keep side-by-side layout the default.
        boolean stackCalendar = contentW < DASHBOARD_CAL_W + calGap + 240;
        boolean singleColumnCards = contentW < RESP_DASH_SINGLE_COL;

        int leftW = stackCalendar ? contentW : contentW - DASHBOARD_CAL_W - calGap;
        int colW = singleColumnCards ? leftW : (leftW - DASH_CARD_GAP) / 2;

        int dashRowH;
        int cardY;
        if (singleColumnCards) {
            dashRowH = Math.max(120, DASH_CARD_H);
            cardY = dashRowH * 3 + DASH_CARD_GAP * 2;
        } else {
            dashRowH = Math.max(DASH_CARD_H, (bounds.height - DASH_CARD_GAP) / 2);
            if (stackCalendar) {
                dashRowH = Math.max(DASH_CARD_H, Math.min(dashRowH, 168));
            }
            cardY = dashRowH * 2 + DASH_CARD_GAP;
        }

        JPanel cards = new JPanel(null);
        cards.setBackground(APP_BG);

        if (isHrUser()) {
            if (singleColumnCards) {
                cards.add(buildHrEmployeeRecordsCard(0, 0, colW, dashRowH));
                cards.add(buildHrPayrollCard(0, dashRowH + DASH_CARD_GAP, colW, dashRowH));
                cards.add(buildHrAnnouncementsCard(0, 2 * (dashRowH + DASH_CARD_GAP), colW, dashRowH));
            } else {
                cards.add(buildHrEmployeeRecordsCard(0, 0, colW, dashRowH));
                cards.add(buildHrPayrollCard(colW + DASH_CARD_GAP, 0, colW, dashRowH));
                cards.add(buildHrAnnouncementsCard(0, dashRowH + DASH_CARD_GAP, leftW, dashRowH));
            }
        } else {
            String linkedId = MotorPH_EmployeeApp.getLinkedEmployeeId(loggedInUser);
            String empLine = linkedId != null ? FileHandlerModule.findEmployeeData(linkedId) : null;
            String[] emp = empLine != null ? FileHandlerModule.smartSplit(empLine) : null;

            if (singleColumnCards) {
                cards.add(buildEmployeePayslipCard(0, 0, colW, dashRowH, emp));
                cards.add(buildEmployeeProfileCard(0, dashRowH + DASH_CARD_GAP, colW, dashRowH, emp));
                cards.add(buildEmployeeUpdatesCard(0, 2 * (dashRowH + DASH_CARD_GAP), colW, dashRowH));
            } else {
                cards.add(buildEmployeePayslipCard(0, 0, colW, dashRowH, emp));
                cards.add(buildEmployeeProfileCard(colW + DASH_CARD_GAP, 0, colW, dashRowH, emp));
                cards.add(buildEmployeeUpdatesCard(0, dashRowH + DASH_CARD_GAP, leftW, dashRowH));
            }
        }

        cards.setPreferredSize(new java.awt.Dimension(leftW, cardY));

        int calW = stackCalendar ? contentW : DASHBOARD_CAL_W;
        JPanel calPanel = buildCalendarPanel(calW, stackCalendar ? Math.max(cardY, 280) : cardY, CAL_MONTH, CAL_YEAR);
        int calH = calPanel.getPreferredSize().height;

        if (stackCalendar) {
            int totalH = cardY + calGap + calH;
            JPanel dashContent = new JPanel(null);
            dashContent.setBackground(APP_BG);
            dashContent.setPreferredSize(new java.awt.Dimension(contentW, totalH));
            cards.setBounds(0, 0, leftW, cardY);
            dashContent.add(cards);

            JPanel calWrapper = new JPanel(null);
            calWrapper.setBackground(PALETTE_WHITE);
            calWrapper.setBounds(0, cardY + calGap, contentW, calH);
            calWrapper.setBorder(cardBorder());
            calPanel.setBounds(0, 0, calW, calH);
            calPanel.setBorder(null);
            calWrapper.add(calPanel);
            dashContent.add(calWrapper);

            if (totalH > bounds.height) {
                JScrollPane dashScroll = new JScrollPane(dashContent,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                dashScroll.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
                dashScroll.setBorder(null);
                dashScroll.getViewport().setBackground(APP_BG);
                dashScroll.getVerticalScrollBar().setUnitIncrement(16);
                frame.add(dashScroll);
            } else {
                dashContent.setBounds(bounds.x, bounds.y, contentW, totalH);
                frame.add(dashContent);
            }
        } else {
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
        }

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

    // ─── Employee-specific dashboard card builders ─────────────────────────

    private static JPanel makeDashboardCardShell(int x, int y, int w, int h) {
        JPanel p = new JPanel(null);
        p.setBackground(PALETTE_WHITE);
        p.setBounds(x, y, w, h);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT_BLUE),
                cardBorder()));
        return p;
    }

    private static void addCardIconAndTitle(JPanel card, String icon, String title, int cardW) {
        JPanel iconCircle = new JPanel(null);
        iconCircle.setBackground(new Color(232, 242, 255));
        iconCircle.setBounds(DASH_CARD_INSET, 18, 44, 44);
        iconCircle.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 248), 1));
        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconLbl.setForeground(ACCENT_BLUE);
        iconLbl.setBounds(0, 0, 44, 44);
        iconCircle.add(iconLbl);
        card.add(iconCircle);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(TEXT_DARK_NAVY);
        titleLbl.setBounds(DASH_CARD_INSET + 56, 20, Math.max(80, cardW - DASH_CARD_INSET * 2 - 56), 22);
        card.add(titleLbl);
    }

    private static void addPreviewRow(JPanel card, String label, String value, int x, int y, int w, int rowH) {
        int lblW = 105;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBounds(x, y, lblW, rowH);
        card.add(lbl);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        val.setForeground(TEXT_DARK_NAVY);
        val.setBounds(x + lblW + 4, y, w - lblW - 4, rowH);
        card.add(val);
    }

    private static void addCardHoverAndClick(JPanel card, int cardH, ActionListener action) {
        final Color normalBg = PALETTE_WHITE;
        final Color hoverBg = PALETTE_LIGHT_BLUE;
        Runnable navigate = () -> action.actionPerformed(
                new ActionEvent(card, ActionEvent.ACTION_PERFORMED, "dashboardCard"));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getY() < cardH - DASH_CARD_FOOTER_H)
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
            if (child instanceof JButton)
                continue;
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
    }

    private static JPanel buildEmployeePayslipCard(int x, int y, int w, int h, String[] emp) {
        LocalDate today = LocalDate.now();
        int dom = today.getDayOfMonth();
        int last = today.lengthOfMonth();
        String mon = today.getMonth().name().charAt(0)
                + today.getMonth().name().substring(1, 3).toLowerCase();
        String period = dom <= 15 ? (mon + " 1 – 15") : (mon + " 16 – " + last);
        String salary = emp != null ? "PHP " + safeColumn(emp, EmployeeModule.BASIC_SALARY) : "—";
        LocalDate nextPay = dom < 15 ? today.withDayOfMonth(15)
                : dom < last ? today.withDayOfMonth(last)
                        : today.plusMonths(1).withDayOfMonth(15);
        long days = java.time.temporal.ChronoUnit.DAYS.between(today, nextPay);
        String payInfo = mon + " " + nextPay.getDayOfMonth()
                + (days == 0 ? " — Today!" : "  (" + days + " day(s) away)");

        JPanel card = makeDashboardCardShell(x, y, w, h);
        addCardIconAndTitle(card, "₱", "My Payslip", w);

        int cx = DASH_CARD_INSET, cw = w - DASH_CARD_INSET * 2, rh = 20, cy = 74;
        addPreviewRow(card, "Pay Period", period, cx, cy, cw, rh);
        cy += rh + 8;
        addPreviewRow(card, "Basic Salary", salary, cx, cy, cw, rh);
        cy += rh + 8;
        addPreviewRow(card, "Next Pay Day", payInfo, cx, cy, cw, rh);
        cy += rh;

        card.putClientProperty(DASH_SUBTITLE_BOTTOM_KEY, cy);
        addDashboardCardButton(card, w, h, "View Payslip", w - DASH_CARD_INSET * 2, true, e -> setupPayrollUI());
        addCardHoverAndClick(card, h, e -> setupPayrollUI());
        return card;
    }

    private static JPanel buildEmployeeProfileCard(int x, int y, int w, int h, String[] emp) {
        String name = emp != null ? EmployeeModule.fullName(emp) : "—";
        String position = emp != null ? safeColumn(emp, EmployeeModule.POSITION) : "—";
        String status = emp != null ? safeColumn(emp, EmployeeModule.STATUS) : "—";
        String empId = emp != null ? "ID " + safeColumn(emp, EmployeeModule.ID) : "—";

        JPanel card = makeDashboardCardShell(x, y, w, h);
        addCardIconAndTitle(card, "U", "My Profile", w);

        int cx = DASH_CARD_INSET, cw = w - DASH_CARD_INSET * 2, rh = 20, cy = 74;
        addPreviewRow(card, "Name", name, cx, cy, cw, rh);
        cy += rh + 8;
        addPreviewRow(card, "Position", position, cx, cy, cw, rh);
        cy += rh + 8;
        addPreviewRow(card, "Status", status + "  ·  " + empId, cx, cy, cw, rh);
        cy += rh;

        card.putClientProperty(DASH_SUBTITLE_BOTTOM_KEY, cy);
        addDashboardCardButton(card, w, h, "Open Profile", w - DASH_CARD_INSET * 2, true, e -> showMyProfileUI());
        addCardHoverAndClick(card, h, e -> showMyProfileUI());
        return card;
    }

    private static JPanel buildEmployeeUpdatesCard(int x, int y, int w, int h) {
        List<NotificationModule.Notification> notifs = buildSystemNotifications();

        JPanel card = makeDashboardCardShell(x, y, w, h);
        addCardIconAndTitle(card, "N", "Updates", w);

        int cx = DASH_CARD_INSET, cw = w - DASH_CARD_INSET * 2, rh = 18, cy = 74;
        int shown = 0;
        for (NotificationModule.Notification n : notifs) {
            if (shown >= 3)
                break;
            int maxChars = cw / 7;
            String line = n.text.length() > maxChars ? n.text.substring(0, maxChars - 1) + "…" : n.text;
            JLabel bullet = new JLabel("• " + line);
            bullet.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            bullet.setForeground(new Color(55, 70, 105));
            bullet.setBounds(cx, cy, cw, rh);
            card.add(bullet);
            cy += rh + 6;
            shown++;
        }
        if (shown == 0) {
            JLabel none = new JLabel("No new notifications.");
            none.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            none.setForeground(TEXT_MUTED);
            none.setBounds(cx, cy, cw, rh);
            card.add(none);
            cy += rh;
        }

        card.putClientProperty(DASH_SUBTITLE_BOTTOM_KEY, cy);
        addDashboardCardButtons(card, w, h,
                new String[] { "Notifications", "Help Center" },
                splitButtonWidths(w, 2),
                new boolean[] { true, false },
                new ActionListener[] { e -> showNotificationsUI(), e -> showHelpCenterUI() });
        addCardHoverAndClick(card, h, e -> showNotificationsUI());
        return card;
    }

    // ─── HR Dashboard card builders ──────────────────────────────────────────

    private static JPanel buildHrEmployeeRecordsCard(int x, int y, int w, int h) {
        List<String[]> all = FileHandlerModule.getAllEmployees();
        int total = all.size();
        long active = all.stream()
                .filter(e -> e.length > EmployeeModule.STATUS
                        && !e[EmployeeModule.STATUS].trim().isEmpty())
                .count();
        String sample1 = total > 0 ? EmployeeModule.fullName(all.get(0)) : "—";
        String sample2 = total > 1 ? EmployeeModule.fullName(all.get(1)) : "";

        JPanel card = makeDashboardCardShell(x, y, w, h);
        addCardIconAndTitle(card, "R", "Employee Records", w);

        int cx = DASH_CARD_INSET, cw = w - DASH_CARD_INSET * 2, rh = 20, cy = 74;
        addPreviewRow(card, "Total Employees", String.valueOf(total), cx, cy, cw, rh);
        cy += rh + 8;
        addPreviewRow(card, "With Status", String.valueOf(active), cx, cy, cw, rh);
        cy += rh + 8;
        int nameMax = (cw - 109) / 7;
        String s1 = sample1.length() > nameMax ? sample1.substring(0, nameMax - 1) + "…" : sample1;
        addPreviewRow(
                card, "Recent", s1
                        + (sample2.isEmpty() ? ""
                                : ", " + sample2.substring(0,
                                        Math.min(sample2.length(), Math.max(0, nameMax - s1.length() - 2)))),
                cx, cy, cw, rh);
        cy += rh;

        card.putClientProperty(DASH_SUBTITLE_BOTTOM_KEY, cy);
        addDashboardCardButton(card, w, h, "Manage Records", w - DASH_CARD_INSET * 2, true,
                e -> showEmployeeRecordsUI());
        addCardHoverAndClick(card, h, e -> showEmployeeRecordsUI());
        return card;
    }

    private static JPanel buildHrPayrollCard(int x, int y, int w, int h) {
        LocalDate today = LocalDate.now();
        int dom = today.getDayOfMonth(), last = today.lengthOfMonth();
        String mon = today.getMonth().name().charAt(0)
                + today.getMonth().name().substring(1, 3).toLowerCase();
        String period = dom <= 15 ? (mon + " 1–15") : (mon + " 16–" + last);
        LocalDate nextPay = dom < 15 ? today.withDayOfMonth(15)
                : dom < last ? today.withDayOfMonth(last)
                        : today.plusMonths(1).withDayOfMonth(15);
        long days = java.time.temporal.ChronoUnit.DAYS.between(today, nextPay);
        String payInfo = mon + " " + nextPay.getDayOfMonth()
                + (days == 0 ? " — Today!" : " (" + days + " day(s))");
        int empCount = FileHandlerModule.getAllEmployees().size();

        JPanel card = makeDashboardCardShell(x, y, w, h);
        addCardIconAndTitle(card, "P", "Payroll Processing", w);

        int cx = DASH_CARD_INSET, cw = w - DASH_CARD_INSET * 2, rh = 20, cy = 74;
        addPreviewRow(card, "Current Period", period, cx, cy, cw, rh);
        cy += rh + 8;
        addPreviewRow(card, "Next Pay Day", payInfo, cx, cy, cw, rh);
        cy += rh + 8;
        addPreviewRow(card, "Employees", String.valueOf(empCount) + " on file", cx, cy, cw, rh);
        cy += rh + 8;
        int openIssues = FileHandlerModule.countPayslipIssuesNeedingAction();
        addPreviewRow(card, "Payslip Reports",
                openIssues > 0 ? openIssues + " need HR review" : "No open reports", cx, cy, cw, rh);
        cy += rh;

        card.putClientProperty(DASH_SUBTITLE_BOTTOM_KEY, cy);
        addDashboardCardButtons(card, w, h,
                new String[] { "Open Payroll", "Review Reports" },
                splitButtonWidths(w, 2),
                new boolean[] { true, false },
                new ActionListener[] {
                    e -> {
                        payrollSubView = "Batch";
                        setupPayrollUI();
                    },
                    e -> {
                        payrollSubView = "Reports";
                        setupPayrollUI();
                    }
                });
        addCardHoverAndClick(card, h, e -> setupPayrollUI());
        return card;
    }

    private static JPanel buildHrAnnouncementsCard(int x, int y, int w, int h) {
        List<NotificationModule.Notification> notifs = buildSystemNotifications();

        JPanel card = makeDashboardCardShell(x, y, w, h);
        addCardIconAndTitle(card, "A", "HR Announcements", w);

        int cx = DASH_CARD_INSET, cw = w - DASH_CARD_INSET * 2, rh = 18, cy = 74;
        int shown = 0;
        for (NotificationModule.Notification n : notifs) {
            if (shown >= 3)
                break;
            int maxChars = cw / 7;
            String line = n.text.length() > maxChars ? n.text.substring(0, maxChars - 1) + "…" : n.text;
            JLabel bullet = new JLabel("• " + line);
            bullet.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            bullet.setForeground(new Color(55, 70, 105));
            bullet.setBounds(cx, cy, cw, rh);
            card.add(bullet);
            cy += rh + 6;
            shown++;
        }
        if (shown == 0) {
            JLabel none = new JLabel("No announcements at this time.");
            none.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            none.setForeground(TEXT_MUTED);
            none.setBounds(cx, cy, cw, rh);
            card.add(none);
            cy += rh;
        }

        card.putClientProperty(DASH_SUBTITLE_BOTTOM_KEY, cy);
        addDashboardCardButton(card, w, h, "Open Notifications", w - DASH_CARD_INSET * 2, false,
                e -> showNotificationsUI());
        addCardHoverAndClick(card, h, e -> showNotificationsUI());
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
        // Sidebar stops at the top of the status bar; extra 6px buffer for platform
        // differences
        int sidebarH = frame.getHeight() - ins.top - ins.bottom - STATUS_BAR_H - 6;
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
        } else {
            addSidebarNavButton(sidebar, "My Profile", 0, btnY, sw, btnH,
                    "My Profile".equals(activePage), e -> showEmployeeLookupUI());
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

        // Anchor Sign Out to a fixed slot above the sidebar bottom with extra clearance
        final int LOGOUT_BTN_H = BTN_HEIGHT;
        final int LOGOUT_BOTTOM_PAD = 12;
        int logoutBtnY = sidebarH - LOGOUT_BOTTOM_PAD - LOGOUT_BTN_H;
        int logoutSepY = logoutBtnY - 12;

        JPanel logoutSep = new JPanel();
        logoutSep.setBackground(new Color(40, 72, 140));
        logoutSep.setBounds(16, logoutSepY, sw - 32, 1);
        sidebar.add(logoutSep);

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setBounds(16, logoutBtnY, sw - 32, LOGOUT_BTN_H);
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
        addPageHeader(title, subtitle, false);
    }

    private static void addPageHeader(String title, String subtitle, boolean showHrPayrollModeToggle) {
        int contentW = getVisibleWidth() - SIDEBAR_WIDTH;
        JPanel topBar = new JPanel(null);
        topBar.setBackground(APP_BG);
        topBar.setBounds(SIDEBAR_WIDTH, 0, contentW, PAGE_HEADER_H);

        boolean isDashboard = "Dashboard".equals(currentView);
        final int backW = 76;
        final int backH = 32;
        final int backY = (PAGE_HEADER_H - backH) / 2;
        int titleX = isDashboard ? CONTENT_PAD : CONTENT_PAD + backW + 12;
        final int rightReserve = showHrPayrollModeToggle ? 292 : CONTENT_PAD;

        if (!isDashboard) {
            JButton btnBack = new JButton("< Back");
            btnBack.setBounds(CONTENT_PAD, backY, backW, backH);
            btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnBack.setFocusable(false);
            btnBack.setOpaque(true);
            btnBack.setContentAreaFilled(true);
            btnBack.setBorderPainted(true);
            btnBack.setBackground(PALETTE_WHITE);
            btnBack.setForeground(ACCENT_BLUE);
            btnBack.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
            btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnBack.addMouseListener(new MouseListener() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnBack.setBackground(new Color(235, 242, 255));
                    btnBack.setForeground(HOVER_BLUE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnBack.setBackground(PALETTE_WHITE);
                    btnBack.setForeground(ACCENT_BLUE);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                }
            });
            btnBack.addActionListener(e -> showDashboard());
            topBar.add(btnBack);
        }

        boolean hasSubtitle = subtitle != null && !subtitle.isEmpty();
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(TEXT_DARK_NAVY);
        titleLbl.setBounds(titleX, hasSubtitle ? 12 : backY + 2, contentW - titleX - rightReserve, 28);
        topBar.add(titleLbl);

        if (hasSubtitle) {
            JLabel subLbl = new JLabel(subtitle);
            subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subLbl.setForeground(new Color(72, 84, 108));
            subLbl.setBounds(titleX, 42, contentW - titleX - rightReserve, 20);
            topBar.add(subLbl);
        }

        if (showHrPayrollModeToggle) {
            addHrPayrollModeButtons(topBar, contentW);
        }

        if (!loggedInUser.isEmpty() && !isHrUser()) {
            JPanel userChip = new JPanel(null);
            userChip.setBackground(PALETTE_WHITE);
            userChip.setBounds(contentW - 248, 20, 224, 32);
            userChip.setBorder(cardBorder());
            String chipText;
            String empId = MotorPH_EmployeeApp.getLinkedEmployeeId(loggedInUser);
            String displayName = loggedInUser;
            if (empId != null) {
                String rawLine = FileHandlerModule.findEmployeeData(empId);
                if (rawLine != null) {
                    String[] empRow = FileHandlerModule.smartSplit(rawLine);
                    String fullName = EmployeeModule.fullName(empRow);
                    if (!"Unknown".equals(fullName))
                        displayName = fullName;
                }
            }
            chipText = "Signed in as  " + displayName;
            JLabel userLbl = new JLabel(chipText, SwingConstants.CENTER);
            userLbl.setFont(STATUS_FONT);
            userLbl.setForeground(TEXT_DARK_NAVY);
            userLbl.setBounds(0, 0, 224, 32);
            userChip.add(userLbl);
            topBar.add(userChip);
        }
        frame.add(topBar);
    }

    /** Single / Batch / Reports toggle embedded in the payroll page header. */
    private static void addHrPayrollModeButtons(JPanel topBar, int contentW) {
        final int btnW = 118;
        final int btnH = 32;
        final int gap = 0;
        final int switchW = btnW * 3 + gap;
        final int switchH = btnH;
        final int switchX = contentW - CONTENT_PAD - switchW;
        final int switchY = (PAGE_HEADER_H - switchH) / 2;

        JPanel modeSwitch = new JPanel(null);
        modeSwitch.setBackground(new Color(226, 232, 242));
        modeSwitch.setBounds(switchX, switchY, switchW, switchH);
        modeSwitch.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        topBar.add(modeSwitch);

        JButton btnSingle = new JButton("Single");
        btnSingle.setBounds(0, 0, btnW, switchH);
        btnSingle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSingle.setFocusable(false);
        btnSingle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stylePayrollModeButton(btnSingle, "Single".equals(payrollSubView), 0);
        btnSingle.setToolTipText("Compute payroll for a single employee");
        btnSingle.addActionListener(e -> {
            if (!"Single".equals(payrollSubView)) {
                payrollSubView = "Single";
                setupPayrollUI();
            }
        });
        modeSwitch.add(btnSingle);

        JButton btnBatch = new JButton("Batch");
        btnBatch.setBounds(btnW + gap, 0, btnW, switchH);
        btnBatch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnBatch.setFocusable(false);
        btnBatch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stylePayrollModeButton(btnBatch, "Batch".equals(payrollSubView), 1);
        btnBatch.setToolTipText("Compute payroll for multiple employees");
        btnBatch.addActionListener(e -> {
            if (!"Batch".equals(payrollSubView)) {
                payrollSubView = "Batch";
                setupPayrollUI();
            }
        });
        modeSwitch.add(btnBatch);

        int pendingReports = FileHandlerModule.countPayslipIssuesNeedingAction();
        String reportsLabel = pendingReports > 0 ? "Reports (" + pendingReports + ")" : "Reports";
        JButton btnReports = new JButton(reportsLabel);
        btnReports.setBounds((btnW + gap) * 2, 0, btnW, switchH);
        btnReports.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnReports.setFocusable(false);
        btnReports.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stylePayrollModeButton(btnReports, "Reports".equals(payrollSubView), 2);
        btnReports.setToolTipText("Review and resolve employee payslip issue reports");
        btnReports.addActionListener(e -> {
            if (!"Reports".equals(payrollSubView)) {
                payrollSubView = "Reports";
                setupPayrollUI();
            }
        });
        modeSwitch.add(btnReports);
    }

    private static void stylePayrollModeButton(JButton btn, boolean active, int position) {
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        if (active) {
            guiStyleAccentButton(btn);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        } else {
            btn.setBackground(PALETTE_WHITE);
            btn.setForeground(TEXT_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, position > 0 ? 1 : 0, 0, position < 2 ? 1 : 0,
                            CARD_BORDER_COLOR),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        }
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

        final int PAD_X = 3;
        int cellW = (width - PAD_X * 2) / 7;
        String[] dow = { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" };
        for (int c = 0; c < 7; c++) {
            JLabel l = new JLabel(dow[c], SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            l.setForeground(new Color(90, 100, 130));
            l.setOpaque(true);
            l.setBackground(new Color(245, 247, 252));
            l.setBounds(PAD_X + c * cellW, DOW_Y, cellW, DOW_H);
            l.setBorder(BorderFactory.createMatteBorder(1, c == 0 ? 1 : 0, 1, 1, new Color(230, 234, 242)));
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

        // Tracks which cell is currently selected so its highlight persists after
        // mouse-exit.
        final Color SELECTED_COLOR = new Color(188, 218, 255);
        final JPanel[] selectedCell = { null };
        final Color[] selectedCellBg = { null };

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < 7; c++) {
                int index = r * 7 + c;
                int displayDay = index - shift + 1;
                JPanel cell = new JPanel(null);
                cell.setOpaque(true);
                cell.setBounds(PAD_X + c * cellW, GRID_TOP + r * CELL_H, cellW, CELL_H);
                // Default style covers empty cells and non-today valid cells
                cell.setBackground(new Color(248, 250, 254));
                cell.setBorder(BorderFactory.createMatteBorder(
                        0, c == 0 ? 1 : 0, 1, 1, new Color(232, 236, 244)));

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
                        cellBg = ACCENT_BLUE;
                    cell.setBackground(cellBg);
                    // All valid cells use the default grid border set above

                    // Day number at the same position for every day — white+bold on today
                    JLabel lblDay = new JLabel(String.valueOf(displayDay));
                    lblDay.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 11));
                    lblDay.setForeground(isToday ? PALETTE_WHITE : new Color(25, 40, 65));
                    lblDay.setBounds(4, 3, cellW - 8, 14);
                    cell.add(lblDay);

                    int dotX = 4;
                    if (hasBirthday) {
                        JLabel dot = new JLabel("*");
                        dot.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        dot.setForeground(isToday ? PALETTE_WHITE : new Color(40, 170, 70));
                        dot.setBounds(dotX, CELL_H - 16, 12, 12);
                        cell.add(dot);
                        dotX += 12;
                    }
                    if (isPayDay) {
                        JLabel dot = new JLabel("P");
                        dot.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        dot.setForeground(isToday ? PALETTE_WHITE : ACCENT_BLUE);
                        dot.setBounds(dotX, CELL_H - 16, 12, 12);
                        cell.add(dot);
                    }
                    if (hasAttend) {
                        JLabel dot = new JLabel(String.valueOf(attendanceMap.get(displayDay).size()));
                        dot.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                        dot.setForeground(isToday ? new Color(200, 220, 255) : new Color(130, 130, 130));
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
                    // Pre-select today; keep its ACCENT_BLUE background intact.
                    if (isToday) {
                        selectedCell[0] = cell;
                        selectedCellBg[0] = normBg;
                    }
                    cell.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            // Deselect previous cell, select this one.
                            if (selectedCell[0] != null)
                                selectedCell[0].setBackground(selectedCellBg[0]);
                            selectedCell[0] = cell;
                            selectedCellBg[0] = normBg;
                            cell.setBackground(SELECTED_COLOR);
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
                            if (cell != selectedCell[0])
                                cell.setBackground(new Color(220, 235, 255));
                        }

                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            if (cell != selectedCell[0])
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
        frame.setLayout(null);
        frame.getContentPane().setBackground(new Color(212, 228, 252));

        // Branded blue header strip replaces the prior plain title label
        addColoredHeaderStrip("MAIN MENU", getVisibleWidth());
        setBreadcrumb("Main Menu");

        int mw = getVisibleWidth();
        int menuPanelX = (mw - 400) / 2;
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.white);
        menuPanel.setLayout(null);
        menuPanel.setBounds(menuPanelX, 70, 400, 380);
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
        addLoggedInFooter(getVisibleWidth(), getVisibleHeight() - 20, 770);
        updateDisplay();
    }

    private static void setupHrPayrollWithSubMenu() {
        java.awt.Rectangle bounds = getContentBounds();
        int panelW = bounds.width;
        int panelH = bounds.height;

        JPanel outerPanel = new JPanel(null);
        outerPanel.setBackground(PALETTE_WHITE);
        outerPanel.setBounds(bounds.x, bounds.y, panelW, panelH);
        outerPanel.setBorder(cardBorder());

        if ("Single".equals(payrollSubView)) {
            setupHrSinglePayrollContent(outerPanel, panelW, panelH);
        } else if ("Reports".equals(payrollSubView)) {
            setupHrPayslipReportsContent(outerPanel, panelW, panelH);
        } else {
            setupHrBulkPayrollContent(outerPanel, panelW, panelH);
        }

        frame.add(outerPanel);
    }

    private static String[] payrollStatusFilterOptions() {
        java.util.LinkedHashSet<String> statuses = new java.util.LinkedHashSet<>();
        statuses.add("All Statuses");
        statuses.add("Regular");
        statuses.add("Probationary");
        for (String[] emp : FileHandlerModule.getAllEmployees()) {
            String status = safeColumn(emp, EmployeeModule.STATUS).trim();
            if (!status.isEmpty()) {
                statuses.add(status);
            }
        }
        return statuses.toArray(new String[0]);
    }

    private static void updatePayrollFilterIndicators() {
        String deptF = cmbPayrollDeptFilter != null
                ? String.valueOf(cmbPayrollDeptFilter.getSelectedItem()) : "All Departments";
        String statusF = cmbPayrollStatusFilter != null
                ? String.valueOf(cmbPayrollStatusFilter.getSelectedItem()) : "All Statuses";
        boolean filtersActive = !"All Departments".equals(deptF) || !"All Statuses".equals(statusF);

        if (btnPayrollBatchFilter != null) {
            Color iconColor = filtersActive ? ACCENT_BLUE : TEXT_DARK_NAVY;
            btnPayrollBatchFilter.setIcon(new FilterFunnelIcon(iconColor, 18));
            if (filtersActive) {
                btnPayrollBatchFilter.setBackground(new Color(224, 238, 255));
                btnPayrollBatchFilter.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            } else {
                btnPayrollBatchFilter.setBackground(PALETTE_WHITE);
                btnPayrollBatchFilter.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            }
        }
    }

    private static void buildPayrollFilterDialog() {
        if (payrollFilterDialog != null) {
            payrollFilterDialog.dispose();
            payrollFilterDialog = null;
        }

        final int shellW = 340;
        final int shellH = 268;
        final int insetX = 20;
        final int insetTop = 18;
        final int insetBottom = 20;
        final int innerW = shellW - insetX * 2;
        final int innerH = shellH - insetTop - insetBottom;
        final int clearBtnW = 124;
        final int doneBtnW = 92;
        final int btnGap = 10;

        payrollFilterDialog = new JDialog(frame, "Filter Employees", false);
        JPanel shell = new JPanel(null);
        shell.setBackground(PALETTE_WHITE);
        shell.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        shell.setPreferredSize(new Dimension(shellW, shellH));

        JPanel inner = new JPanel(null);
        inner.setBackground(PALETTE_WHITE);
        inner.setBounds(insetX, insetTop, innerW, innerH);
        shell.add(inner);

        int fy = 0;

        JLabel deptLbl = createPayrollCaptionLabel("Department");
        deptLbl.setBounds(0, fy, innerW, 14);
        inner.add(deptLbl);
        fy += 18;
        cmbPayrollDeptFilter.setBounds(0, fy, innerW, FIELD_HEIGHT);
        inner.add(cmbPayrollDeptFilter);
        fy += FIELD_HEIGHT + 18;

        JLabel statusLbl = createPayrollCaptionLabel("Status");
        statusLbl.setBounds(0, fy, innerW, 14);
        inner.add(statusLbl);
        fy += 18;
        cmbPayrollStatusFilter.setBounds(0, fy, innerW, FIELD_HEIGHT);
        inner.add(cmbPayrollStatusFilter);
        fy += FIELD_HEIGHT + 22;

        ActionListener applyNow = e -> {
            if (payrollEmployeeListRefresh != null) {
                payrollEmployeeListRefresh.run();
            }
            updatePayrollFilterIndicators();
        };
        cmbPayrollDeptFilter.addActionListener(applyNow);
        cmbPayrollStatusFilter.addActionListener(applyNow);

        int doneX = innerW - doneBtnW;
        int clearX = doneX - btnGap - clearBtnW;

        JButton btnClear = new JButton("Clear Filters");
        btnClear.setBounds(clearX, fy, clearBtnW, FIELD_HEIGHT);
        styleStandardButton(btnClear);
        btnClear.addActionListener(e -> {
            cmbPayrollDeptFilter.setSelectedIndex(0);
            cmbPayrollStatusFilter.setSelectedIndex(0);
            applyNow.actionPerformed(null);
        });
        inner.add(btnClear);

        JButton btnDone = new JButton("Done");
        btnDone.setBounds(doneX, fy, doneBtnW, FIELD_HEIGHT);
        guiStyleAccentButton(btnDone);
        btnDone.addActionListener(e -> payrollFilterDialog.setVisible(false));
        inner.add(btnDone);

        payrollFilterDialog.setContentPane(shell);
        payrollFilterDialog.pack();
        payrollFilterDialog.setResizable(false);
    }

    private static void showPayrollBatchFilterDialog() {
        if (payrollFilterDialog == null || btnPayrollBatchFilter == null) {
            return;
        }
        try {
            java.awt.Point anchor = btnPayrollBatchFilter.getLocationOnScreen();
            int dlgW = payrollFilterDialog.getWidth();
            int dlgH = payrollFilterDialog.getHeight();
            int x = anchor.x + btnPayrollBatchFilter.getWidth() - dlgW;
            int y = anchor.y + btnPayrollBatchFilter.getHeight() + 8;
            java.awt.Rectangle frameOnScreen = frame.getBounds();
            frameOnScreen.setLocation(frame.getLocationOnScreen());
            int edgePad = 16;
            x = Math.max(frameOnScreen.x + edgePad,
                    Math.min(x, frameOnScreen.x + frameOnScreen.width - dlgW - edgePad));
            y = Math.max(frameOnScreen.y + edgePad,
                    Math.min(y, frameOnScreen.y + frameOnScreen.height - dlgH - edgePad));
            payrollFilterDialog.setLocation(x, y);
        } catch (java.awt.IllegalComponentStateException ex) {
            payrollFilterDialog.setLocationRelativeTo(btnPayrollBatchFilter);
        }
        payrollFilterDialog.setVisible(true);
        payrollFilterDialog.toFront();
        cmbPayrollDeptFilter.requestFocusInWindow();
    }

    /**
     * HR payroll — flat layout like Employee Records: toolbars, table, results
     * strip.
     */
    private static void setupHrBulkPayrollContent(JPanel panel, int panelW, int panelH) {

        batchPayrollComputedOnce = false;
        clearBatchPayrollOutput();

        final int rightW = (int) (panelW * 0.42);
        final int leftW = panelW - rightW;

        JPanel leftPanel = new JPanel(null);
        leftPanel.setBackground(PALETTE_WHITE);
        leftPanel.setBounds(0, 0, leftW, panelH);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CARD_BORDER_COLOR));
        panel.add(leftPanel);

        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(PALETTE_WHITE);
        rightPanel.setBounds(leftW, 0, rightW, panelH);
        panel.add(rightPanel);

        final int barW = leftW - PAYROLL_PAD * 2;
        final int filterBarH = 76;
        int y = PAYROLL_PAD;

        JPanel periodBar = createRecordsStyleBar(barW, filterBarH);
        periodBar.setLocation(PAYROLL_PAD, y);
        leftPanel.add(periodBar);

        int px = 12;
        int rowY = (filterBarH - 14 - FIELD_HEIGHT) / 2; // = 13
        monthCombo = createCompactPayrollMonthCombo();
        px = addPayrollToolbarField(periodBar, px, rowY, "Month", monthCombo, 92);

        txtYear = createStyledTextField(true);
        px = addPayrollToolbarField(periodBar, px, rowY, "Year", txtYear, 72);
        txtYear.setText("2024");

        int btnW = 158;
        int btnComputeX = periodBar.getWidth() - btnW - 12;
        JButton btnComputeSalaries = new JButton("Calculate Payroll");
        btnComputeSalaries.setBounds(btnComputeX, rowY + 16, btnW, FIELD_HEIGHT);
        guiStyleAccentButton(btnComputeSalaries);
        btnComputeSalaries.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnComputeSalaries.addActionListener(e -> runComputeAllSalaries());
        periodBar.add(btnComputeSalaries);

        java.awt.event.ActionListener batchPeriodChanged = e -> scheduleBatchPayrollResultsSync();
        monthCombo.addActionListener(batchPeriodChanged);
        txtYear.addActionListener(batchPeriodChanged);

        y += filterBarH + 8;

        cmbPayrollDeptFilter = new JComboBox<>();
        cmbPayrollDeptFilter.setFont(APP_FONT_PLAIN);
        cmbPayrollDeptFilter.addItem("All Departments");
        for (String dept : DepartmentModule.allDepartments()) {
            cmbPayrollDeptFilter.addItem(dept);
        }

        cmbPayrollStatusFilter = new JComboBox<>(payrollStatusFilterOptions());
        cmbPayrollStatusFilter.setFont(APP_FONT_PLAIN);

        final int searchBarH = 56;
        JPanel searchBar = createRecordsStyleBar(barW, searchBarH);
        searchBar.setLocation(PAYROLL_PAD, y);
        leftPanel.add(searchBar);

        final int innerPad = 12;
        final int selectAllBtnW = 112;
        final int filterBtnW = 44;
        final int btnGap = 8;
        final int fieldY = (searchBarH - FIELD_HEIGHT) / 2;
        final int searchFieldW = Math.max(120,
                barW - innerPad * 2 - selectAllBtnW - filterBtnW - btnGap * 2);

        txtPayrollEmpSearch = createStyledTextField(true);
        txtPayrollEmpSearch.setBounds(innerPad, fieldY, searchFieldW, FIELD_HEIGHT);
        attachPlaceholder(txtPayrollEmpSearch, "Search by name or employee #");
        searchBar.add(txtPayrollEmpSearch);

        int selectAllX = innerPad + searchFieldW + btnGap;
        btnPayrollSelectAll = new JButton("All Employees");
        btnPayrollSelectAll.setBounds(selectAllX, fieldY, selectAllBtnW, FIELD_HEIGHT);
        btnPayrollSelectAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPayrollSelectAll.addActionListener(e -> togglePayrollSelectAll());
        searchBar.add(btnPayrollSelectAll);
        refreshPayrollSelectAllButtonState();

        btnPayrollBatchFilter = new JButton(new FilterFunnelIcon(TEXT_DARK_NAVY, 18));
        btnPayrollBatchFilter.setBounds(selectAllX + selectAllBtnW + btnGap, fieldY, filterBtnW, FIELD_HEIGHT);
        btnPayrollBatchFilter.setFocusable(false);
        btnPayrollBatchFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPayrollBatchFilter.setToolTipText("Filter by department and status");
        styleStandardButton(btnPayrollBatchFilter);
        searchBar.add(btnPayrollBatchFilter);

        y += searchBarH + 8;

        payrollSelectTableModel = new DefaultTableModel(
                new String[] { "", "Employee #", "Name", "Department", "Status" }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        payrollSelectTable = new JTable(payrollSelectTableModel);
        payrollSelectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        preparePayrollSelectTable(payrollSelectTable);

        Runnable refreshPayrollEmployeeList = () -> {
            Set<String> checkedIds = getCheckedPayrollEmployeeIds();
            payrollSelectTableModel.setRowCount(0);
            String q = getEffectiveSearchQuery(txtPayrollEmpSearch, "Name or employee #");
            String deptF = cmbPayrollDeptFilter != null
                    ? String.valueOf(cmbPayrollDeptFilter.getSelectedItem()) : "All Departments";
            String statusF = cmbPayrollStatusFilter != null
                    ? String.valueOf(cmbPayrollStatusFilter.getSelectedItem()) : "All Statuses";
            for (String[] emp : FileHandlerModule.getAllEmployees()) {
                if (!rowMatchesDepartmentFilter(emp, deptF)) {
                    continue;
                }
                if (!rowMatchesStatusFilter(emp, statusF)) {
                    continue;
                }
                String id = safeColumn(emp, EmployeeModule.ID);
                String name = EmployeeModule.fullName(emp);
                Object[] previewRow = new Object[] {
                        checkedIds.contains(id), id, name,
                        safeColumn(emp, EmployeeModule.DEPARTMENT),
                        safeColumn(emp, EmployeeModule.STATUS)
                };
                if (!rowMatchesEmployeeSearch(previewRow, emp, q)) {
                    continue;
                }
                payrollSelectTableModel.addRow(previewRow);
            }
            updatePayrollSelectionCount();
            updatePayrollFilterIndicators();
            scheduleBatchPayrollResultsSync();
        };

        payrollEmployeeListRefresh = refreshPayrollEmployeeList;
        buildPayrollFilterDialog();
        updatePayrollFilterIndicators();

        attachLiveSearchFilter(txtPayrollEmpSearch, "Search by name or employee #", refreshPayrollEmployeeList);
        btnPayrollBatchFilter.addActionListener(e -> showPayrollBatchFilterDialog());
        txtPayrollEmpSearch.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    refreshPayrollEmployeeList.run();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        payrollSelectTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE
                    || e.getType() == TableModelEvent.INSERT
                    || e.getType() == TableModelEvent.DELETE) {
                updatePayrollSelectionCount();
                if (!bulkPayrollSelectionUpdate) {
                    scheduleBatchPayrollResultsSync();
                }
            }
        });

        int tableH = Math.max(160, panelH - y - PAYROLL_PAD);
        configurePayrollSelectTableColumns(payrollSelectTable, barW);
        JScrollPane empScroll = new JScrollPane(payrollSelectTable);
        empScroll.setBounds(PAYROLL_PAD, y, barW, tableH);
        styleEmployeeRecordsScrollPane(empScroll);
        leftPanel.add(empScroll);
        y += tableH + PAYROLL_SECTION_GAP;

        refreshPayrollEmployeeList.run();
        setAllPayrollRowsChecked(true);
        refreshPayrollSelectAllButtonState();

        addPayrollOutputBlock(rightPanel, PAYROLL_PAD, rightW, panelH - PAYROLL_PAD * 2,
                "Gross Pay", "Deductions", "Net Pay");

    }

    private static void setupHrSinglePayrollContent(JPanel panel, int panelW, int panelH) {
        final int rightW = (int) (panelW * 0.60);
        final int leftW = panelW - rightW;

        JPanel leftPanel = new JPanel(null);
        leftPanel.setBackground(PALETTE_WHITE);
        leftPanel.setBounds(0, 0, leftW, panelH);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CARD_BORDER_COLOR));
        panel.add(leftPanel);

        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(PALETTE_WHITE);
        rightPanel.setBounds(leftW, 0, rightW, panelH);
        panel.add(rightPanel);

        // ── LEFT COLUMN ──────────────────────────────────────────
        final int lw = leftW - PAYROLL_PAD * 2;
        final int innerW = lw - PAYROLL_PAD * 2; // accounts for bar's own left+right padding
        final int barH = 60;
        final int cardH = 76;
        int rowY = (barH - 14 - FIELD_HEIGHT) / 2;
        int y = PAYROLL_PAD;

        // Employee # input bar
        JPanel empBar = createRecordsStyleBar(lw, barH);
        empBar.setLocation(PAYROLL_PAD, y);
        JLabel empNoLbl = createPayrollCaptionLabel("Employee #");
        empNoLbl.setBounds(PAYROLL_PAD, rowY, innerW, 14);
        empBar.add(empNoLbl);
        txtEmployeeNo = createStyledTextField(true);
        txtEmployeeNo.setBounds(PAYROLL_PAD, rowY + 16, innerW, FIELD_HEIGHT);
        empBar.add(txtEmployeeNo);
        wireEmployeeNumberField(txtEmployeeNo);
        leftPanel.add(empBar);
        y += barH + PAYROLL_SECTION_GAP;

        // txtEmployeeName is required internally by validateEmployeeNumberField
        txtEmployeeName = createStyledTextField(false);

        // Employee info card
        JPanel empCard = createRecordsStyleBar(lw, cardH);
        empCard.setLocation(PAYROLL_PAD, y);

        final int avatarSize = 44;
        JPanel avatarBox = new JPanel(null);
        avatarBox.setBackground(new Color(230, 241, 251));
        avatarBox.setBounds(PAYROLL_PAD, (cardH - avatarSize) / 2, avatarSize, avatarSize);
        avatarBox.setBorder(BorderFactory.createLineBorder(new Color(185, 212, 244), 1));
        JLabel avatarLbl = new JLabel("—");
        avatarLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatarLbl.setForeground(ACCENT_BLUE);
        avatarLbl.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLbl.setBounds(0, 0, avatarSize, avatarSize);
        avatarBox.add(avatarLbl);
        empCard.add(avatarBox);

        int cardTextX = PAYROLL_PAD + avatarSize + 10;
        int cardTextW = lw - cardTextX - PAYROLL_PAD;
        JLabel lblCardName = new JLabel("Enter an employee number");
        lblCardName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCardName.setForeground(TEXT_DARK_NAVY);
        lblCardName.setBounds(cardTextX, 18, cardTextW, 18);
        empCard.add(lblCardName);

        JLabel lblCardInfo = new JLabel(" ");
        lblCardInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCardInfo.setForeground(TEXT_MUTED);
        lblCardInfo.setBounds(cardTextX, 40, cardTextW, 14);
        empCard.add(lblCardInfo);

        leftPanel.add(empCard);
        y += cardH + PAYROLL_SECTION_GAP;

        // Employment section
        final int empInfoH = 90;
        JPanel empInfoBar = createRecordsStyleBar(lw, empInfoH);
        empInfoBar.setLocation(PAYROLL_PAD, y);

        JLabel hdrEmployment = new JLabel("Employment");
        hdrEmployment.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdrEmployment.setForeground(ACCENT_BLUE);
        hdrEmployment.setBounds(PAYROLL_PAD, 8, lw - PAYROLL_PAD * 2, 14);
        empInfoBar.add(hdrEmployment);

        JLabel valPosition = new JLabel("—");
        JLabel valSupervisor = new JLabel("—");
        JLabel valDepartment = new JLabel("—");
        addEmpDetailRow(empInfoBar, PAYROLL_PAD, 26, innerW, "Position", valPosition);
        addEmpDetailRow(empInfoBar, PAYROLL_PAD, 46, innerW, "Department", valDepartment);
        addEmpDetailRow(empInfoBar, PAYROLL_PAD, 66, innerW, "Supervisor", valSupervisor);
        leftPanel.add(empInfoBar);
        y += empInfoH + PAYROLL_SECTION_GAP;

        // Compensation section
        final int compInfoH = 148;
        JPanel compInfoBar = createRecordsStyleBar(lw, compInfoH);
        compInfoBar.setLocation(PAYROLL_PAD, y);

        JLabel hdrComp = new JLabel("Compensation");
        hdrComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdrComp.setForeground(ACCENT_BLUE);
        hdrComp.setBounds(PAYROLL_PAD, 8, lw - PAYROLL_PAD * 2, 14);
        compInfoBar.add(hdrComp);

        JLabel valBasicSalary = new JLabel("—");
        JLabel valRiceSubsidy = new JLabel("—");
        JLabel valPhoneAllowance = new JLabel("—");
        JLabel valClothingAllow = new JLabel("—");
        JLabel valGrossSemiMo = new JLabel("—");
        JLabel valHourlyRate = new JLabel("—");
        addEmpDetailRow(compInfoBar, PAYROLL_PAD, 26, innerW, "Basic Salary", valBasicSalary);
        addEmpDetailRow(compInfoBar, PAYROLL_PAD, 46, innerW, "Rice Subsidy", valRiceSubsidy);
        addEmpDetailRow(compInfoBar, PAYROLL_PAD, 66, innerW, "Phone Allowance", valPhoneAllowance);
        addEmpDetailRow(compInfoBar, PAYROLL_PAD, 86, innerW, "Clothing Allowance", valClothingAllow);
        addEmpDetailRow(compInfoBar, PAYROLL_PAD, 106, innerW, "Gross Semi-monthly", valGrossSemiMo);
        addEmpDetailRow(compInfoBar, PAYROLL_PAD, 126, innerW, "Hourly Rate", valHourlyRate);
        leftPanel.add(compInfoBar);
        y += compInfoH + PAYROLL_SECTION_GAP;

        // Wire card auto-population on key release
        Runnable refreshSinglePayrollEmployeeCard = () -> {
            String id = txtEmployeeNo.getText().trim();
            if (id.isEmpty()) {
                avatarLbl.setText("—");
                lblCardName.setText("Enter an employee number");
                lblCardInfo.setText(" ");
                valPosition.setText("—");
                valDepartment.setText("—");
                valSupervisor.setText("—");
                valBasicSalary.setText("—");
                valRiceSubsidy.setText("—");
                valPhoneAllowance.setText("—");
                valClothingAllow.setText("—");
                valGrossSemiMo.setText("—");
                valHourlyRate.setText("—");
                refreshSinglePayrollAttendancePreview();
                return;
            }
            String data = FileHandlerModule.findEmployeeData(id);
            if (data == null) {
                avatarLbl.setText("?");
                lblCardName.setText("Employee not found");
                lblCardInfo.setText(" ");
                valPosition.setText("—");
                valDepartment.setText("—");
                valSupervisor.setText("—");
                valBasicSalary.setText("—");
                valRiceSubsidy.setText("—");
                valPhoneAllowance.setText("—");
                valClothingAllow.setText("—");
                valGrossSemiMo.setText("—");
                valHourlyRate.setText("—");
                refreshSinglePayrollAttendancePreview();
                return;
            }
            String[] emp = FileHandlerModule.smartSplit(data);
            String name = EmployeeModule.fullName(emp);
            String dept = safeColumn(emp, EmployeeModule.DEPARTMENT);
            String status = safeColumn(emp, EmployeeModule.STATUS);
            String[] parts = name.trim().split("\\s+");
            String initials = parts.length >= 2
                    ? "" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)
                    : name.isEmpty() ? "?" : String.valueOf(name.charAt(0));
            avatarLbl.setText(initials.toUpperCase());
            lblCardName.setText(name);
            lblCardInfo.setText(dept + "  ·  " + status);
            valPosition.setText(safeColumn(emp, EmployeeModule.POSITION));
            valDepartment.setText(dept);
            valSupervisor.setText(safeColumn(emp, EmployeeModule.IMMEDIATE_SUPERVISOR));
            valBasicSalary.setText(fmtPhp(safeColumn(emp, EmployeeModule.BASIC_SALARY)));
            valRiceSubsidy.setText(fmtPhp(safeColumn(emp, EmployeeModule.RICE_SUBSIDY)));
            valPhoneAllowance.setText(fmtPhp(safeColumn(emp, EmployeeModule.PHONE_ALLOWANCE)));
            valClothingAllow.setText(fmtPhp(safeColumn(emp, EmployeeModule.CLOTHING_ALLOWANCE)));
            double payBasic = 0;
            try { payBasic = Double.parseDouble(safeColumn(emp, EmployeeModule.BASIC_SALARY).replace(",", "").trim()); } catch (NumberFormatException ignored) {}
            double payGross  = payBasic / 2.0;
            double payHourly = payGross  * 2.0 / 168.0;
            valGrossSemiMo.setText(String.format("PHP %,.2f", payGross));
            valHourlyRate.setText(String.format("PHP %.2f", payHourly));
            refreshSinglePayrollAttendancePreview();
        };
        txtEmployeeNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshSinglePayrollEmployeeCard.run();
            }
        });

        // Pay period bar
        JPanel periodBar = createRecordsStyleBar(lw, barH);
        periodBar.setLocation(PAYROLL_PAD, y);
        int monthW = (int) (innerW * 0.62);
        int yearX = PAYROLL_PAD + monthW + 8;
        int yearW = innerW - monthW - 8;
        JLabel monthLbl = createPayrollCaptionLabel("Month");
        monthLbl.setBounds(PAYROLL_PAD, rowY, monthW, 14);
        periodBar.add(monthLbl);
        monthCombo = createPayrollMonthCombo();
        monthCombo.setBounds(PAYROLL_PAD, rowY + 16, monthW, FIELD_HEIGHT);
        periodBar.add(monthCombo);
        JLabel yearLbl = createPayrollCaptionLabel("Year");
        yearLbl.setBounds(yearX, rowY, yearW, 14);
        periodBar.add(yearLbl);
        txtYear = createStyledTextField(true);
        txtYear.setBounds(yearX, rowY + 16, yearW, FIELD_HEIGHT);
        txtYear.setText("2024");
        periodBar.add(txtYear);
        leftPanel.add(periodBar);
        y += barH + PAYROLL_SECTION_GAP;

        // Calculate Payroll button — full width
        JButton btnProcess = new JButton("Calculate Payroll");
        btnProcess.setBounds(PAYROLL_PAD, y, lw, 40);
        guiStyleAccentButton(btnProcess);
        btnProcess.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnProcess.addActionListener(e -> runPayrollCalculation());
        leftPanel.add(btnProcess);
        y += 40 + PAYROLL_SECTION_GAP;

        monthCombo.addActionListener(e -> refreshSinglePayrollAttendancePreview());
        txtYear.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshSinglePayrollAttendancePreview();
            }
        });

        addSinglePayrollAttendancePreview(leftPanel, y, lw, panelH);
        refreshSinglePayrollAttendancePreview();

        if (pendingPayrollEmployeeId != null && !pendingPayrollEmployeeId.isEmpty()) {
            txtEmployeeNo.setText(pendingPayrollEmployeeId.trim());
            pendingPayrollEmployeeId = null;
            refreshSinglePayrollEmployeeCard.run();
        }

        // ── RIGHT COLUMN ─────────────────────────────────────────
        addPayrollOutputBlock(rightPanel, PAYROLL_PAD, rightW, panelH - PAYROLL_PAD * 2,
                "Gross Pay", "Deductions", "Net Pay");
    }

    private static void configureSinglePayrollAttTableColumns(JTable table, int viewportWidth) {
        if (table == null || table.getColumnCount() < 4 || viewportWidth <= 0) {
            return;
        }
        final int[] weights = { 34, 24, 24, 18 };
        TableColumnModel cm = table.getColumnModel();
        int assigned = 0;
        for (int i = 0; i < 3; i++) {
            int w = Math.max(52, (viewportWidth * weights[i]) / 100);
            TableColumn col = cm.getColumn(i);
            col.setMinWidth(w);
            col.setMaxWidth(w);
            col.setPreferredWidth(w);
            assigned += w;
        }
        int lastW = Math.max(48, viewportWidth - assigned);
        TableColumn lastCol = cm.getColumn(3);
        lastCol.setMinWidth(lastW);
        lastCol.setMaxWidth(lastW);
        lastCol.setPreferredWidth(lastW);
    }

    private static void addEmpDetailRow(JPanel bar, int x, int y, int w,
            String label, JLabel valueLabel) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBounds(x, y, w / 2, 18);
        bar.add(lbl);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        valueLabel.setForeground(TEXT_DARK_NAVY);
        valueLabel.setBounds(x + w / 2, y, w / 2, 18);
        bar.add(valueLabel);
    }

    private static void addSinglePayrollPreviewStat(JPanel bar, int x, int y, int w,
            String title, JLabel valueLabel) {
        JPanel chip = new JPanel(null);
        chip.setBackground(new Color(245, 249, 255));
        chip.setBounds(x, y, w, 52);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 222, 236), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTitle.setForeground(TEXT_MUTED);
        lblTitle.setBounds(8, 6, w - 16, 12);
        chip.add(lblTitle);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(TEXT_DARK_NAVY);
        valueLabel.setBounds(8, 22, w - 16, 20);
        chip.add(valueLabel);
        bar.add(chip);
    }

    private static void refreshSinglePayrollAttendancePreview() {
        if (singlePayrollAttTableModel == null) {
            return;
        }
        singlePayrollAttTableModel.setRowCount(0);
        if (lblSingleAttDays != null) {
            lblSingleAttDays.setText("—");
        }
        if (lblSingleAttHours != null) {
            lblSingleAttHours.setText("—");
        }
        if (lblSingleAttStatus == null) {
            return;
        }
        lblSingleAttStatus.setForeground(TEXT_MUTED);

        String id = txtEmployeeNo != null ? txtEmployeeNo.getText().trim() : "";
        String year = txtYear != null ? txtYear.getText().trim() : "";
        int monthIdx = monthCombo != null ? monthCombo.getSelectedIndex() : 0;

        if (id.isEmpty()) {
            lblSingleAttStatus.setText("Enter an employee number and pay period to preview attendance.");
            return;
        }
        if (monthIdx <= 0 || year.isEmpty()) {
            lblSingleAttStatus.setText("Select month and year to preview attendance for payroll.");
            return;
        }
        if (FileHandlerModule.findEmployeeData(id) == null) {
            lblSingleAttStatus.setText("Employee not found — attendance preview unavailable.");
            lblSingleAttStatus.setForeground(new Color(200, 60, 60));
            return;
        }

        int inputYear;
        try {
            inputYear = Integer.parseInt(year);
        } catch (NumberFormatException ex) {
            lblSingleAttStatus.setText("Enter a valid year to preview attendance.");
            return;
        }

        int monthNum = MONTH_NUMBERS[monthIdx];
        java.util.List<String[]> periodRows = new ArrayList<>();
        double totalHours = 0;
        for (String line : FileHandlerModule.findAttendanceData(id)) {
            String[] row = FileHandlerModule.smartSplit(line);
            if (row.length < 6) {
                continue;
            }
            String[] dateParts = row[3].trim().split("/");
            if (dateParts.length < 3) {
                continue;
            }
            try {
                int csvMonth = Integer.parseInt(dateParts[0].trim());
                int csvDay = Integer.parseInt(dateParts[1].trim());
                int csvYear = Integer.parseInt(dateParts[2].trim());
                if (csvMonth != monthNum || csvYear != inputYear) {
                    continue;
                }
                double hrs = SalaryComputationModule.calculateShift(row[4].trim(), row[5].trim());
                totalHours += hrs;
                periodRows.add(new String[] {
                        String.format("%02d/%02d/%04d", csvMonth, csvDay, csvYear),
                        row[4].trim(),
                        row[5].trim(),
                        String.format("%.2f", hrs)
                });
            } catch (NumberFormatException ignored) {
            }
        }

        periodRows.sort((a, b) -> a[0].compareTo(b[0]));
        for (String[] previewRow : periodRows) {
            singlePayrollAttTableModel.addRow(previewRow);
        }

        if (lblSingleAttDays != null) {
            lblSingleAttDays.setText(String.valueOf(periodRows.size()));
        }
        if (lblSingleAttHours != null) {
            lblSingleAttHours.setText(String.format("%.2f hrs", totalHours));
        }
        if (periodRows.isEmpty()) {
            lblSingleAttStatus.setForeground(new Color(200, 60, 60));
            lblSingleAttStatus.setText("No attendance for this pay period — payroll cannot be computed.");
        } else {
            lblSingleAttStatus.setForeground(new Color(22, 130, 70));
            lblSingleAttStatus.setText(periodRows.size() + " day(s) recorded and ready for payroll calculation.");
        }
    }

    private static void addSinglePayrollAttendancePreview(JPanel leftPanel, int y, int lw, int panelH) {
        int previewH = Math.max(160, panelH - y - PAYROLL_PAD);
        JPanel previewBar = createRecordsStyleBar(lw, previewH);
        previewBar.setLocation(PAYROLL_PAD, y);

        JLabel hdr = new JLabel("Pay Period Attendance");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdr.setForeground(ACCENT_BLUE);
        hdr.setBounds(PAYROLL_PAD, 8, lw - PAYROLL_PAD * 2, 14);
        previewBar.add(hdr);

        lblSingleAttDays = new JLabel("—");
        lblSingleAttHours = new JLabel("—");
        lblSingleAttStatus = new JLabel("Enter an employee number and pay period to preview attendance.");
        lblSingleAttStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        final int statY = 28;
        final int statW = (lw - PAYROLL_PAD * 2 - 8) / 2;
        addSinglePayrollPreviewStat(previewBar, PAYROLL_PAD, statY, statW, "Days Present", lblSingleAttDays);
        addSinglePayrollPreviewStat(previewBar, PAYROLL_PAD + statW + 8, statY, statW, "Total Hours",
                lblSingleAttHours);

        lblSingleAttStatus.setBounds(PAYROLL_PAD, statY + 58, lw - PAYROLL_PAD * 2, 14);
        previewBar.add(lblSingleAttStatus);

        singlePayrollAttTableModel = new DefaultTableModel(
                new String[] { "Date", "Time In", "Time Out", "Hours" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable attTable = new JTable(singlePayrollAttTableModel);
        applyModernTableStyle(attTable);
        attTable.setRowHeight(28);
        attTable.getTableHeader().setReorderingAllowed(false);
        attTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        final int tableInnerW = lw - PAYROLL_PAD * 2;
        final int tableY = statY + 78;
        final int btnH = 32;
        final int tableH = Math.max(80, previewH - tableY - btnH - 14);
        JScrollPane attScroll = new JScrollPane(attTable);
        attScroll.setBounds(PAYROLL_PAD, tableY, tableInnerW, tableH);
        attScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleEmployeeRecordsScrollPane(attScroll);
        attScroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                configureSinglePayrollAttTableColumns(attTable, attScroll.getViewport().getWidth());
            }
        });
        configureSinglePayrollAttTableColumns(attTable, tableInnerW);
        previewBar.add(attScroll);

        JButton btnViewAll = new JButton("View Full Attendance");
        btnViewAll.setBounds(PAYROLL_PAD, tableY + tableH + 8, 168, btnH);
        styleStandardButton(btnViewAll);
        btnViewAll.setToolTipText("Open the full attendance history for this employee");
        btnViewAll.addActionListener(e -> {
            String id = txtEmployeeNo.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Enter an employee number first.",
                        "Employee Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (FileHandlerModule.findEmployeeData(id) == null) {
                JOptionPane.showMessageDialog(frame,
                        "Employee #" + id + " was not found.",
                        "Employee Not Found", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showEmployeeAttendanceDialog(id);
        });
        previewBar.add(btnViewAll);

        leftPanel.add(previewBar);
    }

    private static String fmtPhp(String raw) {
        try {
            double val = Double.parseDouble(raw.replace(",", "").trim());
            return String.format("PHP %,.2f", val);
        } catch (Exception e) {
            return raw.isEmpty() ? "—" : raw;
        }
    }

    private static void runComputeAllSalaries() {
        if (txtResultArea == null) {
            return;
        }

        int checkedCount = countCheckedPayrollRows();
        if (checkedCount < 1) {
            JOptionPane.showMessageDialog(frame,
                    "Please select at least one employee from the filtered list before computing.",
                    "No Employees Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resetPayrollFieldBorders();
        List<String> errors = new ArrayList<>();
        String year = txtYear.getText().trim();
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
            showBulletErrorDialog(frame, errors, "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String actualMonth = String.valueOf(MONTH_NUMBERS[monthCombo.getSelectedIndex()]);
        java.util.List<String[]> selectedEmps = getCheckedPayrollEmployees();

        SalaryComputationModule.PayrollValidationResult validation = SalaryComputationModule
                .validatePayrollInputs(selectedEmps, actualMonth, year);
        if (!validation.isValid()) {
            showBulletErrorDialog(frame, validation.errors, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validation.warnings.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Some selected employees have no attendance for this period.\n"
                            + "Continue with salary computation for the selected employees?",
                    "Attendance Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
        }

        SalaryComputationModule.BulkPayrollResult result = executeBatchPayrollComputation(
                actualMonth, year, true);
        if (result.computedCount > 0) {
            batchPayrollComputedOnce = true;
            if (result.savedToFile) {
                if (employeeTableModel != null) {
                    refreshEmployeeTable();
                }
                JOptionPane.showMessageDialog(frame,
                        result.computedCount + " salary record(s) computed and saved to Employee Details CSV.\n\n"
                                + "Gross: PHP " + String.format("%,.2f", result.totalGross) + "\n"
                                + "Deductions: PHP " + String.format("%,.2f", result.totalDeductions) + "\n"
                                + "Net: PHP " + String.format("%,.2f", result.totalNet),
                        "Payroll Complete", JOptionPane.INFORMATION_MESSAGE);
                showToast(result.computedCount + " salary record(s) computed and saved.");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Payroll was computed but the CSV file could not be saved.\n\n"
                                + "Close any program using the file and try again.",
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
                showToast(result.computedCount + " record(s) computed (CSV save failed).",
                        new Color(180, 90, 40));
            }
        } else {
            clearBatchPayrollOutput();
            JOptionPane.showMessageDialog(frame,
                    "No attendance data was found for the selected employees in the chosen pay period.",
                    "No Results", JOptionPane.WARNING_MESSAGE);
            showToast("No attendance data found for the selected period.", new Color(180, 90, 40));
        }
    }

    /**
     * Runs batch payroll for checked rows in the current filtered table.
     *
     * @param saveToCsv when {@code true}, persists computed pay columns to the Employee Details CSV
     * @return bulk computation result including save status and per-employee summaries
     */
    private static SalaryComputationModule.BulkPayrollResult executeBatchPayrollComputation(
            String actualMonth, String year, boolean saveToCsv) {
        if (txtResultArea == null || payrollSelectTableModel == null) {
            return new SalaryComputationModule.BulkPayrollResult(
                    false, new ArrayList<>(), 0, 0, 0, 0, 0);
        }

        java.util.List<Integer> selectedRows = getCheckedPayrollModelRows();
        if (selectedRows.isEmpty()) {
            clearBatchPayrollOutput();
            return new SalaryComputationModule.BulkPayrollResult(
                    false, new ArrayList<>(), 0, 0, 0, 0, 0);
        }

        java.util.List<String[]> selectedEmps = getCheckedPayrollEmployees();
        rpClear();
        txtResultArea.setText("");

        SalaryComputationModule.BulkPayrollResult result = SalaryComputationModule
                .computeSelectedEmployeeSalaries(actualMonth, year, selectedEmps, txtResultArea, saveToCsv);
        lastBatchSummaries = result.summaries;

        int processed = 0;
        for (SalaryComputationModule.EmployeePayrollSummary summary : result.summaries) {
            if (summary.computed) {
                rpRenderBulkEmployeeSummary(summary);
                processed++;
            } else {
                rpRenderSkippedEmployee(summary.employeeId, summary.employeeName);
            }
        }

        if (processed > 0) {
            rpRenderBatchTotals(processed, selectedRows.size(), result.totalGross,
                    result.totalDeductions, result.totalNet);
            if (richPane != null) {
                richPane.setCaretPosition(0);
            }
            updatePayrollStatChips(result.totalGross, result.totalDeductions, result.totalNet);
        } else {
            rpSet("No attendance data was found for the selected employees in the chosen pay period.", rsWarn);
            resetPayrollStatChips();
        }

        return result;
    }

    /**
     * Routes to HR payroll (single/batch) or employee My Payslip based on {@link #isHrUser()}.
     * Entry point from sidebar, dashboard cards, and deep links such as {@link #openPayrollForEmployee}.
     */
    static void setupPayrollUI() {
        currentView = "Payroll";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("Payroll");
        if (isHrUser()) {
            String payrollSubtitle = "Reports".equals(payrollSubView)
                    ? "Review employee payslip concerns, fix payroll data, and mark reports resolved."
                    : "Set pay period, select employees, then calculate payroll.";
            addPageHeader("Payroll Processing", payrollSubtitle, true);
        } else {
            addPageHeader("My Payslip");
        }

        if (isHrUser()) {
            setupHrPayrollWithSubMenu();
            addStatusBar();
            updateDisplay();
            return;
        }

        setupEmployeePayslipContent();
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
        if (!isHrUser()) {
            showMyProfileUI();
            return;
        }
        currentView = "Directory";
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
    }

    /**
     * My Profile screen for employee portal users.
     * Auto-loads the linked employee record (10001 — Manuel Garcia III) and
     * displays
     * a structured card with read-only employment info and editable address /
     * phone.
     */
    static void showMyProfileUI() {
        currentView = "My Profile";
        frame.getContentPane().removeAll();
        frame.setLayout(null);
        frame.getContentPane().setBackground(APP_BG);

        buildAndAddSidebar("My Profile");
        addPageHeader("My Profile");

        java.awt.Rectangle bounds = getContentBounds();

        String empId = MotorPH_EmployeeApp.getLinkedEmployeeId(loggedInUser);
        String[] empData = null;
        if (empId != null) {
            String rawLine = FileHandlerModule.findEmployeeData(empId);
            if (rawLine != null)
                empData = FileHandlerModule.smartSplit(rawLine);
        }

        JPanel card = new JPanel(null);
        card.setBackground(PALETTE_WHITE);
        card.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        card.setBorder(cardBorder());

        if (empData == null) {
            JLabel err = new JLabel("Employee profile could not be loaded.");
            err.setBounds(32, 32, bounds.width - 64, 24);
            err.setFont(APP_FONT_PLAIN);
            err.setForeground(TEXT_MUTED);
            card.add(err);
            frame.add(card);
            addStatusBar();
            updateDisplay();
            return;
        }

        final String[] emp = empData;
        final String linkedId = empId;

        int padX = 32;
        int innerW = bounds.width - 16;
        int contentW = innerW - padX * 2;
        boolean singleColumn = bounds.width < RESP_PROFILE_SINGLE_COL;
        int halfW = singleColumn ? contentW : (contentW - 16) / 2;
        int col2X = padX + halfW + 16;

        JPanel inner = new JPanel(null);
        inner.setBackground(PALETTE_WHITE);

        int y = 0;

        // Profile header strip
        JPanel strip = new JPanel(null);
        strip.setBackground(SIDEBAR_BG);
        strip.setBounds(0, 0, innerW, 76);
        JLabel lblName = new JLabel(EmployeeModule.fullName(emp));
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblName.setForeground(PALETTE_WHITE);
        lblName.setBounds(padX, 14, contentW, 24);
        strip.add(lblName);
        JLabel lblIdPos = new JLabel("Employee #" + safeColumn(emp, EmployeeModule.ID)
                + "   ·   " + safeColumn(emp, EmployeeModule.POSITION));
        lblIdPos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIdPos.setForeground(new Color(175, 205, 250));
        lblIdPos.setBounds(padX, 42, contentW, 18);
        strip.add(lblIdPos);
        inner.add(strip);
        y = 76 + 20;

        // Section: Employee Information
        JLabel secInfo = new JLabel("Employee Information");
        secInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        secInfo.setForeground(ACCENT_BLUE);
        secInfo.setBounds(padX, y, contentW, 18);
        inner.add(secInfo);
        y += 26;

        String[][] infoRows = {
                { "Birthday", EmployeeRecordsModule.formatBirthdayForDisplay(safeColumn(emp, EmployeeModule.BIRTHDAY)) },
                { "Status", safeColumn(emp, EmployeeModule.STATUS) },
                { "Position", safeColumn(emp, EmployeeModule.POSITION) },
                { "Immediate Supervisor", safeColumn(emp, EmployeeModule.IMMEDIATE_SUPERVISOR) },
                { "Basic Salary", "PHP " + safeColumn(emp, EmployeeModule.BASIC_SALARY) },
                { "SSS #", safeColumn(emp, EmployeeModule.SSS) },
                { "PhilHealth #", safeColumn(emp, EmployeeModule.PHILHEALTH) },
                { "TIN #", safeColumn(emp, EmployeeModule.TIN) },
                { "Pag-IBIG #", safeColumn(emp, EmployeeModule.PAGIBIG) },
        };

        int rowY = y;
        for (int i = 0; i < infoRows.length; i++) {
            int col = singleColumn ? 0 : i % 2;
            int xPos = col == 0 ? padX : col2X;

            JLabel lbl = new JLabel(infoRows[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(TEXT_MUTED);
            lbl.setBounds(xPos, rowY, halfW, 14);
            inner.add(lbl);

            JLabel val = new JLabel(infoRows[i][1]);
            val.setFont(APP_FONT_PLAIN);
            val.setForeground(TEXT_DARK_NAVY);
            val.setBounds(xPos, rowY + 16, halfW, 20);
            inner.add(val);

            if (singleColumn || col == 1 || i == infoRows.length - 1) {
                rowY += 48;
            }
        }
        y = rowY + 8;

        // Separator
        JPanel sep0 = new JPanel();
        sep0.setBackground(CARD_BORDER_COLOR);
        sep0.setBounds(padX, y, contentW, 1);
        inner.add(sep0);
        y += 18;

        // Section: Compensation & Benefits
        JLabel secComp = new JLabel("Compensation & Benefits");
        secComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        secComp.setForeground(ACCENT_BLUE);
        secComp.setBounds(padX, y, contentW, 18);
        inner.add(secComp);
        y += 26;

        double profileBasic = 0;
        try { profileBasic = Double.parseDouble(safeColumn(emp, EmployeeModule.BASIC_SALARY).replace(",", "").trim()); } catch (NumberFormatException ignored) {}
        double profileGross  = profileBasic / 2.0;
        double profileHourly = profileGross  * 2.0 / 168.0;
        String[][] compRows = {
                { "Rice Subsidy",       "PHP " + safeColumn(emp, EmployeeModule.RICE_SUBSIDY) },
                { "Phone Allowance",    "PHP " + safeColumn(emp, EmployeeModule.PHONE_ALLOWANCE) },
                { "Clothing Allowance", "PHP " + safeColumn(emp, EmployeeModule.CLOTHING_ALLOWANCE) },
                { "Gross Semi-monthly", String.format("PHP %,.2f", profileGross) },
                { "Hourly Rate",        String.format("PHP %.2f", profileHourly) },
        };

        int compRowY = y;
        for (int i = 0; i < compRows.length; i++) {
            int col = singleColumn ? 0 : i % 2;
            int xPos = col == 0 ? padX : col2X;

            JLabel lbl = new JLabel(compRows[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(TEXT_MUTED);
            lbl.setBounds(xPos, compRowY, halfW, 14);
            inner.add(lbl);

            JLabel val = new JLabel(compRows[i][1]);
            val.setFont(APP_FONT_PLAIN);
            val.setForeground(TEXT_DARK_NAVY);
            val.setBounds(xPos, compRowY + 16, halfW, 20);
            inner.add(val);

            if (singleColumn || col == 1 || i == compRows.length - 1) {
                compRowY += 48;
            }
        }
        y = compRowY + 8;

        // Separator
        JPanel sep = new JPanel();
        sep.setBackground(CARD_BORDER_COLOR);
        sep.setBounds(padX, y, contentW, 1);
        inner.add(sep);
        y += 18;

        // Section: Contact Details (editable)
        JLabel secContact = new JLabel("Contact Details");
        secContact.setFont(new Font("Segoe UI", Font.BOLD, 12));
        secContact.setForeground(ACCENT_BLUE);
        secContact.setBounds(padX, y, 145, 18);
        inner.add(secContact);
        JLabel secNote = new JLabel("Address and phone number are editable.");
        secNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        secNote.setForeground(TEXT_MUTED);
        if (singleColumn) {
            secNote.setBounds(padX, y + 22, contentW, 16);
            y += 48;
        } else {
            secNote.setBounds(padX + 156, y + 1, contentW - 156, 16);
            y += 30;
        }
        inner.add(secNote);

        JLabel lblAddr = new JLabel("Address");
        lblAddr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAddr.setForeground(TEXT_MUTED);
        lblAddr.setBounds(padX, y, contentW, 15);
        inner.add(lblAddr);
        y += 18;

        JTextField fldAddress = new JTextField(safeColumn(emp, EmployeeModule.ADDRESS));
        fldAddress.setBounds(padX, y, contentW, FIELD_HEIGHT);
        styleInputField(fldAddress);
        inner.add(fldAddress);
        y += FIELD_HEIGHT + 14;

        JLabel lblPhone = new JLabel("Phone Number");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPhone.setForeground(TEXT_MUTED);
        lblPhone.setBounds(padX, y, halfW, 15);
        inner.add(lblPhone);
        y += 18;

        JTextField fldPhone = new JTextField(safeColumn(emp, EmployeeModule.PHONE));
        fldPhone.setBounds(padX, y, singleColumn ? contentW : halfW, FIELD_HEIGHT);
        styleInputField(fldPhone);
        inner.add(fldPhone);
        y += FIELD_HEIGHT + 24;

        // Buttons
        int btnW = 152;
        boolean stackButtons = bounds.width < 420;
        JButton btnSave = new JButton("Save Changes");
        btnSave.setBounds(padX, y, btnW, BTN_HEIGHT);
        guiStyleAccentButton(btnSave);
        inner.add(btnSave);

        JButton btnBack = new JButton("Back to Dashboard");
        if (stackButtons) {
            btnBack.setBounds(padX, y + BTN_HEIGHT + 10, btnW + 16, BTN_HEIGHT);
            y += BTN_HEIGHT * 2 + 10 + 32;
        } else {
            btnBack.setBounds(padX + btnW + 12, y, btnW + 16, BTN_HEIGHT);
            y += BTN_HEIGHT + 32;
        }
        styleStandardButton(btnBack);
        btnBack.addActionListener(e -> showDashboard());
        inner.add(btnBack);

        inner.setPreferredSize(new java.awt.Dimension(innerW, y));

        btnSave.addActionListener(e -> {
            String newAddr = fldAddress.getText().trim();
            String newPhone = fldPhone.getText().trim();
            List<String> errs = new ArrayList<>();
            if (newAddr.isEmpty())
                errs.add("Address cannot be empty.");
            if (newPhone.isEmpty())
                errs.add("Phone number cannot be empty.");
            if (!errs.isEmpty()) {
                showBulletErrorDialog(frame, errs, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            emp[EmployeeModule.ADDRESS] = newAddr;
            emp[EmployeeModule.PHONE] = newPhone;
            if (FileHandlerModule.updateEmployeeRecord(linkedId, emp)) {
                showToast("Profile updated successfully.");
            } else {
                showToast("Failed to save changes.", new Color(180, 90, 40));
            }
        });

        JScrollPane scroll = new JScrollPane(inner,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(0, 0, bounds.width, bounds.height);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(PALETTE_WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scroll);
        frame.add(card);
        addStatusBar();
        updateDisplay();
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
     * Employee Records screen (Feature 1): table with left-side Edit / Add / Delete
     * actions.
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

        FileHandlerModule.ensureEmployeeFileSchema();
        EmployeeRecordsModule.recomputeSalaryFieldsForAllEmployees();
        EmployeeRevisionModule.loadFromDisk();

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

        // No inline form or search bar — table-only layout with left-side CRUD actions
        txtRecEmpNo = null;
        txtRecLastName = null;
        txtRecFirstName = null;
        txtRecBirthday = null;
        txtRecAddress = null;
        txtRecPhone = null;
        txtRecSSS = null;
        txtRecPhilHealth = null;
        txtRecTIN = null;
        txtRecPagIBIG = null;
        txtRecStatus = null;
        txtRecPosition = null;
        txtRecSupervisor = null;
        txtRecBasicSalary = null;
        txtRecHourlyRate = null;
        txtRecSearch = null;
        cmbRecDeptFilter = null;
        cmbRecStatusFilter = null;
        lblRecFilterCount = null;
        btnRecUndo = null;
        btnRecRedo = null;
        btnRecRevert = null;
        btnRecComputePayroll = null;
        btnRecViewAttendance = null;
        lblRecFormHint = null;

        employeeTableModel = new DefaultTableModel(EmployeeRecordsModule.TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(employeeTableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setFillsViewportHeight(true);
        applyModernTableStyle(employeeTable);
        enableTableSorting(employeeTable);

        final int actionColW = 108;
        final int actionPad = 12;
        final int actionBtnH = 40;
        final int actionBtnGap = 10;
        final int actionColTotal = actionColW + actionPad * 2;

        JPanel actionCol = new JPanel(null);
        actionCol.setBackground(PALETTE_WHITE);
        actionCol.setBounds(0, 0, actionColTotal, panelH);
        actionCol.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CARD_BORDER_COLOR));

        int actionY = 16;
        btnRecView = new JButton("View");
        btnRecView.setBounds(actionPad, actionY, actionColW, actionBtnH);
        styleStandardButton(btnRecView);
        btnRecView.setEnabled(false);
        btnRecView.setToolTipText("Select an employee from the table first");
        btnRecView.addActionListener(e -> showSelectedEmployeeDetailDialog());
        actionCol.add(btnRecView);

        actionY += actionBtnH + actionBtnGap;
        btnRecUpdate = new JButton("Edit");
        btnRecUpdate.setBounds(actionPad, actionY, actionColW, actionBtnH);
        styleStandardButton(btnRecUpdate);
        btnRecUpdate.setEnabled(false);
        btnRecUpdate.setToolTipText("Select an employee from the table first");
        btnRecUpdate.addActionListener(e -> showSelectedEmployeeEditDialog());
        actionCol.add(btnRecUpdate);

        actionY += actionBtnH + actionBtnGap;
        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(actionPad, actionY, actionColW, actionBtnH);
        guiStyleAccentButton(btnAdd);
        btnAdd.setToolTipText("Create a new employee record");
        btnAdd.addActionListener(e -> showAddEmployeePopup());
        actionCol.add(btnAdd);

        actionY += actionBtnH + actionBtnGap;
        btnRecDelete = new JButton("Delete");
        btnRecDelete.setBounds(actionPad, actionY, actionColW, actionBtnH);
        styleStandardButton(btnRecDelete);
        btnRecDelete.setEnabled(false);
        btnRecDelete.setToolTipText("Select an employee from the table first");
        btnRecDelete.addActionListener(e -> runDeleteEmployeeRecord());
        actionCol.add(btnRecDelete);

        actionY += actionBtnH + actionBtnGap;
        JButton btnRevisions = new JButton("Revisions");
        btnRevisions.setBounds(actionPad, actionY, actionColW, actionBtnH);
        styleStandardButton(btnRevisions);
        btnRevisions.setToolTipText("View change history and revert when needed");
        btnRevisions.addActionListener(e -> showEmployeeRevisionHistoryDialog());
        actionCol.add(btnRevisions);
        panel.add(actionCol);

        final int tablePad = 12;
        int tableX = actionColTotal + tablePad;
        int tableW = panelW - tableX - tablePad;
        final int filterBarH = 76;
        final int filterTop = 12;

        JPanel filterBar = new JPanel(null);
        filterBar.setBackground(new Color(248, 251, 255));
        filterBar.setBounds(tableX, filterTop, tableW, filterBarH);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel lblSearch = new JLabel("Search");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSearch.setForeground(TEXT_MUTED);
        lblSearch.setBounds(12, 4, 80, 16);
        filterBar.add(lblSearch);

        txtRecSearch = createStyledTextField(true);
        txtRecSearch.setBounds(12, 22, 200, 28);
        attachPlaceholder(txtRecSearch, "Name or employee #");
        attachLiveSearchFilter(txtRecSearch, "Name or employee #", MotorPH_GUI::applyEmployeeTableFilter);
        filterBar.add(txtRecSearch);

        JLabel lblDept = new JLabel("Department");
        lblDept.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDept.setForeground(TEXT_MUTED);
        lblDept.setBounds(228, 4, 100, 16);
        filterBar.add(lblDept);

        cmbRecDeptFilter = new JComboBox<>();
        cmbRecDeptFilter.setFont(APP_FONT_PLAIN);
        cmbRecDeptFilter.setBounds(228, 22, 168, 28);
        cmbRecDeptFilter.addItem("All Departments");
        for (String dept : DepartmentModule.allDepartments()) {
            cmbRecDeptFilter.addItem(dept);
        }
        cmbRecDeptFilter.addActionListener(e -> applyEmployeeTableFilter());
        filterBar.add(cmbRecDeptFilter);

        JLabel lblStatus = new JLabel("Status");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(TEXT_MUTED);
        lblStatus.setBounds(412, 4, 80, 16);
        filterBar.add(lblStatus);

        cmbRecStatusFilter = new JComboBox<>(new String[] { "All Statuses", "Regular", "Probationary" });
        cmbRecStatusFilter.setFont(APP_FONT_PLAIN);
        cmbRecStatusFilter.setBounds(412, 22, 140, 28);
        cmbRecStatusFilter.addActionListener(e -> applyEmployeeTableFilter());
        filterBar.add(cmbRecStatusFilter);

        lblRecFilterCount = new JLabel("");
        lblRecFilterCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRecFilterCount.setForeground(TEXT_MUTED);
        lblRecFilterCount.setBounds(tableW - 220, 28, 208, 20);
        lblRecFilterCount.setHorizontalAlignment(SwingConstants.RIGHT);
        filterBar.add(lblRecFilterCount);
        panel.add(filterBar);

        configureEmployeeTableColumns(employeeTable);
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        int[] preferredWidths = { 80, 120, 120, 130, 110, 110, 110, 110 };
        for (int i = 0; i < preferredWidths.length; i++) {
            if (i < employeeTable.getColumnModel().getColumnCount()) {
                javax.swing.table.TableColumn column = employeeTable.getColumnModel().getColumn(i);
                column.setPreferredWidth(preferredWidths[i]);
                column.setMinWidth(70); // Prevents columns from becoming completely hidden
            }
        }

        employeeRecordsScrollPane = new JScrollPane(employeeTable);
        employeeRecordsScrollPane.setBounds(tableX, filterTop + filterBarH + 8, tableW,
                panelH - filterTop - filterBarH - 8 - tablePad);
        styleEmployeeRecordsScrollPane(employeeRecordsScrollPane);
        panel.add(employeeRecordsScrollPane);

        employeeTable.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            if (e.getValueIsAdjusting())
                return;
            int viewRow = employeeTable.getSelectedRow();
            if (viewRow < 0) {
                selectedEmployeeId = null;
                lastSelectedEmployeeRow = -1;
                updateEmployeeRecordActionState(false);
                return;
            }
            int modelRow = employeeTable.convertRowIndexToModel(viewRow);
            selectedEmployeeId = String.valueOf(employeeTableModel.getValueAt(modelRow, 0)).trim();
            lastSelectedEmployeeRow = viewRow;
            updateEmployeeRecordActionState(true);
        });

        employeeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && selectedEmployeeId != null) {
                    showSelectedEmployeeDetailDialog();
                }
            }
        });

        refreshEmployeeTable();
        captureRecordFormBaseline();
        // Take the session-start snapshot once (preserve it across navigations)
        if (csvOriginalSnapshot == null)
            csvOriginalSnapshot = takeCsvSnapshot();
        csvUndoStack.clear();
        csvRedoStack.clear();
        updateCsvHistoryButtonStates();
        updateEmployeeRecordActionState(false);
        frame.add(panel);
        addStatusBar();
        updateDisplay();
    }

    static void openPayrollForEmployee(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty())
            return;
        payrollSubView = "Single";
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

    private static void showSelectedEmployeeEditDialog() {
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
        showEmployeeEditPopup(FileHandlerModule.smartSplit(data));
    }

    private static javax.swing.border.Border defaultPopupFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    private static void resetPopupFieldBorder(JTextField field) {
        if (field != null) {
            field.setBorder(defaultPopupFieldBorder());
        }
    }

    private static void setPopupFieldError(JTextField field) {
        if (field != null) {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_ERROR, 2),
                    BorderFactory.createEmptyBorder(3, 7, 3, 7)));
        }
    }

    private static void attachPopupFieldErrorClear(JTextField field) {
        if (field == null || !field.isEditable()) {
            return;
        }
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                resetPopupFieldBorder(field);
            }
        });
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                resetPopupFieldBorder(field);
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                resetPopupFieldBorder(field);
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                resetPopupFieldBorder(field);
            }
        });
    }

    private static String getPopupFieldText(java.util.Map<String, JTextField> fieldMap, String label) {
        JTextField tf = fieldMap.get(label);
        return getPopupFieldTextValue(tf, null);
    }

    private static String getPopupFieldTextValue(JTextField tf, String placeholderHint) {
        if (tf == null) {
            return "";
        }
        String text = tf.getText().trim();
        if (placeholderHint != null && placeholderHint.equals(text)
                && TEXT_PLACEHOLDER_GRAY.equals(tf.getForeground())) {
            return "";
        }
        return text;
    }

    private static EmployeeRecordsModule.RecordFormData buildRecordFormFromPopup(
            java.util.Map<String, JTextField> fieldMap) {
        EmployeeRecordsModule.RecordFormData form = new EmployeeRecordsModule.RecordFormData();
        form.empNo = getPopupFieldText(fieldMap, "Employee #:");
        form.lastName = getPopupFieldText(fieldMap, "Last Name:");
        form.firstName = getPopupFieldText(fieldMap, "First Name:");
        form.birthday = getPopupFieldTextValue(fieldMap.get("Birthday:"), "MM/DD/YYYY — use calendar");
        form.address = getPopupFieldText(fieldMap, "Address:");
        form.phone = getPopupFieldText(fieldMap, "Phone:");
        form.sss = getPopupFieldText(fieldMap, "SSS #:");
        form.philHealth = getPopupFieldText(fieldMap, "PhilHealth #:");
        form.tin = getPopupFieldText(fieldMap, "TIN #:");
        form.pagIbig = getPopupFieldText(fieldMap, "Pag-IBIG #:");
        form.status = getPopupFieldText(fieldMap, "Status:");
        form.department = getPopupFieldText(fieldMap, "Department:");
        form.position = getPopupFieldText(fieldMap, "Position:");
        form.supervisor = getPopupFieldText(fieldMap, "Supervisor:");
        form.basicSalary = getPopupFieldText(fieldMap, "Basic Salary:");
        form.riceSubsidy = getPopupFieldText(fieldMap, "Rice Subsidy:");
        form.phoneAllowance = getPopupFieldText(fieldMap, "Phone Allowance:");
        form.clothingAllowance = getPopupFieldText(fieldMap, "Clothing Allowance:");
        form.grossSemiMonthly = getPopupFieldText(fieldMap, "Gross Semi-monthly:");
        form.hourlyRate = getPopupFieldText(fieldMap, "Hourly Rate:");
        EmployeeRecordsModule.sanitizeFormData(form);
        return form;
    }

    private static List<String> validateEmployeeEditPopup(java.util.Map<String, JTextField> fieldMap,
            String originalId) {
        List<String> errs = new java.util.ArrayList<>(checkCompensationFieldsBlank(fieldMap));
        errs.addAll(EmployeeRecordsModule.validateEditPopup(buildRecordFormFromPopup(fieldMap), originalId));
        return errs;
    }

    private static List<String> validateEmployeeAddPopup(java.util.Map<String, JTextField> fieldMap) {
        List<String> errs = new java.util.ArrayList<>(checkCompensationFieldsBlank(fieldMap));
        errs.addAll(EmployeeRecordsModule.validateAddPopup(buildRecordFormFromPopup(fieldMap)));
        return errs;
    }

    private static List<String> checkCompensationFieldsBlank(java.util.Map<String, JTextField> fieldMap) {
        List<String> errs = new java.util.ArrayList<>();
        String[][] checks = {
            { "Basic Salary:",       "Basic Salary" },
            { "Rice Subsidy:",       "Rice Subsidy" },
            { "Phone Allowance:",    "Phone Allowance" },
            { "Clothing Allowance:", "Clothing Allowance" },
        };
        for (String[] pair : checks) {
            JTextField tf = fieldMap.get(pair[0]);
            if (tf != null && tf.getText().trim().isEmpty()) {
                errs.add(pair[1] + " is required.");
            }
        }
        return errs;
    }

    private static void resetEditPopupFieldBorders(java.util.Map<String, JTextField> fieldMap) {
        for (JTextField tf : fieldMap.values()) {
            resetPopupFieldBorder(tf);
        }
    }

    private static void markEditPopupFieldErrors(List<String> errors,
            java.util.Map<String, JTextField> fieldMap) {
        for (String err : errors) {
            if (err.contains("Employee Number") || err.contains("Employee #")) {
                setPopupFieldError(fieldMap.get("Employee #:"));
            }
            if (err.contains("Last Name"))
                setPopupFieldError(fieldMap.get("Last Name:"));
            if (err.contains("First Name"))
                setPopupFieldError(fieldMap.get("First Name:"));
            if (err.contains("Birthday"))
                setPopupFieldError(fieldMap.get("Birthday:"));
            if (err.contains("Address"))
                setPopupFieldError(fieldMap.get("Address:"));
            if (err.contains("Phone"))
                setPopupFieldError(fieldMap.get("Phone:"));
            if (err.contains("SSS"))
                setPopupFieldError(fieldMap.get("SSS #:"));
            if (err.contains("PhilHealth"))
                setPopupFieldError(fieldMap.get("PhilHealth #:"));
            if (err.contains("TIN"))
                setPopupFieldError(fieldMap.get("TIN #:"));
            if (err.contains("Pag-IBIG"))
                setPopupFieldError(fieldMap.get("Pag-IBIG #:"));
            if (err.contains("Department"))
                setPopupFieldError(fieldMap.get("Department:"));
            if (err.contains("Position"))
                setPopupFieldError(fieldMap.get("Position:"));
            if (err.contains("Supervisor"))
                setPopupFieldError(fieldMap.get("Supervisor:"));
            if (err.contains("Basic Salary"))
                setPopupFieldError(fieldMap.get("Basic Salary:"));
            if (err.contains("Rice Subsidy"))
                setPopupFieldError(fieldMap.get("Rice Subsidy:"));
            if (err.contains("Phone Allowance"))
                setPopupFieldError(fieldMap.get("Phone Allowance:"));
            if (err.contains("Clothing Allowance"))
                setPopupFieldError(fieldMap.get("Clothing Allowance:"));
            if (err.contains("Gross Semi-monthly"))
                setPopupFieldError(fieldMap.get("Gross Semi-monthly:"));
            if (err.contains("Hourly Rate"))
                setPopupFieldError(fieldMap.get("Hourly Rate:"));
        }
    }

    /**
     * Birthday field with a visible calendar button that opens {@link #showDatePickerPopup}.
     */
    private static JTextField createBirthdayFieldWithCalendar(JPanel form, int fieldX, int fy,
            int fieldW, int rowH, String storedValue, JDialog parentDialog) {
        final int calBtnW = 34;
        final int dateFieldW = fieldW - calBtnW - 4;

        JTextField tf = new JTextField(EmployeeRecordsModule.displayBirthdayForForm(storedValue));
        tf.setFont(APP_FONT_PLAIN);
        tf.setEditable(false);
        tf.setBackground(new Color(252, 248, 235));
        tf.setForeground(TEXT_DARK_NAVY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tf.setBounds(fieldX, fy, dateFieldW, rowH);
        tf.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        tf.setToolTipText("Click the calendar icon to select birthday (MM/DD/YYYY)");
        if (tf.getText().trim().isEmpty()) {
            attachPlaceholder(tf, "MM/DD/YYYY — use calendar");
        }
        form.add(tf);

        JButton btnCalendar = new JButton(new CalendarPickerIcon(ACCENT_BLUE, 16));
        btnCalendar.setBounds(fieldX + dateFieldW + 4, fy, calBtnW, rowH);
        btnCalendar.setFocusable(false);
        btnCalendar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCalendar.setToolTipText("Open birthday calendar");
        styleStandardButton(btnCalendar);
        form.add(btnCalendar);

        Runnable openCalendar = () -> showDatePickerPopup(tf, parentDialog);
        btnCalendar.addActionListener(e -> openCalendar.run());
        tf.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openCalendar.run();
            }
        });
        return tf;
    }

    @SuppressWarnings("unchecked")
    private static JTextField addPopupComboRow(JPanel form, int pad, int labelW, int fieldX, int fieldW,
            int rowH, int fy, String label, String[] options, String selected,
            java.util.Map<String, JTextField> fieldMap, java.util.List<JTextField> fields) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(APP_FONT_PLAIN);
        lbl.setForeground(TEXT_DARK_NAVY);
        lbl.setBounds(pad, fy + 4, labelW, rowH - 4);
        form.add(lbl);
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(APP_FONT_PLAIN);
        combo.setForeground(TEXT_DARK_NAVY);
        combo.setBounds(fieldX, fy, fieldW, rowH);
        if (selected != null) {
            for (String opt : options) {
                if (opt.equalsIgnoreCase(selected.trim())) {
                    combo.setSelectedItem(opt);
                    break;
                }
            }
        }
        form.add(combo);
        JTextField proxy = new JTextField(combo.getSelectedItem() == null ? "" : combo.getSelectedItem().toString());
        combo.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                proxy.setText((String) combo.getSelectedItem());
            }
        });
        fieldMap.put(label, proxy);
        fields.add(proxy);
        return proxy;
    }

    private static void wireDepartmentPositionSupervisor(
            JComboBox<String> deptCombo,
            JComboBox<String> posCombo,
            JTextField supervisorField,
            JTextField deptProxy,
            JTextField posProxy) {
        Runnable refreshPositions = () -> {
            String dept = deptCombo.getSelectedItem() == null ? "" : deptCombo.getSelectedItem().toString();
            deptProxy.setText(dept);
            String currentPos = posProxy.getText();
            posCombo.removeAllItems();
            for (String p : DepartmentModule.positionsForDepartment(dept)) {
                posCombo.addItem(p);
            }
            boolean matched = false;
            if (currentPos != null && !currentPos.isEmpty()) {
                for (int i = 0; i < posCombo.getItemCount(); i++) {
                    if (currentPos.equalsIgnoreCase(posCombo.getItemAt(i))) {
                        posCombo.setSelectedIndex(i);
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched && posCombo.getItemCount() > 0) {
                posCombo.setSelectedIndex(0);
            }
            String pos = posCombo.getSelectedItem() == null ? "" : posCombo.getSelectedItem().toString();
            posProxy.setText(pos);
            supervisorField.setText(DepartmentModule.resolveSupervisor(dept, pos));
        };
        deptCombo.addActionListener(e -> refreshPositions.run());
        posCombo.addActionListener(e -> {
            String dept = deptProxy.getText();
            String pos = posCombo.getSelectedItem() == null ? "" : posCombo.getSelectedItem().toString();
            posProxy.setText(pos);
            supervisorField.setText(DepartmentModule.resolveSupervisor(dept, pos));
        });
        refreshPositions.run();
    }

    private static void showEmployeeRevisionHistoryDialog() {
        java.util.List<EmployeeRevisionModule.RevisionEntry> revisions = EmployeeRevisionModule.getEntries();
        JDialog dialog = new JDialog(frame, "Employee Record Revisions", true);
        dialog.setSize(800, 420);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new java.awt.BorderLayout());

        JLabel hdr = new JLabel("  Revision History — select a change to revert");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdr.setForeground(PALETTE_WHITE);
        hdr.setOpaque(true);
        hdr.setBackground(ACCENT_BLUE);
        hdr.setPreferredSize(new java.awt.Dimension(0, 40));
        dialog.add(hdr, java.awt.BorderLayout.NORTH);

        String[] cols = { "Date / Time", "Action", "Employee #", "User", "Summary" };
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (EmployeeRevisionModule.RevisionEntry entry : revisions) {
            model.addRow(new Object[] {
                    entry.formattedTime(), entry.action, entry.employeeId,
                    entry.performedBy.isEmpty() ? "—" : entry.performedBy, entry.summary
            });
        }
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applyModernTableStyle(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        int[] revPreferredWidths = {140, 80, 80, 120, 170};
        for (int i = 0; i < revPreferredWidths.length; i++) {
            if (i < table.getColumnModel().getColumnCount()) {
                javax.swing.table.TableColumn column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(revPreferredWidths[i]);
                column.setMinWidth(65); // Guardrail to prevent column layout collapse
            }
        }
        dialog.add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        JPanel south = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        south.setBackground(APP_BG);
        JButton btnRevert = new JButton("Revert Selected");
        styleStandardButton(btnRevert);
        btnRevert.setEnabled(!revisions.isEmpty());
        btnRevert.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a revision entry to revert.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            EmployeeRevisionModule.RevisionEntry entry = revisions.get(row);
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Revert employee records to the state before this change?\n\n"
                            + entry.formattedTime() + " — " + entry.summary,
                    "Confirm Revert", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                pushCsvSnapshotWithLog("REVERT", entry.employeeId, "Before revert of: " + entry.summary);
                if (EmployeeRevisionModule.revert(entry)) {
                    refreshEmployeeTable();
                    showToast("Employee records reverted successfully.");
                    JOptionPane.showMessageDialog(dialog,
                            "Employee records were restored to the selected revision.",
                            "Revert Successful", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Could not revert to the selected revision.",
                            "Revert Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "An unexpected error occurred during revert:\n" + ex.getMessage(),
                        "Revert Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton btnClose = new JButton("Close");
        guiStyleAccentButton(btnClose);
        btnClose.addActionListener(e -> dialog.dispose());
        south.add(btnRevert);
        south.add(btnClose);
        dialog.add(south, java.awt.BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static void showEmployeeEditPopup(String[] emp) {
        JDialog dialog = new JDialog(frame, "Edit Employee", true);
        dialog.setResizable(false);

        JPanel root = new JPanel(null);
        root.setBackground(APP_BG);
        root.setPreferredSize(new java.awt.Dimension(520, 580));

        // Header strip
        JPanel strip = new JPanel(null);
        strip.setBackground(ACCENT_BLUE);
        strip.setBounds(0, 0, 520, 48);
        JLabel hdrTitle = new JLabel("  Edit Employee — " + EmployeeModule.fullName(emp));
        hdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdrTitle.setForeground(PALETTE_WHITE);
        hdrTitle.setBounds(0, 12, 510, 24);
        strip.add(hdrTitle);
        root.add(strip);

        // Form fields — PAD keeps content away from the scroll pane border
        JPanel form = new JPanel(null);
        form.setBackground(PALETTE_WHITE);
        final int PAD = 14;
        int labelW = 130, fieldX = PAD + 140, fieldW = 300, rowH = 28, rowGap = 10, fy = 8;

        String[][] sections = {
                { "Personal Information" },
                { "Employee #:", safeColumn(emp, EmployeeModule.ID) },
                { "Last Name:", safeColumn(emp, EmployeeModule.LAST_NAME) },
                { "First Name:", safeColumn(emp, EmployeeModule.FIRST_NAME) },
                { "Birthday:", safeColumn(emp, EmployeeModule.BIRTHDAY) },
                { "Address:", safeColumn(emp, EmployeeModule.ADDRESS) },
                { "Phone:", safeColumn(emp, EmployeeModule.PHONE) },
                { "Government IDs" },
                { "SSS #:", safeColumn(emp, EmployeeModule.SSS) },
                { "PhilHealth #:", safeColumn(emp, EmployeeModule.PHILHEALTH) },
                { "TIN #:", safeColumn(emp, EmployeeModule.TIN) },
                { "Pag-IBIG #:", safeColumn(emp, EmployeeModule.PAGIBIG) },
                { "Employment Details" },
                { "Status:", safeColumn(emp, EmployeeModule.STATUS) },
                { "Department:", safeColumn(emp, EmployeeModule.DEPARTMENT) },
                { "Position:", safeColumn(emp, EmployeeModule.POSITION) },
                { "Supervisor:", safeColumn(emp, EmployeeModule.IMMEDIATE_SUPERVISOR) },
                { "Compensation & Allowances" },
                { "Basic Salary:", safeColumn(emp, EmployeeModule.BASIC_SALARY) },
                { "Rice Subsidy:", safeColumn(emp, EmployeeModule.RICE_SUBSIDY) },
                { "Phone Allowance:", safeColumn(emp, EmployeeModule.PHONE_ALLOWANCE) },
                { "Clothing Allowance:", safeColumn(emp, EmployeeModule.CLOTHING_ALLOWANCE) },
                { "Gross Semi-monthly:", safeColumn(emp, EmployeeModule.GROSS_SEMI_MONTHLY) },
                { "Hourly Rate:", safeColumn(emp, EmployeeModule.HOURLY_RATE) },
        };

        java.util.List<JTextField> fields = new java.util.ArrayList<>();
        java.util.Map<String, JTextField> fieldMap = new java.util.LinkedHashMap<>();
        final JComboBox<String>[] deptComboRef = new JComboBox[1];
        final JComboBox<String>[] posComboRef = new JComboBox[1];

        for (String[] row : sections) {
            if (row.length == 1) {
                JLabel sec = new JLabel(row[0]);
                sec.setFont(new Font("Segoe UI", Font.BOLD, 11));
                sec.setForeground(ACCENT_BLUE);
                sec.setBounds(PAD, fy + 4, fieldX + fieldW - PAD, 18);
                form.add(sec);
                fy += 26;
            } else {
                JLabel lbl = new JLabel(row[0]);
                lbl.setFont(APP_FONT_PLAIN);
                lbl.setForeground(TEXT_DARK_NAVY);
                lbl.setBounds(PAD, fy + 4, labelW, rowH - 4);
                form.add(lbl);
                JTextField tf;
                if ("Status:".equals(row[0])) {
                    String[] opts = { "Regular", "Probationary" };
                    @SuppressWarnings("unchecked")
                    JComboBox<String> combo = new JComboBox<>(opts);
                    combo.setFont(APP_FONT_PLAIN);
                    combo.setForeground(TEXT_DARK_NAVY);
                    combo.setBounds(fieldX, fy, fieldW, rowH);
                    String cur = row[1].trim();
                    for (String opt : opts) {
                        if (opt.equalsIgnoreCase(cur)) {
                            combo.setSelectedItem(opt);
                            break;
                        }
                    }
                    form.add(combo);
                    JTextField proxy = new JTextField((String) combo.getSelectedItem());
                    combo.addItemListener(e -> {
                        if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
                            proxy.setText((String) combo.getSelectedItem());
                    });
                    tf = proxy;
                } else if ("Department:".equals(row[0])) {
                    String dept = row[1].trim();
                    if (dept.isEmpty()) {
                        dept = DepartmentModule.inferDepartmentFromPosition(safeColumn(emp, EmployeeModule.POSITION));
                    }
                    @SuppressWarnings("unchecked")
                    JComboBox<String> combo = new JComboBox<>(
                            DepartmentModule.allDepartments().toArray(new String[0]));
                    combo.setFont(APP_FONT_PLAIN);
                    combo.setForeground(TEXT_DARK_NAVY);
                    combo.setBounds(fieldX, fy, fieldW, rowH);
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        if (dept.equalsIgnoreCase(combo.getItemAt(i).toString())) {
                            combo.setSelectedIndex(i);
                            break;
                        }
                    }
                    form.add(combo);
                    deptComboRef[0] = combo;
                    JTextField proxy = new JTextField(
                            combo.getSelectedItem() == null ? dept : combo.getSelectedItem().toString());
                    combo.addItemListener(e -> {
                        if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                            proxy.setText((String) combo.getSelectedItem());
                        }
                    });
                    tf = proxy;
                } else if ("Position:".equals(row[0])) {
                    String dept = safeColumn(emp, EmployeeModule.DEPARTMENT);
                    if (dept.isEmpty()) {
                        dept = DepartmentModule.inferDepartmentFromPosition(row[1]);
                    }
                    @SuppressWarnings("unchecked")
                    JComboBox<String> combo = new JComboBox<>(DepartmentModule.positionsForDepartment(dept));
                    combo.setFont(APP_FONT_PLAIN);
                    combo.setForeground(TEXT_DARK_NAVY);
                    combo.setBounds(fieldX, fy, fieldW, rowH);
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        if (row[1].equalsIgnoreCase(combo.getItemAt(i).toString())) {
                            combo.setSelectedIndex(i);
                            break;
                        }
                    }
                    form.add(combo);
                    posComboRef[0] = combo;
                    JTextField proxy = new JTextField(
                            combo.getSelectedItem() == null ? row[1] : combo.getSelectedItem().toString());
                    combo.addItemListener(e -> {
                        if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                            proxy.setText((String) combo.getSelectedItem());
                        }
                    });
                    tf = proxy;
                } else {
                    tf = new JTextField(row[1]);
                    tf.setFont(APP_FONT_PLAIN);
                    tf.setBackground(INPUT_BG);
                    tf.setForeground(TEXT_DARK_NAVY);
                    tf.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                    if ("Employee #:".equals(row[0]))
                        tf.setEditable(false);
                    if ("Birthday:".equals(row[0])) {
                        tf = createBirthdayFieldWithCalendar(form, fieldX, fy, fieldW, rowH, row[1], dialog);
                    } else {
                        tf.setBounds(fieldX, fy, fieldW, rowH);
                        form.add(tf);
                    }
                }
                if ("SSS #:".equals(row[0])) {
                    attachIdFormat(tf, "XX-XXXXXXX-X");
                } else if ("TIN #:".equals(row[0])) {
                    attachIdFormat(tf, "XXX-XXX-XXX-XXX");
                } else if ("PhilHealth #:".equals(row[0]) || "Pag-IBIG #:".equals(row[0])
                        || "Phone:".equals(row[0])) {
                    attachDigitsOnlyFilter(tf);
                } else if ("Basic Salary:".equals(row[0]) || "Rice Subsidy:".equals(row[0])
                        || "Phone Allowance:".equals(row[0]) || "Clothing Allowance:".equals(row[0])
                        || "Gross Semi-monthly:".equals(row[0])) {
                    attachNumericValidation(tf);
                }
                fields.add(tf);
                fieldMap.put(row[0], tf);
                fy += rowH + rowGap;
            }
        }
        for (JTextField tf : fieldMap.values()) {
            attachPopupFieldErrorClear(tf);
        }
        if (deptComboRef[0] != null && posComboRef[0] != null) {
            wireDepartmentPositionSupervisor(
                    deptComboRef[0], posComboRef[0],
                    fieldMap.get("Supervisor:"),
                    fieldMap.get("Department:"),
                    fieldMap.get("Position:"));
        }
        form.setPreferredSize(new java.awt.Dimension(fieldX + fieldW + PAD, fy + 12));

        wireGrossSemiMonthlyHourlyRate(
                fieldMap.get("Basic Salary:"),
                fieldMap.get("Gross Semi-monthly:"),
                fieldMap.get("Hourly Rate:"));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBounds(16, 60, 488, 450);
        formScroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(formScroll);

        // Buttons
        JButton btnSave = new JButton("Save Changes");
        guiStyleAccentButton(btnSave);
        btnSave.setBounds(268, 520, 124, 34);

        JButton btnCancel = new JButton("Cancel");
        styleStandardButton(btnCancel);
        btnCancel.setBounds(400, 520, 100, 34);

        btnSave.addActionListener(ev -> {
            resetEditPopupFieldBorders(fieldMap);
            List<String> validationErrors = validateEmployeeEditPopup(fieldMap, emp[EmployeeModule.ID]);
            if (!validationErrors.isEmpty()) {
                markEditPopupFieldErrors(validationErrors, fieldMap);
                showBulletErrorDialog(dialog, validationErrors,
                        "Cannot Save — Please Fix Errors", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Save changes to employee #" + emp[EmployeeModule.ID] + "?",
                    "Confirm Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;

            EmployeeRecordsModule.RecordFormData formData = buildRecordFormFromPopup(fieldMap);
            String[] updated = EmployeeRecordsModule.applyFormToRow(emp, formData);
            String computedHourly = EmployeeRecordsModule.computeHourlyRateFromGrossSemiMonthly(
                    updated[EmployeeModule.GROSS_SEMI_MONTHLY]);
            if (!computedHourly.isEmpty()) {
                updated[EmployeeModule.HOURLY_RATE] = computedHourly;
            }

            try {
                pushCsvSnapshotWithLog("UPDATE", emp[EmployeeModule.ID],
                        "Updated employee #" + emp[EmployeeModule.ID]);
                boolean ok = FileHandlerModule.updateEmployeeRecord(emp[EmployeeModule.ID], updated);
                if (ok) {
                    String savedId = updated[EmployeeModule.ID].trim();
                    refreshEmployeeTable();
                    selectEmployeeInTable(savedId);
                    selectedEmployeeId = savedId;
                    showPopupSuccessAndClose(dialog,
                            "Employee #" + savedId + " updated successfully.",
                            "Employee record for #" + savedId + " (" + EmployeeModule.fullName(updated)
                                    + ") was saved successfully.",
                            "Save Successful");
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Could not update the employee record.\n\n"
                                    + "The CSV file may be locked, missing, or unavailable.\n"
                                    + "Close any other program using the file and try again.",
                            "Save Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "An unexpected error occurred while saving changes:\n\n" + ex.getMessage(),
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(ev -> dialog.dispose());
        root.add(btnSave);
        root.add(btnCancel);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private static void showAddEmployeePopup() {
        JDialog dialog = new JDialog(frame, "Add Employee", true);
        dialog.setResizable(false);

        JPanel root = new JPanel(null);
        root.setBackground(APP_BG);
        root.setPreferredSize(new java.awt.Dimension(520, 580));

        JPanel strip = new JPanel(null);
        strip.setBackground(ACCENT_BLUE);
        strip.setBounds(0, 0, 520, 48);
        JLabel hdrTitle = new JLabel("  New Employee Record");
        hdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdrTitle.setForeground(PALETTE_WHITE);
        hdrTitle.setBounds(0, 12, 510, 24);
        strip.add(hdrTitle);
        root.add(strip);

        final int PAD = 14;
        int labelW = 130, fieldX = PAD + 140, fieldW = 300, rowH = 28, rowGap = 10, fy = 8;

        JPanel form = new JPanel(null);
        form.setBackground(PALETTE_WHITE);

        // Two-element rows create a text field; one-element rows are section headers
        String[][] sections = {
                { "Personal Information" },
                { "Employee #:", FileHandlerModule.getNextEmployeeNumber() },
                { "Last Name:", "" },
                { "First Name:", "" },
                { "Birthday:", "" },
                { "Address:", "" },
                { "Phone:", "" },
                { "Government IDs" },
                { "SSS #:", "" },
                { "PhilHealth #:", "" },
                { "TIN #:", "" },
                { "Pag-IBIG #:", "" },
                { "Employment Details" },
                { "Status:", "Regular" },
                { "Department:", DepartmentModule.DEPARTMENTS[0] },
                { "Position:", DepartmentModule.positionsForDepartment(DepartmentModule.DEPARTMENTS[0])[0] },
                { "Supervisor:", "" },
                { "Compensation & Allowances" },
                { "Basic Salary:", "" },
                { "Rice Subsidy:", "" },
                { "Phone Allowance:", "" },
                { "Clothing Allowance:", "" },
                { "Gross Semi-monthly:", "" },
                { "Hourly Rate:", "" },
        };

        java.util.List<JTextField> fields = new java.util.ArrayList<>();
        java.util.Map<String, JTextField> fieldMap = new java.util.LinkedHashMap<>();
        final JComboBox<String>[] deptComboRef = new JComboBox[1];
        final JComboBox<String>[] posComboRef = new JComboBox[1];

        for (String[] row : sections) {
            if (row.length == 1) {
                JLabel sec = new JLabel(row[0]);
                sec.setFont(new Font("Segoe UI", Font.BOLD, 11));
                sec.setForeground(ACCENT_BLUE);
                sec.setBounds(PAD, fy + 4, fieldX + fieldW - PAD, 18);
                form.add(sec);
                fy += 26;
            } else {
                JLabel lbl = new JLabel(row[0]);
                lbl.setFont(APP_FONT_PLAIN);
                lbl.setForeground(TEXT_DARK_NAVY);
                lbl.setBounds(PAD, fy + 4, labelW, rowH - 4);
                form.add(lbl);
                JTextField tf;
                if ("Status:".equals(row[0])) {
                    String[] opts = { "Regular", "Probationary" };
                    @SuppressWarnings("unchecked")
                    JComboBox<String> combo = new JComboBox<>(opts);
                    combo.setFont(APP_FONT_PLAIN);
                    combo.setForeground(TEXT_DARK_NAVY);
                    combo.setBounds(fieldX, fy, fieldW, rowH);
                    form.add(combo);
                    JTextField proxy = new JTextField("Regular");
                    combo.addItemListener(e -> {
                        if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
                            proxy.setText((String) combo.getSelectedItem());
                    });
                    tf = proxy;
                } else if ("Department:".equals(row[0])) {
                    @SuppressWarnings("unchecked")
                    JComboBox<String> combo = new JComboBox<>(
                            DepartmentModule.allDepartments().toArray(new String[0]));
                    combo.setFont(APP_FONT_PLAIN);
                    combo.setForeground(TEXT_DARK_NAVY);
                    combo.setBounds(fieldX, fy, fieldW, rowH);
                    combo.setSelectedItem(row[1]);
                    form.add(combo);
                    deptComboRef[0] = combo;
                    JTextField proxy = new JTextField(row[1]);
                    combo.addItemListener(e -> {
                        if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                            proxy.setText((String) combo.getSelectedItem());
                        }
                    });
                    tf = proxy;
                } else if ("Position:".equals(row[0])) {
                    String initDept = fieldMap.containsKey("Department:")
                            ? fieldMap.get("Department:").getText()
                            : DepartmentModule.DEPARTMENTS[0];
                    @SuppressWarnings("unchecked")
                    JComboBox<String> combo = new JComboBox<>(
                            DepartmentModule.positionsForDepartment(initDept));
                    combo.setFont(APP_FONT_PLAIN);
                    combo.setForeground(TEXT_DARK_NAVY);
                    combo.setBounds(fieldX, fy, fieldW, rowH);
                    if (combo.getItemCount() > 0)
                        combo.setSelectedIndex(0);
                    form.add(combo);
                    posComboRef[0] = combo;
                    JTextField proxy = new JTextField(
                            combo.getSelectedItem() == null ? "" : combo.getSelectedItem().toString());
                    combo.addItemListener(e -> {
                        if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                            proxy.setText((String) combo.getSelectedItem());
                        }
                    });
                    tf = proxy;
                } else {
                    tf = new JTextField(row[1]);
                    tf.setFont(APP_FONT_PLAIN);
                    tf.setBackground(INPUT_BG);
                    tf.setForeground(TEXT_DARK_NAVY);
                    tf.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                    if ("Employee #:".equals(row[0])) {
                        tf.setEditable(false);
                        tf.setBackground(new Color(235, 240, 250));
                        tf.setToolTipText("Auto-assigned next available employee number");
                    }
                    if ("Birthday:".equals(row[0])) {
                        tf = createBirthdayFieldWithCalendar(form, fieldX, fy, fieldW, rowH, row[1], dialog);
                    } else {
                        tf.setBounds(fieldX, fy, fieldW, rowH);
                        form.add(tf);
                    }
                }
                if ("SSS #:".equals(row[0])) {
                    attachIdFormat(tf, "XX-XXXXXXX-X");
                } else if ("TIN #:".equals(row[0])) {
                    attachIdFormat(tf, "XXX-XXX-XXX-XXX");
                } else if ("PhilHealth #:".equals(row[0]) || "Pag-IBIG #:".equals(row[0])
                        || "Phone:".equals(row[0])) {
                    attachDigitsOnlyFilter(tf);
                } else if ("Basic Salary:".equals(row[0]) || "Rice Subsidy:".equals(row[0])
                        || "Phone Allowance:".equals(row[0]) || "Clothing Allowance:".equals(row[0])
                        || "Gross Semi-monthly:".equals(row[0])) {
                    attachNumericValidation(tf);
                }
                fields.add(tf);
                fieldMap.put(row[0], tf);
                fy += rowH + rowGap;
            }
        }
        form.setPreferredSize(new java.awt.Dimension(fieldX + fieldW + PAD, fy + 12));

        for (JTextField tf : fieldMap.values()) {
            attachPopupFieldErrorClear(tf);
        }
        if (deptComboRef[0] != null && posComboRef[0] != null) {
            wireDepartmentPositionSupervisor(
                    deptComboRef[0], posComboRef[0],
                    fieldMap.get("Supervisor:"),
                    fieldMap.get("Department:"),
                    fieldMap.get("Position:"));
        }

        wireGrossSemiMonthlyHourlyRate(
                fieldMap.get("Basic Salary:"),
                fieldMap.get("Gross Semi-monthly:"),
                fieldMap.get("Hourly Rate:"));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBounds(16, 60, 488, 450);
        formScroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(formScroll);

        JButton btnSave = new JButton("Add Employee");
        guiStyleAccentButton(btnSave);
        btnSave.setBounds(252, 520, 140, 34);

        JButton btnCancel = new JButton("Cancel");
        styleStandardButton(btnCancel);
        btnCancel.setBounds(400, 520, 100, 34);

        btnSave.addActionListener(ev -> {
            resetEditPopupFieldBorders(fieldMap);
            List<String> validationErrors = validateEmployeeAddPopup(fieldMap);
            if (!validationErrors.isEmpty()) {
                markEditPopupFieldErrors(validationErrors, fieldMap);
                showBulletErrorDialog(dialog, validationErrors,
                        "Cannot Add Employee — Please Fix Errors", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String empId = getPopupFieldText(fieldMap, "Employee #:");
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Add new employee #" + empId + " to the records?",
                    "Confirm Add", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;

            try {
                EmployeeRecordsModule.RecordFormData formData = buildRecordFormFromPopup(fieldMap);
                String[] newRow = EmployeeRecordsModule.buildFullRowFromForm(formData);
                pushCsvSnapshotWithLog("ADD", empId, "Added employee #" + empId);
                boolean ok = FileHandlerModule.appendEmployeeRecord(FileHandlerModule.joinCsvLine(newRow));
                if (ok) {
                    refreshEmployeeTable();
                    selectEmployeeInTable(empId);
                    selectedEmployeeId = empId;
                    updateEmployeeRecordActionState(true);
                    showPopupSuccessAndClose(dialog,
                            "Employee #" + empId + " added successfully.",
                            "Employee #" + empId + " (" + EmployeeModule.fullName(newRow)
                                    + ") was added successfully.\nThe employee table has been refreshed.",
                            "Add Successful");
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Could not save the new employee record.\n\n"
                                    + "The CSV file may be locked, missing, or unavailable.\n"
                                    + "Close any other program using the file and try again.",
                            "Save Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "An unexpected error occurred while adding the employee:\n\n" + ex.getMessage(),
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(ev -> dialog.dispose());
        root.add(btnSave);
        root.add(btnCancel);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private static void showDatePickerPopup(JTextField targetField, JDialog parentDialog) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String existing = targetField.getText().trim();
        if (existing.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            try {
                String[] pts = existing.split("/");
                cal.set(Integer.parseInt(pts[2]), Integer.parseInt(pts[0]) - 1, Integer.parseInt(pts[1]));
            } catch (Exception ignored) {
            }
        }

        JDialog picker = new JDialog(parentDialog, "Select Birthday", true);
        picker.setResizable(false);

        JPanel root = new JPanel(new java.awt.BorderLayout(0, 4));
        root.setBackground(PALETTE_WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(0, 6, 8, 6));

        // Header: « ‹ [Month v] [Year v] › »
        JPanel header = new JPanel(new java.awt.BorderLayout(4, 0));
        header.setBackground(ACCENT_BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel lblMonth = new JLabel("", JLabel.CENTER);
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMonth.setForeground(PALETTE_WHITE);
        lblMonth.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        lblMonth.setToolTipText("Click to choose month");

        JLabel lblYear = new JLabel("", JLabel.CENTER);
        lblYear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblYear.setForeground(PALETTE_WHITE);
        lblYear.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        lblYear.setToolTipText("Click to choose year");

        JPanel lblPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 1));
        lblPanel.setBackground(ACCENT_BLUE);
        lblPanel.setPreferredSize(new java.awt.Dimension(170, 22));
        lblPanel.add(lblMonth);
        lblPanel.add(lblYear);

        JButton btnPY = mkCalNavBtn("«");
        JButton btnPM = mkCalNavBtn("‹");
        JButton btnNM = mkCalNavBtn("›");
        JButton btnNY = mkCalNavBtn("»");

        JPanel lft = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        lft.setBackground(ACCENT_BLUE);
        lft.add(btnPY);
        lft.add(btnPM);
        JPanel rgt = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
        rgt.setBackground(ACCENT_BLUE);
        rgt.add(btnNM);
        rgt.add(btnNY);

        header.add(lft, java.awt.BorderLayout.WEST);
        header.add(lblPanel, java.awt.BorderLayout.CENTER);
        header.add(rgt, java.awt.BorderLayout.EAST);
        root.add(header, java.awt.BorderLayout.NORTH);

        // Fixed-size grid panel so the window never resizes when switching modes
        JPanel grid = new JPanel();
        grid.setBackground(PALETTE_WHITE);
        grid.setPreferredSize(new java.awt.Dimension(254, 210));
        root.add(grid, java.awt.BorderLayout.CENTER);

        final java.util.Calendar[] navCal = { cal };
        // 0 = day view, 1 = month view, 2 = year view
        final int[] mode = { 0 };
        // First year shown in the 12-year grid
        final int[] yearBase = { cal.get(java.util.Calendar.YEAR) - 5 };

        String[] mNames = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };
        Color activeHl = new Color(185, 220, 255); // highlight color for active selector label

        Runnable[] rh = { null }; // holder so inner lambdas can call refresh
        Runnable refresh = () -> {
            grid.removeAll();
            int curM = navCal[0].get(java.util.Calendar.MONTH);
            int curY = navCal[0].get(java.util.Calendar.YEAR);

            // Show/hide month-navigation arrows (not useful in month/year selector views)
            btnPM.setVisible(mode[0] == 0);
            btnNM.setVisible(mode[0] == 0);

            if (mode[0] == 1) {
                // ── Month grid (4 rows × 3 cols) ───────────────────────────
                lblMonth.setText(mNames[curM] + " v");
                lblMonth.setForeground(activeHl);
                lblYear.setText(String.valueOf(curY));
                lblYear.setForeground(PALETTE_WHITE);
                grid.setLayout(new java.awt.GridLayout(4, 3, 5, 5));
                for (int m = 0; m < 12; m++) {
                    final int fm = m;
                    JButton btn = new JButton(mNames[m].substring(0, 3));
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    btn.setFocusable(false);
                    btn.setOpaque(true);
                    btn.setMargin(new java.awt.Insets(2, 2, 2, 2));
                    if (m == curM) {
                        btn.setBackground(ACCENT_BLUE);
                        btn.setForeground(PALETTE_WHITE);
                        btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2));
                    } else {
                        btn.setBackground(PALETTE_WHITE);
                        btn.setForeground(TEXT_DARK_NAVY);
                        btn.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
                    }
                    btn.addActionListener(ev -> {
                        navCal[0].set(java.util.Calendar.MONTH, fm);
                        mode[0] = 0;
                        rh[0].run();
                    });
                    grid.add(btn);
                }

            } else if (mode[0] == 2) {
                // ── Year grid (4 rows × 3 cols = 12 years) ─────────────────
                lblMonth.setText(mNames[curM]);
                lblMonth.setForeground(PALETTE_WHITE);
                lblYear.setText(yearBase[0] + "–" + (yearBase[0] + 11) + " v");
                lblYear.setForeground(activeHl);
                grid.setLayout(new java.awt.GridLayout(4, 3, 5, 5));
                for (int i = 0; i < 12; i++) {
                    final int fy = yearBase[0] + i;
                    JButton btn = new JButton(String.valueOf(fy));
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    btn.setFocusable(false);
                    btn.setOpaque(true);
                    btn.setMargin(new java.awt.Insets(2, 2, 2, 2));
                    if (fy == curY) {
                        btn.setBackground(ACCENT_BLUE);
                        btn.setForeground(PALETTE_WHITE);
                        btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2));
                    } else {
                        btn.setBackground(PALETTE_WHITE);
                        btn.setForeground(TEXT_DARK_NAVY);
                        btn.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
                    }
                    btn.addActionListener(ev -> {
                        navCal[0].set(java.util.Calendar.YEAR, fy);
                        mode[0] = 1; // drill into month after picking year
                        rh[0].run();
                    });
                    grid.add(btn);
                }

            } else {
                // ── Day grid (mode == 0) ────────────────────────────────────
                lblMonth.setText(mNames[curM] + " v");
                lblMonth.setForeground(PALETTE_WHITE);
                lblYear.setText(curY + " v");
                lblYear.setForeground(PALETTE_WHITE);
                grid.setLayout(new java.awt.GridLayout(0, 7, 2, 2));
                for (String dn : new String[] { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" }) {
                    JLabel lbl = new JLabel(dn, JLabel.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    lbl.setForeground(new Color(100, 115, 145));
                    grid.add(lbl);
                }
                java.util.Calendar first = (java.util.Calendar) navCal[0].clone();
                first.set(java.util.Calendar.DAY_OF_MONTH, 1);
                int startDow = first.get(java.util.Calendar.DAY_OF_WEEK) - 1;
                for (int i = 0; i < startDow; i++)
                    grid.add(new JLabel(""));

                int selM = -1, selD = -1, selY = -1;
                String curStr = targetField.getText().trim();
                if (curStr.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                    try {
                        String[] pts = curStr.split("/");
                        selM = Integer.parseInt(pts[0]) - 1;
                        selD = Integer.parseInt(pts[1]);
                        selY = Integer.parseInt(pts[2]);
                    } catch (Exception ignored) {
                    }
                }
                final int fSM = selM, fSD = selD, fSY = selY;
                java.util.Calendar today = java.util.Calendar.getInstance();
                int maxDay = navCal[0].getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

                for (int day = 1; day <= maxDay; day++) {
                    final int d = day;
                    JButton btn = new JButton(String.valueOf(d));
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    btn.setFocusable(false);
                    btn.setOpaque(true);
                    btn.setPreferredSize(new java.awt.Dimension(34, 26));
                    btn.setMargin(new java.awt.Insets(1, 1, 1, 1));
                    boolean isSel = d == fSD && curM == fSM && curY == fSY;
                    boolean isTod = d == today.get(java.util.Calendar.DAY_OF_MONTH)
                            && curM == today.get(java.util.Calendar.MONTH)
                            && curY == today.get(java.util.Calendar.YEAR);
                    if (isSel) {
                        btn.setBackground(ACCENT_BLUE);
                        btn.setForeground(PALETTE_WHITE);
                        btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 1));
                    } else if (isTod) {
                        btn.setBackground(new Color(220, 232, 255));
                        btn.setForeground(ACCENT_BLUE);
                        btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 1));
                    } else {
                        btn.setBackground(PALETTE_WHITE);
                        btn.setForeground(TEXT_DARK_NAVY);
                        btn.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
                    }
                    btn.addActionListener(ev -> {
                        targetField.setText(String.format("%02d/%02d/%04d", curM + 1, d, curY));
                        picker.dispose();
                    });
                    grid.add(btn);
                }
            }
            grid.revalidate();
            grid.repaint();
        };
        rh[0] = refresh;

        // Navigation: « / » change year (or shift the year-grid page); ‹ / › change
        // month (day mode only)
        btnPY.addActionListener(e -> {
            if (mode[0] == 2)
                yearBase[0] -= 12;
            else
                navCal[0].add(java.util.Calendar.YEAR, -1);
            refresh.run();
        });
        btnPM.addActionListener(e -> {
            navCal[0].add(java.util.Calendar.MONTH, -1);
            refresh.run();
        });
        btnNM.addActionListener(e -> {
            navCal[0].add(java.util.Calendar.MONTH, 1);
            refresh.run();
        });
        btnNY.addActionListener(e -> {
            if (mode[0] == 2)
                yearBase[0] += 12;
            else
                navCal[0].add(java.util.Calendar.YEAR, 1);
            refresh.run();
        });

        lblMonth.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mode[0] = (mode[0] == 1) ? 0 : 1;
                refresh.run();
            }
        });
        lblYear.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (mode[0] != 2) {
                    // Centre the year grid on the current year
                    int cy = navCal[0].get(java.util.Calendar.YEAR);
                    yearBase[0] = cy - 5;
                }
                mode[0] = (mode[0] == 2) ? 0 : 2;
                refresh.run();
            }
        });

        refresh.run();
        picker.setContentPane(root);
        picker.pack();
        picker.setLocationRelativeTo(parentDialog);
        picker.setVisible(true);
    }

    private static JButton mkCalNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(PALETTE_WHITE);
        btn.setBackground(ACCENT_BLUE);
        btn.setBorderPainted(false);
        btn.setFocusable(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new java.awt.Dimension(28, 22));
        btn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    private static void attachDigitsOnlyFilter(JTextField tf) {
        tf.setToolTipText("Digits and hyphens only");
        ((javax.swing.text.AbstractDocument) tf.getDocument())
                .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                    private String clean(String s) {
                        return s == null ? "" : s.replaceAll("[^0-9\\-]", "");
                    }
                    @Override
                    public void insertString(FilterBypass fb, int offset, String string,
                            javax.swing.text.AttributeSet attr)
                            throws javax.swing.text.BadLocationException {
                        fb.insertString(offset, clean(string), attr);
                    }
                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String string,
                            javax.swing.text.AttributeSet attr)
                            throws javax.swing.text.BadLocationException {
                        fb.replace(offset, length, clean(string), attr);
                    }
                });
    }

    private static void attachNumericValidation(JTextField tf) {
        tf.setToolTipText("Enter a number, or NA / 000 for zero");
        tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void check() {
                String t = tf.getText();
                boolean ok = t.isEmpty()
                        || EmployeeRecordsModule.isNaPlaceholder(t)
                        || t.matches("[0-9,\\.\\+\\-\\(\\) ]*");
                if (ok) {
                    tf.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                } else {
                    tf.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER_ERROR, 2),
                            BorderFactory.createEmptyBorder(3, 7, 3, 7)));
                }
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }
        });
    }

    /**
     * When Basic Salary is filled, Gross Semi-monthly (= basic/2) and Hourly Rate
     * (= grossSemi*2/168) are auto-computed and locked. Clearing Basic Salary unlocks
     * Gross Semi-monthly so it can be entered manually.
     * Formula: hourly = (gross semi-monthly × 2) / 168 working hours per month.
     */
    private static void wireGrossSemiMonthlyHourlyRate(JTextField grossField, JTextField hourlyField) {
        wireGrossSemiMonthlyHourlyRate(null, grossField, hourlyField);
    }

    private static void wireGrossSemiMonthlyHourlyRate(JTextField basicField,
            JTextField grossField, JTextField hourlyField) {
        if (grossField == null || hourlyField == null) {
            return;
        }
        Color lockedBg = new Color(235, 240, 250);
        hourlyField.setEditable(false);
        hourlyField.setBackground(lockedBg);
        hourlyField.setToolTipText("Auto-computed from Gross Semi-monthly");

        Runnable recomputeHourly = () -> {
            String hourly = EmployeeRecordsModule.computeHourlyRateFromGrossSemiMonthly(grossField.getText());
            hourlyField.setText(hourly);
        };
        grossField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                recomputeHourly.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                recomputeHourly.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                recomputeHourly.run();
            }
        });

        if (basicField != null) {
            Runnable syncBasic = () -> {
                String raw = basicField.getText().trim().replace(",", "");
                grossField.setEditable(false);
                grossField.setBackground(lockedBg);
                grossField.setToolTipText("Auto-computed from Basic Salary");
                if (raw.isEmpty()) {
                    grossField.setText("");
                    return;
                }
                try {
                    double basic = Double.parseDouble(raw);
                    grossField.setText(String.format("%.2f", basic / 2.0));
                } catch (NumberFormatException ignored) {}
            };
            basicField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { syncBasic.run(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { syncBasic.run(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { syncBasic.run(); }
            });
            syncBasic.run(); // fire immediately for pre-populated basic salary on edit/load
        }
        recomputeHourly.run();
    }

    private static void attachIdFormat(JTextField tf, String pattern) {
        int max = pattern.replace("-", "").length();
        boolean[] busy = { false };
        tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void fmt() {
                SwingUtilities.invokeLater(() -> {
                    if (busy[0])
                        return;
                    busy[0] = true;
                    try {
                        String raw = tf.getText().replaceAll("[^0-9]", "");
                        if (raw.length() > max)
                            raw = raw.substring(0, max);
                        String formatted = applyIdPattern(raw, pattern);
                        if (!formatted.equals(tf.getText())) {
                            tf.setText(formatted);
                            tf.setCaretPosition(formatted.length());
                        }
                    } finally {
                        busy[0] = false;
                    }
                });
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                fmt();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                fmt();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                fmt();
            }
        });
    }

    private static String applyIdPattern(String digits, String pattern) {
        StringBuilder sb = new StringBuilder();
        int di = 0;
        for (int i = 0; i < pattern.length() && di < digits.length(); i++) {
            char c = pattern.charAt(i);
            if (c == 'X')
                sb.append(digits.charAt(di++));
            else
                sb.append(c);
        }
        return sb.toString();
    }

    private static JPanel buildEmployeeProfileViewPanel(String[] emp, List<String> warnings) {
        JPanel root = new JPanel();
        root.setLayout(new java.awt.BorderLayout());
        root.setBackground(PALETTE_WHITE);

        JPanel header = new JPanel(null);
        header.setPreferredSize(new java.awt.Dimension(0, 118));
        header.setBackground(ACCENT_BLUE);

        String fullName = emp != null ? EmployeeModule.fullName(emp) : "Unknown Employee";
        String initials = fullName.trim().isEmpty() ? "?" : String.valueOf(fullName.trim().charAt(0)).toUpperCase();
        if (fullName.contains(" ")) {
            initials = "" + Character.toUpperCase(fullName.charAt(0))
                    + Character.toUpperCase(fullName.charAt(fullName.lastIndexOf(' ') + 1));
        }

        JPanel avatar = new JPanel(null);
        avatar.setBackground(PALETTE_WHITE);
        avatar.setBounds(20, 22, 56, 56);
        avatar.setBorder(BorderFactory.createLineBorder(new Color(220, 232, 255), 2));
        JLabel avatarLbl = new JLabel(initials, SwingConstants.CENTER);
        avatarLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        avatarLbl.setForeground(ACCENT_BLUE);
        avatarLbl.setBounds(0, 0, 56, 56);
        avatar.add(avatarLbl);
        header.add(avatar);

        JLabel nameLbl = new JLabel(fullName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLbl.setForeground(PALETTE_WHITE);
        nameLbl.setBounds(88, 24, 360, 26);
        header.add(nameLbl);

        String empId = safeColumn(emp, EmployeeModule.ID);
        JLabel idLbl = new JLabel("Employee #" + empId);
        idLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        idLbl.setForeground(new Color(210, 225, 255));
        idLbl.setBounds(88, 50, 160, 18);
        header.add(idLbl);

        String status = safeColumn(emp, EmployeeModule.STATUS);
        String dept = safeColumn(emp, EmployeeModule.DEPARTMENT);
        if (dept.isEmpty()) {
            dept = DepartmentModule.inferDepartmentFromPosition(safeColumn(emp, EmployeeModule.POSITION));
        }
        JLabel statusPill = new JLabel("  " + (status.isEmpty() ? "N/A" : status) + "  ", SwingConstants.CENTER);
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusPill.setForeground(ACCENT_BLUE);
        statusPill.setBackground(PALETTE_WHITE);
        statusPill.setOpaque(true);
        statusPill.setBounds(88, 74, status.isEmpty() ? 60 : 92, 22);
        header.add(statusPill);

        JLabel deptPill = new JLabel("  " + dept + "  ", SwingConstants.CENTER);
        deptPill.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deptPill.setForeground(PALETTE_WHITE);
        deptPill.setBackground(new Color(55, 90, 160));
        deptPill.setOpaque(true);
        deptPill.setBounds(190, 74, Math.min(280, Math.max(120, dept.length() * 8 + 24)), 22);
        header.add(deptPill);
        root.add(header, java.awt.BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBackground(PALETTE_WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        if (!warnings.isEmpty()) {
            JPanel warn = new JPanel(new java.awt.BorderLayout());
            warn.setBackground(new Color(255, 248, 240));
            warn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(240, 200, 160), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            JLabel warnTitle = new JLabel("Data quality notes");
            warnTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            warnTitle.setForeground(new Color(180, 90, 40));
            warn.add(warnTitle, java.awt.BorderLayout.NORTH);
            JTextArea warnBody = new JTextArea();
            for (String w : warnings)
                warnBody.append("• " + w + "\n");
            warnBody.setEditable(false);
            warnBody.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            warnBody.setForeground(new Color(120, 70, 30));
            warnBody.setBackground(new Color(255, 248, 240));
            warnBody.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            warn.add(warnBody, java.awt.BorderLayout.CENTER);
            body.add(warn);
            body.add(javax.swing.Box.createVerticalStrut(12));
        }

        body.add(buildProfileSection("Personal Information", new String[][] {
                { "Birthday", EmployeeRecordsModule.formatBirthdayForDisplay(safeColumn(emp, EmployeeModule.BIRTHDAY)) },
                { "Address", safeColumn(emp, EmployeeModule.ADDRESS) },
                { "Phone", safeColumn(emp, EmployeeModule.PHONE) }
        }));
        body.add(javax.swing.Box.createVerticalStrut(10));
        body.add(buildProfileSection("Government IDs", new String[][] {
                { "SSS #", safeColumn(emp, EmployeeModule.SSS) },
                { "PhilHealth #", safeColumn(emp, EmployeeModule.PHILHEALTH) },
                { "TIN #", safeColumn(emp, EmployeeModule.TIN) },
                { "Pag-IBIG #", safeColumn(emp, EmployeeModule.PAGIBIG) }
        }));
        body.add(javax.swing.Box.createVerticalStrut(10));
        body.add(buildProfileSection("Employment", new String[][] {
                { "Position", safeColumn(emp, EmployeeModule.POSITION) },
                { "Department", dept },
                { "Supervisor", safeColumn(emp, EmployeeModule.IMMEDIATE_SUPERVISOR) }
        }));
        body.add(javax.swing.Box.createVerticalStrut(10));
        body.add(buildProfileSection("Compensation", new String[][] {
                { "Basic Salary", "PHP " + safeColumn(emp, EmployeeModule.BASIC_SALARY) },
                { "Rice Subsidy", "PHP " + safeColumn(emp, EmployeeModule.RICE_SUBSIDY) },
                { "Phone Allowance", "PHP " + safeColumn(emp, EmployeeModule.PHONE_ALLOWANCE) },
                { "Clothing Allowance", "PHP " + safeColumn(emp, EmployeeModule.CLOTHING_ALLOWANCE) },
                { "Gross Semi-monthly", "PHP " + safeColumn(emp, EmployeeModule.GROSS_SEMI_MONTHLY) },
                { "Hourly Rate", "PHP " + safeColumn(emp, EmployeeModule.HOURLY_RATE) }
        }));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, java.awt.BorderLayout.CENTER);
        return root;
    }

    private static JPanel buildProfileSection(String title, String[][] rows) {
        JPanel section = new JPanel(null);
        section.setBackground(PALETTE_WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT_BLUE),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12))));
        int y = 8;
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(ACCENT_BLUE);
        titleLbl.setBounds(8, y, 400, 18);
        section.add(titleLbl);
        y += 26;
        for (String[] row : rows) {
            JLabel lbl = new JLabel(row[0]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lbl.setForeground(TEXT_MUTED);
            lbl.setBounds(8, y, 130, 18);
            section.add(lbl);
            String val = row[1] == null || row[1].trim().isEmpty() ? "—" : row[1];
            JLabel valLbl = new JLabel(val);
            valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            valLbl.setForeground(TEXT_DARK_NAVY);
            valLbl.setBounds(142, y, 300, 18);
            section.add(valLbl);
            y += 22;
        }
        section.setPreferredSize(new java.awt.Dimension(480, y + 8));
        section.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, y + 8));
        return section;
    }

    private static void showEmployeeRecordDetailDialog(String[] emp) {
        List<String> warnings = EmployeeRecordsModule.collectViewWarnings(emp);

        JDialog dialog = new JDialog(frame, "Employee Profile", true);
        dialog.setSize(560, 620);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new java.awt.BorderLayout(0, 0));
        dialog.add(buildEmployeeProfileViewPanel(emp, warnings), java.awt.BorderLayout.CENTER);

        JPanel south = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 16, 10));
        south.setBackground(APP_BG);
        JButton btnEdit = new JButton("Edit Record");
        styleStandardButton(btnEdit);
        btnEdit.addActionListener(e -> {
            dialog.dispose();
            showSelectedEmployeeEditDialog();
        });
        JButton close = new JButton("Close");
        guiStyleAccentButton(close);
        close.addActionListener(e -> dialog.dispose());
        south.add(btnEdit);
        south.add(close);
        dialog.add(south, java.awt.BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static void updateEmployeeRecordActionState(boolean hasSelection) {
        if (btnRecView != null) {
            btnRecView.setEnabled(hasSelection && isHrUser());
            btnRecView.setToolTipText(hasSelection ? "View full details for the selected employee"
                    : "Select an employee from the table first");
            refreshStandardButtonState(btnRecView);
        }
        if (btnRecUpdate != null) {
            btnRecUpdate.setEnabled(hasSelection && isHrUser());
            btnRecUpdate.setToolTipText(hasSelection ? "Edit the selected employee record"
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
        if (btnRecViewAttendance != null) {
            btnRecViewAttendance.setEnabled(hasSelection);
            btnRecViewAttendance.setToolTipText(hasSelection ? "View all attendance records for this employee"
                    : "Select an employee from the table first");
            refreshStandardButtonState(btnRecViewAttendance);
        }
        if (lblRecFormHint != null) {
            lblRecFormHint.setText(hasSelection
                    ? "Editing employee #" + selectedEmployeeId + ". Click Clear Form to start over."
                    : "Select a row to edit, or click Add for a new employee.");
        }
    }

    private static void showEmployeeAttendanceDialog(String empId) {
        // Look up the employee name for the dialog title
        String empName = empId;
        for (String[] emp : FileHandlerModule.getAllEmployees()) {
            if (emp != null && emp.length > 0 && empId.equals(emp[EmployeeModule.ID].trim())) {
                empName = EmployeeModule.fullName(emp);
                break;
            }
        }

        // Parse all attendance rows for this employee
        List<String> rawLines = FileHandlerModule.findAttendanceData(empId);
        // Each row: [id, last, first, MM/DD/YYYY, in, out]
        java.util.List<String[]> parsedRows = new java.util.ArrayList<>();
        java.util.Set<String> years = new java.util.TreeSet<>();
        java.util.Set<String> months = new java.util.TreeSet<>();
        String[] DAY_NAMES = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        String[] MONTH_NAMES = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };

        for (String line : rawLines) {
            String[] col = FileHandlerModule.smartSplit(line);
            if (col.length < 6)
                continue;
            String datePart = col[3].trim();
            String[] dp = datePart.split("/");
            if (dp.length < 3)
                continue;
            try {
                int m = Integer.parseInt(dp[0]);
                int d = Integer.parseInt(dp[1]);
                int y = Integer.parseInt(dp[2]);
                java.util.Calendar dc = java.util.Calendar.getInstance();
                dc.set(y, m - 1, d);
                String dayName = DAY_NAMES[dc.get(java.util.Calendar.DAY_OF_WEEK) - 1];
                double hrs = SalaryComputationModule.calculateShift(col[4].trim(), col[5].trim());
                parsedRows.add(new String[] {
                        String.format("%02d/%02d/%04d", m, d, y),
                        dayName,
                        col[4].trim(),
                        col[5].trim(),
                        String.format("%.2f", hrs),
                        String.valueOf(y),
                        String.valueOf(m)
                });
                years.add(String.valueOf(y));
                months.add(String.valueOf(m));
            } catch (Exception ignored) {
            }
        }

        // Sort rows by date ascending
        parsedRows.sort((a, b) -> a[0].compareTo(b[0]));

        // ── Build dialog ──────────────────────────────────────────────────
        JDialog dlg = new JDialog(frame, "Attendance Records — " + empName + " (#" + empId + ")", true);
        dlg.setSize(680, 520);
        dlg.setMinimumSize(new java.awt.Dimension(580, 400));
        dlg.setLocationRelativeTo(frame);
        dlg.setLayout(new java.awt.BorderLayout(0, 0));

        // Header strip
        JPanel headerStrip = new JPanel(null);
        headerStrip.setBackground(ACCENT_BLUE);
        headerStrip.setPreferredSize(new java.awt.Dimension(0, 46));
        JLabel lblTitle = new JLabel("  Attendance Records — " + empName);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(PALETTE_WHITE);
        lblTitle.setBounds(0, 11, 600, 24);
        headerStrip.add(lblTitle);
        dlg.add(headerStrip, java.awt.BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 8));
        filterBar.setBackground(TABLE_HEADER_BG);
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER_COLOR));

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(APP_FONT_BOLD);
        lblFilter.setForeground(TEXT_DARK_NAVY);

        String[] yearOpts = new String[years.size() + 1];
        yearOpts[0] = "All Years";
        int yi = 1;
        for (String yr : years)
            yearOpts[yi++] = yr;

        String[] monthOpts = new String[months.size() + 1];
        monthOpts[0] = "All Months";
        int mi = 1;
        for (String mn : months)
            monthOpts[mi++] = MONTH_NAMES[Integer.parseInt(mn) - 1];

        @SuppressWarnings("unchecked")
        JComboBox<String> cmbYear = new JComboBox<>(yearOpts);
        @SuppressWarnings("unchecked")
        JComboBox<String> cmbMonth = new JComboBox<>(monthOpts);
        cmbYear.setFont(APP_FONT_PLAIN);
        cmbMonth.setFont(APP_FONT_PLAIN);

        JLabel lblSummary = new JLabel();
        lblSummary.setFont(APP_FONT_PLAIN);
        lblSummary.setForeground(TEXT_MUTED);

        filterBar.add(lblFilter);
        filterBar.add(cmbYear);
        filterBar.add(cmbMonth);
        filterBar.add(lblSummary);
        dlg.add(filterBar, java.awt.BorderLayout.NORTH); // will be in center area

        // Table
        String[] cols = { "Date", "Day", "Time In", "Time Out", "Hours Worked" };
        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(APP_FONT_PLAIN);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.getTableHeader().setFont(APP_FONT_BOLD);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(TEXT_DARK_NAVY);
        table.setSelectionBackground(new Color(220, 232, 255));
        table.setSelectionForeground(TEXT_DARK_NAVY);
        // Column widths
        int[] cw = { 110, 55, 90, 90, 110 };
        for (int c = 0; c < cw.length; c++)
            table.getColumnModel().getColumn(c).setPreferredWidth(cw[c]);
        table.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }
        });
        // Zebra stripe renderer
        javax.swing.table.TableCellRenderer zebraRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel)
                    setBackground(row % 2 == 0 ? PALETTE_WHITE : new Color(247, 249, 253));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        for (int c = 0; c < cols.length - 1; c++)
            table.getColumnModel().getColumn(c).setCellRenderer(zebraRenderer);

        JScrollPane scroll = new JScrollPane(table);
        styleScrollPane(scroll);

        // Total footer
        JPanel footer = new JPanel(new java.awt.BorderLayout());
        footer.setBackground(TABLE_HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER_COLOR));
        JLabel lblTotal = new JLabel();
        lblTotal.setFont(APP_FONT_BOLD);
        lblTotal.setForeground(TEXT_DARK_NAVY);
        lblTotal.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        JButton btnClose = new JButton("Close");
        btnClose.setFont(APP_FONT_PLAIN);
        btnClose.setPreferredSize(new java.awt.Dimension(90, 32));
        btnClose.addActionListener(ev -> dlg.dispose());
        JPanel footerRight = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 6));
        footerRight.setBackground(TABLE_HEADER_BG);
        footerRight.add(btnClose);
        footer.add(lblTotal, java.awt.BorderLayout.WEST);
        footer.add(footerRight, java.awt.BorderLayout.EAST);

        // Assemble center
        JPanel center = new JPanel(new java.awt.BorderLayout(0, 0));
        center.add(filterBar, java.awt.BorderLayout.NORTH);
        center.add(scroll, java.awt.BorderLayout.CENTER);
        center.add(footer, java.awt.BorderLayout.SOUTH);
        dlg.add(center, java.awt.BorderLayout.CENTER);

        // ── Populate / re-filter table ────────────────────────────────────
        final java.util.List<String[]> allRows = new java.util.ArrayList<>(parsedRows);
        Runnable repopulate = () -> {
            tableModel.setRowCount(0);
            String selYear = (String) cmbYear.getSelectedItem();
            String selMonth = (String) cmbMonth.getSelectedItem();
            double totalHrs = 0;
            for (String[] r : allRows) {
                boolean yearOk = "All Years".equals(selYear) || r[5].equals(selYear);
                boolean monthOk = "All Months".equals(selMonth)
                        || MONTH_NAMES[Integer.parseInt(r[6]) - 1].equals(selMonth);
                if (yearOk && monthOk) {
                    tableModel.addRow(new Object[] { r[0], r[1], r[2], r[3], r[4] });
                    totalHrs += Double.parseDouble(r[4]);
                }
            }
            int cnt = tableModel.getRowCount();
            lblTotal.setText("  " + cnt + " record" + (cnt == 1 ? "" : "s") +
                    "  |  Total Hours: " + String.format("%.2f", totalHrs));
            lblSummary.setText("(" + cnt + " shown)");
        };

        cmbYear.addActionListener(ev -> repopulate.run());
        cmbMonth.addActionListener(ev -> repopulate.run());
        repopulate.run();

        dlg.setVisible(true);
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
        employeeRecordsCache.clear();
        for (String[] emp : FileHandlerModule.getAllEmployees()) {
            employeeRecordsCache.add(emp);
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
        pushCsvSnapshot();
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

        try {
            String deletedId = selectedEmployeeId;
            pushCsvSnapshotWithLog("DELETE", deletedId, "Deleted employee #" + deletedId);
            if (!FileHandlerModule.deleteEmployeeRecord(deletedId)) {
                JOptionPane.showMessageDialog(frame,
                        "Could not delete the employee record.\n"
                                + "The file may be locked or the record may no longer exist.",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            refreshEmployeeTable();
            selectedEmployeeId = null;
            updateEmployeeRecordActionState(false);
            SwingUtilities.invokeLater(() -> {
                showToast("Employee #" + deletedId + " deleted successfully.");
                JOptionPane.showMessageDialog(frame,
                        "Employee #" + deletedId + " was deleted successfully.\n"
                                + "The employee table has been refreshed.",
                        "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "An unexpected error occurred while deleting the record:\n" + ex.getMessage(),
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Opens Payroll Processing on the Reports tab (employee payslip issue inbox). */
    static void showHrPayslipIssuesUI() {
        if (!isHrUser()) {
            JOptionPane.showMessageDialog(frame,
                    "Payslip issue review is only available in the HR portal.",
                    "HR Portal Required", JOptionPane.INFORMATION_MESSAGE);
            showDashboard();
            return;
        }
        payrollSubView = "Reports";
        setupPayrollUI();
    }

    /**
     * HR payslip issue inbox embedded in Payroll Processing (Reports tab).
     * Full-width table with status filters; double-click or right-click for actions.
     */
    private static void setupHrPayslipReportsContent(JPanel panel, int panelW, int panelH) {
        java.util.List<PayslipIssueModule.PayslipIssue> allIssues =
                new ArrayList<>(FileHandlerModule.loadPayslipIssues());
        java.util.Collections.reverse(allIssues);

        final java.util.List<PayslipIssueModule.PayslipIssue> displayedIssues = new ArrayList<>();
        final String[] activeFilter = { "Open" };

        final int tablePad = 12;
        final int filterTop = 12;
        final int filterBarH = 44;
        final int tableX = tablePad;
        final int tableW = panelW - tablePad * 2;
        final int tableY = filterTop + filterBarH + 8;
        final int tableH = panelH - tableY - tablePad;

        String[] tableColumns = {
                "Submitted", "Employee #", "Name", "Pay Period", "Issue Type", "Status"
        };
        DefaultTableModel issueTableModel = new DefaultTableModel(tableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable issueTable = new JTable(issueTableModel);
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueTable.setFillsViewportHeight(true);
        applyModernTableStyle(issueTable);
        configurePayslipIssueTableColumns(issueTable, tableW);

        java.util.function.Supplier<PayslipIssueModule.PayslipIssue> selectedIssue = () -> {
            int viewRow = issueTable.getSelectedRow();
            if (viewRow < 0 || viewRow >= issueTable.getRowCount()) {
                return null;
            }
            int modelRow = issueTable.convertRowIndexToModel(viewRow);
            if (modelRow < 0 || modelRow >= displayedIssues.size()) {
                return null;
            }
            return displayedIssues.get(modelRow);
        };

        java.util.function.Supplier<PayslipIssueModule.PayslipIssue> requireSelectedIssue = () -> {
            PayslipIssueModule.PayslipIssue sel = selectedIssue.get();
            if (sel == null) {
                JOptionPane.showMessageDialog(frame,
                        "Select a payslip report from the table first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
            }
            return sel;
        };

        JPanel filterBar = createRecordsStyleBar(tableW, filterBarH);
        filterBar.setLocation(tableX, filterTop);
        panel.add(filterBar);

        JLabel filterLbl = new JLabel("Status:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        filterLbl.setForeground(TEXT_MUTED);
        filterLbl.setBounds(12, 14, 52, 18);
        filterBar.add(filterLbl);

        String[] filters = { "Open", "In Progress", "Resolved", "All" };
        int fx = 68;
        JButton[] filterBtns = new JButton[filters.length];
        for (int i = 0; i < filters.length; i++) {
            final String cat = filters[i];
            JButton fb = new JButton(cat);
            fb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fb.setBounds(fx, 10, 96, 26);
            stylePayrollFilterChip(fb, cat.equals(activeFilter[0]));
            filterBtns[i] = fb;
            filterBar.add(fb);
            fx += 102;
        }

        final int barPad = 12;
        final int btnH = 28;
        final int btnY = 8;
        final Font reviewBtnFont = new Font("Segoe UI", Font.BOLD, 12);
        java.awt.FontMetrics reviewFm = filterBar.getFontMetrics(reviewBtnFont);
        final int btnW = Math.max(118, reviewFm.stringWidth("View Report") + 28);
        final int btnX = tableW - barPad - btnW;
        final int countGap = 14;
        final int countX = fx + 8;
        final int countW = Math.max(120, btnX - countGap - countX);

        JLabel countLbl = new JLabel();
        countLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLbl.setForeground(TEXT_MUTED);
        countLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        countLbl.setBounds(countX, 14, countW, 18);
        filterBar.add(countLbl);

        JButton btnReview = new JButton("View Report");
        btnReview.setBounds(btnX, btnY, btnW, btnH);
        guiStyleAccentButton(btnReview);
        btnReview.setFont(reviewBtnFont);
        btnReview.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        btnReview.setToolTipText("Open the selected payslip issue report");
        filterBar.add(btnReview);

        final Runnable refreshCountLbl = () -> {
            int pending = FileHandlerModule.countPayslipIssuesNeedingAction();
            countLbl.setText(displayedIssues.size() + " shown · " + pending + " pending");
        };

        Runnable refreshIssueTable = () -> {
            issueTable.clearSelection();
            issueTable.setRowSorter(null);
            issueTableModel.setRowCount(0);
            displayedIssues.clear();
            for (PayslipIssueModule.PayslipIssue issue : allIssues) {
                if (!payslipIssueMatchesFilter(issue, activeFilter[0])) {
                    continue;
                }
                displayedIssues.add(issue);
                issueTableModel.addRow(new Object[] {
                        formatPayslipIssueTimestamp(issue.timestamp),
                        issue.employeeId,
                        issue.employeeName,
                        issue.payPeriod,
                        issue.issueType,
                        issue.status
                });
            }
            enableTableSorting(issueTable);
            configurePayslipIssueTableColumns(issueTable, tableW);
            if (issueTable.getRowCount() > 0) {
                SwingUtilities.invokeLater(() -> {
                    if (issueTable.getRowCount() > 0) {
                        issueTable.setRowSelectionInterval(0, 0);
                    }
                });
            }
            refreshCountLbl.run();
        };

        Runnable openReview = () -> {
            PayslipIssueModule.PayslipIssue sel = requireSelectedIssue.get();
            if (sel != null) {
                showHrPayslipIssueReviewDialog(sel, allIssues, refreshIssueTable);
            }
        };

        Runnable openPayroll = () -> {
            PayslipIssueModule.PayslipIssue sel = requireSelectedIssue.get();
            if (sel != null) {
                openPayrollForEmployee(sel.employeeId);
            }
        };

        Runnable openEmployee = () -> {
            PayslipIssueModule.PayslipIssue sel = requireSelectedIssue.get();
            if (sel == null) {
                return;
            }
            String data = FileHandlerModule.findEmployeeData(sel.employeeId);
            if (data == null) {
                JOptionPane.showMessageDialog(frame,
                        "Employee #" + sel.employeeId + " could not be found in the CSV.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedEmployeeId = sel.employeeId.trim();
            showEmployeeEditPopup(FileHandlerModule.smartSplit(data));
        };

        Runnable markResolved = () -> {
            PayslipIssueModule.PayslipIssue sel = requireSelectedIssue.get();
            if (sel == null) {
                return;
            }
            if (sel.isResolved()) {
                JOptionPane.showMessageDialog(frame,
                        "This report is already marked resolved.",
                        "Already Resolved", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            sel.status = PayslipIssueModule.STATUS_RESOLVED;
            if (sel.hrNote == null || sel.hrNote.trim().isEmpty()) {
                sel.hrNote = "Reviewed and corrected by HR.";
            }
            sel.resolvedAt = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (FileHandlerModule.savePayslipIssues(allIssues)) {
                showToast("Report marked resolved.");
                refreshIssueTable.run();
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Could not save the updated report status.",
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
            }
        };

        java.util.function.Consumer<java.awt.Component> showReportActionsMenu = anchor -> {
            PayslipIssueModule.PayslipIssue sel = selectedIssue.get();
            if (sel == null) {
                JOptionPane.showMessageDialog(frame,
                        "Select a payslip report from the table first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JPopupMenu menu = new JPopupMenu();
            JMenuItem miReview = new JMenuItem("Review & Fix");
            miReview.addActionListener(e -> openReview.run());
            menu.add(miReview);
            JMenuItem miPayroll = new JMenuItem("Open Payroll");
            miPayroll.addActionListener(e -> openPayroll.run());
            menu.add(miPayroll);
            JMenuItem miEmployee = new JMenuItem("View Employee");
            miEmployee.addActionListener(e -> openEmployee.run());
            menu.add(miEmployee);
            if (!sel.isResolved()) {
                menu.addSeparator();
                JMenuItem miResolve = new JMenuItem("Mark Resolved");
                miResolve.addActionListener(e -> markResolved.run());
                menu.add(miResolve);
            }
            menu.show(anchor, 0, anchor.getHeight());
        };

        for (int i = 0; i < filters.length; i++) {
            final String cat = filters[i];
            filterBtns[i].addActionListener(e -> {
                activeFilter[0] = cat;
                for (int j = 0; j < filterBtns.length; j++) {
                    stylePayrollFilterChip(filterBtns[j], filters[j].equals(activeFilter[0]));
                }
                refreshIssueTable.run();
            });
        }

        btnReview.addActionListener(e -> openReview.run());

        JScrollPane issueScroll = new JScrollPane(issueTable);
        issueScroll.setBounds(tableX, tableY, tableW, tableH);
        styleEmployeeRecordsScrollPane(issueScroll);
        panel.add(issueScroll);

        refreshIssueTable.run();

        issueTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && selectedIssue.get() != null) {
                    openReview.run();
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                maybeShowReportContextMenu(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                maybeShowReportContextMenu(e);
            }

            private void maybeShowReportContextMenu(java.awt.event.MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                int row = issueTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    issueTable.setRowSelectionInterval(row, row);
                }
                showReportActionsMenu.accept(issueTable);
            }
        });
    }

    private static boolean payslipIssueMatchesFilter(PayslipIssueModule.PayslipIssue issue, String filter) {
        if (issue == null || filter == null || "All".equalsIgnoreCase(filter)) {
            return issue != null;
        }
        if ("Open".equalsIgnoreCase(filter)) {
            return issue.isOpen();
        }
        if ("In Progress".equalsIgnoreCase(filter)) {
            return issue.isInProgress();
        }
        if ("Resolved".equalsIgnoreCase(filter)) {
            return issue.isResolved();
        }
        return true;
    }

    private static String formatPayslipIssueTimestamp(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "—";
        }
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(raw.trim());
            return dt.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));
        } catch (Exception e) {
            return raw.trim();
        }
    }

    private static void stylePayrollFilterChip(JButton btn, boolean active) {
        if (btn == null) {
            return;
        }
        btn.setFocusable(false);
        btn.setOpaque(true);
        btn.setBackground(active ? ACCENT_BLUE : new Color(240, 244, 252));
        btn.setForeground(active ? PALETTE_WHITE : TEXT_DARK_NAVY);
        btn.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
    }

    private static void showHrPayslipIssueReviewDialog(PayslipIssueModule.PayslipIssue issue,
            java.util.List<PayslipIssueModule.PayslipIssue> allIssues, Runnable onSaved) {
        if (issue == null) {
            return;
        }

        JDialog dlg = new JDialog(frame, "Review Payslip Report", true);
        dlg.setLayout(null);
        dlg.getContentPane().setBackground(PALETTE_WHITE);
        dlg.setSize(520, 544);
        dlg.setLocationRelativeTo(frame);
        dlg.setResizable(false);

        int pad = 20;
        int w = 520 - pad * 2;
        int gap = 14; // consistent inter-section gap
        int lf  = 4;  // label-to-field gap

        JLabel title = new JLabel("Employee Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK_NAVY);
        title.setBounds(pad, 16, w, 22);
        dlg.add(title);

        JLabel meta = new JLabel("<html>#" + escapeHtml(issue.employeeId) + " · "
                + escapeHtml(issue.employeeName) + "<br>" + escapeHtml(issue.payPeriod)
                + " · " + escapeHtml(formatPayslipIssueTimestamp(issue.timestamp)) + "</html>");
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        meta.setForeground(TEXT_MUTED);
        meta.setBounds(pad, 42, w, 34);
        dlg.add(meta);

        // y=76 after meta; first section gets a slightly larger 18px separator from header
        int y = 94;

        JLabel lblType = new JLabel("Issue Type");
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblType.setForeground(TEXT_MUTED);
        lblType.setBounds(pad, y, w, 16);
        dlg.add(lblType);

        JTextField txtType = new JTextField(issue.issueType);
        txtType.setEditable(false);
        txtType.setFont(APP_FONT_PLAIN);
        txtType.setBounds(pad, y + 16 + lf, w, FIELD_HEIGHT);
        txtType.setBackground(new Color(248, 250, 254));
        dlg.add(txtType);
        y += 16 + lf + FIELD_HEIGHT + gap;

        JLabel lblDesc = new JLabel("Employee Description");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setBounds(pad, y, w, 16);
        dlg.add(lblDesc);

        JTextArea txtDesc = new JTextArea(issue.description);
        txtDesc.setEditable(false);
        txtDesc.setFont(APP_FONT_PLAIN);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setBackground(new Color(248, 250, 254));
        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setBounds(pad, y + 16 + lf, w, 72);
        dlg.add(descScroll);
        y += 16 + lf + 72 + gap;

        JLabel lblStatus = new JLabel("HR Status");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(TEXT_MUTED);
        lblStatus.setBounds(pad, y, w, 16);
        dlg.add(lblStatus);

        String[] statuses = {
                PayslipIssueModule.STATUS_OPEN,
                PayslipIssueModule.STATUS_IN_PROGRESS,
                PayslipIssueModule.STATUS_RESOLVED
        };
        JComboBox<String> cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setSelectedItem(issue.status);
        cmbStatus.setFont(APP_FONT_PLAIN);
        cmbStatus.setBounds(pad, y + 16 + lf, w, FIELD_HEIGHT);
        dlg.add(cmbStatus);
        y += 16 + lf + FIELD_HEIGHT + gap;

        JLabel lblHr = new JLabel("HR Resolution Notes");
        lblHr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHr.setForeground(TEXT_MUTED);
        lblHr.setBounds(pad, y, w, 16);
        dlg.add(lblHr);

        JTextArea txtHr = new JTextArea(issue.hrNote);
        txtHr.setFont(APP_FONT_PLAIN);
        txtHr.setLineWrap(true);
        txtHr.setWrapStyleWord(true);
        txtHr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane hrScroll = new JScrollPane(txtHr);
        hrScroll.setBounds(pad, y + 16 + lf, w, 84);
        dlg.add(hrScroll);
        y += 16 + lf + 84 + gap;

        JButton btnSave = new JButton("Save Resolution");
        guiStyleAccentButton(btnSave);
        btnSave.setBounds(pad, y, 140, BTN_HEIGHT);

        JButton btnPayroll = new JButton("Open Payroll");
        styleStandardButton(btnPayroll);
        btnPayroll.setBounds(pad + 150, y, 120, BTN_HEIGHT);

        JButton btnClose = new JButton("Close");
        styleStandardButton(btnClose);
        btnClose.setBounds(pad + 280, y, 90, BTN_HEIGHT);

        btnClose.addActionListener(e -> dlg.dispose());
        btnPayroll.addActionListener(e -> {
            dlg.dispose();
            openPayrollForEmployee(issue.employeeId);
        });

        btnSave.addActionListener(e -> {
            String hrNote = txtHr.getText().trim();
            String status = String.valueOf(cmbStatus.getSelectedItem());
            if (PayslipIssueModule.STATUS_RESOLVED.equals(status) && hrNote.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "Add HR resolution notes before marking this report as Resolved.",
                        "Resolution Notes Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            issue.status = status;
            issue.hrNote = hrNote;
            if (issue.isResolved()) {
                issue.resolvedAt = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                issue.resolvedAt = "";
            }
            if (FileHandlerModule.savePayslipIssues(allIssues)) {
                dlg.dispose();
                showToast("Payslip report updated.");
                if (onSaved != null) {
                    onSaved.run();
                }
            } else {
                JOptionPane.showMessageDialog(dlg,
                        "Could not save HR resolution. The issues file may be locked.",
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(btnSave);
        dlg.add(btnPayroll);
        dlg.add(btnClose);
        dlg.setVisible(true);
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
        qList.setFixedCellHeight(44);
        qList.setCellRenderer((lst, value, index, isSel, hasFocus) -> {
            JLabel lbl = new JLabel("  •  " + value);
            lbl.setFont(new Font("Segoe UI", isSel ? Font.BOLD : Font.PLAIN, 15));
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
        ansArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
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
        int panelW = bounds.width;
        int panelH = bounds.height;
        int panelX = bounds.x;
        boolean stackActions = panelW < RESP_NOTIF_STACK_ACTIONS;

        JPanel panel = new JPanel(null);
        panel.setBackground(PALETTE_WHITE);
        panel.setBounds(panelX, bounds.y, panelW, panelH);
        panel.setBorder(cardBorder());

        List<NotificationModule.Notification> allNotifications = buildSystemNotifications();
        DefaultListModel<NotificationModule.Notification> model = new DefaultListModel<>();
        for (NotificationModule.Notification n : allNotifications)
            model.addElement(n);

        // ── Filter bar (wraps to two rows on narrow panels) ────────────────
        final int filterTop = 16;
        final int filterRowH = 28;
        int filterBtnW = 88;
        final int filterBtnGap = 8;
        final int filterStartX = 20;
        String[] filters = { "All", "Unread", "Payroll", "Birthday", "Attendance" };
        int filtersAreaW = panelW - filterStartX - 56 - 20;
        while (filters.length * (filterBtnW + filterBtnGap) - filterBtnGap > filtersAreaW && filterBtnW > 68) {
            filterBtnW -= 4;
        }
        boolean filterWrap = filters.length * (filterBtnW + filterBtnGap) - filterBtnGap > filtersAreaW;
        int perFilterRow = filterWrap ? (filters.length + 1) / 2 : filters.length;
        int filterRows = filterWrap ? 2 : 1;
        final int listTop = filterTop + filterRows * (filterRowH + 6) + 14;

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLbl.setForeground(TEXT_DARK_NAVY);
        filterLbl.setBounds(filterStartX, filterTop + 4, 48, 20);
        panel.add(filterLbl);

        JButton[] filterBtns = new JButton[filters.length];
        for (int fi = 0; fi < filters.length; fi++) {
            JButton fb = new JButton(filters[fi]);
            fb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fb.setFocusable(false);
            fb.setOpaque(true);
            fb.setBackground(fi == 0 ? ACCENT_BLUE : new Color(240, 244, 252));
            fb.setForeground(fi == 0 ? PALETTE_WHITE : TEXT_DARK_NAVY);
            fb.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
            int row = filterWrap ? fi / perFilterRow : 0;
            int col = filterWrap ? fi % perFilterRow : fi;
            fb.setBounds(filterStartX + 56 + col * (filterBtnW + filterBtnGap),
                    filterTop + row * (filterRowH + 6), filterBtnW, filterRowH);
            filterBtns[fi] = fb;
            panel.add(fb);
        }

        // ── Unread count label (declared early so lambdas can reference it) ─
        JLabel unreadLbl = new JLabel();
        unreadLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        unreadLbl.setForeground(new Color(100, 115, 145));

        // ── Notification list ──────────────────────────────────────────────
        JList<NotificationModule.Notification> list = new JList<>(model);
        list.setCellRenderer(new NotificationCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(64);
        list.setBackground(new Color(248, 250, 254));

        int listW = stackActions ? panelW - 40 : panelW - 220;
        int actionBlockH = stackActions ? 196 : 0;
        int listH = stackActions ? panelH - listTop - actionBlockH - 20 : panelH - listTop - 44;
        JScrollPane sp = new JScrollPane(list);
        sp.setBounds(20, listTop, listW, Math.max(120, listH));
        sp.setBorder(BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(sp);

        // Tracks which filter chip is currently active so actions know the view context
        final String[] activeFilter = { "All" };

        // Helper: refresh the unread count text after any state change
        Runnable refreshCount = () -> {
            long uc = allNotifications.stream().filter(nn -> !nn.read).count();
            unreadLbl.setText(uc + " unread of " + allNotifications.size());
        };

        // Helper: mark as read — removes from view only when the Unread filter is
        // active
        java.util.function.Consumer<NotificationModule.Notification> markReadInView = n -> {
            markNotificationRead(n);
            if ("Unread".equals(activeFilter[0])) {
                DefaultListModel<NotificationModule.Notification> m = (DefaultListModel<NotificationModule.Notification>) list
                        .getModel();
                m.removeElement(n);
            } else {
                list.repaint(); // re-render row to show dimmed/read style
            }
            refreshCount.run();
        };

        // Helper: dismiss — permanently removes from backing list and current view
        java.util.function.Consumer<NotificationModule.Notification> dismissFromView = n -> {
            allNotifications.remove(n);
            DefaultListModel<NotificationModule.Notification> m = (DefaultListModel<NotificationModule.Notification>) list
                    .getModel();
            m.removeElement(n);
            refreshCount.run();
        };

        // ── Single-click selects; double-click opens detail and marks read ─
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
                markReadInView.accept(n);
                showNotificationDetail(n);
            }
        });

        // ── Action panel (right column, or below list when narrow) ─────────
        int actionX = stackActions ? 20 : listW + 36;
        int actionW = stackActions ? listW : panelW - actionX - 16;
        int actionsTop = stackActions ? listTop + Math.max(120, listH) + 16 : listTop;

        JLabel actTitle = new JLabel("Actions");
        actTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        actTitle.setForeground(TEXT_DARK_NAVY);
        actTitle.setBounds(actionX, actionsTop, actionW, 22);
        panel.add(actTitle);

        JButton btnMarkRead = new JButton("Mark as Read");
        btnMarkRead.setBounds(actionX, actionsTop + 30, actionW, 32);
        guiStyleAccentButton(btnMarkRead);
        btnMarkRead.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                NotificationModule.Notification n = ((DefaultListModel<NotificationModule.Notification>) list
                        .getModel()).getElementAt(idx);
                markReadInView.accept(n);
                showToast("Notification marked as read.");
            }
        });
        panel.add(btnMarkRead);

        JButton btnDismiss = new JButton("Dismiss");
        btnDismiss.setBounds(actionX, actionsTop + 72, actionW, 32);
        styleStandardButton(btnDismiss);
        btnDismiss.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                NotificationModule.Notification n = ((DefaultListModel<NotificationModule.Notification>) list
                        .getModel()).getElementAt(idx);
                dismissFromView.accept(n);
            }
        });
        panel.add(btnDismiss);

        JButton btnMarkAll = new JButton("Mark All Read");
        btnMarkAll.setBounds(actionX, actionsTop + 114, actionW, 32);
        styleStandardButton(btnMarkAll);
        btnMarkAll.addActionListener(e -> {
            for (NotificationModule.Notification n : allNotifications)
                markNotificationRead(n);
            if ("Unread".equals(activeFilter[0])) {
                ((DefaultListModel<NotificationModule.Notification>) list.getModel()).clear();
            } else {
                list.repaint();
            }
            refreshCount.run();
            showToast("All notifications marked as read.");
        });
        panel.add(btnMarkAll);

        // Position and populate the unread count label now that actionX/Y are known
        unreadLbl.setBounds(actionX, actionsTop + 162, actionW, 18);
        refreshCount.run();
        panel.add(unreadLbl);

        // ── Filter button wiring ───────────────────────────────────────────
        for (int fi = 0; fi < filters.length; fi++) {
            final String cat = filters[fi];
            final int idx = fi;
            filterBtns[fi].addActionListener(e -> {
                activeFilter[0] = cat;
                for (int j = 0; j < filterBtns.length; j++) {
                    filterBtns[j].setBackground(j == idx ? ACCENT_BLUE : new Color(240, 244, 252));
                    filterBtns[j].setForeground(j == idx ? PALETTE_WHITE : TEXT_DARK_NAVY);
                }
                DefaultListModel<NotificationModule.Notification> filtered = new DefaultListModel<>();
                for (NotificationModule.Notification n : allNotifications) {
                    if ("All".equals(cat))
                        filtered.addElement(n);
                    else if ("Unread".equals(cat) && !n.read)
                        filtered.addElement(n);
                    else if (n.category.equals(cat))
                        filtered.addElement(n);
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

        // Category badge strip
        JPanel strip = new JPanel(new java.awt.BorderLayout(0, 0));
        strip.setBackground(ACCENT_BLUE);
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
                icon = new ColoredCircleIcon(value.read ? new Color(190, 205, 230) : ACCENT_BLUE, 12);
                if (value.read)
                    fg = new Color(160, 170, 185);
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

    /** Calendar icon for HR birthday picker button. */
    static class CalendarPickerIcon implements Icon {
        private final Color color;
        private final int size;

        CalendarPickerIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int pad = Math.max(1, size / 8);
            int w = size - pad * 2;
            int h = size - pad * 2;
            int left = x + pad;
            int top = y + pad;
            g2.setColor(color);
            g2.fillRoundRect(left, top + h / 5, w, h - h / 5, 3, 3);
            g2.setColor(PALETTE_WHITE);
            g2.fillRect(left + 1, top + h / 5 + 1, w - 2, h / 6);
            g2.setColor(color);
            int ring = Math.max(2, size / 8);
            g2.fillRect(left + w / 4, top, ring, h / 5);
            g2.fillRect(left + w - w / 4 - ring, top, ring, h / 5);
            g2.setColor(new Color(255, 255, 255, 180));
            int cell = Math.max(2, w / 5);
            for (int r = 0; r < 2; r++) {
                for (int col = 0; col < 3; col++) {
                    g2.fillRect(left + 2 + col * (cell + 1), top + h / 2 + r * (cell + 1), cell, cell);
                }
            }
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

    /** Funnel icon for batch payroll filter button. */
    static class FilterFunnelIcon implements Icon {
        private final Color color;
        private final int size;

        FilterFunnelIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int pad = Math.max(1, size / 9);
            int top = y + pad;
            int mid = y + size * 2 / 3;
            int bottom = y + size - pad;
            int left = x + pad;
            int right = x + size - pad;
            int center = x + size / 2;
            int stemHalf = Math.max(2, size / 7);
            java.awt.Polygon funnel = new java.awt.Polygon();
            funnel.addPoint(left, top);
            funnel.addPoint(right, top);
            funnel.addPoint(center + stemHalf, mid);
            funnel.addPoint(center + stemHalf, bottom);
            funnel.addPoint(center - stemHalf, bottom);
            funnel.addPoint(center - stemHalf, mid);
            g2.fill(funnel);
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
        rpClear();
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

        if (monthCombo.getSelectedIndex() > 0 && FileHandlerModule.findEmployeeData(id) != null) {
            int monthNum = MONTH_NUMBERS[monthCombo.getSelectedIndex()];
            java.util.Set<Integer> attended = getAttendanceMonths(id);
            if (!attended.isEmpty() && !attended.contains(monthNum)) {
                setFieldError(monthCombo);
                applyMonthComboRenderer(monthCombo, attended);
                errors.add("No attendance records found for "
                        + monthCombo.getSelectedItem() + " " + year + ".");
            }
        }

        if (!errors.isEmpty()) {
            String errText = formatPlainBulletList(errors);
            txtResultArea.setText(errText);
            if (isHrUser()) {
                rpSet(errText, rsWarn);
                showBulletErrorDialog(frame, errors, "Input Error", JOptionPane.WARNING_MESSAGE);
            } else {
                showEmployeePayslipError(errText);
            }
            return;
        }

        String actualMonth = String.valueOf(MONTH_NUMBERS[monthCombo.getSelectedIndex()]);
        java.util.Set<Integer> attended = getAttendanceMonths(id);
        if (!attended.isEmpty() && !attended.contains(Integer.parseInt(actualMonth))) {
            setFieldError(monthCombo);
            applyMonthComboRenderer(monthCombo, attended);
            List<String> noData = new ArrayList<>();
            noData.add("No attendance records found for "
                    + monthCombo.getSelectedItem() + " " + year + ".");
            String errText = formatPlainBulletList(noData);
            txtResultArea.setText(errText);
            if (isHrUser()) {
                rpSet(errText, rsWarn);
                showBulletErrorDialog(frame, noData, "No Attendance Data", JOptionPane.WARNING_MESSAGE);
            } else {
                showEmployeePayslipError(errText);
            }
            return;
        }

        String data = FileHandlerModule.findEmployeeData(id);
        String[] emp = FileHandlerModule.smartSplit(data);
        txtEmployeeName.setText(EmployeeModule.fullName(emp));

        javax.swing.JTextArea chunk = new javax.swing.JTextArea();
        SalaryComputationModule.calculatePayroll(emp, actualMonth, year, chunk);
        txtResultArea.setText(chunk.getText());
        if (SalaryComputationModule.lastCalculationSucceeded) {
            if (isHrUser()) {
                rpRenderEmployeeCard(id, EmployeeModule.fullName(emp));
                if (richPane != null) {
                    richPane.setCaretPosition(0);
                }
            } else {
                showEmployeePayslipDocument();
            }
        } else {
            String failMsg = chunk.getText().trim().isEmpty()
                    ? "No attendance data found for this pay period."
                    : chunk.getText().trim();
            if (isHrUser()) {
                rpSet(failMsg, rsWarn);
            } else {
                showEmployeePayslipError(failMsg);
            }
        }

        if (SalaryComputationModule.lastCalculationSucceeded) {
            updatePayrollStatChips(
                    SalaryComputationModule.summaryGross,
                    SalaryComputationModule.summaryDeductions,
                    SalaryComputationModule.summaryNet);
        } else {
            resetPayrollStatChips();
        }

        if (SalaryComputationModule.lastCalculationSucceeded) {
            if (isHrUser()) {
                showToast("Payslip generated. Use Copy or Save to export.");
            } else if (!employeePayslipSuppressToast) {
                showToast("Payslip loaded.");
            }
        }
    }

    private static void resetLoginFieldBorders() {
        resetLoginFieldBorder(usernameField);
        resetLoginFieldBorder(passwordField);
    }

    private static void resetLoginFieldBorder(JTextField field) {
        if (field != null)
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 8)));
    }

    private static void setLoginFieldError(JTextField field) {
        if (field != null)
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_ERROR, 2),
                    BorderFactory.createEmptyBorder(5, 9, 5, 7)));
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

    private static java.util.Set<Integer> getAttendanceMonths(String empId) {
        java.util.Set<Integer> months = new java.util.HashSet<>();
        if (empId == null || empId.isEmpty())
            return months;
        for (String line : FileHandlerModule.findAttendanceData(empId)) {
            String[] row = FileHandlerModule.smartSplit(line);
            if (row.length > 3) {
                String[] parts = row[3].trim().split("/");
                try {
                    months.add(Integer.parseInt(parts[0].trim()));
                } catch (NumberFormatException ignore) {
                }
            }
        }
        return months;
    }

    private static void applyMonthComboRenderer(JComboBox<String> combo,
            java.util.Set<Integer> dataMonths) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                boolean noData = index > 0
                        && !dataMonths.isEmpty()
                        && !dataMonths.contains(MONTH_NUMBERS[index]);
                if (noData && !isSelected) {
                    setForeground(BORDER_ERROR);
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });
        combo.repaint();
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
