public class LeaveModule {
    private LeaveModule() {}

    static void printLeaveFlow(int leaveId, String employeeName, String leaveType, String startDate, String endDate) {
        int days = leaveDuration(startDate, endDate);
        System.out.println("Leave request #" + leaveId + " submitted by " + employeeName);
        System.out.println("Type             : " + leaveType);
        System.out.println("Dates            : " + startDate + " to " + endDate);
        System.out.println("Status           : Pending");
        System.out.println("Duration         : " + days + " day(s)");
    }

    static int leaveDuration(String startDate, String endDate) {
        int start = Integer.parseInt(startDate.split("-")[2]);
        int end = Integer.parseInt(endDate.split("-")[2]);
        return (end - start) + 1;
    }
}
