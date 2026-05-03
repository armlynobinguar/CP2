/**
 * MotorPH Employee App — procedural (non-OOP) demo.
 * Single entry class; logic is static functions and primitive/String data.
 *
 * MO-IT103 Computer Programming 2 · Group 3 - H1101
 */
public class Main {

    private static final int STANDARD_START_MINUTES = 8 * 60;

    public static void main(String[] args) {

        System.out.println("============================================");
        System.out.println("     MOTORPH EMPLOYEE APP - DEMO RUN       ");
        System.out.println("============================================\n");

        /* ── 1. Position & department (plain fields, no objects) ────────── */
        System.out.println("--- Setting up Position & Department ---");
        String positionName = "Software Engineer";
        String departmentName = "IT Department";
        System.out.println();

        /* ── 2. Employee ─────────────────────────────────────────────────── */
        System.out.println("--- Creating Employee ---");
        int employeeId = 10001;
        String firstName = "Maria";
        String lastName = "Reyes";
        String birthday = "1995-06-15";
        String address = "123 Mabini St., Lipa City, Batangas";
        String phoneNumber = "09171234567";
        String sssNumber = "12-3456789-0";
        String philhealthNumber = "12-345678901-2";
        String tinNumber = "123-456-789-000";
        String pagibigNumber = "1234-5678-9012";
        String status = "Regular";
        String immediateSupervisor = "John Santos";
        double basicSalary = 35000.00;
        double riceSubsidy = 1500.00;
        double phoneAllowance = 1000.00;
        double clothingAllowance = 1000.00;
        double hourlyRate = hourlyRateFromBasic(basicSalary);

        System.out.println(" Employee Created: " + fullName(firstName, lastName));
        System.out.println();

        displayEmployeeInfo(
                employeeId, firstName, lastName, birthday, address, phoneNumber,
                sssNumber, philhealthNumber, tinNumber, pagibigNumber, status,
                positionName, departmentName, immediateSupervisor,
                basicSalary, riceSubsidy, phoneAllowance, clothingAllowance,
                hourlyRate);
        System.out.println();

        /* ── 3. Attendance ───────────────────────────────────────────────── */
        System.out.println("--- Recording Attendance ---");
        String attDate = "2024-11-04";
        System.out.println("Attendance Record created for "
                + fullName(firstName, lastName) + " on " + attDate);

        String timeIn = "08:05";
        boolean isLate = checkIfLate(timeIn);
        System.out.println("Attendance Record Time-IN -> " + timeIn + (isLate ? " (LATE)" : " (ON TIME)"));

        String timeOut = "17:00";
        System.out.println("Attendance Record Time-OUT -> " + timeOut);
        double dayHours = computeHoursWorked(timeIn, timeOut);
        System.out.println();

        printAttendanceSummary(attDate, fullName(firstName, lastName),
                timeIn, timeOut, dayHours, isLate);
        System.out.println();

        /* ── 4. Leave ───────────────────────────────────────────────────── */
        System.out.println("--- Filing a Leave Request ---");
        int leaveId = 101;
        String leaveType = "Sick Leave";
        String leaveStart = "2024-11-10";
        String leaveEnd = "2024-11-11";
        String leaveStatus = "Pending";

        System.out.println("[LeaveRequest] Created for " + fullName(firstName, lastName)
                + " | Type: " + leaveType + " | Status: Pending");
        submitLeave(leaveId, fullName(firstName, lastName), leaveType, leaveStart, leaveEnd);
        printLeaveStatus(leaveStatus);
        getLeaveDuration(leaveStart, leaveEnd);
        System.out.println();

        /* ── 5. Payroll ─────────────────────────────────────────────────── */
        System.out.println("--- Processing Payroll ---");
        int payrollId = 2001;
        String payPeriodStart = "2024-11-01";
        String payPeriodEnd = "2024-11-15";
        double payrollHoursWorked = dayHours * 10;

        System.out.println("[Deduction] Created for: " + fullName(firstName, lastName));
        System.out.println("[Benefit] Created for: " + fullName(firstName, lastName));
        System.out.println("[Payroll] Created for: " + fullName(firstName, lastName)
                + " | Period: " + payPeriodStart + " to " + payPeriodEnd);

        generatePayslip(
                payrollId, employeeId, firstName, lastName,
                positionName, departmentName,
                payPeriodStart, payPeriodEnd, payrollHoursWorked,
                hourlyRate, basicSalary,
                riceSubsidy, phoneAllowance, clothingAllowance);

        /* ── 6. Benefits (separate demo block, matches original flow) ───── */
        System.out.println("--- Employee Benefits ---");
        System.out.println("[Benefit] Created for: " + fullName(firstName, lastName));
        displayBenefits(fullName(firstName, lastName),
                riceSubsidy, phoneAllowance, clothingAllowance);
        System.out.println();

        /* ── 7. User ────────────────────────────────────────────────────── */
        System.out.println("--- User Login Test ---");
        String username = "mreyes";
        String password = "secure123";
        String role = "Employee";
        boolean loggedIn = false;

        System.out.println("[User] Account created: " + username + " | Role: " + role);
        loggedIn = login(username, password, "mreyes", "wrongpass",
                fullName(firstName, lastName));
        loggedIn = login(username, password, "mreyes", "secure123",
                fullName(firstName, lastName));
        loggedIn = logout(loggedIn, fullName(firstName, lastName));
        System.out.println();

        /* ── 8. HR manager (no subclass — same employee fields + HR meta) ─ */
        System.out.println("--- HR Manager Actions ---");

        String hrFirst = "Ana";
        String hrLast = "Cruz";
        String accessLevel = "Full Access";

        System.out.println(" Employee Created: " + fullName(hrFirst, hrLast));
        System.out.println("[HRManager] HR access granted to: " + fullName(hrFirst, hrLast)
                + " | Level: " + accessLevel);

        System.out.println();
        hrAddEmployee(fullName(hrFirst, hrLast), fullName(firstName, lastName), employeeId);
        hrUpdateEmployee(fullName(hrFirst, hrLast), fullName(firstName, lastName),
                "Status", "Promoted");
        leaveStatus = hrApproveLeave(fullName(hrFirst, hrLast),
                leaveId, fullName(firstName, lastName), leaveStatus);

        hrProcessPayroll(fullName(hrFirst, hrLast), payrollHoursWorked,
                payrollId, employeeId, firstName, lastName,
                positionName, departmentName,
                payPeriodStart, payPeriodEnd, basicSalary,
                riceSubsidy, phoneAllowance, clothingAllowance);

        hrGenerateReport(fullName(hrFirst, hrLast), accessLevel,
                "Payroll", "November 2024");

        System.out.println("\n============================================");
        System.out.println("          DEMO RUN COMPLETE                  ");
        System.out.println("============================================");
    }

