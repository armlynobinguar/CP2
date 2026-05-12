
package MotorPH_EmployeeApp;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class MotorPH_GUI {
    private static JFrame frame;
    private static JTextField txtEmployeeNo, txtEmployeeName, txtMonth, txtYear;
    private static JTextArea txtResultArea;

    public static void initialize() {
        if (frame == null) {
            frame = new JFrame("MotorPH Management System");
            frame.setSize(550, 700);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
        }
        showMainMenu();
    }

    private static void showMainMenu() {
        frame.getContentPane().removeAll(); 
        frame.setLayout(new GridBagLayout()); 
        frame.setSize(400, 400);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createTitledBorder("Main Menu"));

        JButton btnPayroll = new JButton("1. F1: Pay Coverage");
        JButton btnInfo = new JButton("2. Employee Information");
        JButton btnLogout = new JButton("3. Logout");

        btnPayroll.addActionListener(e -> setupPayrollUI());

        btnInfo.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(frame, "Enter Employee ID:");
            if (id != null && !id.trim().isEmpty()) {
                displayEmployeeDetails(id);
            }
        });

        // --- LOGOUT LOGIC ---
        btnLogout.addActionListener(e -> {
            System.exit(0); 
        });

        menuPanel.add(btnPayroll);
        menuPanel.add(btnInfo);
        menuPanel.add(btnLogout);

        frame.add(menuPanel);
        refreshFrame();
    }

    private static void setupPayrollUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout(10, 10));
        frame.setSize(550, 700);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(new JLabel("Employee Number:"));
        txtEmployeeNo = new JTextField();
        form.add(txtEmployeeNo);

        form.add(new JLabel("Employee Name:"));
        txtEmployeeName = new JTextField();
        txtEmployeeName.setEditable(false);
        txtEmployeeName.setBackground(new Color(240, 240, 240));
        form.add(txtEmployeeName);

        form.add(new JLabel("Month (ex. 6):"));
        txtMonth = new JTextField();
        form.add(txtMonth);

        form.add(new JLabel("Year (ex. 2024):"));
        txtYear = new JTextField();
        form.add(txtYear);

        JButton btnProcess = new JButton("Process Payroll");
        btnProcess.addActionListener(e -> handlePayrollLogic());
        form.add(btnProcess);

        JButton btnBack = new JButton("Back to Menu");
        btnBack.addActionListener(e -> showMainMenu());
        form.add(btnBack);

        txtResultArea = new JTextArea();
        txtResultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtResultArea.setEditable(false);
        txtResultArea.setBackground(Color.BLACK);
        txtResultArea.setForeground(Color.GREEN);
        
        frame.add(form, BorderLayout.NORTH);
        frame.add(new JScrollPane(txtResultArea), BorderLayout.CENTER);

        refreshFrame();
    }

    private static void displayEmployeeDetails(String id) {
        String data = FileHandlerModule.findEmployeeData(id);
        if (data != null) {
            String[] emp = FileHandlerModule.smartSplit(data);
            String info = "Employee ID: " + emp[EmployeeModule.ID] +
                          "\nName: " + EmployeeModule.fullName(emp) +
                          "\nBirthday: " + emp[EmployeeModule.BIRTHDAY];
            
            JOptionPane.showMessageDialog(frame, info, "Employee Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Employee ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void handlePayrollLogic() {
        try {
            String id = txtEmployeeNo.getText().trim();
            String month = txtMonth.getText().trim();
            String year = txtYear.getText().trim();

            if (id.isEmpty() || month.isEmpty() || year.isEmpty()) {
                throw new Exception("Error: Please fill in all fields.");
            }

            String data = FileHandlerModule.findEmployeeData(id);
            if (data == null) {
                JOptionPane.showMessageDialog(frame, "Employee Number not found!");
                return;
            }

            String[] emp = FileHandlerModule.smartSplit(data);
            txtEmployeeName.setText(EmployeeModule.fullName(emp));
            txtResultArea.setText(""); 

            PrintStream out = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    txtResultArea.append(String.valueOf((char) b));
                    txtResultArea.setCaretPosition(txtResultArea.getDocument().getLength());
                }
            });
            System.setOut(out);
            System.setErr(out);

            SalaryComputationModule.calculatePayroll(emp, month, year);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }
    }

    private static void refreshFrame() {
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
}