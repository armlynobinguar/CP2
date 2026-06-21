
package motorph_employeeapp;

import java.util.ArrayList;
import java.util.List;

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
 *   Index 10 - Status
 *   Index 11 - Position
 *   Index 12 - Department
 *   Index 13 - Immediate supervisor
 *   Index 14 - Basic salary
 *   Indices 15–18 - Allowances and gross semi-monthly rate
 *   Index 19 - Hourly rate (may contain commas, e.g. "535.71")
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

    /** CSV column index: home address. */
    public static final int ADDRESS = 4;

    /** CSV column index: contact phone number. */
    public static final int PHONE = 5;

    /** CSV column index: SSS membership number. */
    public static final int SSS = 6;

    /** CSV column index: PhilHealth membership number. */
    public static final int PHILHEALTH = 7;

    /** CSV column index: BIR Tax Identification Number. */
    public static final int TIN = 8;

    /** CSV column index: Pag-IBIG membership number. */
    public static final int PAGIBIG = 9;

    /** Total number of columns in the Employee Details CSV. */
    public static final int COLUMN_COUNT = 24;

    /** CSV column index: employment status (Regular, Probationary, etc.). */
    public static final int STATUS = 10;

    /** CSV column index: job title / position (e.g. Chief Executive Officer). */
    public static final int POSITION = 11;

    /** CSV column index: department (e.g. Human Resources). */
    public static final int DEPARTMENT = 12;

    /** CSV column index: immediate supervisor name (or "N/A"). */
    public static final int IMMEDIATE_SUPERVISOR = 13;

    /** CSV column index: basic monthly salary (formatted with thousand separators). */
    public static final int BASIC_SALARY = 14;

    /** CSV column index: rice subsidy allowance. */
    public static final int RICE_SUBSIDY = 15;

    /** CSV column index: phone allowance. */
    public static final int PHONE_ALLOWANCE = 16;

    /** CSV column index: clothing allowance. */
    public static final int CLOTHING_ALLOWANCE = 17;

    /** CSV column index: gross semi-monthly rate. */
    public static final int GROSS_SEMI_MONTHLY = 18;

    /**
     * CSV column index: hourly pay rate used in {@link SalaryComputationModule#calculatePayroll}.
     * Must be parsed with comma stripping before numeric conversion.
     */
    public static final int HOURLY_RATE = 19;

    /** CSV column index: total hours worked for the last computed pay period. */
    public static final int HOURS_WORKED = 20;

    /** CSV column index: computed gross pay for the last pay period. */
    public static final int GROSS_PAY = 21;

    /** CSV column index: computed total deductions for the last pay period. */
    public static final int TOTAL_DEDUCTIONS = 22;

    /** CSV column index: computed net pay for the last pay period. */
    public static final int NET_PAY = 23;

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
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Finds employees whose ID, first name, last name, or full name matches the query.
     * Used by HR directory search and employee lookup screens.
     *
     * @param query free-text search term (case-insensitive substring match)
     * @return matching CSV rows; empty list when query is blank or no matches
     */
    public static List<String[]> searchByNameOrId(String query) {
        List<String[]> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }
        String q = query.trim().toLowerCase();
        for (String[] emp : FileHandlerModule.getAllEmployees()) {
            if (emp == null || emp.length < 3) {
                continue;
            }
            // Build comparable strings from ID and name columns
            String id = emp[ID] == null ? "" : emp[ID].trim();
            String first = emp[FIRST_NAME] == null ? "" : emp[FIRST_NAME].trim().toLowerCase();
            String last = emp[LAST_NAME] == null ? "" : emp[LAST_NAME].trim().toLowerCase();
            String full = (first + " " + last).trim();
            // Match exact ID or partial match on any name field
            if (id.equals(q) || id.contains(q) || first.contains(q) || last.contains(q) || full.contains(q)) {
                results.add(emp);
            }
        }
        return results;
    }
}
