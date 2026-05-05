public interface MotorPhService {
    OperationResult<EmployeeIdentity> validateIdentity(
            String rawEmployeeId, String employeeName, String payCoverage);

    OperationResult<AttendanceResult> evaluateAttendance(String timeIn, String timeOut);

    OperationResult<Integer> createLeaveRequest(
            int leaveId, String employeeName, String leaveType, String startDate, String endDate);

    OperationResult<PayrollBreakdown> computePayroll(
            double hoursWorked, double basicSalary, double rice, double phone, double clothing);

    OperationResult<Boolean> authenticate(
            String realUser, String realPass, String inputUser, String inputPass);
}
