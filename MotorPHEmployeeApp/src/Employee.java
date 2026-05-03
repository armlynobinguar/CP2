/**
 * Represents an employee in the MotorPH system.
 * Central class that stores personal and employment information.
 */
public class Employee {

    // ---------- Attributes ----------
    private int        employeeId;
    private String     firstName;
    private String     lastName;
    private String     birthday;
    private String     address;
    private String     phoneNumber;
    private String     sssNumber;
    private String     philhealthNumber;
    private String     tinNumber;
    private String     pagibigNumber;
    private String     status;
    private Position   position;
    private Department department;
    private String     immediateSupervisor;
    private double     basicSalary;
    private double     riceSubsidy;
    private double     phoneAllowance;
    private double     clothingAllowance;
    private double     hourlyRate;

    // ---------- Constructor ----------
    public Employee(int employeeId, String firstName, String lastName,
                    String birthday, String address, String phoneNumber,
                    String sssNumber, String philhealthNumber,
                    String tinNumber, String pagibigNumber,
                    String status, Position position, Department department,
                    String immediateSupervisor, double basicSalary,
                    double riceSubsidy, double phoneAllowance,
                    double clothingAllowance) {

        this.employeeId          = employeeId;
        this.firstName           = firstName;
        this.lastName            = lastName;
        this.birthday            = birthday;
        this.address             = address;
        this.phoneNumber         = phoneNumber;
        this.sssNumber           = sssNumber;
        this.philhealthNumber    = philhealthNumber;
        this.tinNumber           = tinNumber;
        this.pagibigNumber       = pagibigNumber;
        this.status              = status;
        this.position            = position;
        this.department          = department;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary         = basicSalary;
        this.riceSubsidy         = riceSubsidy;
        this.phoneAllowance      = phoneAllowance;
        this.clothingAllowance   = clothingAllowance;
        this.hourlyRate          = basicSalary / (22 * 8); // 22 working days x 8 hrs
        System.out.println(" Employee Created: " + getFullName());
    }

    // ---------- Methods ----------
    public int getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double newSalary) {
        this.basicSalary = newSalary;
        this.hourlyRate  = newSalary / (22 * 8);
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public Position getPosition() {
        return position;
    }

    public Department getDepartment() {
        return department;
    }

    public void updateContactInfo(String newPhone, String newAddress) {
        System.out.println(" Updated Contact Info -> Phone: " + newPhone + " | Address: " + newAddress);
        this.phoneNumber = newPhone;
        this.address     = newAddress;
    }

    public void displayEmployeeInfo() {
        System.out.println("========================================");
        System.out.println("         EMPLOYEE INFORMATION           ");
        System.out.println("========================================");
        System.out.println("Employee ID      : " + employeeId);
        System.out.println("Name             : " + getFullName());
        System.out.println("Birthday         : " + birthday);
        System.out.println("Address          : " + address);
        System.out.println("Phone Number     : " + phoneNumber);
        System.out.println("SSS No.          : " + sssNumber);
        System.out.println("PhilHealth No.   : " + philhealthNumber);
        System.out.println("TIN No.          : " + tinNumber);
        System.out.println("Pag-IBIG No.     : " + pagibigNumber);
        System.out.println("Status           : " + status);
        System.out.println("Position         : " + position.getPositionName());
        System.out.println("Department       : " + department.getDepartmentName());
        System.out.println("Supervisor       : " + immediateSupervisor);
        System.out.println("Basic Salary     : PHP " + basicSalary);
        System.out.println("Rice Subsidy     : PHP " + riceSubsidy);
        System.out.println("Phone Allowance  : PHP " + phoneAllowance);
        System.out.println("Clothing Allow.  : PHP " + clothingAllowance);
        System.out.println("Hourly Rate      : PHP " + String.format("%.2f", hourlyRate));
        System.out.println("========================================");
    }

    // ---------- Additional Getters ----------
    public String getFirstName()        { return firstName; }
    public String getLastName()         { return lastName; }
    public String getBirthday()         { return birthday; }
    public String getAddress()          { return address; }
    public String getPhoneNumber()      { return phoneNumber; }
    public String getSssNumber()        { return sssNumber; }
    public String getPhilhealthNumber() { return philhealthNumber; }
    public String getTinNumber()        { return tinNumber; }
    public String getPagibigNumber()    { return pagibigNumber; }
    public String getStatus()           { return status; }
    public double getRiceSubsidy()      { return riceSubsidy; }
    public double getPhoneAllowance()   { return phoneAllowance; }
    public double getClothingAllowance(){ return clothingAllowance; }
}
