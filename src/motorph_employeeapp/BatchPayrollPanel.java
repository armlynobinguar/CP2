package motorph_employeeapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Batch Employee Payroll screen — GUI and event handling only.
 */
public class BatchPayrollPanel extends JPanel {

    public static final Color PRIMARY = new Color(0x1A56DB);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color ROW_ALT = new Color(0xF9FAFB);
    public static final Color ROW_HOVER = new Color(0xDBEAFE);
    public static final Color REGULAR_BG = new Color(0xDCFCE7);
    public static final Color REGULAR_FG = new Color(0x166534);

    private final java.awt.Window owner;
    private final PayrollResultsPanel resultsPanel;

    private List<Employee> allEmployees = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();
    private String filterDept = "All Departments";
    private String searchQuery = "";
    private boolean isBatchMode = true;
    private int hoveredRow = -1;
    private PayrollResult[] lastComputedResults = null;

    private JComboBox<String> cmbMonth;
    private JComboBox<Integer> cmbYear;
    private JButton btnCalculate;
    private JButton btnGenerate;
    private JTextField txtSearch;
    private JComboBox<String> cmbDepartment;
    private JButton btnSelectAll;
    private JButton btnClear;
    private JLabel lblCount;
    private JTable employeeTable;
    private DefaultTableModel tableModel;

    public BatchPayrollPanel(java.awt.Window owner) {
        super(new BorderLayout());
        this.owner = owner;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        resultsPanel = new PayrollResultsPanel();
        resultsPanel.showEmptyState();

        JPanel left = new JPanel(new BorderLayout(0, 16));
        left.setOpaque(false);
        left.add(buildControlsRow(), BorderLayout.NORTH);
        left.add(buildFilterBar(), BorderLayout.CENTER);

        JScrollPane tableScroll = new JScrollPane(buildEmployeeTable());
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.setBorder(BorderFactory.createLineBorder(PayrollResultsPanel.CARD_BORDER));
        left.add(tableScroll, BorderLayout.SOUTH);

        add(left, BorderLayout.CENTER);
        add(resultsPanel, BorderLayout.EAST);
        resultsPanel.setPreferredSize(new Dimension(360, 10));

        wireExportActions();
        reloadEmployees();
    }

    public PayrollResultsPanel getResultsPanel() {
        return resultsPanel;
    }

