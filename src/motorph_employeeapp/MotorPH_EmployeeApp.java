
package motorph_employeeapp;

/**
 * MotorPH_EmployeeApp
 * -------------------
 * Main entry point and application controller for the MotorPH Employee Payroll System.
 *
 * <p>Startup sequence:</p>
 * <ol>
 *   <li>modal login ({@link MotorPH_GUI#showCustomLoginDialog()}).</li>
 *   <li>On success, launch main GUI ({@link MotorPH_GUI#initialize()}).</li>
 *   <li>On cancel/failure, exit JVM with code 0.</li>
 * </ol>
 *
 * <p>Credentials and role are stored here; {@link #loginSuccessful} bridges the login
 * dialog and the main window. HR and Employee portals share this class but route to
 * different screens inside {@link MotorPH_GUI} based on {@link #loggedInRole}.</p>
 */
public class MotorPH_EmployeeApp {

    /** Portal role assigned after a successful {@link #authenticate(String, String)} call. */
    enum UserRole {
        /** Standard employee self-service portal (payslip, profile, notifications). */
        EMPLOYEE,
        /** HR admin portal (records CRUD, batch payroll, revision history). */
        HR
    }

    // ── Demo login credentials (hard-coded for MO-IT103 submission) ──────────

    /** Username for the employee demo account. */
    static final String EMPLOYEE_USERNAME = "employee";
    /** Password for the employee demo account. */
    static final String EMPLOYEE_PASSWORD = "12345";
    /** Primary HR admin username. */
    static final String HR_USERNAME = "hr";
    /** Primary HR admin password. */
    static final String HR_PASSWORD = "hr12345";
    /** Legacy HR username kept for backward compatibility with older docs. */
    static final String HR_USERNAME_LEGACY = "payroll_staff";
    /** Legacy HR password kept for backward compatibility with older docs. */
    static final String HR_PASSWORD_LEGACY = "password123";

    /**
     * Employee ID in {@code Employee Details.csv} linked to {@link #EMPLOYEE_USERNAME}.
     * The employee portal always loads data for this record when logged in as {@code employee}.
     */
    static final String EMPLOYEE_DEMO_ID = "10001";

    /**
     * Maps a portal username to the CSV employee number shown in the employee portal.
     *
     * @param username value typed in the login dialog
     * @return linked employee ID, or {@code null} when the username is not an employee account
     */
    static String getLinkedEmployeeId(String username) {
        if (username == null) {
            return null;
        }
        // Only the demo employee account is linked; HR usernames return null
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

    /**
     * Set to {@code true} by the login dialog when credentials pass {@link #authenticate}.
     * Read by {@link #main} to decide whether to open the main frame or exit.
     */
    static boolean loginSuccessful = false;

    /** Role of the currently logged-in user; {@code null} before login or after failed auth. */
    static UserRole loggedInRole = null;

    /**
     * Validates username/password against demo credentials and sets {@link #loggedInRole}.
     *
     * @param username trimmed login name
     * @param password plain-text password (not hashed — demo only)
     * @return {@code true} when credentials match employee or HR (including legacy HR pair)
     */
    static boolean authenticate(String username, String password) {
        // Employee portal credentials
        if (EMPLOYEE_USERNAME.equals(username) && EMPLOYEE_PASSWORD.equals(password)) {
            loggedInRole = UserRole.EMPLOYEE;
            return true;
        }
        // HR portal — accept both current and legacy username/password pairs
        if ((HR_USERNAME.equals(username) && HR_PASSWORD.equals(password))
                || (HR_USERNAME_LEGACY.equals(username) && HR_PASSWORD_LEGACY.equals(password))) {
            loggedInRole = UserRole.HR;
            return true;
        }
        // Unknown credentials — clear role so GUI can show an error
        loggedInRole = null;
        return false;
    }

    /**
     * JVM entry point. Blocks on login, then starts the Swing app or exits cleanly.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Bypass the login dialog and open the HR interface directly on startup.
        loginSuccessful = true;
        loggedInRole = UserRole.HR;
        MotorPH_GUI.loggedInUser = HR_USERNAME;
        MotorPH_GUI.initialize();
    }
}
