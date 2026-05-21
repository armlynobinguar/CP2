
package MotorPH_EmployeeApp;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.event.KeyEvent;    
import java.awt.event.KeyListener;

/**
 * MotorPH_GUI
 * BroCode Java Swing Tutorial Reference Concepts.
 */
public class MotorPH_GUI {
    // Shared structural layout window handles
    static JFrame frame;
    
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
        frame.setSize(480, 520);
        frame.getContentPane().setBackground(new Color(212, 228, 252)); // Hex conversion color match

        // Title Header Label (Lesson: Labels)
        JLabel lblMenuTitle = new JLabel("MAIN MENU", SwingConstants.CENTER);
        lblMenuTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMenuTitle.setForeground(new Color(11, 29, 58));
        lblMenuTitle.setBounds(40, 30, 400, 30);
        frame.add(lblMenuTitle);

        // Container Panel (Lesson: Panels)
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(Color.white);
        menuPanel.setLayout(null); // Using null layout within the panel container
        menuPanel.setBounds(40, 80, 400, 340);
        menuPanel.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));

        // Navigation Menu Interactive Buttons (Lesson: Buttons)
        JButton btnPayroll = new JButton("1. MPHCRO1: Pay Coverage");
        btnPayroll.setBounds(40, 40, 320, 60);
        styleAccentButton(btnPayroll);
        btnPayroll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setupPayrollUI();
            }
        });

        JButton btnInfo = new JButton("2. Employee Information");
        btnInfo.setBounds(40, 140, 320, 60);
        styleAccentButton(btnInfo);
        btnInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEmployeeLookupUI();
            }
        });

        JButton btnLogout = new JButton("3. Logout");
        btnLogout.setBounds(40, 240, 320, 60);
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
                // Invoked when a key is typed. Uses KeyChar. (Required by KeyListener interface)
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Invoked when a physical key is pressed down. Uses KeyCode.
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Invoked whenever a button is released.
                String id = txtEmployeeNo.getText().trim();
                
                // If input clear, instantly empty the read-only name field
                if (id.isEmpty()) {
                    txtEmployeeName.setText("");
                    return;
                }
                 
                // Query file storage module instantly as the user types
                String data = FileHandlerModule.findEmployeeData(id);
                if (data != null) {
                    String[] emp = FileHandlerModule.smartSplit(data);
                    txtEmployeeName.setText(EmployeeModule.fullName(emp));
                } else {
                    txtEmployeeName.setText("Searching records...");
                }
            }
        });

        JLabel lblEmpName = createStyledLabel("Employee Name:");
        lblEmpName.setBounds(30, 65, 230, 30);
        txtEmployeeName = createStyledTextField(false); // Locked representation state
        txtEmployeeName.setBounds(260, 65, 240, 30);

        JLabel lblMonth = createStyledLabel("Pay Coverage Month (ex. 6):");
        lblMonth.setBounds(30, 110, 230, 30);
        txtMonth = createStyledTextField(true);
        txtMonth.setBounds(260, 110, 240, 30);

        JLabel lblYear = createStyledLabel("Pay Coverage Year (ex. 2024):");
        lblYear.setBounds(30, 155, 230, 30);
        txtYear = createStyledTextField(true);
        txtYear.setBounds(260, 155, 240, 30);

        // Core Trigger Processing Action Switches (Lesson: Buttons)
        JButton btnProcess = new JButton("Process Payroll");
        btnProcess.setBounds(30, 215, 220, 45);
        styleAccentButton(btnProcess);
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
        formPanel.add(txtMonth);
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
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                // Instantly re-runs lookup method on key release
                runEmployeeLookupAction();
            }
        });

        JButton btnSearch = new JButton("Search Record");
        btnSearch.setBounds(30, 105, 160, 40);
        styleAccentButton(btnSearch);
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
     * Combines both real-time clearing and button-click empty validation states safely.
     */
    static void runEmployeeLookupAction() {
        String idInput = txtLookupInput.getText().trim();
        
        // Check if the user input is completely blank or cleared out
        if (idInput.isEmpty()) {
            // If the user is deleting characters via key listener, keep the screen clean.
            // If they click a static button trigger while empty, prompt them safely.
            txtLookupDisplay.setText(""); 
            return;
        }
        
        // Query the data file module using the provided input string parameter
        String data = FileHandlerModule.findEmployeeData(idInput);
        if (data != null) {
            String[] emp = FileHandlerModule.smartSplit(data);
            txtLookupDisplay.setText(
                "Employee ID: " + emp[EmployeeModule.ID] + "\n" +
                "Full Name:   " + EmployeeModule.fullName(emp) + "\n" +
                "Birthday:    " + emp[EmployeeModule.BIRTHDAY]
            );
        } else {
            // Fallback display state if parsing fails to locate an ID match
            txtLookupDisplay.setText("Employee ID not found.");
        }
    }

    /**
     * Validation checks and execution routine for calculations.
     * Lesson: JOptionPane Dialog Mechanics
     */
    static void runPayrollCalculation() {
        txtResultArea.setText("");

        String id = txtEmployeeNo.getText().trim();
        String month = txtMonth.getText().trim();
        String year = txtYear.getText().trim();

        // 1. Empty field layout check
        if (id.isEmpty() || month.isEmpty() || year.isEmpty()) {
            JOptionPane.showMessageDialog(
                    frame, 
                    createStyledAlertText("Please fill in all fields to proceed."), 
                    "Missing Information", 
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // 2. Numerical validation parsing parameters
        try {
            Integer.parseInt(id);
            Integer.parseInt(month);
            Integer.parseInt(year);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    frame, 
                    createStyledAlertText("Please type only numerical values for Employee Number, Month, and Year."), 
                    "Input Error", 
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 3. Database entry verification check
        String data = FileHandlerModule.findEmployeeData(id);
        if (data == null) {
            JOptionPane.showMessageDialog(
                    frame, 
                    createStyledAlertText("The requested Employee ID was not found in our database records."), 
                    "Record Not Found", 
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 4. Success state computation pass
        String[] emp = FileHandlerModule.smartSplit(data);
        txtEmployeeName.setText(EmployeeModule.fullName(emp));
         
        SalaryComputationModule.calculatePayroll(emp, month, year, txtResultArea);
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
    
    /**
     * Helper to wrap alert message strings inside beautifully styled JLabels.
     * Ensures popups strictly match the application font and theme palette.
     */
    private static JLabel createStyledAlertText(String text) {
        JLabel alertLabel = new JLabel(text);
        alertLabel.setFont(APP_FONT_PLAIN); // Matches standard UI font body selection
        alertLabel.setForeground(new Color(11, 29, 58)); // Dark Navy color match
        return alertLabel;
    }
    
    private static void styleStandardButton(JButton button) {
        button.setFont(APP_FONT_BOLD);
        button.setFocusable(false);
        button.setBackground(Color.white);
        button.setForeground(new Color(11, 29, 58));
        button.setBorder(BorderFactory.createLineBorder(new Color(163, 196, 243), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private static void styleAccentButton(JButton button) {
        button.setFont(APP_FONT_BOLD);
        button.setFocusable(false);
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