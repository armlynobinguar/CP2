/**
 * MotorPH Employee App
 * Procedural version: no domain objects/inheritance.
 * Orchestration only; logic is split into module files with static methods.
 */
public class Main {

    public static void main(String[] args) {
        DisplayModule.printHeader();

        String rawEmployeeId = "10001";
        String employeeName = "Maria Reyes";
        String payCoverage = "2024-11-01 to 2024-11-15";

        OperationResult<Integer> employeeIdValidation = ValidationModule.validateEmployeeNumber(rawEmployeeId);
        OperationResult<String> employeeNameValidation = ValidationModule.validateEmployeeName(employeeName);
        OperationResult<String> coverageValidation = ValidationModule.validatePayCoverage(payCoverage);

        if (!employeeIdValidation.isSuccess()) {
            System.out.println("Validation error: " + employeeIdValidation.getMessage());
            return;
        }
        if (!employeeNameValidation.isSuccess()) {
            System.out.println("Validation error: " + employeeNameValidation.getMessage());
            return;
        }
        if (!coverageValidation.isSuccess()) {
            System.out.println("Validation error: " + coverageValidation.getMessage());
            return;
        }

        int employeeId = employeeIdValidation.getData();
        employeeName = employeeNameValidation.getData();
        String positionName = "Software Engineer";
        String departmentName = "IT Department";
        double basicSalary = 35000.00;
        double rice = 1500.00;
        double phone = 1000.00;
        double clothing = 1000.00;

        DisplayModule.printEmployeeInfo(
                employeeId, employeeName, positionName, departmentName, basicSalary, rice, phone, clothing);

        System.out.println("--- Recording Attendance ---");
        double dayHours = AttendanceModule.computeHoursWorked("08:05", "17:00");
        System.out.println();

        System.out.println("--- Filing a Leave Request ---");
        LeaveModule.printLeaveFlow(101, employeeName, "Sick Leave", "2024-11-10", "2024-11-11");
        System.out.println();

        System.out.println("--- Processing Payroll ---");
        double payrollHours = dayHours * 10;
        PayrollModule.printPayslip(
                employeeId, employeeName, positionName, departmentName, payrollHours, basicSalary, rice, phone, clothing);

        System.out.println("--- Employee Benefits ---");
        DisplayModule.printBenefits(employeeName, rice, phone, clothing);
        System.out.println();

        System.out.println("--- User Login Test ---");
        UserModule.runUserFlow("mreyes", "secure123", employeeName);
        System.out.println();

        System.out.println("--- HR Manager Actions ---");
        HrModule.runHrFlow("Ana Cruz", employeeName, employeeId);

        System.out.println("\n============================================");
        System.out.println("          DEMO RUN COMPLETE                  ");
        System.out.println("============================================");
    }
}
