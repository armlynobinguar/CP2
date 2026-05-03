/**
 * MotorPH Employee App
 * Main entry point — demonstrates all classes interacting together.
 *
 * MO-IT103 Computer Programming 2
 * Group 3 - H1101
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("============================================");
        System.out.println("     MOTORPH EMPLOYEE APP - DEMO RUN       ");
        System.out.println("============================================\n");

        // ── 1. Set up Position and Department ───────────────────────────
        System.out.println("--- Setting up Position & Department ---");
        Position position = new Position(
                1, "Software Engineer", 3, 40000.00, 60000.00);

        Department department = new Department(
                1, "IT Department", "John Santos");

        System.out.println();

        // ── 2. Create an Employee ────────────────────────────────────────
        System.out.println("--- Creating Employee ---");
        Employee emp = new Employee(
                10001,
                "Maria", "Reyes",
                "1995-06-15",
                "123 Mabini St., Lipa City, Batangas",
                "09171234567",
                "12-3456789-0",
                "12-345678901-2",
                "123-456-789-000",
                "1234-5678-9012",
                "Regular", 
                position, department,
                "John Santos",
                35000.00,
                1500.00, 1000.00, 1000.00);

        System.out.println();
        emp.displayEmployeeInfo();
        System.out.println();

        // ── 3. Attendance ────────────────────────────────────────────────
        System.out.println("--- Recording Attendance ---");
        Attendance att = new Attendance(1, emp, "2024-11-04");
        att.recordTimeIn("08:05");
        att.recordTimeOut("17:00");
        System.out.println(att.getAttendanceSummary());
        System.out.println();

        // ── 4. Leave Request ─────────────────────────────────────────────
        System.out.println("--- Filing a Leave Request ---");
        LeaveRequest leave = new LeaveRequest(
                101, emp, "Sick Leave",
                "2024-11-10", "2024-11-11",
                "Not feeling well - fever and colds.");
        leave.submitLeave();
        leave.getLeaveStatus();
        leave.getLeaveDuration();
        System.out.println();

        // ── 5. Payroll ───────────────────────────────────────────────────
        System.out.println("--- Processing Payroll ---");
        Payroll payroll = new Payroll(
                2001, emp,
                "2024-11-01", "2024-11-15",
                att.getHoursWorked() * 10); // simulating ~10 days worked
        payroll.generatePayslip();

        // ── 6. Benefits ──────────────────────────────────────────────────
        System.out.println("--- Employee Benefits ---");
        Benefit benefit = new Benefit(3001, emp);
        benefit.displayBenefits();
        System.out.println();

        // ── 7. User Account ──────────────────────────────────────────────
        System.out.println("--- User Login Test ---");
        User user = new User(4001, "mreyes", "secure123", "Employee", emp);
        user.login("mreyes", "wrongpass");
        user.login("mreyes", "secure123");
        user.logout();
        System.out.println();

        // ── 8. HR Manager ────────────────────────────────────────────────
        System.out.println("--- HR Manager Actions ---");

        Position hrPosition = new Position(2, "HR Manager", 5, 50000.00, 75000.00);
        Department hrDept   = new Department(2, "Human Resources", "Ana Cruz");

        HRManager hr = new HRManager(
                9001, "Full Access",
                10002, "Ana", "Cruz",
                "1988-03-22",
                "456 Rizal Ave., Batangas City",
                "09981234567",
                "22-3456789-0",
                "22-345678901-2",
                "222-456-789-000",
                "2234-5678-9012",
                "Regular",
                hrPosition, hrDept,
                "CEO", 55000.00,
                1500.00, 1000.00, 1000.00);

        System.out.println();
        hr.addEmployee(emp);
        hr.updateEmployee(emp, "Status", "Promoted");
        hr.approveLeaveRequest(leave);
        hr.processPayroll(payroll);
        hr.generateReport("Payroll", "November 2024");

        System.out.println("\n============================================");
        System.out.println("          DEMO RUN COMPLETE                  ");
        System.out.println("============================================");
    }
}
