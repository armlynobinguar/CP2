/**
 * Manages employee leave applications, including leave type,
 * duration, and approval status within MotorPH.
 */
public class LeaveRequest {

    // ---------- Attributes ----------
    private int      leaveId;
    private Employee employee;
    private String   leaveType;
    private String   startDate;
    private String   endDate;
    private String   status;
    private String   reason;

    // ---------- Constructor ----------
    public LeaveRequest(int leaveId, Employee employee, String leaveType,
                        String startDate, String endDate, String reason) {
        this.leaveId  = leaveId;
        this.employee = employee;
        this.leaveType= leaveType;
        this.startDate= startDate;
        this.endDate  = endDate;
        this.reason   = reason;
        this.status   = "Pending";
        System.out.println("[LeaveRequest] Created for " + employee.getFullName()
                         + " | Type: " + leaveType + " | Status: Pending");
    }

    // ---------- Methods ----------
    public void submitLeave() {
        this.status = "Pending";
        System.out.println("[LeaveRequest] submitLeave() -> Leave request #" + leaveId
                         + " submitted by " + employee.getFullName()
                         + " (" + leaveType + " | " + startDate + " to " + endDate + ")");
    }

    public void approveLeave() {
        if (status.equals("Pending")) {
            this.status = "Approved";
            System.out.println("[LeaveRequest] approveLeave() -> Leave #" + leaveId
                             + " for " + employee.getFullName() + " has been APPROVED.");
        } else {
            System.out.println("[LeaveRequest] approveLeave() -> Cannot approve. Current status: " + status);
        }
    }

    public void rejectLeave() {
        if (status.equals("Pending")) {
            this.status = "Rejected";
            System.out.println("[LeaveRequest] rejectLeave() -> Leave #" + leaveId
                             + " for " + employee.getFullName() + " has been REJECTED.");
        } else {
            System.out.println("[LeaveRequest] rejectLeave() -> Cannot reject. Current status: " + status);
        }
    }

    public String getLeaveStatus() {
        System.out.println("[LeaveRequest] getLeaveStatus() -> " + status);
        return status;
    }

    /**
     * Calculates total leave days using a simple date-difference method.
     * Assumes dates are in "YYYY-MM-DD" format.
     */
    public int getLeaveDuration() {
        // Simple calculation using the day portion of the date string
        int startDay = Integer.parseInt(startDate.split("-")[2]);
        int endDay   = Integer.parseInt(endDate.split("-")[2]);
        int duration = (endDay - startDay) + 1;
        System.out.println("[LeaveRequest] getLeaveDuration() -> " + duration + " day(s)");
        return duration;
    }

    // ---------- Getters ----------
    public int    getLeaveId()   { return leaveId; }
    public String getLeaveType() { return leaveType; }
    public String getStartDate() { return startDate; }
    public String getEndDate()   { return endDate; }
    public String getReason()    { return reason; }
    public Employee getEmployee(){ return employee; }
}
