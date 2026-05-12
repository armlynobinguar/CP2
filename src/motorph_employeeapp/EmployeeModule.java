
package MotorPH_EmployeeApp;

/**
 * EmployeeModule
 * It provides global constants and static procedures to handle 
 * raw employee data arrays (String[]).
 */
public class EmployeeModule {
    // CSV Column Indices
    public static final int ID = 0;
    public static final int LAST_NAME = 1;
    public static final int FIRST_NAME = 2;
    public static final int BIRTHDAY = 3;
    public static final int HOURLY_RATE = 18;

    /**
     * Centralizes name formatting.
     * @param emp Array of employee strings.
     * @return Formatted full name.
     */
    public static String fullName(String[] emp) {
        if (emp == null || emp.length < 3) return "Unknown";
        return emp[FIRST_NAME] + " " + emp[LAST_NAME]; 
    }

    /**
     * Safely extracts the hourly rate.
     * @param emp Array of employee strings.
     * @return Hourly rate as double.
     */
    public static double getHourlyRate(String[] emp) {
        try {
            return Double.parseDouble(emp[HOURLY_RATE].replace(",", "").trim());
        } catch (Exception e) { 
            return 0.0; 
        }
    }
}