public class HrModule {
    private HrModule() {}

    static void runHrFlow(String hrName, String employeeName, int employeeId) {
        System.out.println("[HR] " + hrName + " added employee " + employeeName + " (ID: " + employeeId + ")");
        System.out.println("[HR] " + hrName + " updated " + employeeName + " field: Status -> Promoted");
        System.out.println("[HR] " + hrName + " approved leave request for " + employeeName);
        System.out.println("[HR] " + hrName + " generated Payroll report for November 2024");
    }
}
