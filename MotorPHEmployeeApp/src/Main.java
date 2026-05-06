/**
 * MotorPH Employee App
 * Procedural version: no domain objects/inheritance.
 * Orchestration only; logic is split into module files with static methods.
 */
public class Main {

    public static void main(String[] args) {
        MotorPhService service = new MotorPhServiceImpl();
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        DisplayModule.printHeader();

        int roleChoice = promptRoleChoice(scanner);
        if (roleChoice == 1) {
            runEmployeeView(service, scanner);
        } else {
            runHrView(service, scanner);
        }

        System.out.println("\n============================================");
        System.out.println("          SESSION COMPLETE                   ");
        System.out.println("============================================");
        scanner.close();
    }

    private static int promptRoleChoice(java.util.Scanner scanner) {
        while (true) {
            System.out.println("--- Select Console View ---");
            System.out.println("1) Employee View");
            System.out.println("2) HR View");
            String choice = promptText(scanner, "Enter choice (1 or 2): ");
            if ("1".equals(choice) || "2".equals(choice)) {
                return Integer.parseInt(choice);
            }
            System.out.println("Invalid selection. Please choose 1 or 2.\n");
        }
    }

    private static void runEmployeeView(MotorPhService service, java.util.Scanner scanner) {
        System.out.println("\n--- EMPLOYEE VIEW ---");
        EmployeeIdentity identity = promptIdentity(service, scanner);
        int employeeId = identity.getEmployeeId();
        String employeeName = identity.getEmployeeName();
        String positionName = "Software Engineer";
        String departmentName = "IT Department";
        double basicSalary = promptDouble(scanner, "Basic Salary (e.g. 35000): ");
        double rice = promptDouble(scanner, "Rice Subsidy (e.g. 1500): ");
        double phone = promptDouble(scanner, "Phone Allowance (e.g. 1000): ");
        double clothing = promptDouble(scanner, "Clothing Allowance (e.g. 1000): ");

        DisplayModule.printEmployeeInfo(
                employeeId, employeeName, positionName, departmentName, basicSalary, rice, phone, clothing);

        System.out.println("--- Recording Attendance ---");
        String timeIn = promptText(scanner, "Time-In (HH:MM): ");
        String timeOut = promptText(scanner, "Time-Out (HH:MM): ");
        OperationResult<AttendanceResult> attendanceResult = service.evaluateAttendance(timeIn, timeOut);
        if (!attendanceResult.isSuccess()) {
            System.out.println("Attendance error: " + attendanceResult.getMessage());
            return;
        }
        AttendanceResult attendanceData = attendanceResult.getData();
        DisplayModule.printAttendance(attendanceData);
        double payrollHours = attendanceData.getHoursWorked() * 10;
        System.out.println();

        System.out.println("--- Filing a Leave Request ---");
        int leaveId = (int) promptDouble(scanner, "Leave Request ID (e.g. 101): ");
        String leaveType = promptText(scanner, "Leave Type (e.g. Sick Leave): ");
        String leaveStart = promptText(scanner, "Leave Start Date (YYYY-MM-DD): ");
        String leaveEnd = promptText(scanner, "Leave End Date (YYYY-MM-DD): ");
        OperationResult<Integer> leaveResult =
                service.createLeaveRequest(leaveId, employeeName, leaveType, leaveStart, leaveEnd);
        if (!leaveResult.isSuccess()) {
            System.out.println("Leave error: " + leaveResult.getMessage());
            return;
        }
        DisplayModule.printLeave(leaveId, employeeName, leaveType, leaveStart, leaveEnd, leaveResult.getData());
        System.out.println();

        System.out.println("--- Processing Payroll ---");
        PayrollModule.printPayslip(
                employeeId, employeeName, positionName, departmentName, payrollHours, basicSalary, rice, phone, clothing);

        System.out.println("--- Employee Benefits ---");
        DisplayModule.printBenefits(employeeName, rice, phone, clothing);
        System.out.println();

        System.out.println("--- User Login Test ---");
        String realUser = promptText(scanner, "Create Username: ");
        String realPass = promptText(scanner, "Create Password: ");
        String attemptUser1 = promptText(scanner, "Login Attempt 1 - Username: ");
        String attemptPass1 = promptText(scanner, "Login Attempt 1 - Password: ");
        String attemptUser2 = promptText(scanner, "Login Attempt 2 - Username: ");
        String attemptPass2 = promptText(scanner, "Login Attempt 2 - Password: ");
        System.out.println("User account created: " + realUser + " (Employee)");
        OperationResult<Boolean> loginAttempt1 = service.authenticate(realUser, realPass, attemptUser1, attemptPass1);
        DisplayModule.printLoginResult(loginAttempt1.isSuccess() && loginAttempt1.getData(), employeeName);
        OperationResult<Boolean> loginAttempt2 = service.authenticate(realUser, realPass, attemptUser2, attemptPass2);
        DisplayModule.printLoginResult(loginAttempt2.isSuccess() && loginAttempt2.getData(), employeeName);
        System.out.println(loginAttempt2.isSuccess() && loginAttempt2.getData() ? "Logout success" : "No active session");
        System.out.println();
    }

    private static void runHrView(MotorPhService service, java.util.Scanner scanner) {
        System.out.println("\n--- HR VIEW ---");
        String hrName = promptText(scanner, "HR Manager Name: ");
        EmployeeIdentity identity = promptIdentity(service, scanner);
        int employeeId = identity.getEmployeeId();
        String employeeName = identity.getEmployeeName();
        String payCoverage = identity.getPayCoverage();
        int leaveId = (int) promptDouble(scanner, "Leave Request ID to review: ");

        System.out.println("\n--- HR Manager Actions ---");
        HrModule.runHrFlow(hrName, employeeName, employeeId, leaveId, payCoverage);
    }

    private static EmployeeIdentity promptIdentity(MotorPhService service, java.util.Scanner scanner) {
        while (true) {
            String rawEmployeeId = promptText(scanner, "Employee Number: ");
            String employeeName = promptText(scanner, "Employee Name: ");
            String payCoverage = promptText(scanner, "Pay Coverage (YYYY-MM-DD to YYYY-MM-DD): ");

            OperationResult<EmployeeIdentity> result =
                    service.validateIdentity(rawEmployeeId, employeeName, payCoverage);
            if (result.isSuccess()) {
                return result.getData();
            }
            System.out.println("Validation error: " + result.getMessage());
            System.out.println("Please re-enter employee details.\n");
        }
    }

    private static String promptText(java.util.Scanner scanner, String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    private static double promptDouble(java.util.Scanner scanner, String label) {
        while (true) {
            System.out.print(label);
            String raw = scanner.nextLine().trim();
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }
}
