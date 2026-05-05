public class PayrollModule {
    private PayrollModule() {}

    static void printPayslip(int employeeId, String employeeName, String position, String department,
                             double hoursWorked, double basicSalary, double rice, double phone, double clothing) {
        double rate = hourlyRate(basicSalary);
        double earnedPay = hoursWorked * rate;
        double benefits = rice + phone + clothing;
        double gross = earnedPay + benefits;

        double sss = sssContribution(basicSalary);
        double philhealth = philhealthContribution(basicSalary);
        double pagibig = pagibigContribution(basicSalary);
        double tax = withholdingTax(basicSalary - sss - philhealth - pagibig);
        double totalDeductions = sss + philhealth + pagibig + tax;
        double net = gross - totalDeductions;

        System.out.println("============================================");
        System.out.println("            MOTORPH EMPLOYEE PAYSLIP        ");
        System.out.println("============================================");
        System.out.println("Employee         : " + employeeName);
        System.out.println("Employee ID      : " + employeeId);
        System.out.println("Position         : " + position);
        System.out.println("Department       : " + department);
        System.out.println("Hours Worked     : " + hoursWorked);
        System.out.println("--------------------------------------------");
        System.out.println("EARNINGS");
        System.out.println("  Basic Pay      : PHP " + String.format("%.2f", earnedPay));
        System.out.println("  Rice Subsidy   : PHP " + rice);
        System.out.println("  Phone Allow.   : PHP " + phone);
        System.out.println("  Clothing Allow.: PHP " + clothing);
        System.out.println("  Gross Pay      : PHP " + String.format("%.2f", gross));
        System.out.println("--------------------------------------------");
        System.out.println("DEDUCTIONS");
        System.out.println("  SSS            : PHP " + String.format("%.2f", sss));
        System.out.println("  PhilHealth     : PHP " + String.format("%.2f", philhealth));
        System.out.println("  Pag-IBIG       : PHP " + String.format("%.2f", pagibig));
        System.out.println("  Withholding Tax: PHP " + String.format("%.2f", tax));
        System.out.println("  Total Deductions: PHP " + String.format("%.2f", totalDeductions));
        System.out.println("--------------------------------------------");
        System.out.println("  NET PAY        : PHP " + String.format("%.2f", net));
        System.out.println("============================================");
        System.out.println();
    }

    static double hourlyRate(double basicSalary) {
        return basicSalary / (22 * 8);
    }

    static double sssContribution(double salary) {
        if (salary < 3250) return 135.00;
        if (salary < 3750) return 157.50;
        if (salary < 4250) return 180.00;
        if (salary < 4750) return 202.50;
        if (salary < 5250) return 225.00;
        if (salary < 5750) return 247.50;
        if (salary < 6250) return 270.00;
        if (salary < 6750) return 292.50;
        if (salary < 7250) return 315.00;
        if (salary < 7750) return 337.50;
        if (salary < 8250) return 360.00;
        if (salary < 8750) return 382.50;
        if (salary < 9250) return 405.00;
        if (salary < 9750) return 427.50;
        if (salary < 10250) return 450.00;
        if (salary < 10750) return 472.50;
        if (salary < 11250) return 495.00;
        if (salary < 11750) return 517.50;
        if (salary < 12250) return 540.00;
        if (salary < 12750) return 562.50;
        if (salary < 13250) return 585.00;
        if (salary < 13750) return 607.50;
        if (salary < 14250) return 630.00;
        if (salary < 14750) return 652.50;
        return 675.00;
    }

    static double philhealthContribution(double salary) {
        if (salary <= 10000) return 150.00;
        if (salary >= 60000) return 900.00;
        return (salary * 0.03) / 2;
    }

    static double pagibigContribution(double salary) {
        double value = salary <= 1500 ? salary * 0.01 : salary * 0.02;
        return value > 100 ? 100 : value;
    }

    static double withholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) return 0;
        if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome <= 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        if (taxableIncome <= 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        if (taxableIncome <= 666667) return 40833 + (taxableIncome - 166667) * 0.32;
        return 200833 + (taxableIncome - 666667) * 0.35;
    }
}
