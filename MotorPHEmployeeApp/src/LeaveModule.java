public class LeaveModule {
    private LeaveModule() {}

    static OperationResult<Integer> leaveDuration(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return OperationResult.fail("Leave start and end dates are required.");
        }
        if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}") || !endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return OperationResult.fail("Leave dates must be in YYYY-MM-DD format.");
        }
        int start = Integer.parseInt(startDate.split("-")[2]);
        int end = Integer.parseInt(endDate.split("-")[2]);
        if (end < start) {
            return OperationResult.fail("Leave end date must be the same day or later than start date.");
        }
        return OperationResult.ok((end - start) + 1);
    }

    static OperationResult<Integer> createLeaveRequest(
            int leaveId, String employeeName, String leaveType, String startDate, String endDate) {
        if (leaveId <= 0) {
            return OperationResult.fail("Leave ID must be greater than zero.");
        }
        OperationResult<String> nameValidation = ValidationModule.validateEmployeeName(employeeName);
        if (!nameValidation.isSuccess()) {
            return OperationResult.fail(nameValidation.getMessage());
        }
        if (leaveType == null || leaveType.trim().isEmpty()) {
            return OperationResult.fail("Leave type is required.");
        }
        return leaveDuration(startDate, endDate);
    }

    static void printLeaveFlow(int leaveId, String employeeName, String leaveType, String startDate, String endDate) {
        OperationResult<Integer> leaveResult = createLeaveRequest(leaveId, employeeName, leaveType, startDate, endDate);
        if (!leaveResult.isSuccess()) {
            System.out.println("Leave error: " + leaveResult.getMessage());
            return;
        }
        int days = leaveResult.getData();
        System.out.println("Leave request #" + leaveId + " submitted by " + employeeName);
        System.out.println("Type             : " + leaveType);
        System.out.println("Dates            : " + startDate + " to " + endDate);
        System.out.println("Status           : Pending");
        System.out.println("Duration         : " + days + " day(s)");
    }
}
