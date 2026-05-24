
package motorph_employeeapp;

/**
 * MotorPH_EmployeeApp
 * -------------------
 * Main entry point and application controller for the MotorPH Employee Payroll System.
 *
 * This class owns the high-level startup sequence:
 *   1. Display the login dialog ({@link MotorPH_GUI#showCustomLoginDialog()}).
 *   2. If authentication succeeds, launch the main GUI ({@link MotorPH_GUI#initialize()}).
 *   3. If login is cancelled or fails, exit the JVM cleanly.
 *
 * Authentication credentials are stored as package-private constants and validated
 * inside the GUI login handler. The {@link #loginSuccessful} flag bridges the modal
 * login dialog and the main application window.
 */
public class MotorPH_EmployeeApp {

    /** Username accepted for employee role login. */
    static final String AUTH_VAL_1 = "employee";

    /** Username accepted for payroll staff role login. */
    static final String AUTH_VAL_2 = "payroll_staff";

    /** Password accepted for employee role login. */
    static final String AUTH_VAL_3 = "12345";

    /** Password accepted for payroll staff role login. */
    static final String AUTH_VAL_4 = "password123";

    /**
     * Shared login state flag set to true only after valid username/password pair.
     * Read by main() after the modal login dialog closes.
     */
    static boolean loginSuccessful = false;

    /**
     * Application bootstrap method invoked by the JVM.
     *
     * Flow:
     *   - Opens the login dialog (blocks until user logs in or closes the window).
     *   - On success: initializes the main JFrame and shows the navigation menu.
     *   - On failure/cancel: terminates the process with exit code 0.
     *
     * @param args command-line arguments (unused in this application)
     */
    public static void main(String[] args) {
        // Step 1: Show modal login; user must authenticate before any payroll features load
        MotorPH_GUI.showCustomLoginDialog();

        if (loginSuccessful) {
            // Step 2: Build main window and display the menu screen
            MotorPH_GUI.initialize();
        } else {
            // User closed dialog or never authenticated — shut down
            System.exit(0);
        }
    }
}
