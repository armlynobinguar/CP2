package motorph_employeeapp;

import java.util.List;

final class EmployeeRecordsModuleTest {

    private EmployeeRecordsModuleTest() {
    }

    static void runAll() {
        testNormalizeNumericInput();
        testIsNaPlaceholder();
        testComputeHourlyRateFromGrossSemiMonthly();
        testClearPayrollOutputColumns();
        testValidateBirthdaySanity();
        testValidatePhoneLength();
        testDuplicateGovernmentIds();
    }

    private static void testNormalizeNumericInput() {
        TestSupport.assertEquals("0", EmployeeRecordsModule.normalizeNumericInput("NA"), "NA normalizes to 0");
        TestSupport.assertEquals("0", EmployeeRecordsModule.normalizeNumericInput("N/A"), "N/A normalizes to 0");
        TestSupport.assertEquals("1,500", EmployeeRecordsModule.normalizeNumericInput("1,500"), "Commas preserved");
    }

    private static void testIsNaPlaceholder() {
        TestSupport.assertTrue(EmployeeRecordsModule.isNaPlaceholder("000"), "000 is NA placeholder");
        TestSupport.assertFalse(EmployeeRecordsModule.isNaPlaceholder("123"), "123 is not NA placeholder");
    }

    private static void testComputeHourlyRateFromGrossSemiMonthly() {
        String hourly = EmployeeRecordsModule.computeHourlyRateFromGrossSemiMonthly("45000");
        TestSupport.assertEquals("535.71", hourly, "Hourly rate derived from gross semi-monthly");
    }

    private static void testClearPayrollOutputColumns() {
        String[] row = new String[EmployeeModule.COLUMN_COUNT];
        row[EmployeeModule.HOURS_WORKED] = "120";
        row[EmployeeModule.GROSS_PAY] = "50000";
        row[EmployeeModule.TOTAL_DEDUCTIONS] = "1000";
        row[EmployeeModule.NET_PAY] = "49000";
        EmployeeRecordsModule.clearPayrollOutputColumns(row);
        TestSupport.assertEquals("", row[EmployeeModule.HOURS_WORKED], "Hours cleared");
        TestSupport.assertEquals("", row[EmployeeModule.GROSS_PAY], "Gross cleared");
    }

    private static void testValidateBirthdaySanity() {
        EmployeeRecordsModule.RecordFormData form = new EmployeeRecordsModule.RecordFormData();
        form.empNo = "99999";
        form.lastName = "Test";
        form.firstName = "User";
        form.birthday = "12/31/2099";
        form.phone = "966-860-270";
        form.sss = "11-1111111-1";
        form.philHealth = "111111111111";
        form.tin = "111-111-111-000";
        form.pagIbig = "111111111111";
        form.status = "Regular";
        form.position = "Staff";
        form.department = "Operations";
        form.supervisor = "N/A";
        form.basicSalary = "10000";
        form.riceSubsidy = "0";
        form.phoneAllowance = "0";
        form.clothingAllowance = "0";
        form.grossSemiMonthly = "5000";
        form.hourlyRate = "0";

        List<String> errors = EmployeeRecordsModule.validateAddPopup(form);
        boolean hasFutureBirthday = false;
        for (String err : errors) {
            if (err.toLowerCase().contains("future")) {
                hasFutureBirthday = true;
            }
        }
        TestSupport.assertTrue(hasFutureBirthday, "Future birthday should fail validation");
    }

    private static void testValidatePhoneLength() {
        EmployeeRecordsModule.RecordFormData form = baseValidForm();
        form.phone = "966-860-27011";
        List<String> errors = EmployeeRecordsModule.validateAddPopup(form);
        boolean hasPhoneError = false;
        for (String err : errors) {
            if (err.toLowerCase().contains("phone")) {
                hasPhoneError = true;
            }
        }
        TestSupport.assertTrue(hasPhoneError, "11-digit phone should fail validation");
    }

    private static void testDuplicateGovernmentIds() {
        EmployeeRecordsModule.RecordFormData form = baseValidForm();
        form.sss = "44-4506057-3";
        form.tin = "44-4506057-3";
        List<String> errors = EmployeeRecordsModule.validateAddPopup(form);
        boolean hasDuplicate = false;
        for (String err : errors) {
            if (err.toLowerCase().contains("duplicate")) {
                hasDuplicate = true;
            }
        }
        TestSupport.assertTrue(hasDuplicate, "Duplicate government IDs should fail validation");
    }

    private static EmployeeRecordsModule.RecordFormData baseValidForm() {
        EmployeeRecordsModule.RecordFormData form = new EmployeeRecordsModule.RecordFormData();
        form.empNo = "99998";
        form.lastName = "Test";
        form.firstName = "User";
        form.birthday = "01/15/1990";
        form.phone = "966-860-270";
        form.sss = "11-1111111-1";
        form.philHealth = "111111111111";
        form.tin = "111-111-111-000";
        form.pagIbig = "222222222222";
        form.status = "Regular";
        form.position = "Staff";
        form.department = "Operations";
        form.supervisor = "N/A";
        form.basicSalary = "10000";
        form.riceSubsidy = "0";
        form.phoneAllowance = "0";
        form.clothingAllowance = "0";
        form.grossSemiMonthly = "5000";
        form.hourlyRate = "0";
        return form;
    }
}