    /* ========================= helpers ========================= */

    static String fullName(String first, String last) {
        return first + " " + last;
    }

    static double hourlyRateFromBasic(double basicSalary) {
        return basicSalary / (22 * 8);
    }

    static void displayEmployeeInfo(
            int employeeId, String firstName, String lastName,
            String birthday, String address, String phoneNumber,
            String sssNumber, String philhealthNumber, String tinNumber,
            String pagibigNumber, String status,
            String positionName, String departmentName, String immediateSupervisor,
            double basicSalary, double riceSubsidy, double phoneAllowance,
            double clothingAllowance, double hourlyRate) {

        System.out.println("========================================");
        System.out.println("         EMPLOYEE INFORMATION           ");
        System.out.println("========================================");
        System.out.println("Employee ID      : " + employeeId);
        System.out.println("Name             : " + fullName(firstName, lastName));
        System.out.println("Birthday         : " + birthday);
        System.out.println("Address          : " + address);
        System.out.println("Phone Number     : " + phoneNumber);
        System.out.println("SSS No.          : " + sssNumber);
        System.out.println("PhilHealth No.   : " + philhealthNumber);
        System.out.println("TIN No.          : " + tinNumber);
        System.out.println("Pag-IBIG No.     : " + pagibigNumber);
        System.out.println("Status           : " + status);
        System.out.println("Position         : " + positionName);
        System.out.println("Department       : " + departmentName);
        System.out.println("Supervisor       : " + immediateSupervisor);
        System.out.println("Basic Salary     : PHP " + basicSalary);
        System.out.println("Rice Subsidy     : PHP " + riceSubsidy);
        System.out.println("Phone Allowance  : PHP " + phoneAllowance);
        System.out.println("Clothing Allow.  : PHP " + clothingAllowance);
        System.out.println("Hourly Rate      : PHP " + String.format("%.2f", hourlyRate));
        System.out.println("========================================");
    }

