public class AttendanceResult {
    private final String timeIn;
    private final String timeOut;
    private final boolean late;
    private final double hoursWorked;

    AttendanceResult(String timeIn, String timeOut, boolean late, double hoursWorked) {
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.late = late;
        this.hoursWorked = hoursWorked;
    }

    String getTimeIn() {
        return timeIn;
    }

    String getTimeOut() {
        return timeOut;
    }

    boolean isLate() {
        return late;
    }

    double getHoursWorked() {
        return hoursWorked;
    }
}
