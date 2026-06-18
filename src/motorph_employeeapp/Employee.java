package motorph_employeeapp;

/**
 * Payroll employee model mapped from the Employee Details CSV.
 */
public class Employee {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String department;
    private final String status;
    private final double ratePerDay;
    private double daysWorked;

    public Employee(String id, String firstName, String lastName, String department,
            String status, double ratePerDay, double daysWorked) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.status = status;
        this.ratePerDay = ratePerDay;
        this.daysWorked = daysWorked;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return (firstName + " " + lastName).trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDepartment() {
        return department;
    }

    public String getStatus() {
        return status;
    }

    public double getRatePerDay() {
        return ratePerDay;
    }

    public double getDaysWorked() {
        return daysWorked;
    }

    public void setDaysWorked(double daysWorked) {
        this.daysWorked = daysWorked;
    }
}
