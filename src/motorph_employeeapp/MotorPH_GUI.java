
package MotorPH_EmployeeApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * MotorPH_GUI
 * Procedural Management UI styled inside the blue-white design scheme.
 */
public class MotorPH_GUI {
    static JFrame frame;
    static JTextField txtEmployeeNo, txtEmployeeName, txtMonth, txtYear;
    static JTextArea txtResultArea;
    
    // NEW CONSTANT FOR THE MONOSPACED RECEIPT FONT
    static final Font RECEIPT_FONT = new Font("Consolas", Font.PLAIN, 13);
    
    /**
     * Procedural method to set up and construct the base window frame configuration.
     * Lesson 1: Frames
     */
    public static void initialize() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        frame = new JFrame("MotorPH Management System");
        frame.setSize(550, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
         
        showMainMenu();
    }

    /**
     * Clear active components and draw the Main Navigation Menu window components.
     * Lesson 1: Frames, Lesson 2: Labels, Lesson 3: Panels, Lesson 4: Buttons, Lesson 7: GridLayout
     */
    static void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.setLayout(new GridBagLayout());
        frame.setSize(480, 520); // Expanded slightly for better layout spacing
        frame.getContentPane().setBackground(MotorPH_EmployeeApp.PALETTE_LIGHT_BLUE);

        // 1. Text Header placed explicitly above the panel layout
        JLabel lblMenuTitle = new JLabel("MAIN MENU", SwingConstants.CENTER);
        lblMenuTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMenuTitle.setForeground(MotorPH_EmployeeApp.TEXT_DARK_NAVY);
         
        // 2. Main Navigation Panel Setup
        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        menuPanel.setBackground(MotorPH_EmployeeApp.PALETTE_WHITE);
        menuPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MotorPH_EmployeeApp.BORDER_BLUE, 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        JButton btnPayroll = new JButton("1. MPHCRO1: Pay Coverage");
        JButton btnInfo = new JButton("2. Employee Information");
        JButton btnLogout = new JButton("3. Logout");

        // 3. Turn Option 1 & 2 into modern blue accent links
        styleAccentButton(btnPayroll); 
        styleAccentButton(btnInfo);
         
        // 4. Leave Logout as a standard neutral exit link style
        styleStandardButton(btnLogout); 

        btnPayroll.addActionListener(e -> setupPayrollUI());
        btnInfo.addActionListener(e -> processEmployeeLookup());
        btnLogout.addActionListener(e -> System.exit(0));

        menuPanel.add(btnPayroll);
        menuPanel.add(btnInfo);
        menuPanel.add(btnLogout);

        // 5. Position elements top-down cleanly using GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; 
        gbc.gridy = 0; 
        gbc.insets = new Insets(0, 0, 15, 0); // Spacing below title header
        frame.add(lblMenuTitle, gbc);
         
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        frame.add(menuPanel, gbc);