    public JTable getEmployeeTable() {
        return employeeTable;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JComboBox<String> getMonthCombo() {
        return cmbMonth;
    }

    public JComboBox<Integer> getYearCombo() {
        return cmbYear;
    }

    public Set<String> getSelectedIds() {
        return selectedIds;
    }

    public boolean isBatchMode() {
        return isBatchMode;
    }

    public void setBatchMode(boolean batchMode) {
        isBatchMode = batchMode;
        if (btnSelectAll != null) {
            btnSelectAll.setVisible(batchMode);
        }
    }

    /** Resets selection and button state when the payroll sub-tab changes. */
    public void onTabChanged() {
        selectedIds.clear();
        lastComputedResults = null;
        if (btnSelectAll != null) {
            btnSelectAll.setVisible(isBatchMode);
            btnSelectAll.setText("Select All");
        }
        refreshTable();
        updateSelectionCounter();
        updateButtonStates();
    }

    private JPanel buildControlsRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PayrollResultsPanel.CARD_BORDER),
                "PAY PERIOD",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.PLAIN, 13),
                MUTED));

        cmbMonth = new JComboBox<>(monthOptions());
        cmbMonth.setPreferredSize(new Dimension(160, 40));
        cmbMonth.setMinimumSize(new Dimension(160, 40));
        cmbMonth.setMaximumSize(new Dimension(160, 40));
        cmbMonth.addActionListener(e -> reloadEmployees());

        cmbYear = new JComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            cmbYear.addItem(year);
        }
        cmbYear.setSelectedItem(2024);
        cmbYear.setPreferredSize(new Dimension(160, 40));
        cmbYear.setMinimumSize(new Dimension(160, 40));
        cmbYear.setMaximumSize(new Dimension(160, 40));
        cmbYear.addActionListener(e -> reloadEmployees());

        btnCalculate = new JButton("Calculate Payroll");
        btnCalculate.setBackground(PRIMARY);
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setOpaque(true);
        btnCalculate.setBorderPainted(false);
        btnCalculate.setPreferredSize(new Dimension(160, 40));
        btnCalculate.setMinimumSize(new Dimension(160, 40));
        btnCalculate.setFocusPainted(false);
        btnCalculate.setEnabled(false);
        wireCalculateButton();

        btnGenerate = new JButton("Generate Payslips");
        btnGenerate.setBackground(Color.WHITE);
        btnGenerate.setForeground(PRIMARY);
        btnGenerate.setBorder(BorderFactory.createLineBorder(PRIMARY));
        btnGenerate.setPreferredSize(new Dimension(160, 40));
        btnGenerate.setMinimumSize(new Dimension(160, 40));
        btnGenerate.setFocusPainted(false);
        btnGenerate.setEnabled(false);
        wireGenerateButton();

        panel.add(cmbMonth);
        panel.add(cmbYear);
        panel.add(btnCalculate);
        panel.add(btnGenerate);
        return panel;
    }

    private JPanel buildFilterBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel section = new JLabel("SELECT EMPLOYEES");
        section.setFont(new Font("SansSerif", Font.PLAIN, 13));
        section.setForeground(MUTED);
        section.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(section);
        panel.add(Box.createVerticalStrut(8));

        JPanel searchRow = new JPanel();
        searchRow.setLayout(new BoxLayout(searchRow, BoxLayout.X_AXIS));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        txtSearch = new JTextField();
        txtSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtSearch.setPreferredSize(new Dimension(240, 40));
        attachSearchPlaceholder(txtSearch, "Name or employee #");
        txtSearch.addActionListener(e -> {
            searchQuery = txtSearch.getText().trim();
            refreshTable();
            updateSelectionCounter();
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                onSearchChanged();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                onSearchChanged();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                onSearchChanged();
            }
        });
        searchRow.add(searchIcon);
        searchRow.add(txtSearch);
        panel.add(searchRow);
        panel.add(Box.createVerticalStrut(12));

        JPanel filterActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterActions.setOpaque(false);
        filterActions.setAlignmentX(LEFT_ALIGNMENT);

        cmbDepartment = new JComboBox<>();
        cmbDepartment.setPreferredSize(new Dimension(200, 40));
        cmbDepartment.addActionListener(e -> {
            filterDept = String.valueOf(cmbDepartment.getSelectedItem());
            refreshTable();
            updateSelectionCounter();
            updateButtonStates();
        });

        btnSelectAll = new JButton("Select All");
        btnSelectAll.setFocusPainted(false);
        btnSelectAll.setPreferredSize(new Dimension(100, 40));
        btnSelectAll.setMinimumSize(new Dimension(100, 40));
        btnSelectAll.setVisible(isBatchMode);
        btnSelectAll.addActionListener(e -> handleSelectAll());

        btnClear = new JButton("Clear");
        btnClear.setBorderPainted(false);
        btnClear.setContentAreaFilled(false);
        btnClear.setForeground(MUTED);
        btnClear.addActionListener(e -> handleClear());

        lblCount = new JLabel("0 shown");
        lblCount.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblCount.setForeground(MUTED);

        filterActions.add(cmbDepartment);
        filterActions.add(btnSelectAll);
        filterActions.add(btnClear);
        filterActions.add(lblCount);
        panel.add(filterActions);
        panel.add(Box.createVerticalStrut(16));
        return panel;
    }

    private JTable buildEmployeeTable() {
        tableModel = new DefaultTableModel(
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
        employeeTable = new JTable(tableModel);
        employeeTable.setRowHeight(48);
        employeeTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        employeeTable.setShowVerticalLines(false);
        employeeTable.setGridColor(PayrollResultsPanel.CARD_BORDER);
        employeeTable.setSelectionBackground(ROW_HOVER);
        employeeTable.setFillsViewportHeight(true);

        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        header.setReorderingAllowed(false);

        configureColumns();
        installRenderers();
        installTableListeners();
        return employeeTable;
    }

    private void configureColumns() {
        TableColumn check = employeeTable.getColumnModel().getColumn(0);
        check.setPreferredWidth(40);
        check.setMaxWidth(44);
        employeeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        employeeTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        employeeTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        employeeTable.getColumnModel().getColumn(4).setPreferredWidth(120);
    }

    private void installRenderers() {
        TableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row == hoveredRow) {
                        c.setBackground(ROW_HOVER);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    }
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
                return c;
            }
        };
        for (int col = 1; col < employeeTable.getColumnCount(); col++) {
            if (col == 4) {
                employeeTable.getColumnModel().getColumn(col).setCellRenderer(new StatusBadgeRenderer());
            } else {
                employeeTable.getColumnModel().getColumn(col).setCellRenderer(zebra);
            }
        }

        employeeTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JCheckBox box = new javax.swing.JCheckBox();
                box.setHorizontalAlignment(SwingConstants.CENTER);
                box.setSelected(Boolean.TRUE.equals(value));
                box.setOpaque(true);
                if (isSelected) {
                    box.setBackground(table.getSelectionBackground());
                } else if (row == hoveredRow) {
                    box.setBackground(ROW_HOVER);
                } else {
                    box.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                }
                return box;
            }
        });
    }

    private void installTableListeners() {
        employeeTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = employeeTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    employeeTable.repaint();
                }
            }
        });
        employeeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                employeeTable.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = employeeTable.rowAtPoint(e.getPoint());
                int viewCol = employeeTable.columnAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return;
                }
                int modelRow = employeeTable.convertRowIndexToModel(viewRow);
                if (viewCol == 0) {
                    String id = String.valueOf(tableModel.getValueAt(modelRow, 1)).trim();
                    handleToggleRow(id);
                }
            }
        });
        tableModel.addTableModelListener(e -> updateButtonStates());
    }

    private void reloadEmployees() {
        try {
            String month = String.valueOf(cmbMonth.getSelectedIndex() + 1);
            String year = String.valueOf(cmbYear.getSelectedItem());
            allEmployees = CSVHandler.loadEmployees(month, year);
            populateDepartments();
            refreshTable();
            updateSelectionCounter();
            updateButtonStates();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner,
                    "Could not load employee data.\nCheck that the CSV file exists at: "
                            + CSVHandler.CSV_PATH,
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateDepartments() {
        String previous = filterDept;
        cmbDepartment.removeAllItems();
        for (String dept : CSVHandler.distinctDepartments(allEmployees)) {
            cmbDepartment.addItem(dept);
        }
        if (previous != null) {
            cmbDepartment.setSelectedItem(previous);
        }
        filterDept = String.valueOf(cmbDepartment.getSelectedItem());
    }

    private List<Employee> getVisibleEmployees() {
        List<Employee> visible = new ArrayList<>();
        String q = searchQuery == null ? "" : searchQuery.trim().toLowerCase();
        for (Employee employee : allEmployees) {
            boolean deptMatch = "All Departments".equals(filterDept)
                    || employee.getDepartment().equals(filterDept);
            boolean searchMatch = q.isEmpty()
                    || employee.getName().toLowerCase().contains(q)
                    || employee.getId().contains(q);
            if (deptMatch && searchMatch) {
                visible.add(employee);
            }
        }
        return visible;
    }

    private boolean isAllVisibleSelected() {
        List<Employee> visible = getVisibleEmployees();
        if (visible.isEmpty()) {
            return false;
        }
        for (Employee employee : visible) {
            if (!selectedIds.contains(employee.getId())) {
                return false;
            }
        }
        return true;
    }

    private void refreshTable() {
        List<Employee> visible = getVisibleEmployees();
        tableModel.setRowCount(0);
        for (Employee employee : visible) {
            tableModel.addRow(new Object[] {
                    selectedIds.contains(employee.getId()),
                    employee.getId(),
                    employee.getName(),
                    employee.getDepartment(),
                    employee.getStatus()
            });
        }
        if (isAllVisibleSelected() && !visible.isEmpty()) {
            btnSelectAll.setText("Deselect All");
        } else {
            btnSelectAll.setText("Select All");
        }
    }

    private void handleSelectAll() {
        lastComputedResults = null;
        List<Employee> visible = getVisibleEmployees();
        if (isAllVisibleSelected()) {
            for (Employee employee : visible) {
                selectedIds.remove(employee.getId());
            }
            btnSelectAll.setText("Select All");
        } else {
            for (Employee employee : visible) {
                selectedIds.add(employee.getId());
            }
            btnSelectAll.setText("Deselect All");
        }
        refreshTable();
        updateSelectionCounter();
        updateButtonStates();
    }

    private void handleToggleRow(String employeeId) {
        lastComputedResults = null;
        if (selectedIds.contains(employeeId)) {
            selectedIds.remove(employeeId);
        } else {
            selectedIds.add(employeeId);
        }
        refreshTable();
        updateSelectionCounter();
        updateButtonStates();
    }

    private void handleClear() {
        selectedIds.clear();
        lastComputedResults = null;
        searchQuery = "";
        filterDept = "All Departments";
        txtSearch.setText("");
        cmbDepartment.setSelectedIndex(0);
        btnSelectAll.setText("Select All");
        refreshTable();
        lblCount.setText(allEmployees.size() + " shown");
        updateButtonStates();
    }

    private void updateSelectionCounter() {
        int visible = getVisibleEmployees().size();
        if (selectedIds.isEmpty()) {
            lblCount.setText(visible + " shown");
        } else {
            lblCount.setText(selectedIds.size() + " of " + allEmployees.size() + " selected");
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = !selectedIds.isEmpty();
        btnCalculate.setEnabled(hasSelection);
        btnGenerate.setEnabled(hasSelection
                && lastComputedResults != null
                && lastComputedResults.length > 0);
    }

    private void wireCalculateButton() {
        for (java.awt.event.ActionListener listener : btnCalculate.getActionListeners()) {
            btnCalculate.removeActionListener(listener);
        }
        btnCalculate.addActionListener(e -> handleCalculate());
    }

    private void wireGenerateButton() {
        for (java.awt.event.ActionListener listener : btnGenerate.getActionListeners()) {
            btnGenerate.removeActionListener(listener);
        }
        btnGenerate.addActionListener(e -> handleGeneratePayslips());
    }

    private void handleCalculate() {
        List<Employee> toCompute = new ArrayList<>();
        for (Employee employee : allEmployees) {
            if (selectedIds.contains(employee.getId())) {
                toCompute.add(employee);
            }
        }
        if (toCompute.isEmpty()) {
            JOptionPane.showMessageDialog(owner,
                    "Please select at least one employee.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Employee[] arr = toCompute.toArray(new Employee[0]);
        List<String> errors = SalaryComputationModule.validateEmployees(arr);
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(owner,
                    String.join("\n", errors),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        PayrollResult[] results = SalaryComputationModule.computeAll(arr);
        lastComputedResults = results;
        displayResults(results);
        updateMetricCards(results);

        try {
            CSVHandler.savePayrollResults(java.util.Arrays.asList(results));
            JOptionPane.showMessageDialog(owner,
                    "Payroll computed and saved successfully for "
                            + results.length + " employees.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner,
                    "Computed but could not save to CSV.\nCheck file permissions.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        updateButtonStates();
    }

    private void displayResults(PayrollResult[] results) {
        String text = PayrollResultsPanel.buildResultsText(results);
        double totalNet = 0;
        for (PayrollResult result : results) {
            totalNet += result.getNetPay();
        }
        resultsPanel.showResults(text, results.length, "✓",
                PayrollResultsPanel.formatPeso(totalNet));
        MotorPH_GUI.txtResultArea = resultsPanel.getResultsArea();
    }

    private void updateMetricCards(PayrollResult[] results) {
        resultsPanel.updateMetricCards(results);
    }

    private void handleGeneratePayslips() {
        if (lastComputedResults == null || lastComputedResults.length == 0) {
            JOptionPane.showMessageDialog(owner,
                    "Please compute payroll first before generating payslips.",
                    "No Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        generatePayslips(lastComputedResults);
    }

    private void generatePayslips(PayrollResult[] results) {
        displayResults(results);
        JOptionPane.showMessageDialog(owner,
                "Payslips generated for " + results.length + " employee(s).",
                "Payslips Generated",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void wireExportActions() {
        resultsPanel.getCopyButton().addActionListener(e -> MotorPH_GUI.copyPayslipToClipboard());
        resultsPanel.getDownloadTxtButton().addActionListener(e -> MotorPH_GUI.exportPayrollTextToFile());
        resultsPanel.getDownloadPdfButton().addActionListener(e -> MotorPH_GUI.exportPayslipToFile());
    }

    private void onSearchChanged() {
        String text = txtSearch.getText();
        if ("Name or employee #".equals(text)) {
            searchQuery = "";
        } else {
            searchQuery = text.trim();
        }
        refreshTable();
        updateSelectionCounter();
    }

    private static String[] monthOptions() {
        return new String[] {
                "01 - January", "02 - February", "03 - March", "04 - April",
                "05 - May", "06 - June", "07 - July", "08 - August",
                "09 - September", "10 - October", "11 - November", "12 - December"
        };
    }

    private static void attachSearchPlaceholder(JTextField field, String placeholder) {
        field.setForeground(Color.BLACK);
        field.setText(placeholder);
        field.setForeground(MUTED);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(MUTED);
                }
            }
        });
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            String status = value == null ? "" : value.toString();
            if ("Regular".equalsIgnoreCase(status)) {
                label.setBackground(REGULAR_BG);
                label.setForeground(REGULAR_FG);
                label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                label.setForeground(MUTED);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
            }
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            return label;
        }
    }
}
