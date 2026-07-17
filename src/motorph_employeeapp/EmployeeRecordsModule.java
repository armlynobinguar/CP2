package motorph_employeeapp;

import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeRecordsModule
 * ---------------------
 * Validation and row-building helpers for the HR Employee Records screen.
 *
 * <p>Keeps CRUD business rules separate from Swing event handlers in {@link MotorPH_GUI}:
 * form validation, NA normalization, hourly-rate derivation from gross semi-monthly pay,
 * and mapping between popup fields and 24-column CSV rows via {@link EmployeeModule} indices.</p>
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
             + "Hourly Rate:           PHP " + safe(emp, EmployeeModule.HOURLY_RATE) + "\n"
             + "Hours Worked:          " + safe(emp, EmployeeModule.HOURS_WORKED) + "\n"
             + "Gross Pay:             PHP " + safe(emp, EmployeeModule.GROSS_PAY) + "\n"
             + "Total Deductions:      PHP " + safe(emp, EmployeeModule.TOTAL_DEDUCTIONS) + "\n"
             + "Net Pay:               PHP " + safe(emp, EmployeeModule.NET_PAY);
    }

    /**
     * Validates the employee record form before add or update.
     */
    public static List<String> validateForm(RecordFormData form, boolean isUpdate, String originalId) {
        java.util.LinkedHashSet<String> errors = new java.util.LinkedHashSet<>();

        if (form == null) {
            errors.add("Form data is missing.");
            return new ArrayList<>(errors);
        }

        if (isBlank(form.empNo)) {
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
        if (isBlank(form.birthday)) {
            errors.add("Birthday is required. Click the calendar icon and select a date (MM/DD/YYYY).");
        } else if (!form.birthday.trim().matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            errors.add("Birthday must be in MM/DD/YYYY format. Use the calendar icon to pick a valid date.");
        }

        if (isBlank(form.phone)) {
            errors.add("Phone number is required.");
        } else if (!form.phone.trim().matches("[0-9\\-]+")) {
            errors.add("Phone number must contain digits and dashes only.");
        }

        validateDigitsAndDashes(form.sss, "SSS Number", 10, errors);
        validateDigitsAndDashes(form.philHealth, "PhilHealth Number", 12, errors);
        validateDigitsAndDashes(form.tin, "TIN Number", 12, errors);
        validateDigitsAndDashes(form.pagIbig, "Pag-IBIG Number", 12, errors);

        if (isBlank(form.status)) errors.add("Status is required.");
        if (isBlank(form.position)) errors.add("Position is required.");
        if (isBlank(form.department)) errors.add("Department is required.");
        if (isBlank(form.supervisor)) errors.add("Supervisor is required.");

        validateRequiredNumeric(form.basicSalary, "Basic Salary", errors);
        validateRequiredNumeric(form.riceSubsidy, "Rice Subsidy", errors);
        validateRequiredNumeric(form.phoneAllowance, "Phone Allowance", errors);
        validateRequiredNumeric(form.clothingAllowance, "Clothing Allowance", errors);

        if (!isBlank(form.grossSemiMonthly) && !isNumeric(form.grossSemiMonthly)) {
            errors.add("Gross Semi-monthly must be a valid number.");
        }
        if (!isBlank(form.hourlyRate) && !isNumeric(form.hourlyRate)) {
            errors.add("Hourly Rate must be a valid number.");
        }

        return new ArrayList<>(errors);
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
        row[EmployeeModule.GROSS_SEMI_MONTHLY] = defaultIfBlank(form.grossSemiMonthly,
                safe(row, EmployeeModule.GROSS_SEMI_MONTHLY));
        String hourly = computeHourlyRateFromGrossSemiMonthly(form.grossSemiMonthly);
        row[EmployeeModule.HOURLY_RATE] = hourly.isEmpty()
                ? defaultIfBlank(form.hourlyRate, safe(row, EmployeeModule.HOURLY_RATE))
                : hourly;
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
        withDefaults.grossSemiMonthly = defaultIfBlank(form.grossSemiMonthly, "0");
        withDefaults.hourlyRate = defaultIfBlank(form.hourlyRate, "0");
        return applyFormToRow(null, withDefaults);
    }

    /**
     * Plain data holder for HR Add/Edit employee popup fields.
     * Populated from {@code JTextField} / combo values in {@link MotorPH_GUI}, then passed
     * to {@link #validateForm}, {@link #applyFormToRow}, and {@link #buildFullRowFromForm}.
     */
    public static class RecordFormData {
        /** Employee number (CSV column {@link EmployeeModule#ID}). */
        public String empNo;
        /** Last name (CSV column {@link EmployeeModule#LAST_NAME}). */
        public String lastName;
        /** First name (CSV column {@link EmployeeModule#FIRST_NAME}). */
        public String firstName;
        /** Birthday in MM/DD/YYYY from date picker (CSV column {@link EmployeeModule#BIRTHDAY}). */
        public String birthday;
        /** Home address (CSV column {@link EmployeeModule#ADDRESS}). */
        public String address;
        /** Contact phone (CSV column {@link EmployeeModule#PHONE}). */
        public String phone;
        /** SSS membership number (CSV column {@link EmployeeModule#SSS}). */
        public String sss;
        /** PhilHealth number (CSV column {@link EmployeeModule#PHILHEALTH}). */
        public String philHealth;
        /** BIR TIN, format  (CSV column {@link EmployeeModule#TIN}). */
        public String tin;
        /** Pag-IBIG number (CSV column {@link EmployeeModule#PAGIBIG}). */
        public String pagIbig;
        /** Employment status: Regular, Probationary, etc. (CSV column {@link EmployeeModule#STATUS}). */
        public String status;
        /** Job title (CSV column {@link EmployeeModule#POSITION}). */
        public String position;
        /** Department name (CSV column {@link EmployeeModule#DEPARTMENT}). */
        public String department;
        /** Immediate supervisor display name (CSV column {@link EmployeeModule#IMMEDIATE_SUPERVISOR}). */
        public String supervisor;
        /** Basic monthly salary string (CSV column {@link EmployeeModule#BASIC_SALARY}). */
        public String basicSalary;
        /** Rice subsidy allowance (CSV column {@link EmployeeModule#RICE_SUBSIDY}). */
        public String riceSubsidy;
        /** Phone allowance (CSV column {@link EmployeeModule#PHONE_ALLOWANCE}). */
        public String phoneAllowance;
        /** Clothing allowance (CSV column {@link EmployeeModule#CLOTHING_ALLOWANCE}). */
        public String clothingAllowance;
        /** Gross semi-monthly pay; hourly rate is derived from this when valid (CSV column {@link EmployeeModule#GROSS_SEMI_MONTHLY}). */
        public String grossSemiMonthly;
        /** Hourly rate override or auto-computed value (CSV column {@link EmployeeModule#HOURLY_RATE}). */
        public String hourlyRate;
    }

    /**
     * Validates all fields on the Add Employee popup before writing to CSV.
     */
    public static List<String> validateAddPopup(RecordFormData form) {
        return validateForm(form, false, null);
    }

    /**
     * Validates all fields on the Edit Employee popup before updating CSV.
     */
    public static List<String> validateEditPopup(RecordFormData form, String originalId) {
        return validateForm(form, true, originalId);
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
        row[EmployeeModule.GROSS_SEMI_MONTHLY] = defaultIfBlank(form.grossSemiMonthly,
                safe(row, EmployeeModule.GROSS_SEMI_MONTHLY));
        String hourly = computeHourlyRateFromGrossSemiMonthly(form.grossSemiMonthly);
        row[EmployeeModule.HOURLY_RATE] = hourly.isEmpty()
                ? defaultIfBlank(form.hourlyRate, safe(row, EmployeeModule.HOURLY_RATE))
                : hourly;
        return row;
    }

    /**
     * Returns true when the value is a common "not available" placeholder (NA, N/A, 000, etc.).
     */
    public static boolean isNaPlaceholder(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String compact = trimmed.replace("/", "").replace("-", "").replace(".", "")
                .replace(",", "").replace(" ", "").toUpperCase();
        return "NA".equals(compact) || "N/A".equals(compact) || "000".equals(compact);
    }

    /**
     * Normalizes numeric form input: NA / N/A / 000 / blank → {@code "0"}.
     */
    public static String normalizeNumericInput(String value) {
        if (isBlank(value) || isNaPlaceholder(value)) {
            return "0";
        }
        return value.trim();
    }

    /**
     * Clears NA-style birthday placeholders so the user must pick a date from the calendar.
     */
    public static String normalizeBirthdayInput(String value) {
        if (isBlank(value) || isNaPlaceholder(value)) {
            return "";
        }
        return value.trim();
    }

    /**
     * Value shown in the birthday field when opening Add/Edit (hides invalid or NA stored values).
     */
    public static String displayBirthdayForForm(String raw) {
        String formatted = formatBirthdayForDisplay(raw);
        return formatted == null ? "" : formatted;
    }

    /**
     * Formats a CSV birthday for display in view/edit (MM/DD/YYYY with leading zeros).
     */
    public static String formatBirthdayForDisplay(String raw) {
        if (isBlank(raw) || isNaPlaceholder(raw)) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            String[] parts = trimmed.split("/");
            return String.format("%02d/%02d/%04d",
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        }
        return trimmed;
    }

    /**
     * Applies NA → 0 and related cleanup before validation or save.
     */
    public static void sanitizeFormData(RecordFormData form) {
        if (form == null) {
            return;
        }
        form.birthday = normalizeBirthdayInput(form.birthday);
        form.basicSalary = normalizeNumericInput(form.basicSalary);
        form.riceSubsidy = normalizeNumericInput(form.riceSubsidy);
        form.phoneAllowance = normalizeNumericInput(form.phoneAllowance);
        form.clothingAllowance = normalizeNumericInput(form.clothingAllowance);
        form.grossSemiMonthly = normalizeNumericInput(form.grossSemiMonthly);
        if (isNaPlaceholder(form.hourlyRate)) {
            form.hourlyRate = "0";
        }
    }

    /**
     * Hourly rate from gross semi-monthly pay: (semi-monthly x 2) / 168 working hours.
     */
    public static String computeHourlyRateFromGrossSemiMonthly(String grossSemiMonthly) {
        if (isBlank(grossSemiMonthly) || isNaPlaceholder(grossSemiMonthly)) {
            return "0.00";
        }
        if (!isNumeric(grossSemiMonthly)) {
            return "";
        }
        double grossSemi = Double.parseDouble(grossSemiMonthly.replace(",", "").trim());
        return String.format("%.2f", grossSemi * 2.0 / 168.0);
    }

    /**
     * Recomputes Gross Semi-monthly (= Basic Salary / 2) and Hourly Rate
     * (= gross * 2 / 168) for every employee row and persists the corrected
     * values back to the CSV. Called once at app startup to repair stale data.
     */
    public static void recomputeSalaryFieldsForAllEmployees() {
        List<String[]> all = FileHandlerModule.getAllEmployees();
        if (all.isEmpty()) return;
        boolean changed = false;
        for (String[] row : all) {
            if (row.length <= EmployeeModule.BASIC_SALARY) continue;
            String rawBasic = row[EmployeeModule.BASIC_SALARY].replace(",", "").trim();
            if (rawBasic.isEmpty()) continue;
            try {
                double basic  = Double.parseDouble(rawBasic);
                double gross  = basic / 2.0;
                double hourly = gross  * 2.0 / 168.0;
                if (row.length > EmployeeModule.GROSS_SEMI_MONTHLY)
                    row[EmployeeModule.GROSS_SEMI_MONTHLY] = String.format("%.2f", gross);
                if (row.length > EmployeeModule.HOURLY_RATE)
                    row[EmployeeModule.HOURLY_RATE] = String.format("%.2f", hourly);
                changed = true;
            } catch (NumberFormatException ignored) {}
        }
        if (changed) FileHandlerModule.rewriteEmployeeFile(all);
    }

    /** Ensures a required numeric field parses after {@link #normalizeNumericInput}. */
    private static void validateRequiredNumeric(String value, String displayName, java.util.Set<String> errors) {
        String normalized = normalizeNumericInput(value);
        if (isBlank(value) || !isNumeric(normalized)) {
            errors.add(displayName + " must be a valid number (commas/periods allowed, or enter NA / 000 for zero).");
        }
    }

    /** Ensures a government ID style field contains only digits and dashes and respects digit limits. */
    private static void validateDigitsAndDashes(String value, String displayName,
            int digitLimit, java.util.Set<String> errors) {
        if (isBlank(value)) {
            errors.add(displayName + " is required.");
            return;
        }
        String trimmed = value.trim();
        int digits = 0;
        boolean validChars = true;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isDigit(ch)) {
                digits++;
            } else if (ch == '-') {
                // allowed separator
            } else {
                validChars = false;
                break;
            }
        }
        if (!validChars || digits > digitLimit) {
            if ("SSS Number".equals(displayName)) {
                errors.add("SSS Number must use numbers and hyphens only. It must not exceed 10 numbers.");
            } else if ("TIN Number".equals(displayName)) {
                errors.add("TIN Number must use numbers and hyphens only. It must not exceed 12 numbers.");
            } else if ("PhilHealth Number".equals(displayName)) {
                errors.add("Philhealth Number must use numbers and hyphens only. It must not exceed 12 numbers.");
            } else if ("Pag-IBIG Number".equals(displayName)) {
                errors.add("PAGIBIG Number must use numbers and hyphens only. It must not exceed 12 numbers.");
            } else {
                errors.add("Use numbers, and hyphens only. This must not exceed "
                        + digitLimit + " numbers.");
            }
        }
    }

    private static int countDigits(String value) {
        if (value == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    /**
     * Pads a CSV row to {@link EmployeeModule#COLUMN_COUNT} and defaults empty allowance
     * columns to {@code "0"} so downstream payroll math never sees blank strings.
     */
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

    /** Reads one CSV cell safely; null row or out-of-range index → empty string. */
    private static String safe(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) return "";
        return row[index] == null ? "" : row[index].trim();
    }

    /** True when null or whitespace-only. */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Null-safe {@link String#trim()}. */
    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** Returns {@code fallback} when {@code value} is blank after trim. */
    private static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    /** True for valid doubles or {@link #isNaPlaceholder} values treated as zero. */
    private static boolean isNumeric(String value) {
        if (isNaPlaceholder(value)) {
            return true;
        }
        try {
            Double.parseDouble(value.replace(",", "").trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
