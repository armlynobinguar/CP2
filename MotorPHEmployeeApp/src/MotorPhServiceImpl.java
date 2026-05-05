public class MotorPhServiceImpl implements MotorPhService {
    @Override
    public OperationResult<EmployeeIdentity> validateIdentity(
            String rawEmployeeId, String employeeName, String payCoverage) {
        OperationResult<Integer> employeeIdValidation = ValidationModule.validateEmployeeNumber(rawEmployeeId);
        if (!employeeIdValidation.isSuccess()) {
            return OperationResult.fail(employeeIdValidation.getMessage());
        }
        OperationResult<String> employeeNameValidation = ValidationModule.validateEmployeeName(employeeName);
        if (!employeeNameValidation.isSuccess()) {
            return OperationResult.fail(employeeNameValidation.getMessage());
        }
        OperationResult<String> coverageValidation = ValidationModule.validatePayCoverage(payCoverage);
        if (!coverageValidation.isSuccess()) {
            return OperationResult.fail(coverageValidation.getMessage());
        }
        return OperationResult.ok(new EmployeeIdentity(
                employeeIdValidation.getData(),
                employeeNameValidation.getData(),
                coverageValidation.getData()));
    }

    @Override
    public OperationResult<AttendanceResult> evaluateAttendance(String timeIn, String timeOut) {
        return AttendanceModule.evaluateAttendance(timeIn, timeOut);
    }

    @Override
    public OperationResult<Integer> createLeaveRequest(
            int leaveId, String employeeName, String leaveType, String startDate, String endDate) {
        return LeaveModule.createLeaveRequest(leaveId, employeeName, leaveType, startDate, endDate);
    }

    @Override
    public OperationResult<PayrollBreakdown> computePayroll(
            double hoursWorked, double basicSalary, double rice, double phone, double clothing) {
        return PayrollModule.computePayroll(hoursWorked, basicSalary, rice, phone, clothing);
    }

    @Override
    public OperationResult<Boolean> authenticate(
            String realUser, String realPass, String inputUser, String inputPass) {
        return UserModule.loginValidated(realUser, realPass, inputUser, inputPass);
    }
}
