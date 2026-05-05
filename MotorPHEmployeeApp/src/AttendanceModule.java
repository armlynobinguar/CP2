public class AttendanceModule {
    private static final int SHIFT_START_MINUTES = 8 * 60;

    private AttendanceModule() {}

    static OperationResult<AttendanceResult> evaluateAttendance(String timeIn, String timeOut) {
        OperationResult<String> inValidation = ValidationModule.validateTime(timeIn, "Time-In");
        if (!inValidation.isSuccess()) {
            return OperationResult.fail(inValidation.getMessage());
        }
        OperationResult<String> outValidation = ValidationModule.validateTime(timeOut, "Time-Out");
        if (!outValidation.isSuccess()) {
            return OperationResult.fail(outValidation.getMessage());
        }

        int inMinutes = toMinutes(inValidation.getData());
        int outMinutes = toMinutes(outValidation.getData());
        if (outMinutes < inMinutes) {
            return OperationResult.fail("Time-Out must not be earlier than Time-In.");
        }

        boolean isLate = inMinutes > SHIFT_START_MINUTES;
        double hours = (outMinutes - inMinutes) / 60.0;
        if (hours > 5) {
            hours -= 1;
        }
        if (hours < 0) {
            hours = 0;
        }
        AttendanceResult attendance = new AttendanceResult(
                inValidation.getData(), outValidation.getData(), isLate, hours);
        return OperationResult.ok(attendance);
    }

    static double computeHoursWorked(String timeIn, String timeOut) {
        OperationResult<AttendanceResult> result = evaluateAttendance(timeIn, timeOut);
        if (!result.isSuccess()) {
            System.out.println("Attendance error: " + result.getMessage());
            return 0;
        }
        AttendanceResult data = result.getData();
        System.out.println("Attendance Time-IN -> " + data.getTimeIn() + (data.isLate() ? " (LATE)" : " (ON TIME)"));
        System.out.println("Attendance Time-OUT -> " + data.getTimeOut());
        System.out.println("Attendance Hours Worked -> " + String.format("%.2f", data.getHoursWorked()) + " hrs");
        return data.getHoursWorked();
    }

    static int toMinutes(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }
}
