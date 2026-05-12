
package MotorPH_EmployeeApp;

import javax.swing.*;
import java.awt.*;

/**
 * MotorPH_GUI
 * This module manages the visual interface of the system.
 */
public class MotorPH_GUI {
    // GLOBAL STATE: Shared variables for the whole program
    static JFrame frame;
    static JTextField txtEmployeeNo, txtEmployeeName, txtMonth, txtYear;
    static JTextArea txtResultArea;

    public static void initialize() {
        frame = new JFrame("MotorPH Management System");
        frame.setSize(550, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        showMainMenu();
    }

    static void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.setLayout(new GridBagLayout());
        frame.setSize(400, 400);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createTitledBorder("Main Menu"));

        JButton btnPayroll = new JButton("1. MPHCRO1: Pay Coverage");
        JButton btnInfo = new JButton("2. Employee Information");
        JButton btnLogout = new JButton("3. Logout");

        btnPayroll.addActionListener(e -> setupPayrollUI());
        btnInfo.addActionListener(e -> processEmployeeLookup());
        btnLogout.addActionListener(e -> System.exit(0));

        menuPanel.add(btnPayroll);
        menuPanel.add(btnInfo);
        menuPanel.add(btnLogout);

        frame.add(menuPanel);
        updateDisplay();
    }

    static void setupPayrollUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout(10, 10));
        frame.setSize(550, 700);

 
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); 

        txtEmployeeNo = new JTextField();
        txtEmployeeName = new JTextField();
        txtEmployeeName.setEditable(false);
        txtEmployeeName.setBackground(new Color(240, 240, 240));
        txtMonth = new JTextField();
        txtYear = new JTextField();

        form.add(new JLabel("Employee Number:"));
        form.add(txtEmployeeNo);
        form.add(new JLabel("Employee Name:"));
        form.add(txtEmployeeName);
        form.add(new JLabel("Pay Coverage Month: (ex. 6):"));
        form.add(txtMonth);
        form.add(new JLabel("Pay Coverage Year: (2024):"));
        form.add(txtYear);

        JButton btnProcess = new JButton("Process Payroll");
        btnProcess.addActionListener(e -> runPayrollCalculation());
        form.add(btnProcess);

        JButton btnBack = new JButton("Back to Menu");
        btnBack.addActionListener(e -> showMainMenu());
        form.add(btnBack);

        // Result Area: The black box that shows the payroll
        txtResultArea = new JTextArea();
        txtResultArea.setBackground(Color.BLACK);
        txtResultArea.setForeground(Color.GREEN);
        txtResultArea.setFont(new Font("Monospaced", Font.PLAIN, 13)); 
        txtResultArea.setEditable(false);
        
        frame.add(form, BorderLayout.NORTH);
        frame.add(new JScrollPane(txtResultArea), BorderLayout.CENTER);

        updateDisplay();
    }

    static void runPayrollCalculation() {
        String id = txtEmployeeNo.getText().trim();
        String month = txtMonth.getText().trim();
        String year = txtYear.getText().trim();

        if (id.isEmpty() || month.isEmpty() || year.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
            return;
        }

        String data = FileHandlerModule.findEmployeeData(id);
        if (data == null) {
            JOptionPane.showMessageDialog(frame, "Employee ID Not Found.");
            return;
        }

        String[] emp = FileHandlerModule.smartSplit(data);
        txtEmployeeName.setText(EmployeeModule.fullName(emp));
        
        // Clear the box for the new report
        txtResultArea.setText("Processing Payroll...\n"); 

        SalaryComputationModule.calculatePayroll(emp, month, year, txtResultArea);
    }

    static void processEmployeeLookup() {
        String id = JOptionPane.showInputDialog(frame, "Enter ID:");
        if (id == null) return;
        
        String data = FileHandlerModule.findEmployeeData(id);
        if (data != null) {
            String[] emp = FileHandlerModule.smartSplit(data);
            String info = "ID: " + emp[0] + "\nName: " + EmployeeModule.fullName(emp);
            JOptionPane.showMessageDialog(frame, info, "Employee Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "ID not found.");
        }
    }

    static void updateDisplay() {
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
}