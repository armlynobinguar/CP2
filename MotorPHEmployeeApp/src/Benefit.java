/**
 * Represents company and government-mandated allowances
 * and benefits provided to a MotorPH employee.
 */
public class Benefit {

    // ---------- Attributes ----------
    private int      benefitId;
    private Employee employee;
    private double   riceSubsidy;
    private double   phoneAllowance;
    private double   clothingAllowance;

    // ---------- Constructor ----------
    public Benefit(int benefitId, Employee employee) {
        this.benefitId        = benefitId;
        this.employee         = employee;
        this.riceSubsidy      = employee.getRiceSubsidy();
        this.phoneAllowance   = employee.getPhoneAllowance();
        this.clothingAllowance= employee.getClothingAllowance();
        System.out.println("[Benefit] Created for: " + employee.getFullName());
    }

    // ---------- Methods ----------
    public double getTotalBenefits() {
        double total = riceSubsidy + phoneAllowance + clothingAllowance;
        System.out.println("[Benefit] getTotalBenefits() -> PHP " + total);
        return total;
    }

    public void displayBenefits() {
        System.out.println("========================================");
        System.out.println("       EMPLOYEE BENEFITS SUMMARY        ");
        System.out.println("========================================");
        System.out.println("Employee         : " + employee.getFullName());
        System.out.println("Rice Subsidy     : PHP " + riceSubsidy);
        System.out.println("Phone Allowance  : PHP " + phoneAllowance);
        System.out.println("Clothing Allow.  : PHP " + clothingAllowance);
        System.out.println("----------------------------------------");
        System.out.println("Total Benefits   : PHP " + getTotalBenefits());
        System.out.println("========================================");
    }

    // ---------- Getters ----------
    public double getRiceSubsidy()       { return riceSubsidy; }
    public double getPhoneAllowance()    { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
}
