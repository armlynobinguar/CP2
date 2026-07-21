
package motorph_employeeapp;

/**
 * MotorPH_EmployeeApp
 * -------------------
 * Main entry point for the HR-only administrative tool.
 */
public class MotorPH_EmployeeApp {

    /** HR admin portal role. */
    enum UserRole {
        HR
    }

    /** Primary HR admin username. */
    static final String HR_USERNAME = "hr";
    /** Primary HR admin password. */
    static final String HR_PASSWORD = "hr12345";
    /** Legacy HR username kept for backward compatibility with older docs. */
    static final String HR_USERNAME_LEGACY = "payroll_staff";
    /** Legacy HR password kept for backward compatibility with older docs. */
    static final String HR_PASSWORD_LEGACY = "password123";

    /** @deprecated Retained for compatibility with older references. */
    @Deprecated
    static final String AUTH_VAL_2 = HR_USERNAME;

    /** @deprecated Retained for compatibility with older references. */
    @Deprecated
    static final String AUTH_VAL_4 = HR_PASSWORD;

    /** Marks the application as initialized for the HR administrative experience. */
    static boolean loginSuccessful = true;

    /** Role of the current session. */
    static UserRole loggedInRole = UserRole.HR;

    /**
     * Validates the supplied credentials against the HR admin credentials.
     */
    static boolean authenticate(String username, String password) {
        if ((HR_USERNAME.equals(username) && HR_PASSWORD.equals(password))
                || (HR_USERNAME_LEGACY.equals(username) && HR_PASSWORD_LEGACY.equals(password))) {
            loggedInRole = UserRole.HR;
            loginSuccessful = true;
            return true;
        }
        loggedInRole = null;
        loginSuccessful = false;
        return false;
    }

    /**
     * Compatibility helper for legacy UI call sites.
     * The HR-only application does not link a session to a separate employee record.
     */
    static String getLinkedEmployeeId(String username) {
        return "";
    }

    /**
     * JVM entry point. Shows the HR login screen first, then opens the
     * administration workspace after successful authentication.
     */
    public static void main(String[] args) {
        loggedInRole = UserRole.HR;
        loginSuccessful = false;
        MotorPH_GUI.showCustomLoginDialog();
        if (loginSuccessful) {
            MotorPH_GUI.initialize();
        } else {
            System.exit(0);
        }
    }
}
