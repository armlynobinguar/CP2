public class AttendanceModule {
    private static final int SHIFT_START_MINUTES = 8 * 60;

    private AttendanceModule() {}

    static double computeHoursWorked(String timeIn, String timeOut) {
        int inMinutes = toMinutes(timeIn);
        int outMinutes = toMinutes(timeOut);
        boolean isLate = inMinutes > SHIFT_START_MINUTES;
        double hours = (outMinutes - inMinutes) / 60.0;
        if (hours > 5) {
            hours -= 1;
        }
        if (hours < 0) {
            hours = 0;
        }
        System.out.println("Attendance Time-IN -> " + timeIn + (isLate ? " (LATE)" : " (ON TIME)"));
        System.out.println("Attendance Time-OUT -> " + timeOut);
        System.out.println("Attendance Hours Worked -> " + String.format("%.2f", hours) + " hrs");
        return hours;
    }

    static int toMinutes(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }
}
