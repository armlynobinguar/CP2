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
        "Employee #", "Last Name", "First Name", "Department", "SSS #", "PhilHealth #", "TIN #", "Pag-IBIG #"
    };

    /**
     * Maps a full CSV employee row to the seven table columns required by Feature 1.
     */
    public static Object[] toTableRow(String[] emp) {
        return new Object[] {
            safe(emp, EmployeeModule.ID),
            safe(emp, EmployeeModule.LAST_NAME),
            safe(emp, EmployeeModule.FIRST_NAME),
            safe(emp, EmployeeModule.DEPARTMENT),
            safe(emp, EmployeeModule.SSS),
            safe(emp, EmployeeModule.PHILHEALTH),
            safe(emp, EmployeeModule.TIN),
            safe(emp, EmployeeModule.PAGIBIG)
        };
    }

    /**
     * Builds a multi-line profile for the View Details dialog.
     */
    public static String formatFullProfile(String[] emp) {
        if (emp == null) return "No employee data available.";
        return "Employee ID:           " + safe(emp, EmployeeModule.ID) + "\n"
             + "Full Name:             " + EmployeeModule.fullName(emp) + "\n"
             + "Birthday:              " + safe(emp, EmployeeModule.BIRTHDAY) + "\n"
             + "Address:               " + safe(emp, EmployeeModule.ADDRESS) + "\n"
             + "Phone Number:          " + safe(emp, EmployeeModule.PHONE) + "\n"
             + "SSS #:                 " + safe(emp, EmployeeModule.SSS) + "\n"
             + "PhilHealth #:          " + safe(emp, EmployeeModule.PHILHEALTH) + "\n"
             + "TIN #:                 " + safe(emp, EmployeeModule.TIN) + "\n"
             + "Pag-IBIG #:            " + safe(emp, EmployeeModule.PAGIBIG) + "\n"
             + "Status:                " + safe(emp, EmployeeModule.STATUS) + "\n"
             + "Position:              " + safe(emp, EmployeeModule.POSITION) + "\n"
             + "Department:            " + safe(emp, EmployeeModule.DEPARTMENT) + "\n"
             + "Immediate Supervisor:  " + safe(emp, EmployeeModule.IMMEDIATE_SUPERVISOR) + "\n"
             + "Basic Salary:          PHP " + safe(emp, EmployeeModule.BASIC_SALARY) + "\n"
             + "Hourly Rate:           PHP " + safe(emp, EmployeeModule.HOURLY_RATE);
    }

    /**
     * Validates the employee record form before add or update.
     */
    public static List<String> validateForm(RecordFormData form, boolean isUpdate, String originalId) {
        List<String> errors = new ArrayList<>();

        if (form.empNo == null || form.empNo.trim().isEmpty()) {
            errors.add("Employee Number is required.");
        } else if (!form.empNo.trim().matches("\\d+")) {
            errors.add("Employee Number must be numeric.");
        } else if (!isUpdate && FileHandlerModule.employeeExists(form.empNo.trim())) {
            errors.add("Employee Number \"" + form.empNo.trim() + "\" already exists.");
        } else if (isUpdate && originalId != null && !originalId.trim().equals(form.empNo.trim())
                && FileHandlerModule.employeeExists(form.empNo.trim())) {
            errors.add("Employee Number \"" + form.empNo.trim() + "\" is already assigned to another record.");
        }

        if (isBlank(form.lastName)) errors.add("Last Name is required.");
        if (isBlank(form.firstName)) errors.add("First Name is required.");
        if (isBlank(form.sss)) errors.add("SSS Number is required.");
        if (isBlank(form.philHealth)) errors.add("PhilHealth Number is required.");
        if (isBlank(form.tin)) errors.add("TIN Number is required.");
        if (isBlank(form.pagIbig)) errors.add("Pag-IBIG Number is required.");

        if (!isBlank(form.basicSalary) && !isNumeric(form.basicSalary)) {
            errors.add("Basic Salary must be a valid number.");
        }
        if (!isBlank(form.hourlyRate) && !isNumeric(form.hourlyRate)) {
            errors.add("Hourly Rate must be a valid number.");
        }

        return errors;
    }

    /**
     * Merges form values into an existing employee row, preserving untouched columns.
     */
    public static String[] applyFormToRow(String[] existing, RecordFormData form) {
        String[] row = normalizeRow(existing);
        row[EmployeeModule.ID] = trim(form.empNo);
        row[EmployeeModule.LAST_NAME] = trim(form.lastName);
        row[EmployeeModule.FIRST_NAME] = trim(form.firstName);
        row[EmployeeModule.BIRTHDAY] = defaultIfBlank(form.birthday, safe(row, EmployeeModule.BIRTHDAY));
        row[EmployeeModule.ADDRESS] = defaultIfBlank(form.address, safe(row, EmployeeModule.ADDRESS));
        row[EmployeeModule.PHONE] = defaultIfBlank(form.phone, safe(row, EmployeeModule.PHONE));
        row[EmployeeModule.SSS] = trim(form.sss);
        row[EmployeeModule.PHILHEALTH] = trim(form.philHealth);
        row[EmployeeModule.TIN] = trim(form.tin);
        row[EmployeeModule.PAGIBIG] = trim(form.pagIbig);
        row[EmployeeModule.STATUS] = defaultIfBlank(form.status, safe(row, EmployeeModule.STATUS));
        row[EmployeeModule.POSITION] = defaultIfBlank(form.position, safe(row, EmployeeModule.POSITION));
        row[EmployeeModule.DEPARTMENT] = defaultIfBlank(form.department,
                DepartmentModule.inferDepartmentFromPosition(form.position));
        row[EmployeeModule.IMMEDIATE_SUPERVISOR] = defaultIfBlank(form.supervisor,
                DepartmentModule.resolveSupervisor(row[EmployeeModule.DEPARTMENT], row[EmployeeModule.POSITION]));
        row[EmployeeModule.BASIC_SALARY] = defaultIfBlank(form.basicSalary, safe(row, EmployeeModule.BASIC_SALARY));
        if (row.length > EmployeeModule.RICE_SUBSIDY) {
            row[EmployeeModule.RICE_SUBSIDY] = defaultIfBlank(form.riceSubsidy, safe(row, EmployeeModule.RICE_SUBSIDY));
        }
        if (row.length > EmployeeModule.PHONE_ALLOWANCE) {
            row[EmployeeModule.PHONE_ALLOWANCE] = defaultIfBlank(form.phoneAllowance, safe(row, EmployeeModule.PHONE_ALLOWANCE));
        }
        if (row.length > EmployeeModule.CLOTHING_ALLOWANCE) {
            row[EmployeeModule.CLOTHING_ALLOWANCE] = defaultIfBlank(form.clothingAllowance, safe(row, EmployeeModule.CLOTHING_ALLOWANCE));
        }
        row[EmployeeModule.HOURLY_RATE] = defaultIfBlank(form.hourlyRate, safe(row, EmployeeModule.HOURLY_RATE));
        return row;
    }

    /**
     * Builds a new employee row from the form (defaults for blank optional fields).
     */
    public static String[] createNewRow(RecordFormData form) {
        RecordFormData withDefaults = new RecordFormData();
        withDefaults.empNo = form.empNo;
        withDefaults.lastName = form.lastName;
        withDefaults.firstName = form.firstName;
        withDefaults.birthday = defaultIfBlank(form.birthday, "N/A");
        withDefaults.address = defaultIfBlank(form.address, "N/A");
        withDefaults.phone = defaultIfBlank(form.phone, "N/A");
        withDefaults.sss = form.sss;
        withDefaults.philHealth = form.philHealth;
        withDefaults.tin = form.tin;
        withDefaults.pagIbig = form.pagIbig;
        withDefaults.status = defaultIfBlank(form.status, "Probationary");
        withDefaults.position = defaultIfBlank(form.position, "N/A");
        withDefaults.department = defaultIfBlank(form.department,
                DepartmentModule.inferDepartmentFromPosition(withDefaults.position));
        withDefaults.supervisor = defaultIfBlank(form.supervisor,
                DepartmentModule.resolveSupervisor(withDefaults.department, withDefaults.position));
        withDefaults.basicSalary = defaultIfBlank(form.basicSalary, "0");
        withDefaults.hourlyRate = defaultIfBlank(form.hourlyRate, "0");
        return applyFormToRow(null, withDefaults);
    }

    /** Simple DTO for employee record form field values. */
    public static class RecordFormData {
        public String empNo;
        public String lastName;
        public String firstName;
        public String birthday;
        public String address;
        public String phone;
        public String sss;
        public String philHealth;
        public String tin;
        public String pagIbig;
        public String status;
        public String position;
        public String department;
        public String supervisor;
        public String basicSalary;
        public String riceSubsidy;
        public String phoneAllowance;
        public String clothingAllowance;
        public String grossSemiMonthly;
        public String hourlyRate;
    }

    /**
     * Validates all fields on the Add Employee popup before writing to CSV.
     */
    public static List<String> validateAddPopup(RecordFormData form) {
        List<String> errors = new ArrayList<>(validateForm(form, false, null));
        addExtendedPopupValidation(form, errors);
        return errors;
    }

    /**
     * Validates all fields on the Edit Employee popup before updating CSV.
     */
    public static List<String> validateEditPopup(RecordFormData form, String originalId) {
        List<String> errors = new ArrayList<>(validateForm(form, true, originalId));
        addExtendedPopupValidation(form, errors);
        return errors;
    }

    /**
     * Returns non-blocking warnings for incomplete or invalid stored employee data (View dialog).
     */
    public static List<String> collectViewWarnings(String[] emp) {
        List<String> warnings = new ArrayList<>();
        if (emp == null || emp.length == 0) {
            warnings.add("Employee record is empty or could not be parsed.");
            return warnings;
        }
        if (isBlank(safe(emp, EmployeeModule.ID))) {
            warnings.add("Employee Number is missing.");
        } else if (!safe(emp, EmployeeModule.ID).matches("\\d+")) {
            warnings.add("Employee Number is not numeric.");
        }
        if (isBlank(safe(emp, EmployeeModule.LAST_NAME))) warnings.add("Last Name is missing.");
        if (isBlank(safe(emp, EmployeeModule.FIRST_NAME))) warnings.add("First Name is missing.");
        if (isBlank(safe(emp, EmployeeModule.SSS))) warnings.add("SSS Number is missing.");
        if (isBlank(safe(emp, EmployeeModule.PHILHEALTH))) warnings.add("PhilHealth Number is missing.");
        if (isBlank(safe(emp, EmployeeModule.TIN))) warnings.add("TIN Number is missing.");
        if (isBlank(safe(emp, EmployeeModule.PAGIBIG))) warnings.add("Pag-IBIG Number is missing.");
        if (isBlank(safe(emp, EmployeeModule.DEPARTMENT))) warnings.add("Department is missing.");
        String birthday = safe(emp, EmployeeModule.BIRTHDAY);
        if (!isBlank(birthday) && !birthday.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            warnings.add("Birthday is not in MM/DD/YYYY format.");
        }
        String basic = safe(emp, EmployeeModule.BASIC_SALARY);
        if (!isBlank(basic) && !isNumeric(basic)) {
            warnings.add("Basic Salary contains invalid numeric data.");
        }
        return warnings;
    }

    /**
     * Builds a complete CSV row (19 columns) from popup form values for a new employee.
     */
    public static String[] buildFullRowFromForm(RecordFormData form) {
        String[] row = createNewRow(form);
        row[EmployeeModule.RICE_SUBSIDY] = defaultIfBlank(form.riceSubsidy, "0");
        row[EmployeeModule.PHONE_ALLOWANCE] = defaultIfBlank(form.phoneAllowance, "0");
        row[EmployeeModule.CLOTHING_ALLOWANCE] = defaultIfBlank(form.clothingAllowance, "0");
        if (!isBlank(form.basicSalary) && isNumeric(form.basicSalary)) {
            double basic = Double.parseDouble(form.basicSalary.replace(",", "").trim());
            row[EmployeeModule.GROSS_SEMI_MONTHLY] = String.format("%.2f", basic / 2.0);
            row[EmployeeModule.HOURLY_RATE] = String.format("%.2f", basic / 168.0);
        } else {
            row[EmployeeModule.GROSS_SEMI_MONTHLY] = defaultIfBlank(form.grossSemiMonthly,
                    safe(row, EmployeeModule.GROSS_SEMI_MONTHLY));
        }
        return row;
    }

    private static void addExtendedPopupValidation(RecordFormData form, List<String> errors) {
        if (isBlank(form.birthday)) {
            errors.add("Birthday is required.");
        } else if (!form.birthday.trim().matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            errors.add("Birthday must be in MM/DD/YYYY format.");
        }
        if (isBlank(form.address)) errors.add("Address is required.");
        if (isBlank(form.phone)) errors.add("Phone number is required.");
        if (isBlank(form.position)) errors.add("Position is required.");
        if (isBlank(form.department)) errors.add("Department is required.");
        if (isBlank(form.supervisor)) errors.add("Supervisor is required.");
        validateRequiredNumeric(form.basicSalary, "Basic Salary", errors);
        validateRequiredNumeric(form.riceSubsidy, "Rice Subsidy", errors);
        validateRequiredNumeric(form.phoneAllowance, "Phone Allowance", errors);
        validateRequiredNumeric(form.clothingAllowance, "Clothing Allowance", errors);
        validateIdFormatComplete(form.sss, "XX-XXXXXXX-X", "SSS Number", errors);
        validateIdFormatComplete(form.tin, "XXX-XXX-XXX-XXX", "TIN Number", errors);
    }

    private static void validateRequiredNumeric(String value, String displayName, List<String> errors) {
        if (isBlank(value)) {
            errors.add(displayName + " is required.");
        } else if (!isNumeric(value)) {
            errors.add(displayName + " must be a valid number.");
        }
    }

    private static void validateIdFormatComplete(String value, String pattern, String displayName,
            List<String> errors) {
        if (isBlank(value)) {
            return;
        }
        int expectedDigits = pattern.replace("-", "").length();
        int actualDigits = value.replaceAll("[^0-9]", "").length();
        if (actualDigits < expectedDigits) {
            errors.add(displayName + " must follow the format " + pattern + ".");
        }
    }

    private static String[] normalizeRow(String[] existing) {
        String[] row = new String[EmployeeModule.COLUMN_COUNT];
        if (existing != null) {
            for (int i = 0; i < Math.min(existing.length, row.length); i++) {
                row[i] = existing[i];
            }
        }
        for (int i = 0; i < row.length; i++) {
            if (row[i] == null) row[i] = "";
        }
        if (row.length > EmployeeModule.RICE_SUBSIDY && row[EmployeeModule.RICE_SUBSIDY].isEmpty()) {
            row[EmployeeModule.RICE_SUBSIDY] = "0";
        }
        if (row.length > EmployeeModule.PHONE_ALLOWANCE && row[EmployeeModule.PHONE_ALLOWANCE].isEmpty()) {
            row[EmployeeModule.PHONE_ALLOWANCE] = "0";
        }
        if (row.length > EmployeeModule.CLOTHING_ALLOWANCE && row[EmployeeModule.CLOTHING_ALLOWANCE].isEmpty()) {
            row[EmployeeModule.CLOTHING_ALLOWANCE] = "0";
        }
        if (row.length > EmployeeModule.GROSS_SEMI_MONTHLY && row[EmployeeModule.GROSS_SEMI_MONTHLY].isEmpty()) {
            row[EmployeeModule.GROSS_SEMI_MONTHLY] = "0";
        }
        return row;
    }

    private static String safe(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) return "";
        return row[index] == null ? "" : row[index].trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value.replace(",", "").trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
