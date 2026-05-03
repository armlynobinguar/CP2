/**
 * Handles computation and processing of employee salary,
 * benefits, and deductions for a specific pay period.
 */
public class Payroll {

    // ---------- Attributes ----------
    private int       payrollId;
    private Employee  employee;
    private String    payPeriodStart;
    private String    payPeriodEnd;
    private double    grossPay;
    private double    totalDeductions;
    private double    netPay;
    private double    hoursWorked;

    private Deduction deduction;
    private Benefit   benefit;

    // ---------- Constructor ----------
    public Payroll(int payrollId, Employee employee,
                   String payPeriodStart, String payPeriodEnd, double hoursWorked) {
        this.payrollId      = payrollId;
        this.employee       = employee;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd   = payPeriodEnd;
        this.hoursWorked    = hoursWorked;
        this.deduction      = new Deduction(payrollId, employee);
        this.benefit        = new Benefit(payrollId, employee);
        System.out.println("[Payroll] Created for: " + employee.getFullName()
                         + " | Period: " + payPeriodStart + " to " + payPeriodEnd);
    }

    // ---------- Methods ----------

    /**
     * Calculates gross pay: (hours worked x hourly rate) + benefits/allowances.
     */
    public double computeGrossPay() {
        double earnedPay   = hoursWorked * employee.getHourlyRate();
        double totalBenefits = benefit.getTotalBenefits();
        this.grossPay      = earnedPay + totalBenefits;
        System.out.println("[Payroll] computeGrossPay()");
        System.out.println("          Hours Worked  : " + hoursWorked);
        System.out.println("          Hourly Rate   : PHP " + String.format("%.2f", employee.getHourlyRate()));
        System.out.println("          Earned Pay    : PHP " + String.format("%.2f", earnedPay));
        System.out.println("          Benefits      : PHP " + String.format("%.2f", totalBenefits));
        System.out.println("          Gross Pay     : PHP " + String.format("%.2f", grossPay));
        return grossPay;
    }

    /**
     * Calculates total mandatory deductions for the pay period.
     */
    public double computeDeductions() {
        this.totalDeductions = deduction.getTotalDeductions();
        System.out.println("[Payroll] computeDeductions() -> PHP " + String.format("%.2f", totalDeductions));
        return totalDeductions;
    }

    /**
     * Computes net pay: gross pay minus all deductions.
     */
    public double computeNetPay() {
        computeGrossPay();
        computeDeductions();
        this.netPay = grossPay - totalDeductions;
        System.out.println("[Payroll] computeNetPay() -> PHP " + String.format("%.2f", netPay));
        return netPay;
    }

    /**
     * Generates and displays the complete employee payslip.
     */
    public void generatePayslip() {
        computeNetPay();
        System.out.println();
        System.out.println("============================================");
        System.out.println("            MOTORPH EMPLOYEE PAYSLIP        ");
        System.out.println("============================================");
        System.out.println("Employee         : " + employee.getFullName());
        System.out.println("Employee ID      : " + employee.getEmployeeId());
        System.out.println("Position         : " + employee.getPosition().getPositionName());
        System.out.println("Department       : " + employee.getDepartment().getDepartmentName());
        System.out.println("Pay Period       : " + getPayPeriod());
        System.out.println("Hours Worked     : " + hoursWorked);
        System.out.println("--------------------------------------------");
        System.out.println("EARNINGS");
        System.out.println("  Basic Pay      : PHP " + String.format("%.2f", hoursWorked * employee.getHourlyRate()));
        System.out.println("  Rice Subsidy   : PHP " + benefit.getRiceSubsidy());
        System.out.println("  Phone Allow.   : PHP " + benefit.getPhoneAllowance());
        System.out.println("  Clothing Allow.: PHP " + benefit.getClothingAllowance());
        System.out.println("  Gross Pay      : PHP " + String.format("%.2f", grossPay));
        System.out.println("--------------------------------------------");
        System.out.println("DEDUCTIONS");
        System.out.println("  SSS            : PHP " + String.format("%.2f", deduction.getSssContribution()));
        System.out.println("  PhilHealth     : PHP " + String.format("%.2f", deduction.getPhilhealthContribution()));
        System.out.println("  Pag-IBIG       : PHP " + String.format("%.2f", deduction.getPagibigContribution()));
        System.out.println("  Withholding Tax: PHP " + String.format("%.2f", deduction.getWithholdingTax()));
        System.out.println("  Total Deductions: PHP " + String.format("%.2f", totalDeductions));
        System.out.println("--------------------------------------------");
        System.out.println("  NET PAY        : PHP " + String.format("%.2f", netPay));
        System.out.println("============================================");
        System.out.println();
    }

    public String getPayPeriod() {
        String period = payPeriodStart + " to " + payPeriodEnd;
        System.out.println("[Payroll] getPayPeriod() -> " + period);
        return period;
    }

    // ---------- Getters ----------
    public int    getPayrollId()       { return payrollId; }
    public double getGrossPay()        { return grossPay; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getNetPay()          { return netPay; }
    public double getHoursWorked()     { return hoursWorked; }
}
