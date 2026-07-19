package motorph_employeeapp;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

final class EmployeeRevisionModuleTest {

    private EmployeeRevisionModuleTest() {
    }

    static void runAll() throws Exception {
        testSnapshotAndRevert();
    }

    private static void testSnapshotAndRevert() throws Exception {
        File tempRoot = new File(System.getProperty("java.io.tmpdir"),
                "motorph-rev-test-" + System.currentTimeMillis());
        File resources = new File(tempRoot, "resources");
        File revisions = new File(resources, "revisions");
        if (!resources.mkdirs() || !revisions.mkdirs()) {
            throw new AssertionError("Could not create temp resources directory");
        }

        String originalUserDir = System.getProperty("user.dir");
        File csvFile = new File(resources, "MotorPH_Employee Data - Employee Details.csv");
        try {
            System.setProperty("user.dir", tempRoot.getAbsolutePath());
            FileHandlerModule.setTestEmployeeFileOverrideForTests(csvFile.getAbsolutePath());
            EmployeeRevisionModule.resetRevisionStateForTests();

            List<String[]> before = new ArrayList<>();
            before.add(sampleRow("10001", "Original", "50000.00"));

            writeEmployeeCsv(before);
            EmployeeRevisionModule.logChange("Edit", "10001", "Changed salary", before, "hr");

            List<String[]> mutated = new ArrayList<>();
            mutated.add(sampleRow("10001", "Changed", "99999.00"));
            TestSupport.assertTrue(FileHandlerModule.rewriteEmployeeFile(mutated), "Mutated CSV write");

            List<EmployeeRevisionModule.RevisionEntry> entries = EmployeeRevisionModule.getEntries();
            TestSupport.assertFalse(entries.isEmpty(), "Revision entry logged");
            TestSupport.assertTrue(EmployeeRevisionModule.revert(entries.get(0)), "Revert succeeds");

            List<String[]> restored = FileHandlerModule.getAllEmployees();
            TestSupport.assertEquals("Original", restored.get(0)[EmployeeModule.LAST_NAME],
                    "Reverted last name");
            TestSupport.assertEquals("50000.00", restored.get(0)[EmployeeModule.BASIC_SALARY],
                    "Reverted salary");
        } finally {
            FileHandlerModule.clearTestEmployeeFileOverrideForTests();
            System.setProperty("user.dir", originalUserDir);
            deleteTree(tempRoot);
        }
    }

    private static String[] sampleRow(String id, String lastName, String basicSalary) {
        String[] row = new String[EmployeeModule.COLUMN_COUNT];
        row[EmployeeModule.ID] = id;
        row[EmployeeModule.LAST_NAME] = lastName;
        row[EmployeeModule.FIRST_NAME] = "Employee";
        row[EmployeeModule.BIRTHDAY] = "01/01/1990";
        row[EmployeeModule.ADDRESS] = "Test Address";
        row[EmployeeModule.PHONE] = "966-860-270";
        row[EmployeeModule.SSS] = "11-1111111-1";
        row[EmployeeModule.PHILHEALTH] = "111111111111";
        row[EmployeeModule.TIN] = "111-111-111-000";
        row[EmployeeModule.PAGIBIG] = "222222222222";
        row[EmployeeModule.STATUS] = "Regular";
        row[EmployeeModule.POSITION] = "Staff";
        row[EmployeeModule.DEPARTMENT] = "Operations";
        row[EmployeeModule.IMMEDIATE_SUPERVISOR] = "N/A";
        row[EmployeeModule.BASIC_SALARY] = basicSalary;
        row[EmployeeModule.RICE_SUBSIDY] = "0";
        row[EmployeeModule.PHONE_ALLOWANCE] = "0";
        row[EmployeeModule.CLOTHING_ALLOWANCE] = "0";
        row[EmployeeModule.GROSS_SEMI_MONTHLY] = "0";
        row[EmployeeModule.HOURLY_RATE] = "0";
        return row;
    }

    private static void writeEmployeeCsv(List<String[]> rows) throws Exception {
        File csv = new File(new File(System.getProperty("user.dir"), "resources"),
                "MotorPH_Employee Data - Employee Details.csv");
        try (PrintWriter out = new PrintWriter(new FileWriter(csv))) {
            out.println(FileHandlerModule.EMPLOYEE_FILE_HEADER);
            for (String[] row : rows) {
                out.println(FileHandlerModule.joinCsvLine(row));
            }
        }
    }

    private static void deleteTree(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteTree(child);
                }
            }
        }
        file.delete();
    }
}
