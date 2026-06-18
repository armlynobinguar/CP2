package motorph_employeeapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Right-hand payroll results panel with metric cards, output area, and export actions.
 */
public class PayrollResultsPanel extends JPanel {

    public static final Color CARD_BG = new Color(0xF3F4F6);
    public static final Color CARD_BORDER = new Color(0xE5E7EB);
    public static final Color PRIMARY = new Color(0x1A56DB);

    private final JLabel lblEmptyState;
    private final JPanel metricsPanel;
    private final JLabel lblEmployeeCount;
    private final JLabel lblComputed;
    private final JLabel lblTotalNetPay;
    private final JTextArea resultsArea;
    private final JButton btnCopyClipboard;
    private final JButton btnDownloadTxt;
    private final JButton btnDownloadPdf;

    public PayrollResultsPanel() {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        lblEmptyState = new JLabel(
                "<html><center>Results will appear here after you compute salaries<br>"
                        + "or generate a payslip.</center></html>",
                SwingConstants.CENTER);
        lblEmptyState.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblEmptyState.setForeground(new Color(0x6B7280));
        add(lblEmptyState, BorderLayout.NORTH);

        metricsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setVisible(false);
        lblEmployeeCount = createMetricValueLabel("—");
        lblComputed = createMetricValueLabel("—");
        lblTotalNetPay = createMetricValueLabel("—");
        metricsPanel.add(createMetricCard("Employees", lblEmployeeCount));
        metricsPanel.add(createMetricCard("Computed", lblComputed));
        metricsPanel.add(createMetricCard("Total Net Pay", lblTotalNetPay));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(metricsPanel);
        center.add(Box.createVerticalStrut(12));

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultsArea.setBackground(new Color(0xF9FAFB));
        resultsArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        scroll.setPreferredSize(new Dimension(280, 260));
        center.add(scroll);
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        btnCopyClipboard = createActionButton("Copy to Clipboard");
        btnDownloadTxt = createActionButton("Download .txt");
        btnDownloadPdf = createActionButton("Download .pdf");
        actions.add(btnCopyClipboard);
        actions.add(Box.createVerticalStrut(8));
        actions.add(btnDownloadTxt);
        actions.add(Box.createVerticalStrut(8));
        actions.add(btnDownloadPdf);
        add(actions, BorderLayout.SOUTH);
    }

    private static JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        card.setPreferredSize(new Dimension(140, 80));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(0x6B7280));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 4));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private static JLabel createMetricValueLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setForeground(new Color(0x111827));
        return label;
    }

    private static JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.setFocusPainted(false);
        return button;
    }

    public void showEmptyState() {
        lblEmptyState.setVisible(true);
        metricsPanel.setVisible(false);
        resultsArea.setText("");
    }

    public void showResults(String text, int employeeCount, String computedLabel, String totalNet) {
        lblEmptyState.setVisible(false);
        metricsPanel.setVisible(true);
        lblEmployeeCount.setText(String.valueOf(employeeCount));
        lblComputed.setText(computedLabel);
        lblTotalNetPay.setText(totalNet);
        resultsArea.setText(text);
        resultsArea.setCaretPosition(0);
    }

    public void updateMetricCards(PayrollResult[] results) {
        if (results == null || results.length == 0) {
            showEmptyState();
            return;
        }
        double totalNet = 0;
        for (PayrollResult result : results) {
            totalNet += result.getNetPay();
        }
        showResults(resultsArea.getText(), results.length, "✓", formatPeso(totalNet));
    }

    public static String formatPeso(double amount) {
        return String.format("₱%,.2f", amount);
    }

    public static String buildResultsText(PayrollResult[] results) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-15s %-15s %-15s%n",
                "Emp #", "Gross Pay", "Deductions", "Net Pay"));
        sb.append("-".repeat(55)).append('\n');
        for (PayrollResult result : results) {
            sb.append(String.format("%-10s %-15s %-15s %-15s%n",
                    result.getEmployeeId(),
                    formatPeso(result.getGrossPay()),
                    formatPeso(result.getTotalDeductions()),
                    formatPeso(result.getNetPay())));
        }
        return sb.toString();
    }

    public JTextArea getResultsArea() {
        return resultsArea;
    }

    public JButton getCopyButton() {
        return btnCopyClipboard;
    }

    public JButton getDownloadTxtButton() {
        return btnDownloadTxt;
    }

    public JButton getDownloadPdfButton() {
        return btnDownloadPdf;
    }

    public JLabel getEmployeeCountLabel() {
        return lblEmployeeCount;
    }

    public JLabel getComputedLabel() {
        return lblComputed;
    }

    public JLabel getTotalNetPayLabel() {
        return lblTotalNetPay;
    }
}