    static int parseTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    static boolean checkIfLate(String timeIn) {
        if (timeIn == null) {
            return false;
        }
        int inMinutes = parseTimeToMinutes(timeIn);
        boolean late = inMinutes > STANDARD_START_MINUTES;
        System.out.println("Attendance Is Late? -> " + late);
        return late;
    }

    static double computeHoursWorked(String timeIn, String timeOut) {
        if (timeIn == null || timeOut == null) {
            System.out.println("Attendance Hours Worked -> Incomplete time records.");
            return 0;
        }
        int inMinutes = parseTimeToMinutes(timeIn);
        int outMinutes = parseTimeToMinutes(timeOut);
        double hours = (outMinutes - inMinutes) / 60.0;
        if (hours > 5) {
            hours -= 1;
        }
        if (hours < 0) {
            hours = 0;
        }
        System.out.println("Attendance Hours Worked -> " + String.format("%.2f", hours) + " hrs");
        return hours;
    }

    static void printAttendanceSummary(String date, String empName,
            String timeIn, String timeOut, double hoursWorked, boolean isLate) {
        String summary = "[ " + date + " ] " + empName
                + " | In: " + timeIn
                + " | Out: " + timeOut
                + " | Hours: " + String.format("%.2f", hoursWorked)
                + " | Late: " + (isLate ? "Yes" : "No");
        System.out.println("Attendance Summary -> " + summary);
    }

    static void submitLeave(int leaveId, String empName, String leaveType,
            String startDate, String endDate) {
        System.out.println("[LeaveRequest] submitLeave() -> Leave request #" + leaveId
                + " submitted by " + empName
                + " (" + leaveType + " | " + startDate + " to " + endDate + ")");
    }

    static void printLeaveStatus(String status) {
        System.out.println("[LeaveRequest] getLeaveStatus() -> " + status);
    }

    static int getLeaveDuration(String startDate, String endDate) {
        int startDay = Integer.parseInt(startDate.split("-")[2]);
        int endDay = Integer.parseInt(endDate.split("-")[2]);
        int duration = (endDay - startDay) + 1;
        System.out.println("[LeaveRequest] getLeaveDuration() -> " + duration + " day(s)");
        return duration;
    }

    static double computeSSSContribution(double salary) {
        double sss;
        if      (salary < 3250)   sss = 135.00;
        else if (salary < 3750)   sss = 157.50;
        else if (salary < 4250)   sss = 180.00;
        else if (salary < 4750)   sss = 202.50;
        else if (salary < 5250)   sss = 225.00;
        else if (salary < 5750)   sss = 247.50;
        else if (salary < 6250)   sss = 270.00;
        else if (salary < 6750)   sss = 292.50;
        else if (salary < 7250)   sss = 315.00;
        else if (salary < 7750)   sss = 337.50;
        else if (salary < 8250)   sss = 360.00;
        else if (salary < 8750)   sss = 382.50;
        else if (salary < 9250)   sss = 405.00;
        else if (salary < 9750)   sss = 427.50;
        else if (salary < 10250)  sss = 450.00;
        else if (salary < 10750)  sss = 472.50;
        else if (salary < 11250)  sss = 495.00;
        else if (salary < 11750)  sss = 517.50;
        else if (salary < 12250)  sss = 540.00;
        else if (salary < 12750)  sss = 562.50;
        else if (salary < 13250)  sss = 585.00;
        else if (salary < 13750)  sss = 607.50;
        else if (salary < 14250)  sss = 630.00;
        else if (salary < 14750)  sss = 652.50;
        else                       sss = 675.00;

        System.out.println("[Deduction] computeSSSContribution() -> PHP " + sss);
        return sss;
    }

    static double computePhilHealth(double salary) {
        double philhealth;
        if (salary <= 10000) {
            philhealth = 150.00;
        } else if (salary >= 60000) {
            philhealth = 900.00;
        } else {
            philhealth = (salary * 0.03) / 2;
        }
        System.out.println("[Deduction] computePhilHealth() -> PHP " + philhealth);
        return philhealth;
    }

    static double computePagIbig(double salary) {
        double pagibig = salary <= 1500 ? salary * 0.01 : salary * 0.02;
        if (pagibig > 100) {
            pagibig = 100.00;
        }
        System.out.println("[Deduction] computePagIbig() -> PHP " + pagibig);
        return pagibig;
    }

