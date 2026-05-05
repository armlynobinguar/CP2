public class DisplayModule {
    private DisplayModule() {}

    static void printHeader() {
        System.out.println("============================================");
        System.out.println("     MOTORPH EMPLOYEE APP - DEMO RUN       ");
        System.out.println("============================================\n");
        System.out.println("--- Setting up Position & Department ---");
        System.out.println();
    }

    static void printEmployeeInfo(int employeeId, String employeeName, String position, String department,
                                  double basicSalary, double rice, double phone, double clothing) {
        System.out.println("--- Creating Employee ---");
        System.out.println(" Employee Created: " + employeeName);
        System.out.println();
        System.out.println("========================================");
        System.out.println("         EMPLOYEE INFORMATION           ");
        System.out.println("========================================");
        System.out.println("Employee ID      : " + employeeId);
        System.out.println("Name             : " + employeeName);
        System.out.println("Position         : " + position);
        System.out.println("Department       : " + department);
        System.out.println("Basic Salary     : PHP " + basicSalary);
        System.out.println("Rice Subsidy     : PHP " + rice);
        System.out.println("Phone Allowance  : PHP " + phone);
        System.out.println("Clothing Allow.  : PHP " + clothing);
        System.out.println("Hourly Rate      : PHP " + String.format("%.2f", PayrollModule.hourlyRate(basicSalary)));
        System.out.println("========================================");
        System.out.println();
    }

    static void printBenefits(String employeeName, double rice, double phone, double clothing) {
        double total = rice + phone + clothing;
        System.out.println("========================================");
        System.out.println("       EMPLOYEE BENEFITS SUMMARY        ");
        System.out.println("========================================");
        System.out.println("Employee         : " + employeeName);
        System.out.println("Rice Subsidy     : PHP " + rice);
        System.out.println("Phone Allowance  : PHP " + phone);
        System.out.println("Clothing Allow.  : PHP " + clothing);
        System.out.println("----------------------------------------");
        System.out.println("Total Benefits   : PHP " + total);
        System.out.println("========================================");
    }
}
