public class HrModule {
    private HrModule() {}

    static void runHrFlow(String hrName, String employeeName, int employeeId, int leaveId, String payCoverage) {
        System.out.println("[HR] " + hrName + " added employee " + employeeName + " (ID: " + employeeId + ")");
        System.out.println("[HR] " + hrName + " updated " + employeeName + " field: Status -> Promoted");
        System.out.println("[HR] " + hrName + " approved leave request #" + leaveId + " for " + employeeName);
        System.out.println("[HR] " + hrName + " generated payroll report for " + payCoverage);
    }
}