    static double computeWithholdingTax(double taxableIncome) {
        double tax;
        if      (taxableIncome <= 20833)   tax = 0;
        else if (taxableIncome <= 33333)   tax = (taxableIncome - 20833) * 0.20;
        else if (taxableIncome <= 66667)   tax = 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome <= 166667)  tax = 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome <= 666667)  tax = 40833 + (taxableIncome - 166667) * 0.32;
        else                                tax = 200833 + (taxableIncome - 666667) * 0.35;

        System.out.println("[Deduction] computeWithholdingTax() -> PHP " + String.format("%.2f", tax));
        return tax;
    }

    /** Returns { grossPay, totalDeductions, netPay, earnedPay, sss, phil, pagibig, tax } */
    static double[] computePayFigures(double hoursWorked, double hourlyRate, double basicSalary,
            double riceSubsidy, double phoneAllowance, double clothingAllowance) {

        double earnedPay = hoursWorked * hourlyRate;
        double totalBenefits = riceSubsidy + phoneAllowance + clothingAllowance;
        System.out.println("[Benefit] getTotalBenefits() -> PHP " + totalBenefits);

        double grossPay = earnedPay + totalBenefits;
        System.out.println("[Payroll] computeGrossPay()");
        System.out.println("          Hours Worked  : " + hoursWorked);
        System.out.println("          Hourly Rate   : PHP " + String.format("%.2f", hourlyRate));
        System.out.println("          Earned Pay    : PHP " + String.format("%.2f", earnedPay));
        System.out.println("          Benefits      : PHP " + String.format("%.2f", totalBenefits));
        System.out.println("          Gross Pay     : PHP " + String.format("%.2f", grossPay));

        double sss = computeSSSContribution(basicSalary);
        double phil = computePhilHealth(basicSalary);
        double pagibig = computePagIbig(basicSalary);
        double taxable = basicSalary - sss - phil - pagibig;
        double tax = computeWithholdingTax(taxable);
        double totalDeductions = sss + phil + pagibig + tax;
        System.out.println("[Deduction] getTotalDeductions() -> PHP " + String.format("%.2f", totalDeductions));

        System.out.println("[Payroll] computeDeductions() -> PHP " + String.format("%.2f", totalDeductions));

        double netPay = grossPay - totalDeductions;
        System.out.println("[Payroll] computeNetPay() -> PHP " + String.format("%.2f", netPay));

        return new double[] { grossPay, totalDeductions, netPay, earnedPay, sss, phil, pagibig, tax };
    }

    static void generatePayslip(
            int payrollId, int employeeId, String firstName, String lastName,
            String positionName, String departmentName,
            String payPeriodStart, String payPeriodEnd, double hoursWorked,
            double hourlyRate, double basicSalary,
            double riceSubsidy, double phoneAllowance, double clothingAllowance) {

        String period = payPeriodStart + " to " + payPeriodEnd;
        System.out.println("[Payroll] getPayPeriod() -> " + period);

        double[] fig = computePayFigures(hoursWorked, hourlyRate, basicSalary,
                riceSubsidy, phoneAllowance, clothingAllowance);
        double grossPay = fig[0];
        double totalDeductions = fig[1];
        double netPay = fig[2];
        double earnedPay = fig[3];
        double sss = fig[4];
        double phil = fig[5];
        double pagibig = fig[6];
        double tax = fig[7];

        System.out.println();
        System.out.println("============================================");
        System.out.println("            MOTORPH EMPLOYEE PAYSLIP        ");
        System.out.println("============================================");
        System.out.println("Employee         : " + fullName(firstName, lastName));
        System.out.println("Employee ID      : " + employeeId);
        System.out.println("Position         : " + positionName);
        System.out.println("Department       : " + departmentName);
        System.out.println("Pay Period       : " + period);
        System.out.println("Hours Worked     : " + hoursWorked);
        System.out.println("--------------------------------------------");
        System.out.println("EARNINGS");
        System.out.println("  Basic Pay      : PHP " + String.format("%.2f", earnedPay));
        System.out.println("  Rice Subsidy   : PHP " + riceSubsidy);
        System.out.println("  Phone Allow.   : PHP " + phoneAllowance);
        System.out.println("  Clothing Allow.: PHP " + clothingAllowance);
        System.out.println("  Gross Pay      : PHP " + String.format("%.2f", grossPay));
        System.out.println("--------------------------------------------");
        System.out.println("DEDUCTIONS");
        System.out.println("  SSS            : PHP " + String.format("%.2f", sss));
        System.out.println("  PhilHealth     : PHP " + String.format("%.2f", phil));
        System.out.println("  Pag-IBIG       : PHP " + String.format("%.2f", pagibig));
        System.out.println("  Withholding Tax: PHP " + String.format("%.2f", tax));
        System.out.println("  Total Deductions: PHP " + String.format("%.2f", totalDeductions));
        System.out.println("--------------------------------------------");
        System.out.println("  NET PAY        : PHP " + String.format("%.2f", netPay));
        System.out.println("============================================");
        System.out.println();
    }

    static void displayBenefits(String empName,
            double riceSubsidy, double phoneAllowance, double clothingAllowance) {
        System.out.println("========================================");
        System.out.println("       EMPLOYEE BENEFITS SUMMARY        ");
        System.out.println("========================================");
        System.out.println("Employee         : " + empName);
        System.out.println("Rice Subsidy     : PHP " + riceSubsidy);
        System.out.println("Phone Allowance  : PHP " + phoneAllowance);
        System.out.println("Clothing Allow.  : PHP " + clothingAllowance);
        System.out.println("----------------------------------------");
        double total = riceSubsidy + phoneAllowance + clothingAllowance;
        System.out.println("[Benefit] getTotalBenefits() -> PHP " + total);
        System.out.println("Total Benefits   : PHP " + total);
        System.out.println("========================================");
    }

    static boolean login(String username, String password,
            String inputUser, String inputPass, String empFullName) {
        if (username.equals(inputUser) && password.equals(inputPass)) {
            System.out.println("[User] login() -> SUCCESS. Welcome, " + empFullName + "!");
            return true;
        }
        System.out.println("[User] login() -> FAILED. Invalid username or password.");
        return false;
    }

    static boolean logout(boolean loggedIn, String empFullName) {
        if (loggedIn) {
            System.out.println("[User] logout() -> " + empFullName + " has logged out.");
            return false;
        }
        System.out.println("[User] logout() -> No active session to end.");
        return loggedIn;
    }

    static void hrAddEmployee(String hrName, String newEmpName, int newEmpId) {
        System.out.println("[HRManager] addEmployee() -> " + hrName
                + " added new employee: " + newEmpName
                + " (ID: " + newEmpId + ")");
    }

    static void hrUpdateEmployee(String hrName, String targetName,
            String field, String newValue) {
        System.out.println("[HRManager] updateEmployee() -> " + hrName
                + " updated " + targetName
                + " | Field: " + field + " -> " + newValue);
    }

    static String hrApproveLeave(String hrName, int leaveId,
            String empName, String currentStatus) {
        System.out.println("[HRManager] approveLeaveRequest() -> " + hrName
                + " reviewing leave for: " + empName);
        if ("Pending".equals(currentStatus)) {
            System.out.println("[LeaveRequest] approveLeave() -> Leave #" + leaveId
                    + " for " + empName + " has been APPROVED.");
            return "Approved";
        }
        System.out.println("[LeaveRequest] approveLeave() -> Cannot approve. Current status: "
                + currentStatus);
        return currentStatus;
    }

    static void hrProcessPayroll(String hrName, double payrollHoursWorked,
            int payrollId, int employeeId, String firstName, String lastName,
            String positionName, String departmentName,
            String payPeriodStart, String payPeriodEnd, double basicSalary,
            double riceSubsidy, double phoneAllowance, double clothingAllowance) {

        System.out.println("[HRManager] processPayroll() -> " + hrName
                + " is processing payroll for: " + payrollHoursWorked + " hrs.");

        generatePayslip(
                payrollId, employeeId, firstName, lastName,
                positionName, departmentName,
                payPeriodStart, payPeriodEnd, payrollHoursWorked,
                hourlyRateFromBasic(basicSalary), basicSalary,
                riceSubsidy, phoneAllowance, clothingAllowance);
    }

    static void hrGenerateReport(String hrName, String accessLevel,
            String reportType, String period) {
        System.out.println("[HRManager] generateReport()");
        System.out.println("============================================");
        System.out.println("  MOTORPH " + reportType.toUpperCase() + " REPORT");
        System.out.println("  Period    : " + period);
        System.out.println("  Generated by: " + hrName + " (HR Manager)");
        System.out.println("  Access Level: " + accessLevel);
        System.out.println("============================================");
    }
}
