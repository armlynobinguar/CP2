/**
 * MotorPH Employee App
 * Procedural version: no domain objects/inheritance.
 * Orchestration only; logic is split into module files with static methods.
 */
public class Main {

    public static void main(String[] args) {
        MotorPhService service = new MotorPhServiceImpl();
        DisplayModule.printHeader();

        String rawEmployeeId = "10001";
        String employeeName = "Maria Reyes";
        String payCoverage = "2024-11-01 to 2024-11-15";

        OperationResult<EmployeeIdentity> identityResult =
                service.validateIdentity(rawEmployeeId, employeeName, payCoverage);
        if (!identityResult.isSuccess()) {
            System.out.println("Validation error: " + identityResult.getMessage());
            return;
        }
        EmployeeIdentity identity = identityResult.getData();
        int employeeId = identity.getEmployeeId();
        employeeName = identity.getEmployeeName();
        String positionName = "Software Engineer";
        String departmentName = "IT Department";
        double basicSalary = 35000.00;
        double rice = 1500.00;
        double phone = 1000.00;
        double clothing = 1000.00;

        DisplayModule.printEmployeeInfo(
                employeeId, employeeName, positionName, departmentName, basicSalary, rice, phone, clothing);

        System.out.println("--- Recording Attendance ---");
        OperationResult<AttendanceResult> attendanceResult = service.evaluateAttendance("08:05", "17:00");
        if (!attendanceResult.isSuccess()) {
            System.out.println("Attendance error: " + attendanceResult.getMessage());
            return;
        }
        AttendanceResult attendanceData = attendanceResult.getData();
        DisplayModule.printAttendance(attendanceData);
        double dayHours = attendanceData.getHoursWorked();
        System.out.println();

        System.out.println("--- Filing a Leave Request ---");
        int leaveId = 101;
        String leaveType = "Sick Leave";
        String leaveStart = "2024-11-10";
        String leaveEnd = "2024-11-11";
        OperationResult<Integer> leaveResult =
                service.createLeaveRequest(leaveId, employeeName, leaveType, leaveStart, leaveEnd);
        if (!leaveResult.isSuccess()) {
            System.out.println("Leave error: " + leaveResult.getMessage());
            return;
        }
        DisplayModule.printLeave(leaveId, employeeName, leaveType, leaveStart, leaveEnd, leaveResult.getData());
        System.out.println();

        System.out.println("--- Processing Payroll ---");
        double payrollHours = dayHours * 10;
        PayrollModule.printPayslip(
                employeeId, employeeName, positionName, departmentName, payrollHours, basicSalary, rice, phone, clothing);

        System.out.println("--- Employee Benefits ---");
        DisplayModule.printBenefits(employeeName, rice, phone, clothing);
        System.out.println();

        System.out.println("--- User Login Test ---");
        String realUser = "mreyes";
        String realPass = "secure123";
        System.out.println("User account created: " + realUser + " (Employee)");
        OperationResult<Boolean> loginAttempt1 = service.authenticate(realUser, realPass, "mreyes", "wrongpass");
        DisplayModule.printLoginResult(loginAttempt1.isSuccess() && loginAttempt1.getData(), employeeName);
        OperationResult<Boolean> loginAttempt2 = service.authenticate(realUser, realPass, "mreyes", "secure123");
        DisplayModule.printLoginResult(loginAttempt2.isSuccess() && loginAttempt2.getData(), employeeName);
        System.out.println(loginAttempt2.isSuccess() && loginAttempt2.getData() ? "Logout success" : "No active session");
        System.out.println();

        System.out.println("--- HR Manager Actions ---");
        HrModule.runHrFlow("Ana Cruz", employeeName, employeeId);

        System.out.println("\n============================================");
        System.out.println("          DEMO RUN COMPLETE                  ");
        System.out.println("============================================");
    }
}
