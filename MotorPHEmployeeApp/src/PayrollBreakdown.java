public class PayrollBreakdown {
    private final double hoursWorked;
    private final double hourlyRate;
    private final double earnedPay;
    private final double benefits;
    private final double grossPay;
    private final double sss;
    private final double philhealth;
    private final double pagibig;
    private final double withholdingTax;
    private final double totalDeductions;
    private final double netPay;

    PayrollBreakdown(double hoursWorked, double hourlyRate, double earnedPay, double benefits, double grossPay,
                     double sss, double philhealth, double pagibig, double withholdingTax,
                     double totalDeductions, double netPay) {
        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
        this.earnedPay = earnedPay;
        this.benefits = benefits;
        this.grossPay = grossPay;
        this.sss = sss;
        this.philhealth = philhealth;
        this.pagibig = pagibig;
        this.withholdingTax = withholdingTax;
        this.totalDeductions = totalDeductions;
        this.netPay = netPay;
    }

    double getHoursWorked() { return hoursWorked; }
    double getHourlyRate() { return hourlyRate; }
    double getEarnedPay() { return earnedPay; }
    double getBenefits() { return benefits; }
    double getGrossPay() { return grossPay; }
    double getSss() { return sss; }
    double getPhilhealth() { return philhealth; }
    double getPagibig() { return pagibig; }
    double getWithholdingTax() { return withholdingTax; }
    double getTotalDeductions() { return totalDeductions; }
    double getNetPay() { return netPay; }
}
