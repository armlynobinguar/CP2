public class TestRunner {
    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        try {
            testValidationModule();
            passed++;
            System.out.println("[PASS] ValidationModule tests");
        } catch (RuntimeException ex) {
            failed++;
            System.out.println("[FAIL] ValidationModule tests: " + ex.getMessage());
        }

        try {
            testAttendanceModule();
            passed++;
            System.out.println("[PASS] AttendanceModule tests");
        } catch (RuntimeException ex) {
            failed++;
            System.out.println("[FAIL] AttendanceModule tests: " + ex.getMessage());
        }

        try {
            testLeaveModule();
            passed++;
            System.out.println("[PASS] LeaveModule tests");
        } catch (RuntimeException ex) {
            failed++;
            System.out.println("[FAIL] LeaveModule tests: " + ex.getMessage());
        }

        try {
            testPayrollModule();
            passed++;
            System.out.println("[PASS] PayrollModule tests");
        } catch (RuntimeException ex) {
            failed++;
            System.out.println("[FAIL] PayrollModule tests: " + ex.getMessage());
        }

        try {
            testServiceLayer();
            passed++;
            System.out.println("[PASS] MotorPhService tests");
        } catch (RuntimeException ex) {
            failed++;
            System.out.println("[FAIL] MotorPhService tests: " + ex.getMessage());
        }

        System.out.println("\nTests passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testValidationModule() {
        assertTrue(ValidationModule.validateEmployeeNumber("10001").isSuccess(), "Employee number should pass");
        assertTrue(!ValidationModule.validateEmployeeNumber("-1").isSuccess(), "Negative employee number should fail");

        assertTrue(ValidationModule.validateEmployeeName("Maria Reyes").isSuccess(), "Employee name should pass");
        assertTrue(!ValidationModule.validateEmployeeName(" ").isSuccess(), "Blank employee name should fail");

        assertTrue(ValidationModule.validatePayCoverage("2024-11-01 to 2024-11-15").isSuccess(), "Coverage should pass");
        assertTrue(!ValidationModule.validatePayCoverage("11/01/2024 - 11/15/2024").isSuccess(), "Invalid coverage format should fail");
    }

    private static void testAttendanceModule() {
        OperationResult<AttendanceResult> ok = AttendanceModule.evaluateAttendance("08:05", "17:00");
        assertTrue(ok.isSuccess(), "Attendance should pass");
        assertApprox(7.92, ok.getData().getHoursWorked(), 0.02, "Hours worked should match expected value");

        OperationResult<AttendanceResult> bad = AttendanceModule.evaluateAttendance("17:00", "08:00");
        assertTrue(!bad.isSuccess(), "Reverse times should fail");
    }

    private static void testLeaveModule() {
        OperationResult<Integer> ok = LeaveModule.createLeaveRequest(
                101, "Maria Reyes", "Sick Leave", "2024-11-10", "2024-11-11");
        assertTrue(ok.isSuccess(), "Leave request should pass");
        assertTrue(ok.getData() == 2, "Leave duration should be 2 days");

        OperationResult<Integer> bad = LeaveModule.createLeaveRequest(
                101, "Maria Reyes", "Sick Leave", "2024-11-12", "2024-11-11");
        assertTrue(!bad.isSuccess(), "Invalid leave range should fail");
    }

    private static void testPayrollModule() {
        OperationResult<PayrollBreakdown> ok = PayrollModule.computePayroll(79.2, 35000, 1500, 1000, 1000);
        assertTrue(ok.isSuccess(), "Payroll should pass");
        assertTrue(ok.getData().getNetPay() > 0, "Net pay should be positive");

        OperationResult<PayrollBreakdown> bad = PayrollModule.computePayroll(-1, 35000, 1500, 1000, 1000);
        assertTrue(!bad.isSuccess(), "Negative hours should fail");
    }

    private static void testServiceLayer() {
        MotorPhService service = new MotorPhServiceImpl();
        OperationResult<EmployeeIdentity> identity = service.validateIdentity(
                "10001", "Maria Reyes", "2024-11-01 to 2024-11-15");
        assertTrue(identity.isSuccess(), "Service identity validation should pass");

        OperationResult<Boolean> login = service.authenticate("mreyes", "secure123", "mreyes", "secure123");
        assertTrue(login.isSuccess() && login.getData(), "Service login should pass");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    private static void assertApprox(double expected, double actual, double tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new RuntimeException(message + " (expected " + expected + ", got " + actual + ")");
        }
    }
}
