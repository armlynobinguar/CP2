/**
 * Represents a job position or role within MotorPH.
 * Stores the salary grade and pay range for the position.
 */
public class Position {

    // ---------- Attributes ----------
    private int    positionId;
    private String positionName;
    private int    salaryGrade;
    private double minSalary;
    private double maxSalary;

    // ---------- Constructor ----------
    public Position(int positionId, String positionName, int salaryGrade,
                    double minSalary, double maxSalary) {
        this.positionId   = positionId;
        this.positionName = positionName;
        this.salaryGrade  = salaryGrade;
        this.minSalary    = minSalary;
        this.maxSalary    = maxSalary;
    }

    // ---------- Methods ----------
    public String getPositionName() {
        return positionName;
    }

    public int getSalaryGrade() {
        return salaryGrade;
    }

    public String getSalaryRange() {
        return "PHP " + minSalary + " - PHP " + maxSalary;
    }

    // ---------- Getters ----------
    public int    getPositionId() { return positionId; }
    public double getMinSalary()  { return minSalary; }
    public double getMaxSalary()  { return maxSalary; }
}
