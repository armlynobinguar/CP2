package motorph_employeeapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Single Employee Payroll screen — GUI and event handling only.
 */
public class SingleEmployeePayrollPanel extends JPanel {

    private final java.awt.Window owner;
    private final PayrollResultsPanel resultsPanel;

    private JTextField txtEmployeeNo;
    private JTextField txtEmployeeName;
    private JComboBox<String> cmbMonth;
    private JComboBox<Integer> cmbYear;
    private JButton btnCalculate;

    public SingleEmployeePayrollPanel(java.awt.Window owner) {
        super(new BorderLayout());
        this.owner = owner;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        resultsPanel = new PayrollResultsPanel();
        resultsPanel.showEmptyState();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        JLabel section = new JLabel("EMPLOYEE LOOKUP");
        section.setFont(new Font("SansSerif", Font.PLAIN, 13));
        section.setForeground(BatchPayrollPanel.MUTED);
        form.add(section, gbc);

        gbc.gridy++;
        form.add(fieldBlock("Employee #", txtEmployeeNo = MotorPH_GUI.createPayrollTextField(true)), gbc);

        gbc.gridy++;
        form.add(fieldBlock("Full Name", txtEmployeeName = MotorPH_GUI.createPayrollTextField(false)), gbc);

        gbc.gridy++;
        JLabel period = new JLabel("PAY PERIOD");
        period.setFont(new Font("SansSerif", Font.PLAIN, 13));
        period.setForeground(BatchPayrollPanel.MUTED);
        form.add(period, gbc);

        gbc.gridy++;
        JPanel periodRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        periodRow.setOpaque(false);
        cmbMonth = new JComboBox<>(BatchPayrollPanelMonthOptions());
        cmbMonth.setPreferredSize(new Dimension(160, 40));
        cmbYear = new JComboBox<>();
        for (int year = 2020; year <= 2030; year++) {
            cmbYear.addItem(year);
        }
        cmbYear.setSelectedItem(2024);
        cmbYear.setPreferredSize(new Dimension(160, 40));
        periodRow.add(cmbMonth);
        periodRow.add(cmbYear);
        form.add(periodRow, gbc);

        gbc.gridy++;
        btnCalculate = new JButton("Calculate Payroll");
        btnCalculate.setBackground(BatchPayrollPanel.PRIMARY);
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setPreferredSize(new Dimension(220, 40));
        btnCalculate.setFocusPainted(false);
        btnCalculate.addActionListener(e -> MotorPH_GUI.runPayrollFromSinglePanel(this));
        form.add(btnCalculate, gbc);

        add(form, BorderLayout.WEST);
        form.setPreferredSize(new Dimension(380, 10));
        add(resultsPanel, BorderLayout.CENTER);

        resultsPanel.getCopyButton().addActionListener(e -> MotorPH_GUI.copyPayslipToClipboard());
        resultsPanel.getDownloadTxtButton().addActionListener(e -> MotorPH_GUI.exportPayrollTextToFile());
        resultsPanel.getDownloadPdfButton().addActionListener(e -> MotorPH_GUI.exportPayslipToFile());

        MotorPH_GUI.wirePayrollEmployeeField(txtEmployeeNo);
    }

    private static JPanel fieldBlock(String label, JTextField field) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(BatchPayrollPanel.MUTED);
        block.add(lbl, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    private static String[] BatchPayrollPanelMonthOptions() {
        return new String[] {
                "01 - January", "02 - February", "03 - March", "04 - April",
                "05 - May", "06 - June", "07 - July", "08 - August",
                "09 - September", "10 - October", "11 - November", "12 - December"
        };
    }

    public PayrollResultsPanel getResultsPanel() {
        return resultsPanel;
    }

    public JTextField getEmployeeNoField() {
        return txtEmployeeNo;
    }

    public JTextField getEmployeeNameField() {
        return txtEmployeeName;
    }

    public JComboBox<String> getMonthCombo() {
        return cmbMonth;
    }

    public JComboBox<Integer> getYearCombo() {
        return cmbYear;
    }
}
