package motorph_employeeapp;

/**
 * Computed payroll outcome for one employee.
 */
public class PayrollResult {

    private final String employeeId;
    private final double grossPay;
    private final double totalDeductions;
    private final double netPay;

    public PayrollResult(String employeeId, double grossPay,
            double totalDeductions, double netPay) {
        this.employeeId = employeeId;
        this.grossPay = grossPay;
        this.totalDeductions = totalDeductions;
        this.netPay = netPay;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public double getGrossPay() {
        return grossPay;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public double getNetPay() {
        return netPay;
    }
}
