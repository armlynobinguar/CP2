package motorph_employeeapp;

import java.util.List;

final class EmployeeRecordsModuleTest {

    private EmployeeRecordsModuleTest() {
    }

    static void runAll() {
        testNormalizeNumericInput();
        testIsNaPlaceholder();
        testComputeHourlyRateFromGrossSemiMonthly();
        testValidateAddPopupRequiredFields();
        testValidateAddPopupInvalidBirthdayFormat();
        testValidateAddPopupInvalidPhoneChars();
        testValidateAddPopupInvalidSssDigits();
        testCollectViewWarnings();
        testFormatBirthdayForDisplay();
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

    private static void testValidateAddPopupRequiredFields() {
        EmployeeRecordsModule.RecordFormData form = baseValidForm();
        form.lastName = "";
        List<String> errors = EmployeeRecordsModule.validateAddPopup(form);
        TestSupport.assertTrue(errors.stream().anyMatch(m -> m.contains("Last Name")),
                "Missing last name should fail validation");
    }

    private static void testValidateAddPopupInvalidBirthdayFormat() {
        EmployeeRecordsModule.RecordFormData form = baseValidForm();
        form.birthday = "not-a-date";
        List<String> errors = EmployeeRecordsModule.validateAddPopup(form);
        TestSupport.assertTrue(errors.stream().anyMatch(m -> m.toLowerCase().contains("birthday")),
                "Invalid birthday format should fail validation");
    }

    private static void testValidateAddPopupInvalidPhoneChars() {
        EmployeeRecordsModule.RecordFormData form = baseValidForm();
        form.phone = "966-860-270!";
        List<String> errors = EmployeeRecordsModule.validateAddPopup(form);
        TestSupport.assertTrue(errors.stream().anyMatch(m -> m.toLowerCase().contains("phone")),
                "Invalid phone characters should fail validation");
    }

    private static void testValidateAddPopupInvalidSssDigits() {
        EmployeeRecordsModule.RecordFormData form = baseValidForm();
        form.sss = "123456789012345";
        List<String> errors = EmployeeRecordsModule.validateAddPopup(form);
        TestSupport.assertTrue(errors.stream().anyMatch(m -> m.contains("SSS")),
                "SSS with too many digits should fail validation");
    }

    private static void testCollectViewWarnings() {
        String[] emp = new String[EmployeeModule.COLUMN_COUNT];
        emp[EmployeeModule.ID] = "10001";
        emp[EmployeeModule.LAST_NAME] = "Test";
        emp[EmployeeModule.FIRST_NAME] = "User";
        emp[EmployeeModule.BIRTHDAY] = "bad-date";
        List<String> warnings = EmployeeRecordsModule.collectViewWarnings(emp);
        TestSupport.assertTrue(warnings.stream().anyMatch(w -> w.toLowerCase().contains("birthday")),
                "Invalid stored birthday should produce a view warning");
    }

    private static void testFormatBirthdayForDisplay() {
        TestSupport.assertEquals("06/22/1986",
                EmployeeRecordsModule.formatBirthdayForDisplay("6/22/1986"),
                "Birthday display adds leading zeros");
    }

    private static EmployeeRecordsModule.RecordFormData baseValidForm() {
        EmployeeRecordsModule.RecordFormData form = new EmployeeRecordsModule.RecordFormData();
        form.empNo = "99998";
        form.lastName = "Test";
        form.firstName = "User";
        form.birthday = "01/15/1990";
        form.address = "123 Main St";
        form.phone = "966-860-270";
        form.sss = "11-1111111-1";
        form.philHealth = "111111111111";
        form.tin = "111-111-111-000";
        form.pagIbig = "222222222222";
        form.status = "Regular";
        form.position = "Staff";
        form.department = "Operations";
        form.supervisor = "N/A";
        form.basicSalary = "20000";
        form.riceSubsidy = "0";
        form.phoneAllowance = "0";
        form.clothingAllowance = "0";
        form.grossSemiMonthly = "10000";
        form.hourlyRate = "0";
        return form;
    }
}
