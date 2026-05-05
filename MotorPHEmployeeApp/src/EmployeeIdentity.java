public class EmployeeIdentity {
    private final int employeeId;
    private final String employeeName;
    private final String payCoverage;

    EmployeeIdentity(int employeeId, String employeeName, String payCoverage) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.payCoverage = payCoverage;
    }

    int getEmployeeId() {
        return employeeId;
    }

    String getEmployeeName() {
        return employeeName;
    }

    String getPayCoverage() {
        return payCoverage;
    }
}
