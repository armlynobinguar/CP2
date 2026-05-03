/**
 * Encapsulates mandatory government deductions applied to an employee's pay.
 * Covers SSS, PhilHealth, Pag-IBIG, and withholding tax computations.
 */
public class Deduction {

    // ---------- Attributes ----------
    private int      deductionId;
    private Employee employee;
    private double   sssContribution;
    private double   philhealthContribution;
    private double   pagibigContribution;
    private double   withholdingTax;

    // ---------- Constructor ----------
    public Deduction(int deductionId, Employee employee) {
        this.deductionId = deductionId;
        this.employee    = employee;
        System.out.println("[Deduction] Created for: " + employee.getFullName());
    }

    // ---------- Methods ----------

    /**
     * Computes SSS contribution based on basic salary bracket.
     * Uses a simplified bracket table aligned with SSS schedule.
     */
    public double computeSSSContribution() {
        double salary = employee.getBasicSalary();
        double sss;

        if      (salary < 3250)  sss = 135.00;
        else if (salary < 3750)  sss = 157.50;
        else if (salary < 4250)  sss = 180.00;
        else if (salary < 4750)  sss = 202.50;
        else if (salary < 5250)  sss = 225.00;
        else if (salary < 5750)  sss = 247.50;
        else if (salary < 6250)  sss = 270.00;
        else if (salary < 6750)  sss = 292.50;
        else if (salary < 7250)  sss = 315.00;
        else if (salary < 7750)  sss = 337.50;
        else if (salary < 8250)  sss = 360.00;
        else if (salary < 8750)  sss = 382.50;
        else if (salary < 9250)  sss = 405.00;
        else if (salary < 9750)  sss = 427.50;
        else if (salary < 10250) sss = 450.00;
        else if (salary < 10750) sss = 472.50;
        else if (salary < 11250) sss = 495.00;
        else if (salary < 11750) sss = 517.50;
        else if (salary < 12250) sss = 540.00;
        else if (salary < 12750) sss = 562.50;
        else if (salary < 13250) sss = 585.00;
        else if (salary < 13750) sss = 607.50;
        else if (salary < 14250) sss = 630.00;
        else if (salary < 14750) sss = 652.50;
        else                     sss = 675.00;

        this.sssContribution = sss;
        System.out.println("[Deduction] computeSSSContribution() -> PHP " + sss);
        return sss;
    }

    /**
     * Computes PhilHealth contribution.
     * Rate: 3% of basic salary, split 50/50 between employee and employer.
     * Employee share = 1.5% of basic salary.
     */
    public double computePhilHealth() {
        double salary = employee.getBasicSalary();
        double philhealth;

        if (salary <= 10000) {
            philhealth = 150.00;
        } else if (salary >= 60000) {
            philhealth = 900.00;
        } else {
            philhealth = (salary * 0.03) / 2;
        }

        this.philhealthContribution = philhealth;
        System.out.println("[Deduction] computePhilHealth() -> PHP " + philhealth);
        return philhealth;
    }

    /**
     * Computes Pag-IBIG contribution.
     * Employee contributes 2% of salary; max PHP 100.
     */
    public double computePagIbig() {
        double salary = employee.getBasicSalary();
        double pagibig;

        if (salary <= 1500) {
            pagibig = salary * 0.01;
        } else {
            pagibig = salary * 0.02;
        }

        if (pagibig > 100) pagibig = 100.00;

        this.pagibigContribution = pagibig;
        System.out.println("[Deduction] computePagIbig() -> PHP " + pagibig);
        return pagibig;
    }

    /**
     * Computes withholding tax based on taxable income per BIR tax table.
     * Taxable income = basic salary - SSS - PhilHealth - PagIbig.
     */
    public double computeWithholdingTax() {
        double taxable = employee.getBasicSalary()
                       - sssContribution
                       - philhealthContribution
                       - pagibigContribution;

        double tax;

        if      (taxable <= 20833)  tax = 0;
        else if (taxable <= 33333)  tax = (taxable - 20833) * 0.20;
        else if (taxable <= 66667)  tax = 2500  + (taxable - 33333) * 0.25;
        else if (taxable <= 166667) tax = 10833 + (taxable - 66667) * 0.30;
        else if (taxable <= 666667) tax = 40833 + (taxable - 166667) * 0.32;
        else                        tax = 200833 + (taxable - 666667) * 0.35;

        this.withholdingTax = tax;
        System.out.println("[Deduction] computeWithholdingTax() -> PHP " + String.format("%.2f", tax));
        return tax;
    }

    /**
     * Returns the total of all deductions combined.
     * Triggers all computations first to ensure values are current.
     */
    public double getTotalDeductions() {
        computeSSSContribution();
        computePhilHealth();
        computePagIbig();
        computeWithholdingTax();

        double total = sssContribution + philhealthContribution
                     + pagibigContribution + withholdingTax;

        System.out.println("[Deduction] getTotalDeductions() -> PHP " + String.format("%.2f", total));
        return total;
    }

    // ---------- Getters ----------
    public double getSssContribution()       { return sssContribution; }
    public double getPhilhealthContribution(){ return philhealthContribution; }
    public double getPagibigContribution()   { return pagibigContribution; }
    public double getWithholdingTax()        { return withholdingTax; }
}
