
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

    enum UserRole {
        EMPLOYEE,
        HR
    }

    static final String EMPLOYEE_USERNAME = "employee";
    static final String EMPLOYEE_PASSWORD = "12345";
    static final String HR_USERNAME = "hr";
    static final String HR_PASSWORD = "hr12345";
    static final String HR_USERNAME_LEGACY = "payroll_staff";
    static final String HR_PASSWORD_LEGACY = "password123";

    /** Demo employee portal account maps to this CSV employee record. */
    static final String EMPLOYEE_DEMO_ID = "10001";

    /** Resolves the employee record ID linked to a portal login username. */
    static String getLinkedEmployeeId(String username) {
        if (username == null) {
            return null;
        }
        if (EMPLOYEE_USERNAME.equals(username.trim())) {
            return EMPLOYEE_DEMO_ID;
        }
        return null;
    }

    /** @deprecated Use {@link #EMPLOYEE_USERNAME} */
    @Deprecated
    static final String AUTH_VAL_1 = EMPLOYEE_USERNAME;

    /** @deprecated Use {@link #HR_USERNAME} */
    @Deprecated
    static final String AUTH_VAL_2 = HR_USERNAME;

    /** @deprecated Use {@link #EMPLOYEE_PASSWORD} */
    @Deprecated
    static final String AUTH_VAL_3 = EMPLOYEE_PASSWORD;

    /** @deprecated Use {@link #HR_PASSWORD} */
    @Deprecated
    static final String AUTH_VAL_4 = HR_PASSWORD;

    static boolean loginSuccessful = false;
    static UserRole loggedInRole = null;

    static boolean authenticate(String username, String password) {
        if (EMPLOYEE_USERNAME.equals(username) && EMPLOYEE_PASSWORD.equals(password)) {
            loggedInRole = UserRole.EMPLOYEE;
            return true;
        }
        if ((HR_USERNAME.equals(username) && HR_PASSWORD.equals(password))
                || (HR_USERNAME_LEGACY.equals(username) && HR_PASSWORD_LEGACY.equals(password))) {
            loggedInRole = UserRole.HR;
            return true;
        }
        loggedInRole = null;
        return false;
    }

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
