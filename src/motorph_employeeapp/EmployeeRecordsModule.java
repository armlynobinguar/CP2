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

    /** Demo monthly basic salary floor (NCR minimum wage approximation). */
    public static final double MIN_BASIC_SALARY_MONTHLY = 15000.0;

    /** Soft warning threshold for unusually high basic salary. */
    public static final double HIGH_BASIC_SALARY_WARNING = 500000.0;

    /** Stable field identifiers for structured validation and UI highlighting. */
    public static final class FieldKeys {
        public static final String EMP_NO = "EMP_NO";
        public static final String LAST_NAME = "LAST_NAME";
        public static final String FIRST_NAME = "FIRST_NAME";
        public static final String BIRTHDAY = "BIRTHDAY";
        public static final String ADDRESS = "ADDRESS";
        public static final String PHONE = "PHONE";
        public static final String SSS = "SSS";
        public static final String PHILHEALTH = "PHILHEALTH";
        public static final String TIN = "TIN";
        public static final String PAGIBIG = "PAGIBIG";
        public static final String STATUS = "STATUS";
        public static final String DEPARTMENT = "DEPARTMENT";
        public static final String POSITION = "POSITION";
        public static final String SUPERVISOR = "SUPERVISOR";
        public static final String BASIC_SALARY = "BASIC_SALARY";
        public static final String RICE_SUBSIDY = "RICE_SUBSIDY";
        public static final String PHONE_ALLOWANCE = "PHONE_ALLOWANCE";
        public static final String CLOTHING_ALLOWANCE = "CLOTHING_ALLOWANCE";
        public static final String GROSS_SEMI_MONTHLY = "GROSS_SEMI_MONTHLY";
        public static final String HOURLY_RATE = "HOURLY_RATE";
        public static final String GENERAL = "GENERAL";

        private FieldKeys() {
        }
    }

    /** One validation issue tied to a logical form field. */
    public static final class FieldValidationError {
        public final String fieldKey;
        public final String message;

        public FieldValidationError(String fieldKey, String message) {
            this.fieldKey = fieldKey == null ? FieldKeys.GENERAL : fieldKey;
            this.message = message == null ? "" : message;
        }
    }

    /** Maps a {@link FieldKeys} constant to the Add/Edit popup label text. */
    public static String popupLabelForFieldKey(String fieldKey) {
        if (fieldKey == null) {
            return "";
        }
        switch (fieldKey) {
            case FieldKeys.EMP_NO: return "Employee #:";
            case FieldKeys.LAST_NAME: return "Last Name:";
            case FieldKeys.FIRST_NAME: return "First Name:";
            case FieldKeys.BIRTHDAY: return "Birthday:";
            case FieldKeys.ADDRESS: return "Address:";
            case FieldKeys.PHONE: return "Phone:";
            case FieldKeys.SSS: return "SSS #:";
            case FieldKeys.PHILHEALTH: return "PhilHealth #:";
            case FieldKeys.TIN: return "TIN #:";
            case FieldKeys.PAGIBIG: return "Pag-IBIG #:";
            case FieldKeys.STATUS: return "Status:";
            case FieldKeys.DEPARTMENT: return "Department:";
            case FieldKeys.POSITION: return "Position:";
            case FieldKeys.SUPERVISOR: return "Supervisor:";
            case FieldKeys.BASIC_SALARY: return "Basic Salary:";
            case FieldKeys.RICE_SUBSIDY: return "Rice Subsidy:";
            case FieldKeys.PHONE_ALLOWANCE: return "Phone Allowance:";
            case FieldKeys.CLOTHING_ALLOWANCE: return "Clothing Allowance:";
            case FieldKeys.GROSS_SEMI_MONTHLY: return "Gross Semi-monthly:";
            case FieldKeys.HOURLY_RATE: return "Hourly Rate:";
            default: return "";
        }
    }

    /** Maps an Add/Edit popup label back to a {@link FieldKeys} constant. */
    public static String fieldKeyForPopupLabel(String popupLabel) {
        if (popupLabel == null) {
            return FieldKeys.GENERAL;
        }
        switch (popupLabel) {
            case "Employee #:": return FieldKeys.EMP_NO;
            case "Last Name:": return FieldKeys.LAST_NAME;
            case "First Name:": return FieldKeys.FIRST_NAME;
            case "Birthday:": return FieldKeys.BIRTHDAY;
            case "Address:": return FieldKeys.ADDRESS;
            case "Phone:": return FieldKeys.PHONE;
            case "SSS #:": return FieldKeys.SSS;
            case "PhilHealth #:": return FieldKeys.PHILHEALTH;
            case "TIN #:": return FieldKeys.TIN;
            case "Pag-IBIG #:": return FieldKeys.PAGIBIG;
            case "Status:": return FieldKeys.STATUS;
            case "Department:": return FieldKeys.DEPARTMENT;
            case "Position:": return FieldKeys.POSITION;
            case "Supervisor:": return FieldKeys.SUPERVISOR;
            case "Basic Salary:": return FieldKeys.BASIC_SALARY;
            case "Rice Subsidy:": return FieldKeys.RICE_SUBSIDY;
            case "Phone Allowance:": return FieldKeys.PHONE_ALLOWANCE;
            case "Clothing Allowance:": return FieldKeys.CLOTHING_ALLOWANCE;
            case "Gross Semi-monthly:": return FieldKeys.GROSS_SEMI_MONTHLY;
            case "Hourly Rate:": return FieldKeys.HOURLY_RATE;
            default: return FieldKeys.GENERAL;
        }
    }

    /** Converts structured errors to plain messages for bullet dialogs. */
    public static List<String> messagesFromErrors(List<FieldValidationError> errors) {
        List<String> messages = new ArrayList<>();
        if (errors == null) {
            return messages;
        }
        java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>();
        for (FieldValidationError error : errors) {
            if (error != null && error.message != null && !error.message.isEmpty()) {
                unique.add(error.message);
            }
        }
        messages.addAll(unique);
        return messages;
    }

    /** Counts distinct invalid fields (not duplicate messages). */
    public static int countDistinctFields(List<FieldValidationError> errors) {
        if (errors == null || errors.isEmpty()) {
            return 0;
        }
        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();
        for (FieldValidationError error : errors) {
            if (error != null) {
                keys.add(error.fieldKey);
            }
        }
        return keys.size();
    }

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
        return messagesFromErrors(validateFormFields(form, isUpdate, originalId));
    }

    /** Structured validation for HR Add/Edit popups and legacy forms. */
    public static List<FieldValidationError> validateFormFields(RecordFormData form, boolean isUpdate,
            String originalId) {
        List<FieldValidationError> errors = new ArrayList<>();
        if (form == null) {
            addError(errors, FieldKeys.GENERAL, "Form data is missing.");
            return errors;
        }

        if (isBlank(form.empNo)) {
            addError(errors, FieldKeys.EMP_NO, "Employee Number is required.");
        } else if (!form.empNo.trim().matches("\\d+")) {
            addError(errors, FieldKeys.EMP_NO, "Employee Number must be numeric.");
        } else if (!isUpdate && FileHandlerModule.employeeExists(form.empNo.trim())) {
            addError(errors, FieldKeys.EMP_NO,
                    "Employee Number \"" + form.empNo.trim() + "\" already exists.");
        } else if (isUpdate && originalId != null && !originalId.trim().equals(form.empNo.trim())
                && FileHandlerModule.employeeExists(form.empNo.trim())) {
            addError(errors, FieldKeys.EMP_NO,
                    "Employee Number \"" + form.empNo.trim() + "\" is already assigned to another record.");
        }

        if (isBlank(form.lastName)) {
            addError(errors, FieldKeys.LAST_NAME, "Last Name is required.");
        } else if (!isValidPersonName(form.lastName)) {
            addError(errors, FieldKeys.LAST_NAME,
                    "Last Name must contain letters, spaces, hyphens and apostrophes only.");
        }
        if (isBlank(form.firstName)) {
            addError(errors, FieldKeys.FIRST_NAME, "First Name is required.");
        } else if (!isValidPersonName(form.firstName)) {
            addError(errors, FieldKeys.FIRST_NAME,
                    "First Name must contain letters, spaces, hyphens and apostrophes only.");
        }

        if (isBlank(form.birthday)) {
            addError(errors, FieldKeys.BIRTHDAY,
                    "Birthday is required. Click the calendar icon and select a date (MM/DD/YYYY).");
        } else if (!form.birthday.trim().matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            addError(errors, FieldKeys.BIRTHDAY,
                    "Birthday must be in MM/DD/YYYY format. Use the calendar icon to pick a valid date.");
        }
        validateBirthdaySanity(form.birthday, errors);

        if (isBlank(form.phone)) {
            addError(errors, FieldKeys.PHONE, "Phone number is required.");
        } else if (!form.phone.trim().matches("[0-9\\-]+")) {
            addError(errors, FieldKeys.PHONE, "Phone number must contain digits and dashes only.");
        } else {
            validatePhoneDigitCount(form.phone, errors);
        }

        checkDuplicateGovernmentIds(form, errors);
        checkDuplicateGovernmentIdsAcrossDatabase(form, originalId, errors);

        validateDigitsAndDashes(form.sss, FieldKeys.SSS, "SSS Number", 10, errors);
        validateDigitsAndDashes(form.philHealth, FieldKeys.PHILHEALTH, "PhilHealth Number", 12, errors);
        validateDigitsAndDashes(form.tin, FieldKeys.TIN, "TIN Number", 12, errors);
        validateDigitsAndDashes(form.pagIbig, FieldKeys.PAGIBIG, "Pag-IBIG Number", 12, errors);

        if (isBlank(form.status)) {
            addError(errors, FieldKeys.STATUS, "Status is required.");
        }
        if (isBlank(form.position)) {
            addError(errors, FieldKeys.POSITION, "Position is required.");
        }
        if (isBlank(form.department)) {
            addError(errors, FieldKeys.DEPARTMENT, "Department is required.");
        }
        if (isBlank(form.supervisor)) {
            addError(errors, FieldKeys.SUPERVISOR, "Supervisor is required.");
        }

        validateRequiredNumeric(form.basicSalary, FieldKeys.BASIC_SALARY, "Basic Salary", errors);
        validateRequiredNumeric(form.riceSubsidy, FieldKeys.RICE_SUBSIDY, "Rice Subsidy", errors);
        validateRequiredNumeric(form.phoneAllowance, FieldKeys.PHONE_ALLOWANCE, "Phone Allowance", errors);
        validateRequiredNumeric(form.clothingAllowance, FieldKeys.CLOTHING_ALLOWANCE, "Clothing Allowance", errors);

        if (!isBlank(form.grossSemiMonthly) && !isNumeric(form.grossSemiMonthly)) {
            addError(errors, FieldKeys.GROSS_SEMI_MONTHLY, "Gross Semi-monthly must be a valid number.");
        }
        if (!isBlank(form.hourlyRate) && !isNumeric(form.hourlyRate)) {
            addError(errors, FieldKeys.HOURLY_RATE, "Hourly Rate must be a valid number.");
        }

        return errors;
    }

    /** Non-blocking salary consistency warnings shown before save. */
    public static List<String> collectFormWarnings(RecordFormData form) {
        List<String> warnings = new ArrayList<>();
        if (form == null) {
            return warnings;
        }
        Double basic = parseNumericValue(form.basicSalary);
        Double grossSemi = parseNumericValue(form.grossSemiMonthly);
        if (basic != null) {
            if (basic < MIN_BASIC_SALARY_MONTHLY) {
                warnings.add(String.format(
                        "Basic Salary (PHP %,.2f) is below the demo minimum of PHP %,.2f.",
                        basic, MIN_BASIC_SALARY_MONTHLY));
            }
            if (basic > HIGH_BASIC_SALARY_WARNING) {
                warnings.add(String.format(
                        "Basic Salary (PHP %,.2f) is unusually high. Confirm this is intentional.",
                        basic));
            }
        }
        if (basic != null && grossSemi != null && basic > 0) {
            double expectedGross = basic / 2.0;
            if (Math.abs(grossSemi - expectedGross) > 0.01) {
                warnings.add(String.format(
                        "Gross Semi-monthly (PHP %,.2f) does not match Basic Salary / 2 (PHP %,.2f).",
                        grossSemi, expectedGross));
            }
        }
        return warnings;
    }

    /** Validates address and phone for the employee self-service profile editor. */
    public static List<FieldValidationError> validateProfileContact(String address, String phone) {
        RecordFormData form = new RecordFormData();
        form.address = address;
        form.phone = phone;
        form.empNo = "0";
        form.lastName = "Profile";
        form.firstName = "User";
        form.birthday = "01/01/1990";
        form.sss = "11-1111111-1";
        form.philHealth = "111111111111";
        form.tin = "111-111-111-000";
        form.pagIbig = "222222222222";
        form.status = "Regular";
        form.position = "Staff";
        form.department = "Operations";
        form.supervisor = "N/A";
        form.basicSalary = "0";
        form.riceSubsidy = "0";
        form.phoneAllowance = "0";
        form.clothingAllowance = "0";
        form.grossSemiMonthly = "0";
        form.hourlyRate = "0";

        List<FieldValidationError> errors = new ArrayList<>();
        if (isBlank(address)) {
            addError(errors, FieldKeys.ADDRESS, "Address cannot be empty.");
        }
        if (isBlank(phone)) {
            addError(errors, FieldKeys.PHONE, "Phone number is required.");
        } else if (!phone.trim().matches("[0-9\\-]+")) {
            addError(errors, FieldKeys.PHONE, "Phone number must contain digits and dashes only.");
        } else {
            validatePhoneDigitCount(phone, errors);
        }
        return errors;
    }

    /** Validates one popup field on blur for live feedback. */
    public static List<FieldValidationError> validateSingleField(String fieldKey, RecordFormData form,
            boolean isUpdate, String originalId) {
        List<FieldValidationError> all = validateFormFields(form, isUpdate, originalId);
        List<FieldValidationError> scoped = new ArrayList<>();
        for (FieldValidationError error : all) {
            if (fieldKey != null && fieldKey.equals(error.fieldKey)) {
                scoped.add(error);
            }
        }
        return scoped;
    }

    /** True when a person-name field contains only letters, spaces, hyphens, and apostrophes. */
    public static boolean isValidPersonName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.trim().length(); i++) {
            char ch = value.trim().charAt(i);
            if (Character.isLetter(ch) || ch == ' ' || ch == '-' || ch == '\'') {
                continue;
            }
            return false;
        }
        return true;
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
        clearPayrollOutputColumns(row);
        return row;
    }

    /** Clears payroll-run output columns so the master CSV stays profile-only. */
    public static void clearPayrollOutputColumns(String[] row) {
        if (row == null) {
            return;
        }
        if (row.length > EmployeeModule.HOURS_WORKED) {
            row[EmployeeModule.HOURS_WORKED] = "";
        }
        if (row.length > EmployeeModule.GROSS_PAY) {
            row[EmployeeModule.GROSS_PAY] = "";
        }
        if (row.length > EmployeeModule.TOTAL_DEDUCTIONS) {
            row[EmployeeModule.TOTAL_DEDUCTIONS] = "";
        }
        if (row.length > EmployeeModule.NET_PAY) {
            row[EmployeeModule.NET_PAY] = "";
        }
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
        return messagesFromErrors(validateAddPopupFields(form));
    }

    public static List<FieldValidationError> validateAddPopupFields(RecordFormData form) {
        return validateFormFields(form, false, null);
    }

    /**
     * Validates all fields on the Edit Employee popup before updating CSV.
     */
    public static List<String> validateEditPopup(RecordFormData form, String originalId) {
        return messagesFromErrors(validateEditPopupFields(form, originalId));
    }

    public static List<FieldValidationError> validateEditPopupFields(RecordFormData form, String originalId) {
        return validateFormFields(form, true, originalId);
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
        for (FieldValidationError err : validateProfileContact(
                safe(emp, EmployeeModule.ADDRESS), safe(emp, EmployeeModule.PHONE))) {
            if (FieldKeys.PHONE.equals(err.fieldKey)) {
                warnings.add("Stored phone number looks invalid.");
            } else if (FieldKeys.ADDRESS.equals(err.fieldKey)) {
                warnings.add("Stored address looks invalid.");
            }
        }
        return warnings;
    }

    /** Partially masks government IDs for read-only employee profile display. */
    public static String maskSensitiveId(String value, int visiblePrefix, int visibleSuffix) {
        if (isBlank(value) || "-".equals(value.trim())) {
            return value == null ? "" : value.trim();
        }
        String trimmed = value.trim();
        if (trimmed.length() <= visiblePrefix + visibleSuffix) {
            return trimmed;
        }
        StringBuilder masked = new StringBuilder();
        masked.append(trimmed, 0, visiblePrefix);
        for (int i = visiblePrefix; i < trimmed.length() - visibleSuffix; i++) {
            char ch = trimmed.charAt(i);
            masked.append(ch == '-' || ch == ' ' ? ch : '•');
        }
        masked.append(trimmed.substring(trimmed.length() - visibleSuffix));
        return masked.toString();
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

    /** Requires exactly nine digits in the phone field (dashes optional). */
    private static void validatePhoneDigitCount(String value, List<FieldValidationError> errors) {
        int digits = 0;
        for (int i = 0; i < value.trim().length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                digits++;
            }
        }
        if (digits != 9) {
            addError(errors, FieldKeys.PHONE,
                    "Phone number must contain exactly 9 digits (e.g. 966-860-270).");
        }
    }

    /** Rejects birthdays in the future or unreasonably old. */
    private static void validateBirthdaySanity(String birthday, List<FieldValidationError> errors) {
        if (isBlank(birthday) || !birthday.trim().matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            return;
        }
        String[] parts = birthday.trim().split("/");
        try {
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int currentYear = cal.get(java.util.Calendar.YEAR);
            if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1900) {
                addError(errors, FieldKeys.BIRTHDAY, "Birthday is not a valid calendar date.");
            } else if (year > currentYear) {
                addError(errors, FieldKeys.BIRTHDAY, "Birthday cannot be in the future.");
            } else if (year < currentYear - 100) {
                addError(errors, FieldKeys.BIRTHDAY,
                        "Birthday year looks invalid (more than 100 years ago).");
            }
        } catch (NumberFormatException ignored) {
            addError(errors, FieldKeys.BIRTHDAY, "Birthday is not a valid calendar date.");
        }
    }

    /** Ensures government ID numbers are unique within the same form. */
    private static void checkDuplicateGovernmentIds(RecordFormData form, List<FieldValidationError> errors) {
        java.util.LinkedHashMap<String, String> seen = new java.util.LinkedHashMap<>();
        addGovIdIfPresent(seen, FieldKeys.SSS, form.sss);
        addGovIdIfPresent(seen, FieldKeys.PHILHEALTH, form.philHealth);
        addGovIdIfPresent(seen, FieldKeys.TIN, form.tin);
        addGovIdIfPresent(seen, FieldKeys.PAGIBIG, form.pagIbig);
        java.util.Set<String> normalizedValues = new java.util.LinkedHashSet<>();
        for (String value : seen.values()) {
            if (!normalizedValues.add(value)) {
                addError(errors, FieldKeys.GENERAL,
                        "Duplicate government ID numbers are not allowed within the same employee record.");
                return;
            }
        }
    }

    /** Rejects government IDs already assigned to another employee in the CSV. */
    private static void checkDuplicateGovernmentIdsAcrossDatabase(RecordFormData form, String originalId,
            List<FieldValidationError> errors) {
        String excludeId = originalId == null ? "" : originalId.trim();
        checkGovIdAgainstDatabase(form.sss, FieldKeys.SSS, "SSS Number", excludeId, errors);
        checkGovIdAgainstDatabase(form.philHealth, FieldKeys.PHILHEALTH, "PhilHealth Number", excludeId, errors);
        checkGovIdAgainstDatabase(form.tin, FieldKeys.TIN, "TIN Number", excludeId, errors);
        checkGovIdAgainstDatabase(form.pagIbig, FieldKeys.PAGIBIG, "Pag-IBIG Number", excludeId, errors);
    }

    private static void checkGovIdAgainstDatabase(String value, String fieldKey, String label,
            String excludeEmployeeId, List<FieldValidationError> errors) {
        if (isBlank(value) || isNaPlaceholder(value)) {
            return;
        }
        String normalized = normalizeGovId(value);
        if (normalized.isEmpty()) {
            return;
        }
        for (String[] row : FileHandlerModule.getAllEmployees()) {
            if (row == null || row.length == 0) {
                continue;
            }
            String rowId = safe(row, EmployeeModule.ID);
            if (!excludeEmployeeId.isEmpty() && rowId.equals(excludeEmployeeId)) {
                continue;
            }
            String existing = normalizeGovId(safe(row, govIdColumn(fieldKey)));
            if (!existing.isEmpty() && existing.equals(normalized)) {
                addError(errors, fieldKey,
                        label + " is already used by employee #" + rowId + ".");
                return;
            }
        }
    }

    private static int govIdColumn(String fieldKey) {
        switch (fieldKey) {
            case FieldKeys.SSS: return EmployeeModule.SSS;
            case FieldKeys.PHILHEALTH: return EmployeeModule.PHILHEALTH;
            case FieldKeys.TIN: return EmployeeModule.TIN;
            case FieldKeys.PAGIBIG: return EmployeeModule.PAGIBIG;
            default: return EmployeeModule.SSS;
        }
    }

    private static String normalizeGovId(String value) {
        return value == null ? "" : value.trim().replace("-", "").replace(" ", "");
    }

    private static void addGovIdIfPresent(java.util.Map<String, String> seen, String fieldKey, String value) {
        if (isBlank(value) || isNaPlaceholder(value)) {
            return;
        }
        String normalized = normalizeGovId(value);
        if (!normalized.isEmpty()) {
            seen.put(fieldKey, normalized);
        }
    }

    private static void addError(List<FieldValidationError> errors, String fieldKey, String message) {
        if (errors == null || message == null || message.isEmpty()) {
            return;
        }
        for (FieldValidationError existing : errors) {
            if (existing.fieldKey.equals(fieldKey) && existing.message.equals(message)) {
                return;
            }
        }
        errors.add(new FieldValidationError(fieldKey, message));
    }

    /** Ensures a required numeric field parses after {@link #normalizeNumericInput}. */
    private static void validateRequiredNumeric(String value, String fieldKey, String displayName,
            List<FieldValidationError> errors) {
        String normalized = normalizeNumericInput(value);
        if (isBlank(value) || !isNumeric(normalized)) {
            addError(errors, fieldKey,
                    displayName + " must be a valid number (commas/periods allowed, or enter NA / 000 for zero).");
        }
    }

    /** Ensures a government ID style field contains only digits and dashes and respects digit limits. */
    private static void validateDigitsAndDashes(String value, String fieldKey, String displayName,
            int digitLimit, List<FieldValidationError> errors) {
        if (isBlank(value)) {
            addError(errors, fieldKey, displayName + " is required.");
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
                addError(errors, fieldKey,
                        "SSS Number must use numbers and hyphens only. It must not exceed 10 numbers.");
            } else if ("TIN Number".equals(displayName)) {
                addError(errors, fieldKey,
                        "TIN Number must use numbers and hyphens only. It must not exceed 12 numbers.");
            } else if ("PhilHealth Number".equals(displayName)) {
                addError(errors, fieldKey,
                        "Philhealth Number must use numbers and hyphens only. It must not exceed 12 numbers.");
            } else if ("Pag-IBIG Number".equals(displayName)) {
                addError(errors, fieldKey,
                        "PAGIBIG Number must use numbers and hyphens only. It must not exceed 12 numbers.");
            } else {
                addError(errors, fieldKey, "Use numbers, and hyphens only. This must not exceed "
                        + digitLimit + " numbers.");
            }
        }
    }

    private static Double parseNumericValue(String value) {
        if (isBlank(value) || isNaPlaceholder(value) || !isNumeric(normalizeNumericInput(value))) {
            return null;
        }
        try {
            return Double.parseDouble(normalizeNumericInput(value).replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