        updateDisplay();
    }

    /**
     * Constructs and formats the specialized workspace layout frame fields for processing payroll variables.
     * Lesson 1: Frames, Lesson 5: BorderLayout, Lesson 7: GridLayout, Lesson 11: TextFields, Lesson 20: KeyListener
     */
    static void setupPayrollUI() {
        frame.getContentPane().removeAll();
        
        // 1. REDUCED VERTICAL GAP FROM 10 TO 2 (Pulls the center panel upwards)
        frame.setLayout(new BorderLayout(10, 2)); 
        frame.setSize(550, 750);
        frame.getContentPane().setBackground(MotorPH_EmployeeApp.PALETTE_LIGHT_BLUE);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        
        // 2. REDUCED BOTTOM PADDING FROM 15 TO 0 (Removes extra blank space under the buttons)
        form.setBorder(BorderFactory.createEmptyBorder(25, 25, 0, 25)); 
        form.setBackground(MotorPH_EmployeeApp.PALETTE_LIGHT_BLUE);

        txtEmployeeNo = createStyledTextField(true);   
        txtEmployeeName = createStyledTextField(false); 
        txtMonth = createStyledTextField(true);        
        txtYear = createStyledTextField(true);         

        txtEmployeeNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String id = txtEmployeeNo.getText().trim();
                if (id.isEmpty()) {
                    txtEmployeeName.setText("");
                    return;
                }
                 
                String data = FileHandlerModule.findEmployeeData(id);
                if (data != null) {
                    String[] emp = FileHandlerModule.smartSplit(data);
                    txtEmployeeName.setText(EmployeeModule.fullName(emp));
                } else {
                    txtEmployeeName.setText("Searching records...");
                }
            }
        });

        form.add(createStyledLabel("Employee Number (ex. 10001):"));
        form.add(txtEmployeeNo);
        form.add(createStyledLabel("Employee Name:"));
        form.add(txtEmployeeName);
        form.add(createStyledLabel("Pay Coverage Month (ex. 6):"));
        form.add(txtMonth);
        form.add(createStyledLabel("Pay Coverage Year (ex. 2024):"));
        form.add(txtYear);

        JButton btnProcess = new JButton("Process Payroll");
        styleAccentButton(btnProcess);
        btnProcess.addActionListener(e -> runPayrollCalculation());
        form.add(btnProcess);

        JButton btnBack = new JButton("Back to Menu");
        styleStandardButton(btnBack);
        btnBack.addActionListener(e -> showMainMenu());
        form.add(btnBack);

        txtResultArea = new JTextArea();
        txtResultArea.setBackground(MotorPH_EmployeeApp.PALETTE_WHITE); 
        txtResultArea.setForeground(MotorPH_EmployeeApp.TEXT_DARK_NAVY); 
        
        // APPLIED RECEIPT FONT HERE
        txtResultArea.setFont(RECEIPT_FONT); 
        
        txtResultArea.setEditable(false);
        txtResultArea.setMargin(new Insets(15, 15, 15, 15));
         
        JScrollPane scrollPane = new JScrollPane(txtResultArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(MotorPH_EmployeeApp.BORDER_BLUE, 1));
         
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(MotorPH_EmployeeApp.PALETTE_LIGHT_BLUE);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 25, 25, 25));
        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        frame.add(form, BorderLayout.NORTH);
        frame.add(centerWrapper, BorderLayout.CENTER);

        updateDisplay();
    }
    
    /**
     * Builds and manages a modal lookup popup box utility to reference independent employee parameters.
     * Lesson 1: Frames (JDialog details), Lesson 3: Panels, Lesson 10: JOptionPane (Dialog Mechanics), Lesson 11: TextFields
     */
    static void processEmployeeLookup() {
        JDialog lookupDialog = new JDialog(frame, "Employee Information Lookup", true);
        lookupDialog.setSize(400, 280);
        lookupDialog.setLayout(new BorderLayout());
        lookupDialog.setLocationRelativeTo(frame);
        lookupDialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(MotorPH_EmployeeApp.PALETTE_WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MotorPH_EmployeeApp.BORDER_BLUE, 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblPrompt = createStyledLabel("Enter Employee ID:");
        lblPrompt.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField inputField = createStyledTextField(true);
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        inputField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea infoDisplayArea = new JTextArea(4, 20);
        infoDisplayArea.setFont(MotorPH_EmployeeApp.APP_FONT_PLAIN);
        infoDisplayArea.setBackground(MotorPH_EmployeeApp.PALETTE_LIGHT_BLUE);
        infoDisplayArea.setForeground(MotorPH_EmployeeApp.TEXT_DARK_NAVY);
        infoDisplayArea.setEditable(false);
        infoDisplayArea.setMargin(new Insets(10, 10, 10, 10));
        infoDisplayArea.setAlignmentX(Component.LEFT_ALIGNMENT);
         
        JScrollPane infoScroll = new JScrollPane(infoDisplayArea);
        infoScroll.setBorder(BorderFactory.createLineBorder(MotorPH_EmployeeApp.BORDER_BLUE, 1));
        infoScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String idInput = inputField.getText().trim();
                if (idInput.isEmpty()) {
                    infoDisplayArea.setText("");
                    return;
                }
                String data = FileHandlerModule.findEmployeeData(idInput);
                if (data != null) {
                    String[] emp = FileHandlerModule.smartSplit(data);
                    infoDisplayArea.setText(
                        "Employee ID: " + emp[EmployeeModule.ID] + "\n" +
                        "Full Name:   " + EmployeeModule.fullName(emp) + "\n" +
                        "Birthday:    " + emp[EmployeeModule.BIRTHDAY]
                    );
                } else {
                    infoDisplayArea.setText("Employee ID not found.");
                }
            }
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(MotorPH_EmployeeApp.PALETTE_WHITE);
         
        JButton btnClose = new JButton("Close");
        styleStandardButton(btnClose);
        btnClose.addActionListener(e -> lookupDialog.dispose());
        actionPanel.add(btnClose);

        contentPanel.add(lblPrompt);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(inputField);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(infoScroll);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(actionPanel);

        lookupDialog.add(contentPanel, BorderLayout.CENTER);
        lookupDialog.setVisible(true);
    }

    /**
     * Procedural factory helper function to assemble structured standard Text Labels.
     * Lesson 2: Labels
     */
    private static JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MotorPH_EmployeeApp.APP_FONT_BOLD);
        label.setForeground(MotorPH_EmployeeApp.TEXT_DARK_NAVY);
        return label;
    }
    
    /**
     * Procedural factory helper function to assemble customized text box input interfaces.
     * Lesson 11: TextFields
     */
    private static JTextField createStyledTextField(boolean isEditable) {
        JTextField field = new JTextField();
        field.setFont(MotorPH_EmployeeApp.APP_FONT_PLAIN);
        field.setEditable(isEditable);
        field.setForeground(MotorPH_EmployeeApp.TEXT_DARK_NAVY);
        field.setCaretColor(MotorPH_EmployeeApp.TEXT_DARK_NAVY);
         
        if (isEditable) {
            field.setBackground(MotorPH_EmployeeApp.PALETTE_WHITE);
            field.setFocusable(true);
        } else {
            field.setBackground(new Color(222, 233, 250));
            field.setFocusable(false); 
        }
         
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MotorPH_EmployeeApp.BORDER_BLUE, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }
    
    /**
     * Formats neutral actions using flat background schemes.
     * Lesson 4: Buttons
     */
    private static void styleStandardButton(JButton button) {
        button.setFont(MotorPH_EmployeeApp.APP_FONT_BOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(MotorPH_EmployeeApp.PALETTE_WHITE);
        button.setForeground(MotorPH_EmployeeApp.TEXT_DARK_NAVY);
        button.setBorder(BorderFactory.createLineBorder(MotorPH_EmployeeApp.BORDER_BLUE, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * Sets vivid accent styling highlights over operational core action elements.
     * Lesson 4: Buttons, Lesson 21: MouseListener
     */
    private static void styleAccentButton(JButton button) {
        button.setFont(MotorPH_EmployeeApp.APP_FONT_BOLD);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBackground(MotorPH_EmployeeApp.ACCENT_BLUE);
        button.setForeground(MotorPH_EmployeeApp.PALETTE_WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(MotorPH_EmployeeApp.HOVER_BLUE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(MotorPH_EmployeeApp.ACCENT_BLUE);
            }
        });
    }
    
    /**
     * Procedural calculation bridge pulling data out of layout elements to execute payroll loops.
     * Lesson 10: JOptionPane, Lesson 11: TextFields
     */
    static void runPayrollCalculation() {
        // Reset output fields to clean state
        txtResultArea.setText("");

        String id = txtEmployeeNo.getText().trim();
        String month = txtMonth.getText().trim();
        String year = txtYear.getText().trim();

        // 1. Validating empty strings before checking values
        if (id.isEmpty() || month.isEmpty() || year.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
            return;
        }

        // 2. TRY-CATCH NUMERICAL VALIDATION
        try {
            // Integer.parseInt conversions attempt to force the text strings into primitive int numbers.
            // If any field contains a letter, Java throws a NumberFormatException immediately.
            Integer.parseInt(id);
            Integer.parseInt(month);
            Integer.parseInt(year);
            
        } catch (NumberFormatException e) {
            // This block catches the exception so the program survives.
            // JOptionPane shows a popup alerting the user to only type numerical digits.
            JOptionPane.showMessageDialog(frame, 
                    "Please type only numerical values for Employee Number, Month, and Year.", 
                    "Input Error", 
                    JOptionPane.WARNING_MESSAGE);
            return; // Exit method immediately to prevent the system from executing bad data
        }

        // 3. Proceed with search if numbers are validated successfully
        String data = FileHandlerModule.findEmployeeData(id);
        if (data == null) {
            JOptionPane.showMessageDialog(frame, "Employee ID Not Found.");
            return;
        }

        String[] emp = FileHandlerModule.smartSplit(data);
        txtEmployeeName.setText(EmployeeModule.fullName(emp));
         
        SalaryComputationModule.calculatePayroll(emp, month, year, txtResultArea);
    }
    
    /**
     * Refreshes layout trees to force screen paint passes explicitly.
     * Lesson 1: Frames
     */
    static void updateDisplay() {
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
}