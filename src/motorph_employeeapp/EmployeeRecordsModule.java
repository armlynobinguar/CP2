package motorph_employeeapp;

import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeRecordsModule
 * ---------------------
 * Validation and row-building helpers for the Employee Records management screen.
 * Keeps CRUD business rules separate from Swing event handlers in {@link MotorPH_GUI}.
 */
public class EmployeeRecordsModule {

    /** Column headers shown in the employee records {@code JTable}. */
    public static final String[] TABLE_COLUMNS = {
        "Employee #", "Last Name", "First Name", "SSS #", "PhilHealth #", "TIN #", "Pag-IBIG #"
    };

    /**
     * Maps a full CSV employee row to the seven table columns required by Feature 1.
     */
    public static Object[] toTableRow(String[] emp) {
        return new Object[] {
            safe(emp, EmployeeModule.ID),
            safe(emp, EmployeeModule.LAST_NAME),
            safe(emp, EmployeeModule.FIRST_NAME),
            safe(emp, EmployeeModule.SSS),
            safe(emp, EmployeeModule.PHILHEALTH),
            safe(emp, EmployeeModule.TIN),
            safe(emp, EmployeeModule.PAGIBIG)
        };
    }

    /**
     * Validates the employee record form before add or update.
     *
     * @param empNo      employee number from the form
     * @param lastName   last name
     * @param firstName  first name
     * @param sss        SSS number
     * @param philHealth PhilHealth number
     * @param tin        TIN number
     * @param pagIbig    Pag-IBIG number
     * @param isUpdate   true when editing an existing record
     * @param originalId employee ID currently being edited (ignored on add)
     * @return list of validation error messages; empty when valid
     */
    public static List<String> validateForm(String empNo, String lastName, String firstName,
            String sss, String philHealth, String tin, String pagIbig,
            boolean isUpdate, String originalId) {
        List<String> errors = new ArrayList<>();

        if (empNo == null || empNo.trim().isEmpty()) {
            errors.add("Employee Number is required.");
        } else if (!empNo.trim().matches("\\d+")) {
            errors.add("Employee Number must be numeric.");
        } else if (!isUpdate && FileHandlerModule.employeeExists(empNo.trim())) {
            errors.add("Employee Number \"" + empNo.trim() + "\" already exists.");
        } else if (isUpdate && originalId != null && !originalId.trim().equals(empNo.trim())
                && FileHandlerModule.employeeExists(empNo.trim())) {
            errors.add("Employee Number \"" + empNo.trim() + "\" is already assigned to another record.");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            errors.add("Last Name is required.");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            errors.add("First Name is required.");
        }
        if (sss == null || sss.trim().isEmpty()) {
            errors.add("SSS Number is required.");
        }
        if (philHealth == null || philHealth.trim().isEmpty()) {
            errors.add("PhilHealth Number is required.");
        }
        if (tin == null || tin.trim().isEmpty()) {
            errors.add("TIN Number is required.");
        }
        if (pagIbig == null || pagIbig.trim().isEmpty()) {
            errors.add("Pag-IBIG Number is required.");
        }

        return errors;
    }

    /**
     * Merges form values into an existing employee row, preserving untouched columns.
     */
    public static String[] applyFormToRow(String[] existing, String empNo, String lastName,
            String firstName, String sss, String philHealth, String tin, String pagIbig) {
        String[] row = normalizeRow(existing);
        row[EmployeeModule.ID] = empNo.trim();
        row[EmployeeModule.LAST_NAME] = lastName.trim();
        row[EmployeeModule.FIRST_NAME] = firstName.trim();
        row[EmployeeModule.SSS] = sss.trim();
        row[EmployeeModule.PHILHEALTH] = philHealth.trim();
        row[EmployeeModule.TIN] = tin.trim();
        row[EmployeeModule.PAGIBIG] = pagIbig.trim();
        return row;
    }

    /**
     * Builds a new employee row with defaults for columns not captured on the form.
     */
    public static String[] createNewRow(String empNo, String lastName, String firstName,
            String sss, String philHealth, String tin, String pagIbig) {
        String[] row = new String[EmployeeModule.COLUMN_COUNT];
        row[EmployeeModule.ID] = empNo.trim();
        row[EmployeeModule.LAST_NAME] = lastName.trim();
        row[EmployeeModule.FIRST_NAME] = firstName.trim();
        row[EmployeeModule.BIRTHDAY] = "N/A";
        row[4] = "N/A";
        row[5] = "N/A";
        row[EmployeeModule.SSS] = sss.trim();
        row[EmployeeModule.PHILHEALTH] = philHealth.trim();
        row[EmployeeModule.TIN] = tin.trim();
        row[EmployeeModule.PAGIBIG] = pagIbig.trim();
        row[EmployeeModule.STATUS] = "Probationary";
        row[EmployeeModule.POSITION] = "N/A";
        row[EmployeeModule.IMMEDIATE_SUPERVISOR] = "N/A";
        row[EmployeeModule.BASIC_SALARY] = "0";
        row[14] = "0";
        row[15] = "0";
        row[16] = "0";
        row[17] = "0";
        row[EmployeeModule.HOURLY_RATE] = "0";
        return row;
    }

    private static String[] normalizeRow(String[] existing) {
        String[] row = new String[EmployeeModule.COLUMN_COUNT];
        if (existing != null) {
            for (int i = 0; i < Math.min(existing.length, row.length); i++) {
                row[i] = existing[i];
            }
        }
        for (int i = 0; i < row.length; i++) {
            if (row[i] == null) {
                row[i] = "";
            }
        }
        return row;
    }

    private static String safe(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) {
            return "";
        }
        return row[index] == null ? "" : row[index].trim();
    }
}
