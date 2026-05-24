
package motorph_employeeapp;

/**
 * EmployeeModule
 * --------------
 * Utility module for interpreting employee records parsed from CSV.
 *
 * Employee rows are stored as {@code String[]} arrays after {@link FileHandlerModule#smartSplit(String)}.
 * Column indices below map to the MotorPH Employee Details CSV structure:
 *
 *   Index 0  - Employee ID (e.g. "10001")
 *   Index 1  - Last name
 *   Index 2  - First name
 *   Index 3  - Birthday
 *   ... (columns 4–17 hold address, contact, and employment fields)
 *   Index 18 - Hourly rate (may contain commas, e.g. "535.71")
 */
public class EmployeeModule {

    /** CSV column index: unique employee identifier. */
    public static final int ID = 0;

    /** CSV column index: employee last name. */
    public static final int LAST_NAME = 1;

    /** CSV column index: employee first name. */
    public static final int FIRST_NAME = 2;

    /** CSV column index: employee birthday (display format from CSV). */
    public static final int BIRTHDAY = 3;

    /**
     * CSV column index: hourly pay rate used in {@link SalaryComputationModule#calculatePayroll}.
     * Must be parsed with comma stripping before numeric conversion.
     */
    public static final int HOURLY_RATE = 18;

    /**
     * Builds a display name from parsed employee data.
     *
     * @param emp split CSV row; must contain at least first and last name columns
     * @return "FirstName LastName", or "Unknown" if data is missing or too short
     */
    public static String fullName(String[] emp) {
        if (emp == null || emp.length < 3) return "Unknown";
        return emp[FIRST_NAME] + " " + emp[LAST_NAME];
    }

    /**
     * Reads hourly rate from the employee record for gross salary multiplication.
     *
     * @param emp split CSV row containing rate at index {@link #HOURLY_RATE}
     * @return parsed hourly rate, or 0.0 if missing or malformed
     */
    public static double getHourlyRate(String[] emp) {
        try {
            // Remove thousand separators before parsing (e.g. "1,234.56" -> "1234.56")
            return Double.parseDouble(emp[HOURLY_RATE].replace(",", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
