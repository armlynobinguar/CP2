
package motorph_employeeapp;

/**
 * MotorPH_EmployeeApp
 * Main controller handling system authentication.
 */
public class MotorPH_EmployeeApp {

    // GLOBAL AUTHENTICATION CREDENTIAL CONSTANTS
    static final String AUTH_VAL_1 = "employee";
    static final String AUTH_VAL_2 = "payroll_staff";
    static final String AUTH_VAL_3 = "12345";
    static final String AUTH_VAL_4 = "password123";

    static boolean loginSuccessful = false;

    /**
     * Main procedural program entry point. (From Lesson: Frames)
     */
    public static void main(String[] args) {
        // Render Login window strictly tracking global boolean state flag references
        MotorPH_GUI.showCustomLoginDialog();

        if (loginSuccessful) {
            // Directly launch matching BroCode setup
            MotorPH_GUI.initialize();
        } else {
            System.exit(0);
        }
    }
}